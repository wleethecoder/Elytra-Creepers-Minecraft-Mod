package com.leecrafts.elytracreepers.util;

import java.util.Random;

public class NeuralNetwork {

    private final int weights;

    public NeuralNetwork() {
        this.weights = (new Random()).nextInt(100);
    }

    public int getWeights() {
        return this.weights;
    }

}
