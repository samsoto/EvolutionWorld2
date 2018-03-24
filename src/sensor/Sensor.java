package sensor;

import evolution.Debug;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import worldObject.Agent;
import worldObject.Agent;
import worldObject.CollidableObject;
import worldObject.CollidableObject;
import worldObject.Food;
import worldObject.Food;

public class Sensor implements Cloneable {

    //Shape of the sensor
    protected final int RADIUS;
    protected final int ARC_LENGTH;
    protected final int OFFSET;
    protected Shape sensor;
    
    protected CollidableObject closesObject=null;
    protected Double closesDistance = Double.POSITIVE_INFINITY;

    protected double[] outputs; //food, RGB, life
    protected int numFoodCollisions = 0;
    protected int numAgentCollisions = 0;
    protected int numEssence = 0;

    protected Agent host;

    final int NUM_OUTPUTS;

    protected boolean agentInSight = false;
    protected boolean foodInSight = false;

    protected CollidableObject target;

    public void setHost(Agent host) {
        this.host = host;
    }

    public Sensor(Agent host, int offset, int radius, int arcLength) {
        this.NUM_OUTPUTS = 4;
        this.host = host;
        this.RADIUS = radius;
        this.ARC_LENGTH = arcLength;
        this.OFFSET = offset;
        this.sensor = new Arc2D.Double();
        this.outputs = new double[NUM_OUTPUTS];
        this.sensor = new Arc2D.Double();
    }


    public double[] getOutputs() {
        return this.outputs;
    }


    protected void agentInSight(boolean b) {
        if (!b) { //safety measures
            target = null;
        }
        agentInSight = b;
    }

    protected void foodInSight(boolean b) {
        foodInSight = b;
    }

    @Override
    public Object clone() {
        try {
            Sensor sensor = (Sensor) super.clone();
            sensor.host = null;
            sensor.target = null;
            sensor.outputs = new double[NUM_OUTPUTS];
            return sensor;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Sensor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    

    public void collision(CollidableObject cObject) {
        if (getSensor().intersects(cObject.getBounds())) {
            
            //*********************************************
            // Sensor vs Agent
            //*********************************************
            if (cObject.getObjectType() == CollidableObject.AGENT) {
                numAgentCollisions++;
                agentInSight(true);

                Agent a = (Agent) cObject;
                
                
                double distance = Point2D.distance(host.getCenterX(), host.getCenterY(), a.getCenterX(), a.getCenterY());
                distance = distance > RADIUS ? RADIUS : distance; //clamp distance to the radius of the sensor            
                if(distance < closesDistance){
                    closesDistance = distance;
                    closesObject = a;
                    outputs[0] = 1-(distance/RADIUS);
                    outputs[1] = 1-(1/(1+a.score));
                    outputs[2] = 1-a.getLife();
                }
                
                //outputs[1] += a.color.getRed();
                //outputs[2] += a.color.getGreen();
                //outputs[3] += a.color.getBlue();
                //outputs[1] += 1 - a.lifePercent;
                target = cObject;

            //*********************************************
            // Sensor vs Food
            //*********************************************
            } else if (cObject.getObjectType() == CollidableObject.FOOD) {
                numFoodCollisions++;
                Food f = (Food) cObject;
                
                double distance = Point2D.distance(host.getCenterX(), host.getCenterY(), f.getCenterX(), f.getCenterY());
                distance = distance > RADIUS ? RADIUS : distance; //clamp distance to the radius of the sensor            
                if(distance < closesDistance){
                    closesDistance = distance;
                    closesObject = f;
                    outputs[3] = 1-(distance/RADIUS);
                }
                

                foodInSight(true);
            } else if (cObject.getObjectType() == CollidableObject.ESSENCE) {
                numEssence++;
                //outputs[5] = numEssence;
            }
        }
    }

    public void updateOutputs() {

        if (numFoodCollisions > 0) {
            //outputs[0] /= (double) numFoodCollisions;
        }
        if (numAgentCollisions > 0) {
            //outputs[1] /= (double) numAgentCollisions * 255;
            //outputs[2] /= (double) numAgentCollisions * 255;
            //outputs[3] /= (double) numAgentCollisions * 255;
            //outputs[4] /= (double) numAgentCollisions * 255;
        }
        if (numEssence > 0) {
            //outputs[5] /= (double) numEssence;
        }
    }


    public void draw(Graphics2D g2d) {

        updateOutputs();

        float red = 0.0f;
        float green = (float) outputs[3];
        float blue = (float) outputs[0];
        float alpha = !agentInSight && !foodInSight ? 0.0f : 0.3f;

//        if (agentInSight) {
//            red = (float) outputs[1];
//            green = (float) outputs[2];
//            blue = (float) outputs[3];
//        }

        //get updated sensor data
        Shape s = getSensor();

        if (Debug.drawFOV) {
            //draw sensor
            g2d.setColor(new Color(red, green, blue, alpha));
            g2d.fill(s);
        }

        if (Debug.drawFOVWireFrame) {
            //draw outline
            g2d.setColor(new Color(0.0f, 0.0f, 1.0f, 0.4f));
            g2d.draw(s);
        }
        

    }

    public void reset() {
        agentInSight(false);
        foodInSight(false);
        outputs = new double[outputs.length];
        numAgentCollisions = 0;
        numFoodCollisions = 0;
        closesDistance = Double.POSITIVE_INFINITY;
        closesObject = null;
    }

    public Shape getSensor() {

        //get the top left corner of the agent
        int x = (int) host.getCenterX() - (RADIUS / 2);
        int y = (int) host.getCenterY() - (RADIUS / 2);

        //centered with the direction the agent is facing
        int eyeAngle = (int) ((-host.getAngle() - (ARC_LENGTH / 2)) % 360);

        //rotate by an offset
        eyeAngle += OFFSET;

        //update the Arc Object to represent the field of vision
        ((Arc2D) sensor).setArc(x, y, RADIUS, RADIUS, eyeAngle, ARC_LENGTH, Arc2D.PIE);

        return sensor;
    }
}
