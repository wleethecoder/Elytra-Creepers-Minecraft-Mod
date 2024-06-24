package com.leecrafts.bowmaster.neuralnetwork.activationfunction;

public class Softmax implements ActivationFunction {
    public double activate(double input) {
        return input; // Softmax on single value doesn't make sense, use activate(double[] inputs)
    }

    public double[] activate(double[] inputs) {
        double[] outputs = new double[inputs.length];
        double max = Double.NEGATIVE_INFINITY;
        for (double input : inputs) {
            if (input > max) {
                max = input;
            }
        }
        double sum = 0;
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = Math.exp(inputs[i] - max);
            sum += outputs[i];
        }
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] /= sum;
        }
        return outputs;
    }
}