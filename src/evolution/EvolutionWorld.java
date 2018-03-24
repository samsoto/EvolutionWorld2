package evolution;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter; 
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import sensor.Sensor;
import worldObject.Agent;
import worldObject.CollidableObject;
import worldObject.Food;

public final class EvolutionWorld extends JPanel implements Runnable, MouseListener, KeyListener {

    //Various Simulation variables
    private final AtomicBoolean isRunning;
    private int threadSpeed = 1;
    private List<Agent> agentList;
    private List<Food> foodList;
    private final ScreenControls screenControls;

    //keeps track of how many species there are
    int speciesID = 0;

    //Spawn agents
    private final int AGENT_SPAWN_AMOUNT = 15;

    //Spawn food
    public static long FOOD_SPAWN_PERIOD = 300;
    private final int FOOD_SPAWN_AMOUNT = 100;
    private long lastFoodSpawn = 0;

    //Size of the containment area
    public static final int BORDER_SIZE = 2000;

    //Util - used in almost every classes
    public static Random rand;

    //Write data to a file (Simulation analysis) 
    private final int TIME_UNTIL_NEXT_GEN = 1200;
    private int nextGenTimer = TIME_UNTIL_NEXT_GEN;
    private BufferedWriter scoreFile;
    private int numGenerations = 1;

    //used for auto targeting an Agent
    int dx = 0;
    int dy = 0;

    //used to record empirical data
    int fileCount = 0;
    private BufferedWriter brainActivity;

    //Constructor
    public EvolutionWorld() {

        super();
        rand = new Random(System.currentTimeMillis());
        screenControls = ScreenControls.getInstance();
        init();

        setFocusable(true);
        addKeyListener(screenControls);
        addKeyListener(this);
        addMouseWheelListener(screenControls);
        addMouseListener(screenControls);
        addMouseListener(this);
        addMouseMotionListener(screenControls);
        isRunning = new AtomicBoolean(true);
        (new Thread(this)).start();

    }

    /**
     * The BufferedWrite this method returns is used for recording an agent's
     * brain activity over time. The results will be used as a statistical
     * sample for computing neural complexity. Note a different program will
     * compute neural complexity for it has a time complexity that is
     * exponential and thus cannot be computed in real time
     *
     * @return brainActivity
     */
    public BufferedWriter getBrainActivityBuffWriter() {
        return brainActivity;
    }

    public Agent getTargetAgent() {
        return screenControls.getTarget();
    }

    public synchronized void init() {

        //init random seed. This is used in varous classes
        rand = new Random(System.currentTimeMillis());

        /*
        //These writers are used to recored empirical data about the agent
        try {
            scoreFile = new BufferedWriter(new FileWriter(new File("score" + fileCount + ".txt")));
            brainActivity = new BufferedWriter(new FileWriter(new File("brainActivity.txt")));
        } catch (IOException ex) {
            Logger.getLogger(EvolutionWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
        */

        //init agents
        agentList = new ArrayList<>();
        for (int i = 0; i < AGENT_SPAWN_AMOUNT; i++) {
            agentList.add(new Agent(speciesID++));
        }

        //init food
        foodList = new ArrayList<>();
        for (int i = 0; i < FOOD_SPAWN_AMOUNT; i++) {
            int x = rand.nextInt(BORDER_SIZE / Food.SIZE) * Food.SIZE;
            int y = rand.nextInt(BORDER_SIZE / Food.SIZE) * Food.SIZE;
            foodList.add(new Food(x, y));
        }

    }

    public void reset() {
        //screenControls.stopFollowing();
        init();
    }

    public void timedSpawn() {

        //Spawn random food
        if (System.currentTimeMillis() >= lastFoodSpawn + (FOOD_SPAWN_PERIOD / threadSpeed)) {
            int x = rand.nextInt(BORDER_SIZE / Food.SIZE) * Food.SIZE;
            int y = rand.nextInt(BORDER_SIZE / Food.SIZE) * Food.SIZE;
            foodList.add(new Food(x, y));
            lastFoodSpawn = System.currentTimeMillis();
        }

    }

    public void collisions() {

        //Agent vs Agent
        for (int i = 0; i < agentList.size() - 1; i++) {
            for (int j = i + 1; j < agentList.size(); j++) {
                Agent agent1 = agentList.get(i);
                Agent agent2 = agentList.get(j);
                if (CollidableObject.checkCollision(agent1, agent2)) {
                    agent1.handleCollision(agent2);
                    agent2.handleCollision(agent1);
                }
            }
        }

        //Agent vs Food
        for (int i = 0; i < agentList.size(); i++) {
            for (int j = 0; j < foodList.size(); j++) {
                Agent agent = agentList.get(i);
                Food food = foodList.get(j);
                if (CollidableObject.checkCollision(agent, food)) {
                    agent.handleCollision(food);
                    food.handleCollision(agent);
                }
            }
        }

        //AgentEyes vs Agents
        for (int i = 0; i < agentList.size(); i++) {
            for (int j = 0; j < agentList.size(); j++) {

                if (i == j) {
                    continue;
                }

                Agent agent1 = agentList.get(i);
                Agent agent2 = agentList.get(j);
                Sensor[] eyes = agent1.getEyes();
                for (Sensor eye : eyes) {
                    eye.collision(agent2);
                }
            }
        }

        //AgentEyes vs Food
        for (int i = 0; i < agentList.size(); i++) {
            for (int j = 0; j < foodList.size(); j++) {

                Agent agent = agentList.get(i);
                Sensor[] eyes = agent.getEyes();
                Food food = foodList.get(j);

                for (Sensor eye : eyes) {
                    eye.collision(food);
                }

            }
        }
    }

    public void startNextGeneration() {
        synchronized (this) {

            numGenerations++;
            if (numGenerations > 150) {
                close();
                fileCount++;
                this.numGenerations = 1;
                reset();
            }

            foodList = new ArrayList<>();
            for (int i = 0; i < FOOD_SPAWN_AMOUNT; i++) {
                int x = rand.nextInt(BORDER_SIZE / Food.SIZE) * Food.SIZE;
                int y = rand.nextInt(BORDER_SIZE / Food.SIZE) * Food.SIZE;
                foodList.add(new Food(x, y));
            }

            //Sort the agent list according to score so that the 
            //top agents with the highest scores can be selected
            Object[] agentScoreArray = agentList.toArray();
            Arrays.sort(agentScoreArray);

            //Average the top top four agents scores and write it to a file.
            //this is used to collect statistical data to measure
            //the averge score vs number of generations. The results
            //show an increase in the average score with time. 
            try {
                double ave = 0;
                for (int i = 0; i < 4; i++) {
                    ave += ((Agent) agentScoreArray[i]).score;
                }
                ave /= 4;
                scoreFile.write("" + ave + "\n");
            } catch (IOException ex) {
            }

            //These will be the top agent from this round that will 
            //be allowed to move onto the next round
            List<Agent> elites = new ArrayList<>();

            //If the user has an agent selected then that agent is guaranteed
            //to make it to the next round. This is to make it easy for
            //recording brain activity so neural complexity can be measured
            Agent selectedAgent = null;
            if (screenControls.getTarget() != null) {
                selectedAgent = screenControls.getTarget();
                selectedAgent.setLife(1);
                selectedAgent.score = 0;
                elites.add(selectedAgent);
            }

            //get top 4 agents with the highest score and
            //allow them to move onto the next round in addition 
            //to a mutated copy of themselves
            for (int i = 0; i < 3; i++) {
                Agent a = (Agent) agentScoreArray[i];
                if (!a.equals(selectedAgent)) {
                    a.score = 0;
                    elites.add(a);
                    elites.add((Agent) a.clone());
                }
            }
            agentList = elites;

            //Some agents (the winners) will be copied to the next round.
            //The remaining agents will be randomly created
            for (int i = 0; i < AGENT_SPAWN_AMOUNT - elites.size(); i++) {
                agentList.add(new Agent(speciesID++));
            }

            //Set the time for when the next generation will occur
            nextGenTimer = TIME_UNTIL_NEXT_GEN;
        }
    }

    public void purge() {

        //purge agent list
        Iterator<Agent> a_iterator = agentList.listIterator();
        while (a_iterator.hasNext()) {
            Agent agent = a_iterator.next();
            if (agent.getLife() <= 0) {
                if (agent.wasKilled) {
                    //not used at the moment
                }
                //For this version agents do not die when they
                //lose all of their life, instead if some other 
                //agent kill them they lose their score. Thus life 
                //in this way acts like defence for an agents score.

                //iterator.remove();
            }
        }

        //purge food
        Iterator<Food> f_iterator = foodList.listIterator();
        while (f_iterator.hasNext()) {
            Food f = f_iterator.next();
            if (!f.isAlive) {
                f_iterator.remove();
            }
        }

    }

    public void requestTargetHighestScore() {
        synchronized (this) {
            Agent follow = null;
            int highestScore = 0;
            for (Agent agent : agentList) {
                if (agent.score >= highestScore) {
                    highestScore = agent.score;
                    follow = agent;
                }
            }
            screenControls.followTarget(follow);
        }
    }

    public synchronized void update() {

        //Generation managment
        nextGenTimer--;
        if (nextGenTimer <= 0) {
            startNextGeneration();
        }

        //spawn new bots/food every 'x' seconds
        timedSpawn();

        //update agents
        for (Agent a : agentList) {
            a.update();
        }

        //age food and essence 
        for (Food food : foodList) {
            food.age();
        }

        //test and handle collisions
        collisions();

        //purge all dead food and agents
        purge();

    }

    public void close() {
        try {
            scoreFile.close();
        } catch (IOException ex) {
            Logger.getLogger(EvolutionWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        Color boarderColor;

        //draw background
        boarderColor = Debug.screenInv ? Color.BLACK : new Color(173, 216, 230);
        g2d.setColor(boarderColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (Debug.envDebug) {

            boarderColor = Debug.screenInv ? Color.WHITE : Color.BLACK;
            g2d.setColor(boarderColor);

            g2d.drawString("Generation: " + numGenerations, 10, 20);
            g2d.drawString("Time until next Gen: " + this.nextGenTimer, 10, 40);
            g2d.drawString("Thread Mult: " + this.threadSpeed, 10, 60);

            Object[] a = agentList.toArray();
            Arrays.sort(a);
            int co = 0;
            for (Object aa : a) {
                g2d.drawString("Score: " + ((Agent) aa).score, 20, 100 + (20 * (co++)));
            }
        }

        //adjust for screen controls   
        double scale = screenControls.getScale();

        //affine transformations
        g2d.translate(getWidth() / 2, getHeight() / 2);
        g2d.scale(scale, scale);
        g2d.translate(-screenControls.getX(), -screenControls.getY());

        //draw game boarder
        boarderColor = Debug.screenInv ? Color.WHITE : Color.BLACK;
        g2d.setColor(boarderColor);
        g2d.drawRect(0, 0, BORDER_SIZE, BORDER_SIZE);

        Agent target = screenControls.getTarget();
        if (target != null) {
            int ax = (int) target.getX() - 20;
            int ay = (int) target.getY() - 20;
            int size = (int) target.getSize() + 40;
            g2d.setColor(new Color(0.3f, 0.0f, 0.7f, 0.6f));
            g2d.fillOval(ax, ay, size, size);
            target.drawNeuralNetwork(g2d);
        }

        //draw food
        if (Debug.drawFood) {
            for (Food food : foodList) {
                food.draw(g2d);
                if (Debug.debug) {
                    boarderColor = Debug.screenInv ? Color.WHITE : Color.BLACK;
                    g2d.setColor(boarderColor);
                    g2d.drawString(""
                            + (int) (100 * food.getFoodPercent()),
                            (int) (food.getX() + food.getSize() / 5),
                            (int) (food.getY() + food.getSize() / 1.7));
                }
            }
        }

        /**
         * *** DRAW AGENT ****
         */
        for (Agent agent : agentList) {
            agent.drawFieldOFVision(g2d);
        }

        for (Agent agent : agentList) {
            agent.draw(g2d);

            if (Debug.drawHealthBar) {
                //draw healthbars
                agent.drawHealthBar(g2d);
            }

        }

        g2d.dispose();
    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                Thread.sleep(1000 / (60 * threadSpeed));

            } catch (InterruptedException ex) {
                Logger.getLogger(EvolutionWorld.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            update();
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        double scale = screenControls.getScale();

        //move dx and dy so that screenControls's x and y are in the top left corner
        dx = (int) (screenControls.getX());
        dy = (int) (screenControls.getY());

        //adjust so that screenControls's x and y are in the center of the screen
        dx += (int) (-(getWidth() / 2) / scale);
        dy += (int) (-(getHeight() / 2) / scale);

        //final step: adjust by the mouse position
        dx += (int) (e.getX() / scale);
        dy += (int) (e.getY() / scale);

        synchronized (this) {
            for (Agent agent : agentList) {
                if (agent.getBounds().contains(dx, dy)) {
                    screenControls.followTarget(agent);
                    //agent.startRecordingBrainActivity(brainActivity);
                    break;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (threadSpeed < 10) {
                threadSpeed++;
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
            //prevents division by zero
            if (threadSpeed > 1) {
                threadSpeed--;
            }
        }
    }

}
