package com.leecrafts.elytracreepers.neat.calculations;

import java.io.Serializable;

public class Connection implements Serializable {

    private Node from;
    private Node to;

    private double weight;
    private boolean enabled = true;

    public Connection(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}