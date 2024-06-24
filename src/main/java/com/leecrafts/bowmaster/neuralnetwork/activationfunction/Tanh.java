package com.leecrafts.bowmaster.neuralnetwork.activationfunction;

public class Tanh implements ActivationFunction {
    public double activate(double input) {
        return Math.tanh(input);
    }

    public double[] activate(double[] inputs) {
        double[] outputs = new double[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = Math.tanh(inputs[i]);
        }
        return outputs;
    }
}