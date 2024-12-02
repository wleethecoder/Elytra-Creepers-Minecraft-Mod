package com.leecrafts.elytracreepers.neat.genome;

import java.io.Serial;
import java.io.Serializable;

public class NodeGene extends Gene implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    // x and y values are for drawing the network
    private double x;
    private double y;

    public NodeGene(int innovationNumber) {
        super(innovationNumber);
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeGene nodeGene)) {
            return false;
        }
        return this.innovationNumber == nodeGene.innovationNumber;
    }

    @Override
    public int hashCode() {
        return this.innovationNumber;
    }

}