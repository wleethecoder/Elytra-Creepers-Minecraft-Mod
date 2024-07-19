package com.leecrafts.bowmaster.util;

import com.leecrafts.bowmaster.neuralnetwork.NetworkLayer;
import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeuralNetworkUtil {

    // TODO do not use absolute path
    private static final String MODEL_DIRECTORY_PATH = "/Users/wlee2019/Downloads/mod repos/skeleton bow master/src/main/java/com/leecrafts/bowmaster/util/models";
//    private static final String MODEL_DIRECTORY_PATH = "src/main/java/com/leecrafts/bowmaster/util/models";
    private static final String MODEL_BASE_NAME = "model";
    private static final int INPUT_SIZE = 9;
    private static final int OUTPUT_SIZE = 12;
    private static double LEARNING_RATE = 0.1;
    private static final double GAMMA = 0.99;
    public static final double EPSILON_MAX = 0.9;
    public static final double EPSILON_MIN = 0.1;
    public static final double EPSILON_DECAY = (EPSILON_MAX - EPSILON_MIN) / 100; // linear epsilon decay
    public static double EPSILON = EPSILON_MAX;
    public static final double GAUSSIAN_NOISE_MAX = 1.0;
    public static final double GAUSSIAN_NOISE_MIN = 0.1;
    public static final double GAUSSIAN_NOISE_DECAY = (GAUSSIAN_NOISE_MAX - GAUSSIAN_NOISE_MIN) / 100; // linear gaussian noise decay
    public static double GAUSSIAN_NOISE = GAUSSIAN_NOISE_MAX;

    /*
    Actions:
    - Turn head (pitch and yaw)
    - Press / don't press right click
    - Move forward/backward/neither
    - Strafe left/right/neither
    - Jump / no jump
     */
    public static NeuralNetwork createNetwork() {
        int[] hiddenLayerSizes = {32};
        String[] hiddenActivations = {NeuralNetwork.TANH};
        int[] outputSizes = {2, 2, 3, 3, 2};
        String[] outputActivations = {
                NeuralNetwork.TANH,
                NeuralNetwork.SOFTMAX,
                NeuralNetwork.SOFTMAX,
                NeuralNetwork.SOFTMAX,
                NeuralNetwork.SOFTMAX};

        return new NeuralNetwork(INPUT_SIZE, hiddenLayerSizes, hiddenActivations, outputSizes, outputActivations);
    }

//    public static void updateNetwork(MultiOutputFreeformNetwork network,
//                                     ArrayList<double[]> states,
//                                     ArrayList<double[]> actionProbs,
//                                     ArrayList<Double> rewards) {
//        // cumulative rewards if not already provided
//        double[] cumulativeRewards = new double[rewards.size()];
//        double cumulative = 0;
//        for (int i = rewards.size() - 1; i >= 0; i--) {
//            cumulative = rewards.get(i) + cumulative * GAMMA;
//            cumulativeRewards[i] = cumulative;
//        }
//
//        // Traverse each time step
//        for (int t = 0; t < states.size(); t++) {
//            double[] probs = actionProbs.get(t);
//            double Gt = cumulativeRewards[t];
//
//            // Assuming a method to get all neurons, including hidden and output layers
//            List<FreeformLayer> allLayers = network.getAllLayers();
//            for (FreeformLayer layer : allLayers) {
//                for (FreeformNeuron neuron : layer.getNeurons()) {
//                    List<FreeformConnection> connections = neuron.getOutputs();
//                    for (FreeformConnection connection : connections) {
//                        double inputActivation = neuron.getActivation();
//
//                        // Calculate the gradient
//                        double grad = - (Gt * (1 - probs[t]) * inputActivation); // Simplified gradient calculation
//                        // Update weights
//                        double oldWeight = connection.getWeight();
//                        double newWeight = oldWeight - LEARNING_RATE * grad;
//                        connection.setWeight(newWeight);
//                    }
//                }
//            }
//        }
//    }

    public static void updateNetwork(NeuralNetwork network,
                                     ArrayList<double[]> states,
                                     ArrayList<List<double[]>> actionProbs,
                                     ArrayList<int[]> actions,
                                     ArrayList<Double> rewards) {
        ArrayList<Double> returns = new ArrayList<>();
        double cumulativeReturn = 0.0;
        for (int i = rewards.size() - 1; i >= 0; i--) {
            cumulativeReturn = rewards.get(i) + GAMMA * cumulativeReturn;
            returns.add(0, cumulativeReturn);
        }

        // Assume each layer might have different gradient needs based on activation functions
        List<double[]> averageGradients = new ArrayList<>(network.getOutputLayers().size());
        for (NetworkLayer layer : network.getOutputLayers()) {
            averageGradients.add(new double[layer.getNeurons().size()]);
        }

        for (int t = 0; t < states.size(); t++) {
            List<double[]> probsList = actionProbs.get(t);
            double Gt = returns.get(t);
            for (int layer = 0; layer < network.getOutputLayers().size(); layer++) {
                double[] probs = probsList.get(layer);
                double[] gradients = new double[probs.length];

                for (int a = 0; a < probs.length; a++) {
                    String af = network.getOutputActivations()[layer];
                    if (af.equals(NeuralNetwork.SOFTMAX)) {
                        int chosenAction = actions.get(t)[layer];
                        // Multiply gradient by Gt here
                        gradients[a] = ((a == chosenAction ? 1 : 0) - probs[a]) * Gt;
                    }
                    else if (af.equals(NeuralNetwork.TANH)) {
                        gradients[a] = probs[a] * Gt; // Direct use of action value as part of the gradient calculation
                    }
                }

                network.getOutputLayers().get(layer).updateLayerWeights(gradients, LEARNING_RATE);

                // Accumulate gradients for backpropagation (simple average or sum)
                for (int i = 0; i < gradients.length; i++) {
                    averageGradients.get(layer)[i] += (gradients[i] / states.size());
                }
            }
        }

        // Backpropagation
        network.backpropagate(averageGradients, LEARNING_RATE);
    }

    public static void print2DArray(double[][] array) {
        System.out.print("[");
        for (int i = 0; i < array.length; i++) {
            System.out.print("[");
            for (int j = 0; j < array[i].length; j++) {
                System.out.print(array[i][j]);
                if (j < array[i].length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print("]");
            if (i < array.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

//    public static int argmax(double[] array) {
//        int maxIndex = 0;
//        double maxValue = Double.MIN_VALUE;
//        for (int i = 0; i < array.length; i++) {
//            if (array[i] > maxValue) {
//                maxValue = array[i];
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }

    public static void saveModel(NeuralNetwork network) {
        NeuralNetwork.saveModel(network, file(getNewestModelNumber() + 1));
    }

    public static NeuralNetwork loadOrCreateModel() {
        return loadOrCreateModel(getNewestModelNumber());
    }

    public static NeuralNetwork loadOrCreateModel(int modelNumber) {
        // TODO consider learning rate decay
        // TODO consider epsilon decay
        EPSILON = Math.max(EPSILON_MAX - EPSILON_DECAY * (modelNumber + 1), EPSILON_MIN);
        GAUSSIAN_NOISE = Math.max(GAUSSIAN_NOISE_MAX - GAUSSIAN_NOISE_DECAY * (modelNumber + 1), GAUSSIAN_NOISE_MIN);
        File file = file(modelNumber);
        if (file.exists()) {
            System.out.println("existing model found (" + modelNumber + ")");
            return NeuralNetwork.loadModel(file);
        }
        else {
            System.out.println("no model found, so creating new one");
            return createNetwork();
        }
    }

    private static int getNewestModelNumber() {
        File[] files = new File(MODEL_DIRECTORY_PATH).listFiles();  // Get all files in the directory
        int maxNum = -1;
        if (files == null) {
            return maxNum;
        }

        Pattern pattern = Pattern.compile(String.format("^%s-(\\d+)\\.dat$", MODEL_BASE_NAME));  // Regex to find files
        for (File file : files) {
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    maxNum = Math.max(maxNum, Integer.parseInt(matcher.group(1)));
                }
            }
        }

        return maxNum;
    }

    private static File file(int modelNumber) {
        return new File(String.format("%s/%s-%d.dat", MODEL_DIRECTORY_PATH, MODEL_BASE_NAME, modelNumber));
    }

}
