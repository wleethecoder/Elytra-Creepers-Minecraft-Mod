package com.leecrafts.elytracreepers.neat.controller;

import com.leecrafts.elytracreepers.neat.datastructures.RandomHashSet;
import com.leecrafts.elytracreepers.neat.datastructures.RandomSelector;
import com.leecrafts.elytracreepers.neat.genome.ConnectionGene;
import com.leecrafts.elytracreepers.neat.genome.Genome;
import com.leecrafts.elytracreepers.neat.genome.NodeGene;

import java.io.Serializable;
import java.util.HashMap;

public class NEATController implements Serializable {

    public static final int MAX_NODES = (int) Math.pow(2, 20);

    // TODO adjust hyperparameters
    private final double C1 = 1;
    private final double C2 = 1;
    private final double C3 = 1;

    private final double CP = 4;

    private final double WEIGHT_SHIFT_STRENGTH = 0.3;
    private final double WEIGHT_RANDOM_STRENGTH = 1;

    private static final double SURVIVAL_RATE = 0.8;

    private final double PROBABILITY_MUTATE_LINK = 0.05;
    private final double PROBABILITY_MUTATE_NODE = 0.05;
    private final double PROBABILITY_MUTATE_WEIGHT_SHIFT = 0.1;
    private final double PROBABILITY_MUTATE_WEIGHT_RANDOM = 0.1;
    private final double PROBABILITY_MUTATE_TOGGLE_LINK = 0.01;

    // We COULD use an ArrayList<ConnectionGene>, but we want O(1) access time
    // Using a HashMap works because we overrode hashCode for ConnectionGene so that hash codes are unique for all connection genes
    private final HashMap<ConnectionGene, ConnectionGene> allConnections = new HashMap<>();

    private final RandomHashSet<NodeGene> allNodes = new RandomHashSet<>();

    private RandomHashSet<Agent> agents = new RandomHashSet<>();
    private RandomHashSet<Species> species = new RandomHashSet<>();

    private int inputSize;
    private int outputSize;
    private int populationSize;

    public NEATController(int inputSize, int outputSize, int populationSize) {
        this.reset(inputSize, outputSize, populationSize);
    }

    // returns a new genome with the input and output nodes and no connections
    public Genome emptyGenome() {
        Genome genome = new Genome(this);
        for (int i = 0; i < this.inputSize + this.outputSize; i++) {
            genome.getNodes().add(this.getNode(i + 1));
        }
        return genome;
    }

    public void reset(int inputSize, int outputSize, int populationSize) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.populationSize = populationSize;

        this.allConnections.clear();
        this.allNodes.clear();
        this.agents.clear();

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

        for (int i = 0; i < this.populationSize; i++) {
            Agent agent = new Agent();
            agent.setGenome(this.emptyGenome());
            agent.generateCalculator();
            this.agents.add(agent);
        }
    }

    public Agent getAgent(int index) {
        return this.agents.get(index);
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
            this.allConnections.put(connectionGene, connectionGene);
        }
        return connectionGene;
    }

    public int getReplaceIndex(NodeGene from, NodeGene to) {
        ConnectionGene connectionGene = this.allConnections.get(this.getConnection(from, to));
        if (connectionGene == null) {
            return 0;
        }
        return connectionGene.getReplaceIndex();
    }

    public void setReplaceIndex(NodeGene from, NodeGene to, int index) {
        this.allConnections.get(new ConnectionGene(from, to)).setReplaceIndex(index);
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

    public void evolve() {
        this.generateSpecies();
        this.kill();
        this.removeExtinctSpecies();
        this.reproduce();
        this.mutate();

        for (Agent agent : this.agents.getData()) {
            agent.generateCalculator();
        }
    }

    private void generateSpecies() {
        for (Species s : this.species.getData()) {
            s.reset();
        }
        for (Agent agent : this.agents.getData()) {
            if (agent.getSpecies() != null) {
                continue;
            }
            boolean found = false;
            for (Species s : this.species.getData()) {
                if (s.put(agent)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.species.add(new Species(agent));
            }
        }

        for (Species s : this.species.getData()) {
            s.evaluateScore();
        }
    }

    private void kill() {
        for (Species s : this.species.getData()) {
            s.kill(1 - SURVIVAL_RATE);
        }
    }

    private void removeExtinctSpecies() {
        for (int i = this.species.size() - 1; i >= 0; i--) {
            if (this.species.get(i).size() <= 1) {
                this.species.get(i).goExtinct();
                this.species.remove(i);
            }
        }
    }

    private void reproduce() {
        RandomSelector<Species> randomSelector = new RandomSelector<>();
        for (Species s : this.species.getData()) {
            randomSelector.add(s, s.getScore());
        }
        for (Agent agent : this.agents.getData()) {
            if (agent.getSpecies() == null) {
                Species s = randomSelector.random();
                agent.setGenome(s.breed());
                s.forcePut(agent);
            }
        }
    }

    public void mutate() {
        for (Agent agent : this.agents.getData()) {
            agent.mutate();
        }
    }

    public Agent getBestAgent() {
        double bestScore = -Double.MAX_VALUE;
        Agent bestAgent = null;
        for (Agent agent : this.agents.getData()) {
            if (agent.getScore() > bestScore) {
                bestScore = agent.getScore();
                bestAgent = agent;
            }
        }
        return bestAgent;
    }

    public double[] populationMeanAndStd() {
        return meanAndStd(this.agents);
    }

    public double[] bestSpeciesMeanAndStd() {
        double bestScore = -Double.MAX_VALUE;
        Species bestSpecies = null;
        for (Species s : this.species.getData()) {
            if (s.getScore() > bestScore) {
                bestScore = s.getScore();
                bestSpecies = s;
            }
        }
        if (bestSpecies != null) {
            return meanAndStd(bestSpecies.getAgents());
        }

        // this code shouldn't run
        return null;
    }

    private double[] meanAndStd(RandomHashSet<Agent> agents) {
        int size = agents.getData().size();
        double mean = 0;
        for (Agent agent : agents.getData()) {
            mean += agent.getScore();
        }
        mean /= size;

        double std = 0;
        for (Agent agent : agents.getData()) {
            std += Math.pow(agent.getScore() - mean, 2);
        }
        std = Math.sqrt(std / size);

        return new double[] {mean, std};
    }

    public int numSpecies() {
        return this.species.getData().size();
    }

    public void printSpecies() {
        System.out.println("####################################");
        for (Species s : this.species.getData()) {
            System.out.println("Species " + s + "; average score: " + s.getScore() + "; size: " + s.size());
        }
    }

    public String perSpeciesMetrics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("####################################")
                .append("\n");
        for (Species s : this.species.getData()) {
            double[] meanAndStd = this.meanAndStd(s.getAgents());
            stringBuilder.append(s)
                    .append(",")
                    .append(meanAndStd[0])
                    .append(",")
                    .append(meanAndStd[1])
                    .append(",")
                    .append(s.size());
        }
        return stringBuilder.append("\n").toString();
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

    public double getCP() {
        return CP;
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

    public int getPopulationSize() {
        return this.populationSize;
    }

}
