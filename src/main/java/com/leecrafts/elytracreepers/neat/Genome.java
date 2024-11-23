package com.leecrafts.elytracreepers.neat;

import java.util.List;

public class Genome {
    private int id;
    private List<Node> nodes;
    private List<Connection> connections;
    private double fitness;
    private int species;

    // Methods for network evaluation, mutation, crossover
    public double evaluateNetwork(List<Double> inputs) {
        return 0;
    }
    public void mutateWeights() {
    }
    public void mutateAddNode() {
    }
    public void mutateAddConnection() {
    }
}
