package com.leecrafts.elytracreepers.neat;

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

    public static void main(String[] args) {
        emptyGenomeSize();
        displayFrame();
    }

}
