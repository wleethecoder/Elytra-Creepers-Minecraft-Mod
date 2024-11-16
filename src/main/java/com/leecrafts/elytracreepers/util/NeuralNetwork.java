package com.leecrafts.elytracreepers.util;

import java.util.Random;

public class NeuralNetwork {

    private int weights;

    public NeuralNetwork() {
        this.weights = (new Random()).nextInt(100);
    }

    public int getWeights() {
        return this.weights;
    }

    public void setWeights(int weights) {
        this.weights = weights;
    }

}
