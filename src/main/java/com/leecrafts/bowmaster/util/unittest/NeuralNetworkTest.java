package com.leecrafts.bowmaster.util.unittest;

import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import org.encog.engine.network.activation.ActivationSoftMax;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static void test3() {
        ArrayList<Double> winnerRewards = new ArrayList<>(Arrays.asList(5.6, 19.2, -0.5));
        ArrayList<Double> loserRewards = new ArrayList<>(Arrays.asList(1.4, 6.0, -9.5));
        NeuralNetworkUtil.logRewards(winnerRewards, loserRewards);
    }

    private static void test4() {
        String basePath = "/Users/wlee2019/Downloads/mod repos/skeleton bow master/run/networks";
        NeuralNetwork network = NeuralNetwork.loadModel(new File(basePath + "/model-2.dat"));
        network.printWeights();
        NeuralNetwork network1 = NeuralNetwork.loadModel(new File(basePath + "/model-1.dat"));
        network1.printWeights();
    }

    private static void test5() {
        NeuralNetwork network = NeuralNetworkUtil.createNetwork();
        network.printWeights();
        List<double[]> outputs = network.feedForward(new double[] {5,5,5,5,5,5,5,5,5});
        for (double[] output : outputs) {
            for (double o : output) {
                System.out.print(o + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        System.out.println("Running unit tests");
//        test1();
//        test2();
//        test3();
        test4();
//        test5();
    }

}
