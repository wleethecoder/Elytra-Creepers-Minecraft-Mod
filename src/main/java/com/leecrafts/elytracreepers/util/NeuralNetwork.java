package com.leecrafts.elytracreepers.util;

public class NeuralNetwork {
    private final int[] layerSizes;
    private final double[] weights;
    private final double[] biases;
    private final int[] layerWeightOffsets;  // Pre-calculated offsets for quick weight access
    private final int[] layerBiasOffsets;    // Pre-calculated offsets for quick bias access
    private final String outputActivationFunction;
    private final String hiddenActivationFunction;

    /**
     * Creates a neural network with the specified layer sizes.
     *
     * @param layerSizes Array containing the number of neurons in each layer (including input and output layers)
     */
    public NeuralNetwork(int[] layerSizes, String outputActivationFunction, String hiddenActivationFunction) {
        this.layerSizes = layerSizes.clone();

        // Calculate total weights and offsets
        int totalWeights = 0;
        int totalBiases = 0;
        this.layerWeightOffsets = new int[layerSizes.length - 1];
        this.layerBiasOffsets = new int[layerSizes.length - 1];

        for (int layer = 0; layer < layerSizes.length - 1; layer++) {
            this.layerWeightOffsets[layer] = totalWeights;
            this.layerBiasOffsets[layer] = totalBiases;

            totalWeights += layerSizes[layer] * layerSizes[layer + 1];
            totalBiases += layerSizes[layer + 1];
        }

        this.weights = new double[totalWeights];
        this.biases = new double[totalBiases];

        // Initialize with random weights between -1 and 1
        initializeRandomly();

        this.outputActivationFunction = outputActivationFunction;
        this.hiddenActivationFunction = hiddenActivationFunction;
    }

    /**
     * Initializes weights and biases with random values between -1 and 1
     */
    private void initializeRandomly() {
        for (int i = 0; i < this.weights.length; i++) {
            this.weights[i] = Math.random() * 2 - 1;
        }
        for (int i = 0; i < this.biases.length; i++) {
            this.biases[i] = Math.random() * 2 - 1;
        }
    }

    /**
     * Performs forward propagation through the network
     *
     * @param inputs Input values for the network
     * @return Output values from the network
     */
    public double[] forward(double[] inputs) {
        if (inputs.length != this.layerSizes[0]) {
            throw new IllegalArgumentException("Input size doesn't match network architecture");
        }

        double[] currentLayer = inputs.clone();
        double[] nextLayer;

        // Process each layer
        for (int layer = 0; layer < this.layerSizes.length - 1; layer++) {
            nextLayer = new double[this.layerSizes[layer + 1]];

            // For each neuron in the next layer
            for (int j = 0; j < this.layerSizes[layer + 1]; j++) {
                double sum = this.getBias(layer, j);

                // For each neuron in the current layer
                for (int i = 0; i < this.layerSizes[layer]; i++) {
                    sum += currentLayer[i] * this.getWeight(layer, i, j);
                }

                nextLayer[j] = layer == this.layerSizes.length - 2 ? outputActivation(sum) : hiddenActivation(sum);
            }

            currentLayer = nextLayer;
        }

        return currentLayer;
    }

    /**
     * Output activation function (tanh)
     */
    private double hiddenActivation(double x) {
        if (this.hiddenActivationFunction.equals("relu")) {
            return Math.max(x, 0);
        }
        return x;
    }

    /**
     * Output activation function (tanh)
     */
    private double outputActivation(double x) {
        if (this.outputActivationFunction.equals("tanh")) {
            return Math.tanh(x);
        }
        return x;
    }

    /**
     * Gets the index of a specific weight in the weights array
     */
    private int getWeightIndex(int layer, int fromNeuron, int toNeuron) {
        return this.layerWeightOffsets[layer] + (fromNeuron * this.layerSizes[layer + 1] + toNeuron);
    }

    /**
     * Gets a specific weight from the network
     */
    public double getWeight(int layer, int fromNeuron, int toNeuron) {
        return this.weights[getWeightIndex(layer, fromNeuron, toNeuron)];
    }

    /**
     * Sets a specific weight in the network
     */
    public void setWeight(int layer, int fromNeuron, int toNeuron, double value) {
        this.weights[getWeightIndex(layer, fromNeuron, toNeuron)] = value;
    }

    /**
     * Gets a specific bias from the network
     */
    public double getBias(int layer, int neuron) {
        return this.biases[this.layerBiasOffsets[layer] + neuron];
    }

    /**
     * Sets a specific bias in the network
     */
    public void setBias(int layer, int neuron, double value) {
        this.biases[this.layerBiasOffsets[layer] + neuron] = value;
    }

    /**
     * Gets all weights as a flat array (useful for evolution operations)
     */
    public double[] getWeights() {
        return this.weights.clone();
    }

    /**
     * Gets all biases as a flat array (useful for evolution operations)
     */
    public double[] getBiases() {
        return this.biases.clone();
    }

    /**
     * Sets all weights from a flat array (useful for evolution operations)
     */
    public void setWeights(double[] newWeights) {
        if (newWeights.length != this.weights.length) {
            throw new IllegalArgumentException("Weight array length doesn't match network architecture");
        }
        System.arraycopy(newWeights, 0, this.weights, 0, this.weights.length);
    }

    /**
     * Sets all biases from a flat array (useful for evolution operations)
     */
    public void setBiases(double[] newBiases) {
        if (newBiases.length != this.biases.length) {
            throw new IllegalArgumentException("Bias array length doesn't match network architecture");
        }
        System.arraycopy(newBiases, 0, this.biases, 0, this.biases.length);
    }

    /**
     * Prints all weights
     */
    public void printWeights() {
        for (int layer = 0; layer < this.layerSizes.length - 1; layer++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Layer ").append(layer + 1).append(":");
            for (int i = 0; i < this.layerSizes[layer]; i++) {
                stringBuilder.append("\n\tWeights from neuron ").append(i).append(" to layer ").append(layer + 2).append(": [");
                for (int j = 0; j < this.layerSizes[layer + 1]; j++) {
                    stringBuilder.append(this.getWeight(layer, i, j));
                    stringBuilder.append(j == this.layerSizes[layer + 1] - 1 ? "]" : ", ");
                }
            }
            stringBuilder.append("\n\tBiases: [");
            for (int i = 0; i < this.layerSizes[layer + 1]; i++) {
                stringBuilder.append(this.getBias(layer, i));
                stringBuilder.append(i == this.layerSizes[layer + 1] - 1 ? "]" : ", ");
            }
            System.out.println(stringBuilder);
        }
    }

}