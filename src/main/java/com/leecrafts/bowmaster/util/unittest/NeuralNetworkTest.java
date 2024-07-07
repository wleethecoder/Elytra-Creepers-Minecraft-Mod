package com.leecrafts.bowmaster.util.unittest;

import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetworkTest {

    private static BasicNetwork createToyBasicNetwork() {
        FeedForwardPattern pattern = new FeedForwardPattern();
        pattern.setInputNeurons(2);
        pattern.addHiddenLayer(3);
        pattern.setOutputNeurons(2);
        pattern.setActivationFunction(new ActivationSoftMax()); // output of softmax is (0, 1)

        BasicNetwork network = (BasicNetwork) pattern.generate();
//        network.reset();
        double[] weights = network.getFlat().getWeights();
        double[] weightsToAssign = {0.4,1.8,3.2,0.8,0.8,2.6,2.8,1.6,1.5,2.5,1.3,1.1,0.6,0.6,2.0,0.5,0.5};
        System.arraycopy(weightsToAssign, 0, weights, 0, weights.length);
        return network;
    }

    public static NeuralNetwork createToyNetwork() {
        int[] hiddenLayerSizes = {2};
        String[] hiddenActivations = {NeuralNetwork.TANH};
        int[] outputSizes = {3, 2};
        String[] outputActivations = {NeuralNetwork.TANH, NeuralNetwork.SOFTMAX};

        return new NeuralNetwork(2, hiddenLayerSizes, hiddenActivations, outputSizes, outputActivations);
    }

    private static void test1() {
        NeuralNetwork network = createToyNetwork();
        network.printWeights();
        NeuralNetworkUtil.saveModel(network);
        NeuralNetwork network1 = NeuralNetworkUtil.loadOrCreateModel();
        network1.printWeights();
    }

    private static void test2() {
        NeuralNetwork network = createToyNetwork();
        network.printWeights();

        ArrayList<double[]> states = new ArrayList<>();
        states.add(new double[] {5, 4});

        ArrayList<List<double[]>> actionProbs = new ArrayList<>();
        ArrayList<double[]> list = new ArrayList<>();
        list.add(new double[] {-0.2, 0.9, -0.3});
        list.add(new double[] {0.4, 0.6});
        actionProbs.add(list);

        ArrayList<int[]> actions = new ArrayList<>();
        actions.add(new int[] {Integer.MAX_VALUE, 1}); // first array entry should be ignored

        ArrayList<Double> rewards = new ArrayList<>();
        rewards.add(4.5);

        NeuralNetworkUtil.updateNetwork(network, states, actionProbs, actions, rewards);
        network.printWeights();
    }

//    private static void test1() {
//        BasicNetwork network = createToyBasicNetwork();
//        ArrayList<double[]> states = new ArrayList<>();
//        states.add(new double[] {-2, 1});
//        states.add(new double[] {3, 5});
//        ArrayList<double[]> actions = new ArrayList<>(); // log probabilities
//        actions.add(new double[] {-0.2, -1.6});
//        actions.add(new double[] {-0.1, -0.7});
//        ArrayList<Double> rewards = new ArrayList<>();
//        rewards.add(2.0);
//        rewards.add(-1.0);
//        double learningRate = 0.1;
//        double gamma = 0.75;
//        System.out.println(network.dumpWeightsVerbose());
//        System.out.println(network.dumpWeights());
//    }

//    private static void test2() {
//        MultiOutputFreeformNetwork network = createToyMultiOutputFreeformNetwork();
//        double[] observations = new double[] {-.5, 0.7, 1};
//        double[] outputs = NeuralNetworkUtil.computeOutput(network, observations);
//
//        NeuralNetworkUtil.printWeights(network);
//        for (int i = 0; i < outputs.length; i++) {
//            System.out.println(outputs[i]);
//        }
//    }

    public static void main(String[] args) {
        System.out.println("Running unit tests to verify correctness of REINFORCE algorithm");
//        test1();
        test2();
    }

}
