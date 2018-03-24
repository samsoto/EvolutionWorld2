package evolution;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import worldObject.Agent;

public final class ScreenControls implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {

    //Singleton instance
    private static ScreenControls instance = null;

    //Target agent
    private Agent target = null;

    //Zoom controls
    private final double ZOOM_SPEED;
    private double scale;

    //Pan controls
    private final double TRANSITION_DAMPER;
    private int x, y;
    private int oldX, oldY;
    private int mouseX, mouseY;

    //This is private to defeat instantiation (note this is a singleton class)
    private ScreenControls() {

        TRANSITION_DAMPER = 0.4;
        ZOOM_SPEED = 1.04;
        scale = 0.3;
        x = 1000;
        y = 1000;
        oldX = x;
        oldY = y;
        mouseX = 0;
        mouseY = 0;
    }

    //Guarantees only one instance of this class
    public static ScreenControls getInstance() {
        if (instance == null) {
            instance = new ScreenControls();
        }
        return instance;
    }

    public Agent getTarget() {
        return target;
    }

    public void followTarget(Agent agent) {
        target = agent;
    }

    public void zoomOut() {
        scale /= ZOOM_SPEED;
    }

    public void zoomIn() {
        scale *= ZOOM_SPEED;
    }

    public double getScale() {
        return scale;
    }

    public void stopFollowing() {
        if (target != null) {
            target.stopRecordingBrainActivity();
            target = null;
        }

    }

    public int getX() {
        if (target != null) {

            oldX = x;
            x = (int) (target.getCenterX());

            if (Math.abs(x - oldX) > 0) {

                if (x > oldX) { //if this agent is traveling right
                    x = (int) (oldX + TRANSITION_DAMPER * Math.abs(x - oldX));
                } else { //if this agent is traveling left
                    x = (int) (oldX - TRANSITION_DAMPER * Math.abs(x - oldX));
                }
            }

        }
        return x;
    }

    public int getY() {
        if (target != null) {

            oldY = y;
            y = (int) (target.getCenterY());

            if (Math.abs(y - oldY) > 0) {

                //if this agent is traveling right
                if (y > oldY) {
                    y = (int) (oldY + TRANSITION_DAMPER * Math.abs(y - oldY));
                } else { //if this agent is traveling left
                    y = (int) (oldY - TRANSITION_DAMPER * Math.abs(y - oldY));
                }
            }

        }
        return y;
    }

    //*********************************************************
    // Mouse events
    //*********************************************************
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            zoomOut();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            zoomIn();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            if (e.getPreciseWheelRotation() > 0) {
                zoomOut();
            }
            if (e.getPreciseWheelRotation() < 0) {
                zoomIn();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        stopFollowing();
        mouseX = e.getX();
        mouseY = e.getY();
        oldX = x;
        oldY = y;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        stopFollowing();
        x = (int) (((oldX) + (mouseX - e.getX()) / scale) * 1);
        y = (int) (((oldY) + (mouseY - e.getY()) / scale) * 1);
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
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

}
