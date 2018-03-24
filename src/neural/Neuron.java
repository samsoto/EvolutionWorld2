package neural;

import java.util.Random;

public class Neuron implements Cloneable {

    public NeuralNetwork neuralNet;
    public int[] inIdx;
    public double[] weight;
    public boolean[] complement;
    public boolean[] hardValue;
    public double[] inputs;
    private double out;
    private final double bias;
    private final boolean type;
    private final double kp;
    private final double gw;
    private double oldOut;
    private double target;

    public Neuron(int inputSize, int numNeurons, NeuralNetwork neuralNet, Random rand) {
        this.neuralNet = neuralNet;
        inIdx = new int[inputSize];
        inputs = new double[inputSize];
        weight = new double[inputSize];
        complement = new boolean[inputSize];
        hardValue = new boolean[inputSize];
        
        bias = (rand.nextDouble() * 4) - 3;
        type = rand.nextDouble() < 0.05;  
        gw = rand.nextDouble()*5;
        kp = (rand.nextDouble()*0.2)+0.9;  // [0.9, 1.1]
        out = 0;
        oldOut=0;
        target = 0;

        int a = (int) (inputSize * 0.8);
        for (int i = 0; i < a; i++) {
            inIdx[i] = rand.nextInt(neuralNet.INPUTS);
            weight[i] = rand.nextDouble() < 0.5 ? 0 : (rand.nextDouble() * 6) - 3;
            complement[i] = rand.nextBoolean();
            hardValue[i] = rand.nextBoolean();
        }

        for (int i = a; i < inputSize; i++) {
            inIdx[i] = rand.nextInt(numNeurons);
            weight[i] = (rand.nextDouble() * 6) - 3;
            complement[i] = rand.nextBoolean();
            hardValue[i] = rand.nextBoolean();
        }
    }

    public void setOut(double o) {
        out = o;
        out = out < 0 ? 0 : out;
        out = out > 1 ? 1 : out;
    }

    public double getOut() {
        out = out < 0.0 ? 0.0 : out;
        out = out > 1.0 ? 1.0 : out;
        return out;
    }

    public void getInputs() {
        //get an array of all neurons in a specific neural network
        Neuron[] neurons = neuralNet.getNeurons();
        
        for (int i = 0; i < inIdx.length; i++) {
            int idx = inIdx[i];   //index of the input neuron  
            Neuron n = neurons[idx];
            inputs[i] = n.out;
        }
    }

    public void tick() {

        double acc=0;
        for(int i=0 ; i<inputs.length; i++){
            int idx = inIdx[i];
            double val = inputs[i];
            if(type){
                val -= oldOut;
                val *= 10;
            }
            acc += val*weight[i];
        }
        acc *= gw;
        acc += bias;
        acc = 1.0/(1.0 + Math.exp(-acc));
        target = acc;
    }
    
    public void back(){
        oldOut = out;
    }
    
    public void kp(){
        out = out + (target - out)*kp;
    }

    @Override
    public Object clone() {
        try {
            Neuron n = (Neuron) super.clone();
            n.inIdx = inIdx.clone();
            n.weight = weight.clone();
            n.complement = complement.clone();
            n.inputs = inputs.clone();
            n.hardValue = hardValue.clone();
            return n;
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }
}
