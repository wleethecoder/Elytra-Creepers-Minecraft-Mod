package com.leecrafts.bowmaster.util;

import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;

import java.io.File;
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
    public static double EPSILON = 0.9;
    public static final double EPSILON_MAX = 0.9;
    public static final double EPSILON_MIN = 0.1;
    public static final double EPSILON_DECAY = 0.008; // linear epsilon decay

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
        String[] hiddenActivations = {"tanh"};
        int[] outputSizes = {2, 2, 3, 3, 2};
        String[] outputActivations = {"tanh", "softmax", "softmax", "softmax", "softmax"};

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

        Pattern pattern = Pattern.compile(String.format("^%s-(\\d+)\\.eg$", MODEL_BASE_NAME));  // Regex to find files
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
        return new File(String.format("%s/%s-%d.eg", MODEL_DIRECTORY_PATH, MODEL_BASE_NAME, modelNumber));
    }

}
