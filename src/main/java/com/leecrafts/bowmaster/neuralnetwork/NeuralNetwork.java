package com.leecrafts.bowmaster.neuralnetwork;

import com.leecrafts.bowmaster.neuralnetwork.activationfunction.ActivationFunction;
import com.leecrafts.bowmaster.neuralnetwork.activationfunction.Softmax;
import com.leecrafts.bowmaster.neuralnetwork.activationfunction.Tanh;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeuralNetwork implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NetworkLayer inputLayer;
    private final List<NetworkLayer> hiddenLayers;
    private final List<NetworkLayer> outputLayers;

    // A simple factory for creating activation functions from strings
    private static final Map<String, ActivationFunction> activationFunctionMap = new HashMap<>();
    static {
        activationFunctionMap.put("tanh", new Tanh());
        activationFunctionMap.put("softmax", new Softmax());
    }

    public NeuralNetwork(int inputSize, int[] hiddenLayerSizes, String[] hiddenActivations, int[] outputSizes, String[] outputActivations) {
        this.inputLayer = new NetworkLayer(1, inputSize, new ActivationFunction() { // Placeholder function
            public double activate(double input) { return input; }
            public double[] activate(double[] inputs) { return inputs; }
        }); // Only one neuron in the input layer acting as a placeholder

        // Initialize hidden layers with specified activation functions
        this.hiddenLayers = new ArrayList<>();
        int previousSize = inputSize;
        for (int i = 0; i < hiddenLayerSizes.length; i++) {
            ActivationFunction af = activationFunctionMap.get(hiddenActivations[i]);
            hiddenLayers.add(new NetworkLayer(hiddenLayerSizes[i], previousSize, af));
            previousSize = hiddenLayerSizes[i];
        }

        // Initialize output layers with specified activation functions
        this.outputLayers = new ArrayList<>();
        for (int i = 0; i < outputSizes.length; i++) {
            ActivationFunction af = activationFunctionMap.get(outputActivations[i]);
            outputLayers.add(new NetworkLayer(outputSizes[i], previousSize, af));
        }
    }

    public List<double[]> feedForward(double[] inputs) {
        double[] currentOutput = this.inputLayer.feedForward(inputs);
        for (NetworkLayer layer : this.hiddenLayers) {
            currentOutput = layer.feedForward(currentOutput);
        }
        List<double[]> finalOutputs = new ArrayList<>();
        for (NetworkLayer layer : this.outputLayers) {
            finalOutputs.add(layer.feedForward(currentOutput));
        }
        return finalOutputs;
    }

    public static void saveModel(NeuralNetwork network, String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(network);
            System.out.println("Network saved to " + filePath);
        } catch (IOException e) {
            System.out.println("Error saving network: " + e.getMessage());
        }
    }

    public static NeuralNetwork loadModel(String filePath) {
        NeuralNetwork network = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            network = (NeuralNetwork) in.readObject();
            System.out.println("Network loaded from " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading network: " + e.getMessage());
        }
        return network;
    }

}
