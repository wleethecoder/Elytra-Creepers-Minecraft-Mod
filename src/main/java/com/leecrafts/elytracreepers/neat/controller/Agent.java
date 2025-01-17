package com.leecrafts.elytracreepers.neat.controller;

import com.leecrafts.elytracreepers.neat.calculations.Calculator;
import com.leecrafts.elytracreepers.neat.genome.Genome;

import java.io.Serial;
import java.io.Serializable;

public class Agent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private Calculator calculator;

    private Genome genome;
    private double score;
    private Species species;

    public void generateCalculator() {
        this.calculator = new Calculator(this.genome);
    }

    public double[] calculate(double... input) {
        if (this.calculator == null) {
            generateCalculator();
        }
        return this.calculator.calculate(input);
    }

    public double distance(Agent other) {
        return this.genome.distance(other.getGenome());
    }

    public void mutate() {
        this.genome.mutate();
    }

    public Calculator getCalculator() {
        return this.calculator;
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }
}
