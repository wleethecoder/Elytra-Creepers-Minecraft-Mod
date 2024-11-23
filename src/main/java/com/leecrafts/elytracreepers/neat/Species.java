package com.leecrafts.elytracreepers.neat;

import java.util.List;

public class Species {
    private int id;
    private List<Genome> members;
    private double bestFitness;
    private int staleness; // Generations without improvement
    private Genome representative;

    // Methods for species management
    public void calculateAdjustedFitness(){
    }
    public void reproduce() {
    }
    public boolean isStagnant() {
        return false;
    }
}
