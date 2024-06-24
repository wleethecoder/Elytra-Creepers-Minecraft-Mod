package com.leecrafts.bowmaster.neuralnetwork;

import com.leecrafts.bowmaster.neuralnetwork.activationfunction.ActivationFunction;
import com.leecrafts.bowmaster.neuralnetwork.activationfunction.Softmax;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NetworkLayer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Neuron> neurons;
    private final ActivationFunction activationFunction;

    public NetworkLayer(int numberOfNeurons, int inputSize, ActivationFunction activationFunction) {
        this.activationFunction = activationFunction;
        this.neurons = new ArrayList<>();
        for (int i = 0; i < numberOfNeurons; i++) {
            this.neurons.add(new Neuron(inputSize, this.activationFunction));
        }
    }

    public List<Neuron> getNeurons() {
        return this.neurons;
    }

    public double[] feedForward(double[] inputs) {
        double[] outputs = new double[this.neurons.size()];
        for (int i = 0; i < this.neurons.size(); i++) {
            Neuron neuron = this.neurons.get(i);
            neuron.calculateOutput(inputs);
            outputs[i] = neuron.getOutput();
        }
        // Apply softmax at the layer level if necessary
        if (this.activationFunction instanceof Softmax softmax) {
            outputs = softmax.activate(outputs);
        }
        return outputs;
    }
}
