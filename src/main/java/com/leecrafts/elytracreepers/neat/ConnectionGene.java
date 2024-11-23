package com.leecrafts.elytracreepers.neat;

public class ConnectionGene {
    private int fromNodeId;
    private int toNodeId;
    private int innovationNumber;
    private double weight;
    private boolean enabled;

    // Used as key in innovation history
    @Override
    public boolean equals(Object o) {
        return false;
    }
    @Override
    public int hashCode() {
        return 0;
    }
}
