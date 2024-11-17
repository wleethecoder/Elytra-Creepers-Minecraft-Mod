package com.leecrafts.elytracreepers.util;

import java.util.Arrays;

public class NeuralNetworkTest {

    private static void testWeightsArrayLength() {
        System.out.println("testing weights array length");
        NeuralNetwork network = new NeuralNetwork(new int[]{2, 3, 2}, "linear", "linear");
        network.printWeights();
        System.out.println(network.getWeights().length);
        assert network.getWeights().length == 12;
    }

    private static void testFeedForward() {
        System.out.println("testing feed forward");
        NeuralNetwork network = new NeuralNetwork(new int[] {2, 3, 2}, "linear", "linear");
        double[] weights = new double[] {5,3,1,-5,7,6,7,10,-5,-9,6,2};
        double[] biases = new double[] {1,3,2,-3,-8};
        network.setWeights(weights);
        network.setBiases(biases);
        network.printWeights();
        double[] output = network.forward(new double[] {6, 8});
        System.out.println(Arrays.toString(output));
        assert output[0] == -115 && output[1] == -679;
    }

    public static void main(String[] args) {
        testWeightsArrayLength();
        testFeedForward();
    }

}
