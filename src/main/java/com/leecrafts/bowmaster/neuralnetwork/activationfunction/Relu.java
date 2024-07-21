package com.leecrafts.bowmaster.neuralnetwork.activationfunction;

import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;

import java.io.Serializable;

public class Relu implements ActivationFunction, Serializable {

    @Override
    public String getString() {
        return NeuralNetwork.RELU;
    }

    @Override
    public double activate(double input) {
        return Math.max(input, 0);
    }

    @Override
    public double[] activate(double[] inputs) {
        double[] outputs = new double[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = this.activate(inputs[i]);
        }
        return outputs;
    }

}
