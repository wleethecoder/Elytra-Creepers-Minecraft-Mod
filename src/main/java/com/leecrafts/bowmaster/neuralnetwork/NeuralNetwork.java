package com.leecrafts.bowmaster.neuralnetwork;

import com.leecrafts.bowmaster.neuralnetwork.activationfunction.ActivationFunction;
import com.leecrafts.bowmaster.neuralnetwork.activationfunction.Softmax;
import com.leecrafts.bowmaster.neuralnetwork.activationfunction.Tanh;

import java.io.*;
import java.util.*;

public class NeuralNetwork implements Serializable {

//    private final NetworkLayer inputLayer;
    private final List<NetworkLayer> hiddenLayers;
    private final List<NetworkLayer> outputLayers;
    private final String[] outputActivations;
    public static final String TANH = "tanh";
    public static final String SOFTMAX = "softmax";

    // A simple factory for creating activation functions from strings
    private static final Map<String, ActivationFunction> activationFunctionMap = new HashMap<>();
    static {
        activationFunctionMap.put(TANH, new Tanh());
        activationFunctionMap.put(SOFTMAX, new Softmax());
    }

    public NeuralNetwork(int inputSize, int[] hiddenLayerSizes, String[] hiddenActivations, int[] outputSizes, String[] outputActivations) {
//        this.inputLayer = new NetworkLayer(1, inputSize, new ActivationFunction() { // Placeholder function
//            public String getString() { return ""; }
//            public double activate(double input) { return input; }
//            public double[] activate(double[] inputs) { return inputs; }
//        }); // Only one neuron in the input layer acting as a placeholder

        // Initialize hidden layers with specified activation functions
        this.hiddenLayers = new ArrayList<>();
        int previousSize = inputSize;
        for (int i = 0; i < hiddenLayerSizes.length; i++) {
            ActivationFunction af = activationFunctionMap.get(hiddenActivations[i]);
            this.hiddenLayers.add(new NetworkLayer(hiddenLayerSizes[i], previousSize, af));
            previousSize = hiddenLayerSizes[i];
        }

        // Initialize output layers with specified activation functions
        this.outputLayers = new ArrayList<>();
        this.outputActivations = outputActivations;
        for (int i = 0; i < outputSizes.length; i++) {
            ActivationFunction af = activationFunctionMap.get(this.outputActivations[i]);
            this.outputLayers.add(new NetworkLayer(outputSizes[i], previousSize, af));
        }

    }

    public List<double[]> feedForward(double[] inputs) {
//        double[] currentOutput = this.inputLayer.feedForward(inputs);
        double[] currentOutput = inputs;
        for (NetworkLayer layer : this.hiddenLayers) {
            currentOutput = layer.feedForward(currentOutput);
        }
        List<double[]> finalOutputs = new ArrayList<>();
        for (NetworkLayer layer : this.outputLayers) {
            finalOutputs.add(layer.feedForward(currentOutput));
        }
        return finalOutputs;
    }

    public void backpropagate(List<double[]> initialErrors, double learningRate) {
        // Reverse the layers for backpropagation, starting from output to input
        for (int i = 0; i < this.outputLayers.size(); i++) {
            List<NetworkLayer> allLayers = new ArrayList<>(this.hiddenLayers);
            allLayers.add(this.outputLayers.get(i));
            Collections.reverse(allLayers);

            double[] nextLayerGradients = initialErrors.get(i); // Start with the gradient resulting from the output

            // Iterate over all layers, starting from the last going back to the first
            for (int layerIndex = 0; layerIndex < allLayers.size(); layerIndex++) {
                NetworkLayer currentLayer = allLayers.get(layerIndex);
                double[] currentLayerGradients = new double[currentLayer.getNeurons().size()];

                for (int neuronIndex = 0; neuronIndex < currentLayer.getNeurons().size(); neuronIndex++) {
                    Neuron neuron = currentLayer.getNeurons().get(neuronIndex);
                    double activationDerivative = 1;

                    if (neuron.getActivationFunction().getString().equals(NeuralNetwork.TANH)) {
                        activationDerivative = 1 - Math.pow(neuron.getOutput(), 2);
                    } else if (neuron.getActivationFunction().getString().equals(NeuralNetwork.SOFTMAX)) {
                        double[] softmaxOutputs = neuron.getSoftmaxOutputs();  // Assuming you have a method to get softmax outputs stored after the forward pass
                        double[] softmaxDerivatives = new double[softmaxOutputs.length];

                        assert softmaxOutputs.length == nextLayerGradients.length;
                        for (int j = 0; j < softmaxOutputs.length; j++) {
                            for (int k = 0; k < softmaxOutputs.length; k++) {
                                if (j == k) {
                                    softmaxDerivatives[j] += softmaxOutputs[j] * (1 - softmaxOutputs[k]) * nextLayerGradients[k];  // Diagonal term
                                } else {
                                    softmaxDerivatives[j] -= softmaxOutputs[j] * softmaxOutputs[k] * nextLayerGradients[k];  // Off-diagonal term
                                }
                            }
                        }
                        activationDerivative = softmaxDerivatives[neuronIndex]; // Use the computed derivatives as part of the gradient calculation
                    }

                    double[] gradients = new double[neuron.getWeights().length];
                    for (int weightIndex = 0; weightIndex < gradients.length; weightIndex++) {
                        double input = (weightIndex == neuron.getWeights().length - 1) ? 1 : neuron.getInputs()[weightIndex];
                        double gradient = 0;
                        for (int j = 0; j < nextLayerGradients.length; j++) {  // Sum over all gradients from the next layer
                            gradient += nextLayerGradients[j] * input;
                        }
                        gradient *= activationDerivative;
                        neuron.updateWeight(weightIndex, gradient, learningRate);
                        gradients[weightIndex] = gradient;  // Store for summing later
//                    gradients[weightIndex] = activationDerivative * nextLayerGradients[neuronIndex] * input;
//                    neuron.updateWeight(weightIndex, gradients[weightIndex], learningRate);
                    }

                    // Sum the product of all outgoing weights and the gradients of neurons in the next layer
                    double sumGradient = 0;
                    for (int weightIndex = 0; weightIndex < neuron.getWeights().length; weightIndex++) {
                        sumGradient += neuron.getWeights()[weightIndex] * gradients[weightIndex];
                    }
                    currentLayerGradients[neuronIndex] = sumGradient * activationDerivative;
                }
                nextLayerGradients = currentLayerGradients; // Update gradients for the next layer iteration
            }
        }
    }

    public List<NetworkLayer> getOutputLayers() {
        return this.outputLayers;
    }

    public String[] getOutputActivations() {
        return this.outputActivations;
    }

    public void printWeights() {
        System.out.println("Neural Network Weights:");

//        // Print weights for the input layer
//        System.out.println("\nInput Layer:");
//        printLayerWeights(this.inputLayer);

        // Print weights for each hidden layer
        int hiddenLayerIndex = 1;
        for (NetworkLayer layer : this.hiddenLayers) {
            System.out.println("\nHidden Layer " + hiddenLayerIndex + ":");
            printLayerWeights(layer);
            hiddenLayerIndex++;
        }

        // Print weights for each output layer
        int outputLayerIndex = 1;
        for (NetworkLayer layer : this.outputLayers) {
            System.out.println("\nOutput Layer " + outputLayerIndex + ":");
            printLayerWeights(layer);
            outputLayerIndex++;
        }
    }

    private void printLayerWeights(NetworkLayer layer) {
        int neuronIndex = 1;
        for (Neuron neuron : layer.getNeurons()) {
            System.out.print("Neuron " + neuronIndex + ": ");
            double[] weights = neuron.getWeights();
            System.out.println(Arrays.toString(weights));
            neuronIndex++;
        }
    }

    public static void saveModel(NeuralNetwork network, File file) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(network);
            System.out.println("Network saved to " + file.getPath());
        } catch (IOException e) {
            System.out.println("Error saving network: " + e.getMessage());
            System.out.println(e.toString());
        }
    }

    public static NeuralNetwork loadModel(File file) {
        NeuralNetwork network = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            network = (NeuralNetwork) in.readObject();
            System.out.println("Network loaded from " + file.getPath());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading network: " + e.getMessage());
        }
        return network;
    }

}
