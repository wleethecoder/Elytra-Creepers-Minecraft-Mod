package com.leecrafts.elytracreepers.neat;

import com.leecrafts.elytracreepers.event.ModEvents;
import net.minecraft.world.entity.LivingEntity;

public class NEATController {
    private Population population;
    private int generation = 0;

    public void initializePopulation(int size) {
        // initialize population
        ModEvents.REMAINING = size;
        // entity.setData(new Genome)
        // population.add(entity.getData())
    }

    public Genome getNewGenome() {
        // Returns a genome for a new creeper
//        return population.getNextGenome();
        return null;
    }

    public void recordFitness(LivingEntity livingEntity, double fitness) {
        ModEvents.REMAINING--;

        if (ModEvents.REMAINING == 0) {
            evolveNewGeneration();
        }
    }

    private void evolveNewGeneration() {
    }
}
