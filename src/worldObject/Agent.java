package worldObject;

import evolution.EvolutionWorld;
import evolution.Debug;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.util.Random;
import neural.NeuralNetwork;
import neural.Neuron;
import sensor.Sensor;

public class Agent extends CollidableObject implements Cloneable, Comparable<Agent> {

    //movement
    protected final double MAX_SPEED;
    protected final double MAX_TURN_SPEED;

    //parameters 
    private final Random rand;
    public int speciesId;
    public int generation = 1;
    public double spikePercent;
    public Sensor[] sensors;
    public double mutateRate;
    public boolean wasKilled = false;

    //scoring
    public int score = 0;

    //neural network
    final int INPUTS;
    final int HIDDEN;
    final int OUTPUTS;
    final int GENES;
    public NeuralNetwork neuralNet;
    public double[] inputs; //input buffer   

    //constructor
    public Agent(int id) {

        super(CollidableObject.AGENT);
        rand = EvolutionWorld.rand;

        //init sensors
        int offset = 11;
        sensors = new Sensor[]{
            new Sensor(this, offset, 600, 35),
            new Sensor(this, -offset, 600, 35)
        };

        //count how many sensor inputs there are
        int count = 0;
        for (Sensor s : sensors) {
            count += s.getOutputs().length;
        }

        //config the size of the neural network
        INPUTS = 3 + count;
        HIDDEN = 12;
        OUTPUTS = 3;
        GENES = INPUTS + HIDDEN + OUTPUTS;
        neuralNet = new NeuralNetwork(INPUTS, HIDDEN, OUTPUTS);

        //used as a buffer for updating the neural network
        inputs = new double[INPUTS];

        //species identifier
        speciesId = id;

        //init move parameters 
        MAX_TURN_SPEED = 10;
        MAX_SPEED = 2;
        angle = rand.nextInt(360);

        mutateRate = 0.2; //20% of clones will be mutated
        lifePercent = 1;
        spikePercent = 0.5;

        //init size and position
        setX((rand.nextDouble() * EvolutionWorld.BORDER_SIZE));
        setY((rand.nextDouble() * EvolutionWorld.BORDER_SIZE));
        setSize(30);

        //init color 
        int red = rand.nextInt(256);
        int green = rand.nextInt(256);
        int blue = rand.nextInt(256);
        color = new Color(red, green, blue);
    }

    //This method is used to start recording this agent's brain 
    //activity over time. The results will be used
    //as a statistical sample for computing neural complexity
    public void startRecordingBrainActivity(BufferedWriter bw) {
        neuralNet.startRecordingBrainActivity(bw);
    }

    public void stopRecordingBrainActivity() {
        neuralNet.stopRecordingBrainActivity();
    }

    @Override
    public void handleCollision(CollidableObject other) {

        //****************************************************************
        //Agent vs Agent
        //****************************************************************
        if (other.getObjectType() == CollidableObject.AGENT) {
            Agent a = (Agent) other;

            a.decreaseLife(0.13 * spikePercent + 0.1 * (speed / MAX_SPEED));

            //if you kill another bot
            if (a.lifePercent <= 0) {

                //mark the other agent as killed
                //a.wasKilled = true;
                score += a.score;
                a.score = 0;

                lifePercent += 0.3;
                lifePercent += 0.2 * spikePercent;
                lifePercent = lifePercent > 1 ? 1 : lifePercent;

                //Preditors get points for killing another Agent
                //pointsUntilSpawn -= (AMOUNT_UNTIL_SPAWN / 4) + 1;
                //score++;
            }
        }

        //****************************************************************
        //Agent vs Food
        //****************************************************************
        if (other.getObjectType() == CollidableObject.FOOD) {
            Food food = (Food) other;
            double c = food.consume(this);
            increaseLife(c);
            score++;
        }
    }

    //****************************************************************
    // NEURAL NETWORK INPUTS
    //****************************************************************
    private void neuralNetworkInputs() {

        //(self) life percentage
        inputs[0] = lifePercent;
        inputs[1] = 1;
        inputs[2] = 1 - (1 / (1 + score));

        //dynamically input eye data
        int idx = 2;
        for (Sensor eye : sensors) {
            eye.updateOutputs();
            for (double d : eye.getOutputs()) {
                inputs[++idx] = d;
            }
        }

        //update neural network
        neuralNet.setInputs(inputs);
        neuralNet.tick();
    }

    //****************************************************************
    // NEURAL NETWORK OUTPUTS
    //****************************************************************
    private void neuralNetworkOutputs() {

        double[] out = neuralNet.getOutputs();

        //Steering
        turn(out[0]);

        //Moving
        double boost = out[1];
        boost = boost > 0.5 ? 1 : 0;
        if (boost == 1) {
            decreaseLife(0.0009);
        }
        move(out[2] + 2 * boost);

        //reset sensors
        for (Sensor s : sensors) {
            s.reset();
        }
    }

    public void update() {

        //decrease life be a little bit eah frame
        decreaseLife(0.0007);

        //Send the neural network input data
        neuralNetworkInputs();

        //Get output data from the neural network
        neuralNetworkOutputs();

    }

    public void draw(Graphics g) {

        //create a new graphics object
        Graphics2D g2d = (Graphics2D) g.create();

        //affine transformation
        g2d.translate(getCenterX(), getCenterY());
        g2d.rotate(Math.toRadians(angle));

        //draw the agent
        g2d.setColor(color);
        g2d.drawLine(0, 0, (int) (3 * size * spikePercent), 0);
        g2d.fillOval((int) -size / 2, (int) -size / 2, (int) size, (int) size);

        //draw a boarder around the agent (makes the agent is more visible)
        Color boarder = Debug.screenInv ? Color.WHITE : Color.BLACK;
        g2d.setColor(boarder);
        g2d.drawOval((int) -size / 2, (int) -size / 2, (int) size, (int) size);
    }

    public void drawFieldOFVision(Graphics2D g) {
        for (Sensor eye : sensors) {
            eye.draw(g);
        }
    }

    public void drawNeuralNetwork(Graphics2D g2d) {
        Neuron[] neurons = neuralNet.getNeurons();
        int boxSize = 20;

        int offsetX = -80;
        int offsetY = 25;
        for (int i = 0; i < INPUTS; i++) {
            double out = neurons[i].getOut();
            g2d.setColor(new Color((int) (out * 255), (int) (out * 255), (int) (out * 255)));
            g2d.fillRect(i * boxSize + (int) getX() + offsetX, (int) getSize() + (int) getY() + offsetY, boxSize, boxSize);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(i * boxSize + (int) getX() + offsetX, (int) getSize() + (int) getY() + offsetY, boxSize, boxSize);

        }
        for (int i = 0; i < HIDDEN; i++) {
            double out = neurons[i + INPUTS].getOut();
            g2d.setColor(new Color((int) (out * 255), (int) (out * 255), (int) (out * 255)));
            g2d.fillRect(i * boxSize + (int) getX() + offsetX, (int) getSize() + (int) getY() + offsetY + boxSize, boxSize, boxSize);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(i * boxSize + (int) getX() + offsetX, (int) getSize() + (int) getY() + offsetY + boxSize, boxSize, boxSize);
        }
        for (int i = 0; i < OUTPUTS; i++) {
            double out = neurons[i + INPUTS + HIDDEN].getOut();
            g2d.setColor(new Color((int) (out * 255), (int) (out * 255), (int) (out * 255)));
            g2d.fillRect(i * boxSize + (int) getX() + offsetX, (int) getSize() + (int) getY() + offsetY + (2 * boxSize), boxSize, boxSize);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(i * boxSize + (int) getX() + offsetX, (int) getSize() + (int) getY() + offsetY + (2 * boxSize), boxSize, boxSize);
        }
    }

    public void drawHealthBar(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        final int HB_WIDTH = 50; //heath bar
        final int HB_HEIGHT = 12;//heath bar

        int offset = 20;
        Color boarder = Debug.screenInv ? Color.WHITE : Color.BLACK;
        g2d.setColor(Color.BLACK);
        g2d.fillRect((int) (-size + getX() + offset), (int) (-1 * size + getY()), HB_WIDTH, HB_HEIGHT);
        g2d.setColor(Color.GREEN);
        g2d.fillRect((int) (-size + getX() + offset), (int) (-1 * size + getY()), (int) (lifePercent * HB_WIDTH), HB_HEIGHT);
        g2d.setColor(boarder);
        g2d.drawRect((int) (-size + getX() + offset), (int) (-1 * size + getY()), HB_WIDTH, HB_HEIGHT);

        int offsetX = 60;
        int offsetY = 10;
        if (Debug.agentDebug) {
            g2d.drawString("Life: " + (int) (100 * lifePercent) + "%", (int) (getX() + offsetX), (int) (getY() + offsetY));
            g2d.drawString("Score: " + score, (int) (getX() + offsetX), (int) (getY() + offsetY + 20));
            //g2d.drawString("Gen: " + generation, (int) (getX() + 20), (int) (getY() + 70));
            //g2d.drawString("PUS: " + score, (int) (getX() + 20), (int) (getY() + 90));
            //g2d.drawString("SP: " + (int) (100 * spikePercent) + "%", (int) (getX() + 20), (int) (getY() + 110));
            //g2d.drawString("ID: " + speciesId, (int) (getX() + 20), (int) (getY() + 130));
        }

    }

    public Agent mutate() {
        if (rand.nextDouble() <= mutateRate) {
            neuralNet.mutate();
        }
        return this;
    }

    public Sensor[] getEyes() {
        return sensors;
    }

    public void move(double d) {
        speed = MAX_SPEED * d;
        setX(getX() + (MAX_SPEED * d * Math.cos(Math.toRadians(angle))));
        setY(getY() + (MAX_SPEED * d * Math.sin(Math.toRadians(angle))));
    }

    //d must be between 0 and 1
    public void turn(double d) {
        double turn = (d * 2) - 1;
        angle = (angle + MAX_TURN_SPEED * turn) % 360;
    }

    public void spike(double d) {
        spikePercent = d;
    }

    public void setLife(double d) {
        this.lifePercent = d;
    }

    @Override
    public int compareTo(Agent t) {
        if (score > t.score) {
            return -1;
        } else if (score == t.score) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "" + score;
    }

    @Override
    public Object clone() {
        try {
            Agent a = (Agent) super.clone();
            a.setX(getX() + (rand.nextInt(600) - 300));
            a.setY(getY() + (rand.nextInt(600) - 300));
            a.bounds = new Rectangle((int) a.x, (int) a.y, size, size);
            a.lifePercent = 1;
            a.score = 0;
            a.angle = EvolutionWorld.rand.nextInt(360);
            a.neuralNet = (NeuralNetwork) a.neuralNet.clone();
            a.generation++;
            a.inputs = new double[INPUTS];
            Sensor[] temp = a.sensors;
            a.sensors = new Sensor[temp.length];
            for (int i = 0; i < sensors.length; i++) {
                a.sensors[i] = (Sensor) temp[i].clone();
                a.sensors[i].setHost(a);
            }
            a.inputs = inputs.clone();
            return a.mutate();
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }
}
