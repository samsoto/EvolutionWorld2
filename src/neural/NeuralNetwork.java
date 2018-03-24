package neural;

import evolution.EvolutionWorld;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NeuralNetwork implements Cloneable {

    //Network configuration
    public final int INPUTS;
    public final int OUTPUTS;
    public final int HIDDEN;
    public final int GENES;

    //mutation rate 5% chance
    private final double MUTATE_RATE = 0.05;

    //Nodes and output
    private Neuron[] neuralNet;
    private double[] outputs;

    //Util
    private Random rand;
    BufferedWriter brainActivity = null;
    boolean recording = false;
    double TIME = 15;
    double timer = TIME;

    public NeuralNetwork(int inputs, int hidden, int outputs) {

        this.rand = EvolutionWorld.rand;

        this.INPUTS = inputs;
        this.OUTPUTS = outputs;
        this.HIDDEN = hidden;
        this.GENES = INPUTS + OUTPUTS + HIDDEN;

        this.outputs = new double[OUTPUTS];
        this.neuralNet = new Neuron[GENES];

        for (int i = 0; i < GENES; i++) {
            this.neuralNet[i] = new Neuron(1 + rand.nextInt(7), GENES, this, rand);
        }

    }

    public void mutate() {
        for (int i = 0; i < neuralNet.length; i++) {
            Neuron neuron = neuralNet[i];
            for (int j = 0; j < neuron.inIdx.length; j++) {
                //mutate input index (where the input is coming from)
                if (rand.nextDouble() <= MUTATE_RATE) {
                    neuron.inIdx[j] = rand.nextInt(neuron.inIdx.length);
                }
                //mutate complement
                if (rand.nextDouble() <= MUTATE_RATE) {
                    neuron.complement[j] = rand.nextBoolean();
                }
                //mutate weights
                if (rand.nextDouble() <= MUTATE_RATE) {
                    neuron.weight[j] = rand.nextDouble();
                }
            }
        }
    }

    public void setInputs(double[] inputs) {
        if (inputs.length != INPUTS) {
            System.err.println("Wrong Input Size! "
                    + "Class: NeuralNetworks, Method: setInputs");
        }
        for (int i = 0; i < INPUTS; i++) {
            neuralNet[i].setOut(inputs[i]);
        }
    }

    public double[] getOutputs() {
        //return an array of the output node's out values
        for (int i = 0; i < OUTPUTS; i++) {
            outputs[i] = neuralNet[(i) + (INPUTS) + (HIDDEN)].getOut();
        }
        return outputs;
    }

    public void tick() {
        for (int i = INPUTS; i < GENES; i++) {
            neuralNet[i].getInputs();
        }
        for (int i = INPUTS; i < GENES; i++) {
            neuralNet[i].tick();
        }

        for (int i = INPUTS; i < GENES; i++) {
            neuralNet[i].back();
        }

        for (int i = INPUTS; i < GENES; i++) {
            neuralNet[i].kp();
        }

        if (recording) {
            try {
                if (timer <= 0) {
                    timer = TIME;
                    for (int i = 0; i < GENES; i++) {
                        brainActivity.write((int) (3 * neuralNet[i].getOut()) + ""); //0-9
                    }
                    brainActivity.write("\n");
                }
                timer--;
            } catch (IOException ex) {
            }
        }
    }

    public Neuron[] getNeurons() {
        return neuralNet;
    }

    public void startRecordingBrainActivity(BufferedWriter bw) {
        brainActivity = bw;
        recording = true;
    }

    public void stopRecordingBrainActivity() {
        if (brainActivity != null) {
            try {
                System.out.println("Stopped recording brain activity");
                brainActivity.close();
                brainActivity = null;
                recording = false;
            } catch (IOException ex) {
                Logger.getLogger(NeuralNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
       }

    }

    @Override
    public Object clone() {

        try {
            Neuron[] temp = new Neuron[GENES];
            NeuralNetwork nn = (NeuralNetwork) super.clone();
            nn.outputs = new double[GENES];
            for (int i = 0; i < neuralNet.length; i++) {
                Neuron neuron = nn.neuralNet[i];
                neuron = (Neuron) neuron.clone();
                neuron.neuralNet = nn;
                temp[i] = neuron;
            }
            nn.neuralNet = temp;
            return nn;

        } catch (CloneNotSupportedException ex) {
        }
        return null;

    }

}
