package com.leecrafts.bowmaster.neuralnetwork;

import com.leecrafts.bowmaster.neuralnetwork.activationfunction.ActivationFunction;
import com.leecrafts.bowmaster.neuralnetwork.activationfunction.Relu;

import java.io.Serializable;
import java.util.Random;

public class Neuron implements Serializable {

    private final double[] weights;
    private double numberOfNodesFromThisLayer;
    private double output;
    private double[] inputs;
    private double[] softmaxOutputs;
    private final ActivationFunction activationFunction;
    private final Random random = new Random();

    public Neuron(int inputSize, ActivationFunction activationFunction, int numberOfNodesFromThisLayer) {
        this.weights = new double[inputSize + 1]; // +1 for bias
        this.activationFunction = activationFunction;
        this.numberOfNodesFromThisLayer = numberOfNodesFromThisLayer;
        initializeWeights();
    }

    private void initializeWeights() {
        int inputSize = this.weights.length - 1;
        for (int i = 0; i < inputSize; i++) {
            if (this.activationFunction instanceof Relu) {
                // He weight initialization
                this.weights[i] = this.random.nextGaussian() * Math.sqrt(2.0 / inputSize);
            }
            else {
                // Xavier weight initialization (for softmax and tanh)
                double limit = Math.sqrt(6 / (inputSize + this.numberOfNodesFromThisLayer));
                this.weights[i] = this.random.nextDouble(-limit, limit);
            }
        }

        // initialize bias
        this.weights[inputSize] = 0.01;
    }

    public double[] getWeights() {
        return this.weights;
    }

    public ActivationFunction getActivationFunction() {
        return this.activationFunction;
    }

    public void calculateOutput(double[] inputs) {
        this.setInputs(inputs);
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

    public void setInputs(double[] inputs) {
        this.inputs = inputs;
    }

    public double[] getInputs() {
        return this.inputs;
    }

    public void setSoftmaxOutputs(double[] outputs) {
        this.softmaxOutputs = outputs;
    }

    public double[] getSoftmaxOutputs() {
        return softmaxOutputs;
    }

    // Method to update weights
    public void updateWeights(double gradient, double learningRate) {
        for (int i = 0; i < this.weights.length; i++) {
            this.updateWeight(i, gradient, learningRate);
        }
    }

    public void updateWeight(int index, double gradient, double learningRate) {
        this.weights[index] += learningRate * gradient;
    }


}
