package com.leecrafts.elytracreepers.neat.controller;

import com.leecrafts.elytracreepers.event.ModEvents;
import com.leecrafts.elytracreepers.neat.*;
import com.leecrafts.elytracreepers.neat.datastructures.RandomHashSet;
import com.leecrafts.elytracreepers.neat.genome.ConnectionGene;
import com.leecrafts.elytracreepers.neat.genome.Genome;
import com.leecrafts.elytracreepers.neat.genome.NodeGene;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;

public class NEATController {

    public static final int MAX_NODES = (int) Math.pow(2, 20);
    public static final boolean TRAINING = true;
    public static final BlockPos SPAWN_POS = new BlockPos(-24, -23, 3);
    public static final int POPULATION_SIZE = 1;
    public static final int INPUT_SIZE = 5; // TODO change if needed
    public static final int OUTPUT_SIZE = 4;
    public static final NeuralNetwork NETWORK = new NeuralNetwork(
            new int[] {INPUT_SIZE, 16, 32, OUTPUT_SIZE},
            "linear",
            "relu");

    private final double C1 = 1;
    private final double C2 = 1;
    private final double C3 = 1;
    private final double WEIGHT_SHIFT_STRENGTH = 0.3;
    private final double WEIGHT_RANDOM_STRENGTH = 1;

    private final double PROBABILITY_MUTATE_LINK = 0.01;
    private final double PROBABILITY_MUTATE_NODE = 0.1;
    private final double PROBABILITY_MUTATE_WEIGHT_SHIFT = 0.02;
    private final double PROBABILITY_MUTATE_WEIGHT_RANDOM = 0.02;
    private final double PROBABILITY_MUTATE_TOGGLE_LINK = 0.02;

    // We COULD use an ArrayList<ConnectionGene>, but we want O(1) access time
    // Using a HashMap works because we overrode hashCode for ConnectionGene so that hash codes are unique for all connection genes
    private final HashMap<ConnectionGene, ConnectionGene> allConnections = new HashMap<>();

    private final RandomHashSet<NodeGene> allNodes = new RandomHashSet<>();
    private int inputSize;
    private int outputSize;
    private int maxClients;

    private Population population;
    private int generation = 0;

    public NEATController(int inputSize, int outputSize, int clients) {
        this.reset(inputSize, outputSize, clients);
    }

    // returns a new genome with the input and output nodes and no connections
    public Genome emptyGenome() {
        Genome genome = new Genome(this);
        for (int i = 0; i < this.inputSize + this.outputSize; i++) {
            genome.getNodes().add(this.getNode(i + 1));
        }
        return genome;
    }

    public void reset(int inputSize, int outputSize, int clients) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.maxClients = clients;

        this.allConnections.clear();
        this.allNodes.clear();

        for (int i = 0; i < this.inputSize; i++) {
            NodeGene nodeGene = this.getNode();
            nodeGene.setX(0.1);
            nodeGene.setY((i + 1) / (double) (this.inputSize + 1));
        }
        for (int i = 0; i < this.outputSize; i++) {
            NodeGene nodeGene = this.getNode();
            nodeGene.setX(0.9);
            nodeGene.setY((i + 1) / (double) (this.outputSize + 1));
        }

        // TODO add agent population
        this.initializePopulation(clients);
    }

    // returns copy of a ConnectionGene
    public static ConnectionGene getConnection(ConnectionGene connectionGene) {
        ConnectionGene connectionGene1 = new ConnectionGene(connectionGene.getFrom(), connectionGene.getTo());
        connectionGene1.setInnovationNumber(connectionGene.getInnovationNumber());
        connectionGene1.setWeight(connectionGene.getWeight());
        connectionGene1.setEnabled(connectionGene.isEnabled());
        return connectionGene1;
    }

    // returns a connection gene given a "from" node and "to" node
    // the connection gene will have the appropriate innovation number
    public ConnectionGene getConnection(NodeGene from, NodeGene to) {
        ConnectionGene connectionGene = new ConnectionGene(from, to);
        if (this.allConnections.containsKey(connectionGene)) {
            connectionGene.setInnovationNumber(this.allConnections.get(connectionGene).getInnovationNumber());
        }
        else {
            connectionGene.setInnovationNumber(this.allConnections.size() + 1);
        }
        return connectionGene;
    }

    public NodeGene getNode() {
        NodeGene nodeGene = new NodeGene(this.allNodes.size() + 1);
        this.allNodes.add(nodeGene);
        return nodeGene;
    }

    public NodeGene getNode(int innovationNumber) {
        if (innovationNumber <= this.allNodes.size()) {
            return this.allNodes.get(innovationNumber - 1);
        }
        return this.getNode();
    }

    public double getC1() {
        return this.C1;
    }

    public double getC2() {
        return this.C2;
    }

    public double getC3() {
        return this.C3;
    }

    public double getWeightShiftStrength() {
        return this.WEIGHT_SHIFT_STRENGTH;
    }

    public double getWeightRandomStrength() {
        return this.WEIGHT_RANDOM_STRENGTH;
    }

    public double getProbabilityMutateLink() {
        return this.PROBABILITY_MUTATE_LINK;
    }

    public double getProbabilityMutateNode() {
        return this.PROBABILITY_MUTATE_NODE;
    }

    public double getProbabilityMutateWeightShift() {
        return this.PROBABILITY_MUTATE_WEIGHT_SHIFT;
    }

    public double getProbabilityMutateWeightRandom() {
        return this.PROBABILITY_MUTATE_WEIGHT_RANDOM;
    }

    public double getProbabilityMutateToggleLink() {
        return this.PROBABILITY_MUTATE_TOGGLE_LINK;
    }

    public void initializePopulation(int size) {
        // initialize population
        ModEvents.REMAINING = size;
        // for loop...entity.setData(new Genome)
        // population.add(entity.getData())
    }

    public void recordFitness(LivingEntity livingEntity, double fitness) {
        ModEvents.REMAINING--;

        if (ModEvents.REMAINING == 0) {
            evolveNewGeneration();
        }
    }

    private void evolveNewGeneration() {
    }

}
