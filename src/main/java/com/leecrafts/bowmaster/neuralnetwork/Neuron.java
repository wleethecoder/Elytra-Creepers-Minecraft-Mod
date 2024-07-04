package com.leecrafts.bowmaster.neuralnetwork;

import com.leecrafts.bowmaster.neuralnetwork.activationfunction.ActivationFunction;

import java.io.Serializable;

public class Neuron implements Serializable {

    private final double[] weights;
    private double output;
    private final ActivationFunction activationFunction;

    public Neuron(int inputSize, ActivationFunction activationFunction) {
        this.weights = new double[inputSize + 1]; // +1 for bias
        this.activationFunction = activationFunction;
        initializeWeights();
    }

    private void initializeWeights() {
        for (int i = 0; i < this.weights.length; i++) {
            this.weights[i] = Math.random(); // Simple random initialization
        }
    }

    public double[] getWeights() {
        return this.weights;
    }

    public void calculateOutput(double[] inputs) {
        double sum = 0;
        for (int i = 0; i < inputs.length; i++) {
            sum += this.weights[i] * inputs[i];
        }
        sum += this.weights[this.weights.length - 1]; // Add bias
        this.setOutput(this.activationFunction.activate(sum)); // Store the output internally
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getOutput() {
        return output;
    }

    // Method to update weights
    public void updateWeights(double[] gradient, double learningRate) {
        for (int i = 0; i < this.weights.length; i++) {
            this.weights[i] += learningRate * gradient[i];
        }
    }

}
