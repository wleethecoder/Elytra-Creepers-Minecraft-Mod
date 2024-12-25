package com.leecrafts.elytracreepers.neat.util;

import com.leecrafts.elytracreepers.neat.controller.Agent;
import com.leecrafts.elytracreepers.neat.controller.NEATController;
import com.leecrafts.elytracreepers.neat.controller.Species;
import com.leecrafts.elytracreepers.neat.genome.Genome;
import com.leecrafts.elytracreepers.neat.visual.Frame;

import java.io.*;

public class NEATTest {

    private static final double[] in = new double[] {0.04406537922176701, 0.18126992327777725, 0.24199198935168187, 0.1813263608295541, 0.24448311455523108, 0.14145820074195736, 0.2602359917148832, 0.3497226936060528, 0.9711452639458819, 0.8405658596218063};
    private static final String ASSETS_DIRECTORY_PATH = "/assets/elytracreepers/agent";
    public static final File AGENT_DIRECTORY_PATH = new File(System.getProperty("user.dir"), "src/main/resources/assets/elytracreepers/agent");
    public static final File NEATCONTROLLER_DIRECTORY_PATH = new File(System.getProperty("user.dir"), "src/main/resources/assets/elytracreepers/neatcontroller");

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

    private static NEATController agents(boolean displayFrame) {
        NEATController neatController = new NEATController(10,1,1000);
//        double[] in = new double[10];
//        for (int i = 0; i < 10; i++) {
//            in[i] = Math.random();
//        }
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < neatController.getPopulationSize(); j++) {
                Agent agent = neatController.getAgent(j);
                double score = agent.calculate(in)[0];
                agent.setScore(score);
            }
            neatController.evolve();
            neatController.printSpecies();
        }

        if (displayFrame) {
            new Frame(neatController.getAgent(0).getGenome());
        }
        return neatController;
    }

    private static void saveAndLoad() {
        NEATController neatController = agents(false);
        Agent agent = neatController.getBestAgent();

        // saving
        File file = new File(AGENT_DIRECTORY_PATH, "agent.dat");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(agent);
            System.out.println("Agent saved to " + file.getPath());
        } catch (IOException e) {
            System.out.println("Error saving agent: " + e.getMessage());
            System.out.println(e.toString());
        }

        // loading
        Agent agent1 = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            agent1 = (Agent) in.readObject();
            System.out.println("Agent loaded from " + file.getPath());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading agent: " + e.getMessage());
        }
        if (agent1 != null) {
            double score = agent1.calculate(in)[0];
            System.out.println(score);
        }
    }

    private static void displayGenomeOfLoadedEntityAgent(int agentNumber) {
        File file = new File(AGENT_DIRECTORY_PATH, String.format("%s-%d.dat", "agent", agentNumber));
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Agent agent = (Agent) in.readObject();
            System.out.println("Agent loaded from " + file.getPath());
            new Frame(agent.getGenome());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading agent: " + e.getMessage());
        }
    }

    private static void displayGenomeOfLoadedNeatController(int number, int generationNumber, int agentNumber) {
        File file = new File(NEATCONTROLLER_DIRECTORY_PATH, String.format("%s-%d-%d.dat", "neatcontroller", number, generationNumber));
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            NEATController neatController = (NEATController) in.readObject();
            System.out.println("Neatcontroller loaded from " + file.getPath());
            Agent bestAgent = neatController.getBestAgent();
//            double[] output = bestAgent.calculate(100,-100,.7853,2.19,0);
//            System.out.println(Arrays.toString(output));

            if (agentNumber > -1) {
                // save best agent from loaded neatcontroller
                File file1 = new File(AGENT_DIRECTORY_PATH, String.format("/%s-%d.dat", "agent", agentNumber));
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file1))) {
                    out.writeObject(bestAgent.getCalculator());
                    System.out.println("this agent saved to " + file1.getPath());
                } catch (IOException e) {
                    System.out.println("Error saving this agent: " + e.getMessage());
                    System.out.println(e.toString());
                }
            }

            System.out.println(bestAgent.getScore());
            new Frame(bestAgent.getGenome());

            for (int i = 0; i < neatController.numSpecies(); i++) {
                Species species = neatController.getSpecies(i);
                Agent representative = species.getRepresentative();
//                System.out.println(Arrays.toString(representative.calculate(100, -100, .7853, 2.19, 0)));
//                System.out.println(representative.getScore());
                new Frame(representative.getGenome());
            }

            int mostConnections = 0;
            StringBuilder allNumConnections = new StringBuilder();
            for (int i = 0; i < neatController.getPopulationSize(); i++) {
                Agent agent = neatController.getAgent(i);
                int numConnections = agent.getGenome().getConnections().size();
                allNumConnections.append(numConnections);
                allNumConnections.append(" ");
                mostConnections = Math.max(mostConnections, numConnections);
            }
            System.out.println(allNumConnections);
            System.out.println(mostConnections);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading neatcontroller: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
//        emptyGenomeSize();
//        displayFrame();
//        agents(true);
//        saveAndLoad();
//        displayGenomeOfLoadedEntityAgent(4);
        displayGenomeOfLoadedNeatController(1, 446, 1);
//        NEATController nc = new NEATController(NEATUtil.INPUT_SIZE-2, NEATUtil.OUTPUT_SIZE, NEATUtil.POPULATION_SIZE);
//        Agent a = nc.getAgent(0);
//        new Frame(a.getGenome());
    }

}
