package com.leecrafts.elytracreepers.neat.util;

import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import com.leecrafts.elytracreepers.neat.genome.Genome;
import com.leecrafts.elytracreepers.neat.visual.Frame;

public class NEATTest {

    private static void emptyGenomeSize() {
        NEATController neatController = new NEATController(4,10,50);
        Genome g = neatController.emptyGenome();
        int size = g.getNodes().size();
        System.out.println(size);
        assert size == 14;
    }

    private static void displayFrame() {
        NEATController neatController = new NEATController(3,2,0);
        new Frame(neatController.emptyGenome());
    }

    private static void agents() {
        NEATController neatController = new NEATController(10,1,1000);
        double[] in = new double[10];
        for (int i = 0; i < 10; i++) {
            in[i] = Math.random();
        }
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < neatController.getPopulationSize(); j++) {
                Agent agent = neatController.getAgent(j);
                double score = agent.calculate(in)[0];
                agent.setScore(score);
            }
            neatController.evolve();
            neatController.printSpecies();
        }

        new Frame(neatController.getAgent(0).getGenome());
    }

    public static void main(String[] args) {
//        emptyGenomeSize();
//        displayFrame();
        agents();
    }

}
