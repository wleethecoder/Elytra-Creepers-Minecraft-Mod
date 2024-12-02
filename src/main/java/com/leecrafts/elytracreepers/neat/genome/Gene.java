package com.leecrafts.elytracreepers.neat.genome;

import java.io.Serial;
import java.io.Serializable;

public class Gene implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    protected int innovationNumber;

    public Gene(int innovationNumber) {
        this.innovationNumber = innovationNumber;
    }

    public Gene() {
    }

    public int getInnovationNumber() {
        return this.innovationNumber;
    }

    public void setInnovationNumber(int innovationNumber) {
        this.innovationNumber = innovationNumber;
    }

}
