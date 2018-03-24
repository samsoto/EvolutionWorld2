package worldObject;

import java.awt.Color;
import java.awt.Rectangle;

public abstract class CollidableObject {

    //Types of CollidableObjects
    public static final int AGENT = 0;
    public static final int FOOD = 1;
    public static final int ESSENCE = 2;
    
    //Movement
    protected double x, y;      
    protected double angle;
    protected double speed;

    //Attributes
    protected int type;   
    protected Rectangle bounds; 
    protected Color color;         
    protected double lifePercent;  
    protected int size;           

    //Constructor
    public CollidableObject(int type) {
        this(type, 0, 0, 10);
    }

    //Constructor
    public CollidableObject(int type, double x, double y, int size) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle((int) x, (int) y, size, size);
        this.color = new Color(0, 0, 0);
        this.lifePercent = 0;
    }

    //handle collisions
    public abstract void handleCollision(CollidableObject other);

    //check for collisions
    public static boolean checkCollision(CollidableObject c1, CollidableObject c2) {
        return c1.getBounds().intersects(c2.getBounds());
    }

    //********************************************************************
    // Sets
    //********************************************************************
    public void setSize(int size) {
        this.size = size;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setCenterX(double x) {
        this.x = x - (size / 2);
    }

    public void setCenterY(double y) {
        this.y = y - (size / 2);
    }

    public void decreaseLife(double x) {
        lifePercent -= x;
        if (lifePercent < 0) {
            lifePercent = 0;
        }
    }

    public void increaseLife(double x) {
        lifePercent += x;
        if (lifePercent > 1) {
            lifePercent = 1;
        }
    }

    //********************************************************************
    // Gets
    //********************************************************************
    public double getAngle(){
        return angle;
    }
    
    public double getSpeed(){
        return speed;
    }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getCenterX() {
        return (int) (x + size / 2);
    }

    public int getCenterY() {
        return (int) (y + size / 2);
    }

    public int getObjectType() {
        return type;
    }

    public double getSize() {
        return size;
    }

    public Rectangle getBounds() {
        bounds.width = size;
        bounds.height = size;
        bounds.x = (int) x;
        bounds.y = (int) y;
        return bounds;
    }

    public double getLife() {
        return lifePercent;
    }
}
