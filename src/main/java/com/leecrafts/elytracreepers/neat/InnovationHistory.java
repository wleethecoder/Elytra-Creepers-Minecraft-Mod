package com.leecrafts.elytracreepers.neat;

import com.leecrafts.elytracreepers.neat.genome.ConnectionGene;
import com.leecrafts.elytracreepers.neat.genome.NodeGene;

import java.util.Map;

public class InnovationHistory {
    private Map<ConnectionGene, Integer> innovations;
    private int currentInnovation;

    // Methods for tracking innovations
    public int getInnovationNumber(NodeGene from, NodeGene to) {
        return 0;
    }
    public boolean isExistingInnovation(NodeGene from, NodeGene to) {
        return false;
    }
}
