package com.leecrafts.elytracreepers.util;

import com.leecrafts.elytracreepers.neat.NeuralNetwork;
import net.minecraft.core.BlockPos;

public class NeuralNetworkUtil {

    public static final boolean TRAINING = true;
    public static final BlockPos SPAWN_POS = new BlockPos(-24, -23, 3);
    public static final int POPULATION_SIZE = 1;
    private static final int INPUT_SIZE = 5; // TODO change if needed
    private static final int OUTPUT_SIZE = 4;
    public static final NeuralNetwork NETWORK = new NeuralNetwork(
            new int[] {INPUT_SIZE, 16, 32, OUTPUT_SIZE},
            "linear",
            "relu");

}
