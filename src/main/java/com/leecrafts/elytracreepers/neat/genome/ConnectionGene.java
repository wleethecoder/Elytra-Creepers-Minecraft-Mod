package com.leecrafts.elytracreepers.neat.genome;

import com.leecrafts.elytracreepers.neat.controller.NEATController;

import java.io.Serial;
import java.io.Serializable;

public class ConnectionGene extends Gene implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private NodeGene from;
    private NodeGene to;

    private double weight;
    private boolean enabled = true;

    private int replaceIndex;

    public ConnectionGene(NodeGene from, NodeGene to) {
        this.from = from;
        this.to = to;
    }

    public NodeGene getFrom() {
        return this.from;
    }

    public NodeGene getTo() {
        return this.to;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getReplaceIndex() {
        return this.replaceIndex;
    }

    public void setReplaceIndex(int replaceIndex) {
        this.replaceIndex = replaceIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConnectionGene connectionGene)) {
            return false;
        }
        return this.from.equals(connectionGene.from) && this.to.equals(connectionGene.to);
    }

    @Override
    public int hashCode() {
        // this value will always be unique for each connection gene
        return this.from.getInnovationNumber() * NEATController.MAX_NODES + this.to.getInnovationNumber();
    }
}
