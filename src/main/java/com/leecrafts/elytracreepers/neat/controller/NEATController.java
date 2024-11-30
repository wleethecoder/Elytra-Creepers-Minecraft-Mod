package com.leecrafts.elytracreepers.neat.controller;

import com.leecrafts.elytracreepers.neat.datastructures.RandomHashSet;
import com.leecrafts.elytracreepers.neat.datastructures.RandomSelector;
import com.leecrafts.elytracreepers.neat.genome.ConnectionGene;
import com.leecrafts.elytracreepers.neat.genome.Genome;
import com.leecrafts.elytracreepers.neat.genome.NodeGene;

import java.util.ArrayList;
import java.util.HashMap;

public class NEATController {

    public static final int MAX_NODES = (int) Math.pow(2, 20);

    private final double C1 = 1;
    private final double C2 = 1;
    private final double C3 = 1;

    private final double CP = 4;

    private final double WEIGHT_SHIFT_STRENGTH = 0.3;
    private final double WEIGHT_RANDOM_STRENGTH = 1;

    private static final double SURVIVAL_RATE = 0.8;

    private final double PROBABILITY_MUTATE_LINK = 0.01;
    private final double PROBABILITY_MUTATE_NODE = 0.003;
    private final double PROBABILITY_MUTATE_WEIGHT_SHIFT = 0.002;
    private final double PROBABILITY_MUTATE_WEIGHT_RANDOM = 0.002;
    private final double PROBABILITY_MUTATE_TOGGLE_LINK = 0;

    // We COULD use an ArrayList<ConnectionGene>, but we want O(1) access time
    // Using a HashMap works because we overrode hashCode for ConnectionGene so that hash codes are unique for all connection genes
    private final HashMap<ConnectionGene, ConnectionGene> allConnections = new HashMap<>();

    private final RandomHashSet<NodeGene> allNodes = new RandomHashSet<>();

    private RandomHashSet<Client> clients = new RandomHashSet<>();
    private RandomHashSet<Species> species = new RandomHashSet<>();

    private int inputSize;
    private int outputSize;
    private int maxClients;

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
        this.clients.clear();

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

        for (int i = 0; i < this.maxClients; i++) {
            Client client = new Client();
            client.setGenome(this.emptyGenome());
            client.generateCalculator();
            this.clients.add(client);
        }
    }

    public Client getClient(int index) {
        return this.clients.get(index);
    }

    // TODO this is for the unit test; remove it if you don't need it otherwise
    public ArrayList<Client> getClients() {
        return this.clients.getData();
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

        for (Client client : this.clients.getData()) {
            client.generateCalculator();
        }
    }

    private void generateSpecies() {
        for (Species s : this.species.getData()) {
            s.reset();
        }
        for (Client client : this.clients.getData()) {
            if (client.getSpecies() != null) {
                continue;
            }
            boolean found = false;
            for (Species s : this.species.getData()) {
                if (s.put(client)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.species.add(new Species(client));
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
        for (Client client : this.clients.getData()) {
            if (client.getSpecies() == null) {
                Species s = randomSelector.random();
                client.setGenome(s.breed());
                s.forcePut(client);
            }
        }
    }

    public void mutate() {
        for (Client client : this.clients.getData()) {
            client.mutate();
        }
    }

    public void printSpecies() {
        System.out.println("####################################");
        for (Species s : this.species.getData()) {
            System.out.println("Species " + s + "; average score: " + s.getScore() + "; size: " + s.size());
        }
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

    public int getMaxClients() {
        return this.maxClients;
    }

}
