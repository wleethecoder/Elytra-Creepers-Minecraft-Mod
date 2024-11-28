package com.leecrafts.elytracreepers.neat;

import com.leecrafts.elytracreepers.neat.genome.Genome;

import java.util.List;

public class Population {
    private List<Genome> genomes;
    private SpeciesManager speciesManager;
    private InnovationHistory history;
    private int generation;

    // Methods for evolution
    public void evolve() {
    }
    public Genome getBestGenome() {
        return null;
    }
    public void createNextGeneration() {
    }
}
