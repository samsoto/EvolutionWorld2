package evolution;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import neural.NeuralNetwork;
import neural.Neuron;

public class NeuralNetPanel extends JPanel implements Runnable {

    AtomicBoolean isRunning;
    NeuralNetwork neuralNet = null;
    Color color = null;
    Node[] node = null;

    public NeuralNetPanel() {
        isRunning = new AtomicBoolean(true);
        (new Thread(this)).start();
    }

    public void setNeuralNetwork(NeuralNetwork neuralNet) {
        synchronized (this) {
            this.neuralNet = neuralNet;
            setup();
        }

    }

    public void setup() {
        if (neuralNet != null) {
            node = new Node[neuralNet.GENES];

            //----------------------------------------------------
            final int size = 25;

            //INPUT LAYER
            for (int i = 0; i < neuralNet.INPUTS; i++) {
                float alpha = (float) neuralNet.getNeurons()[i].getOut();
                color = new Color(0.0f, 0.0f, 1.0f, alpha);
                node[i] = new Node(30 - (size / 2), 30 + 25 * i - (size / 2), color);
            }

            //HIDDEN LAYER
            double angle = 360.0 / neuralNet.HIDDEN;
            for (int i = 0; i < neuralNet.HIDDEN; i++) {

                double x = Math.sin(Math.toRadians(angle * i));
                double y = Math.cos(Math.toRadians(angle * i));

                x *= 100;
                y *= 100;

                x += 200;
                y += 200;

                float alpha = (float) neuralNet.getNeurons()[neuralNet.INPUTS + i].getOut();
                color = new Color(0.0f, 0.0f, 1.0f, alpha);
                node[neuralNet.INPUTS + i] = new Node((int) x - (size / 2), (int) y - (size / 2), color);

            }

            //OUTPUT LAYER
            for (int i = 0; i < neuralNet.OUTPUTS; i++) {
                float alpha = (float) neuralNet.getNeurons()[(neuralNet.INPUTS + neuralNet.HIDDEN) + i].getOut();
                color = new Color(0.0f, 0.0f, 1.0f, alpha);
                node[neuralNet.INPUTS + neuralNet.HIDDEN + i] = new Node(370 - (size / 2), 30 + 30 * i - (size / 2), color);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {

        synchronized (this) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            //draw background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.scale(0.7, 0.7);

            if (neuralNet != null) {
                
                final int yOffset = 80;

                //CONNECTIONS
                for (int i = neuralNet.INPUTS; i < neuralNet.GENES; i++) {
                    Neuron neuron = neuralNet.getNeurons()[i];
                    for (int j = 0; j < neuron.inIdx.length; j++) {
                        g2d.setColor(Color.WHITE);
                        int x1 = node[i].x;
                        int y1 = node[i].y;
                        int x2 = node[neuron.inIdx[j]].x;
                        int y2 = node[neuron.inIdx[j]].y;
                        g2d.drawLine(x1, y1+yOffset, x2, y2+yOffset);
                    }
                }

                final int size = 27;
                
                

                //INPUT LAYER
                for (int i = 0; i < neuralNet.INPUTS; i++) {
                    float alpha = (float) neuralNet.getNeurons()[i].getOut();
                    color = new Color(0.0f, 0.0f, 1.0f, alpha);

                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(node[i].x(), node[i].y()+yOffset, node[i].size-4, node[i].size-4);

                    g2d.setColor(color);
                    g2d.fillOval(node[i].x(), node[i].y()+yOffset, node[i].size-4, node[i].size-4);

                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(node[i].x(), node[i].y()+yOffset, node[i].size-4, node[i].size-4);
                }

                //HIDDEN LAYER
                double angle = 360.0 / neuralNet.HIDDEN;
                for (int i = 0; i < neuralNet.HIDDEN; i++) {
                    float alpha = (float) neuralNet.getNeurons()[neuralNet.INPUTS + i].getOut();
                    color = new Color(0.0f, 0.0f, 1.0f, alpha);
                    int offset = i + neuralNet.INPUTS;

                    Node _node = node[offset];
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(_node.x(), _node.y()+yOffset, _node.size, _node.size);

                    g2d.setColor(color);
                    g2d.fillOval(_node.x(), _node.y()+yOffset, _node.size, _node.size);

                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(_node.x(), _node.y()+yOffset, _node.size, _node.size);
                }

                //OUTPUT LAYER
                for (int i = 0; i < neuralNet.OUTPUTS; i++) {

                    float alpha = (float) neuralNet.getNeurons()[(neuralNet.INPUTS + neuralNet.HIDDEN) + i].getOut();
                    color = new Color(0.0f, 0.0f, 1.0f, alpha);
                    int offset = i + neuralNet.INPUTS + neuralNet.HIDDEN;

                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(node[offset].x(), node[offset].y()+yOffset, node[offset].size, node[offset].size);

                    g2d.setColor(color);
                    g2d.fillOval(node[offset].x(), node[offset].y()+yOffset, node[offset].size, node[offset].size);

                    g2d.setColor(Color.WHITE);
                    g2d.drawOval(node[offset].x(), node[offset].y()+yOffset, node[offset].size, node[offset].size);
                }

            }else{
                g2d.setColor(Color.WHITE);
                g2d.scale(1.5, 1.5);
                g2d.drawString("No Agent Selected", 10, 20);
            }
        }

    }

    @Override
    public void run() {
        while (isRunning.get()) {
            try {
                Thread.sleep(1000 / 25);
            } catch (InterruptedException ex) {
            }
            repaint();
        }
    }

    private class Node {

        public int x;
        public int y;
        public Color color;
        public final int size = 30;

        public Node(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public int x() {
            return x - (size / 2);
        }

        public int y() {
            return y - (size / 2);
        }

    }
}
