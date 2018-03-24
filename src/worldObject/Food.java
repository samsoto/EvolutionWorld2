package worldObject;

import evolution.Debug;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Food extends CollidableObject {

    final double CONSUME_AMOUNT = 0.2;
    final double REGENERATE_AMOUNT = 0.001;

    public boolean isAlive;

    public static final int SIZE = 20;

    public Food(int x, int y) {
        super(CollidableObject.FOOD);
        //SIZE = size;
        lifePercent = 1;
        isAlive = true;
        setCenterX(x);
        setCenterY(y);
        setSize(SIZE);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 255, 0, (int) (0.8 * 255 * lifePercent)));
        g2d.fillRect((int) getX(), (int) getY(), SIZE, SIZE);
//BETA ---------------------------------------------------------------------------
        Color debugColor = Debug.screenInv ? Color.WHITE : Color.BLACK;
        g2d.setColor(debugColor);
        g2d.drawRect((int) getX(), (int) getY(), SIZE, SIZE);
    }

    public double getFoodPercent() {
        return lifePercent;
    }

    public double age() {
        if (lifePercent <= 0.1) {
            lifePercent -= REGENERATE_AMOUNT;
        }
        if(lifePercent <= 0){
            lifePercent=0;
            isAlive = false;
        }
        return REGENERATE_AMOUNT;
    }

    public double consume(Agent a) {
        
        
        double value = 1 - a.spikePercent;
        value = (value*2)-1;
        value = Math.tanh(2*value);
        value = (value+1)/2;
        
        double c = CONSUME_AMOUNT * value;
        
        lifePercent -= c;
        lifePercent = lifePercent < 0 ? 0 : lifePercent;

        if (lifePercent == 0) {
            isAlive = false;
            return 0;
        }

        return c;
    }

    @Override
    public void handleCollision(CollidableObject other) {

    }

}
