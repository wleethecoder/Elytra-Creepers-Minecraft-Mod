package com.leecrafts.elytracreepers.neat.genome;

import com.leecrafts.elytracreepers.neat.controller.NEATController;
import com.leecrafts.elytracreepers.neat.datastructures.RandomHashSet;

import java.io.Serial;
import java.io.Serializable;

public class Genome implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final RandomHashSet<ConnectionGene> connections = new RandomHashSet<>();
    private final RandomHashSet<NodeGene> nodes = new RandomHashSet<>();

    private final NEATController neatController;

    public Genome(NEATController neatController) {
        this.neatController = neatController;
    }

    public double distance(Genome otherGenome) {

        Genome thisGenome = this;

        int thisGeneHighestInnovationNumber = 0;
        if (thisGenome.getConnections().size() != 0) {
            thisGeneHighestInnovationNumber = thisGenome.getConnections().get(thisGenome.getConnections().size() - 1).getInnovationNumber();
        }
        int otherGeneHighestInnovationNumber = 0;
        if (otherGenome.getConnections().size() != 0) {
            otherGeneHighestInnovationNumber = otherGenome.getConnections().get(otherGenome.getConnections().size() - 1).getInnovationNumber();
        }

        if (thisGeneHighestInnovationNumber > otherGeneHighestInnovationNumber) {
            Genome temp = thisGenome;
            thisGenome = otherGenome;
            otherGenome = temp;
        }

        int i = 0;
        int j = 0;

        int disjoint = 0;
        int excess;
        double weightDiff = 0;
        int similar = 0;

        while (i < thisGenome.getConnections().size() &&
                j < otherGenome.getConnections().size()) {
            ConnectionGene thisGene = thisGenome.getConnections().get(i);
            ConnectionGene otherGene = otherGenome.getConnections().get(j);
            int thisInnovationNumber = thisGene.getInnovationNumber();
            int otherInnovationNumber = otherGene.getInnovationNumber();
            if (thisInnovationNumber == otherInnovationNumber) {
                // similar gene
                similar++;
                weightDiff += Math.abs(thisGene.getWeight() - otherGene.getWeight());
                i++;
                j++;
            }
            else if (thisInnovationNumber > otherInnovationNumber) {
                // disjoint gene of otherGene
                disjoint++;
                j++;
            }
            else {
                // disjoint gene of thisGene
                disjoint++;
                i++;
            }
        }

        weightDiff /= Math.max(1, similar);
        excess = thisGenome.getConnections().size() - i;

        double N = Math.max(thisGenome.getConnections().size(), otherGenome.getConnections().size());
        if (N < 40) {
            N = 1;
        }
        return this.neatController.getC1() * excess / N +
                this.neatController.getC2() * disjoint / N +
                this.neatController.getC3() * weightDiff;
    }

    public static Genome crossOver(Genome genome1, Genome genome2) {

        Genome child = genome1.getNeatController().emptyGenome();

        int i = 0;
        int j = 0;

        while (i < genome1.getConnections().size() &&
                j < genome2.getConnections().size()) {
            ConnectionGene connectionGene1 = genome1.getConnections().get(i);
            ConnectionGene connectionGene2 = genome2.getConnections().get(j);
            int innovationNumber1 = connectionGene1.getInnovationNumber();
            int innovationNumber2 = connectionGene2.getInnovationNumber();
            if (innovationNumber1 == innovationNumber2) {
                // similar gene
                child.getConnections().add(NEATController.getConnection(
                        Math.random() > 0.5 ? connectionGene1 : connectionGene2));
                i++;
                j++;
            }
            else if (innovationNumber1 > innovationNumber2) {
                // disjoint gene of connectionGene2
//                child.getConnections().add(NEATController.getConnection(gene2));
                j++;
            }
            else {
                // disjoint gene of connectionGene1
                child.getConnections().add(NEATController.getConnection(connectionGene1));
                i++;
            }
        }

        while (i < genome1.getConnections().size()) {
            ConnectionGene connectionGene1 = genome1.getConnections().get(i);
            child.getConnections().add(NEATController.getConnection(connectionGene1));
            i++;
        }
        for (ConnectionGene connectionGene : child.getConnections().getData()) {
            child.getNodes().add(connectionGene.getFrom());
            child.getNodes().add(connectionGene.getTo());
        }

        return child;
    }

    public void mutate() {
        if (this.neatController.getProbabilityMutateLink() > Math.random()) {
            this.mutateLink();
        }
        if (this.neatController.getProbabilityMutateNode() > Math.random()) {
            this.mutateNode();
        }
        if (this.neatController.getProbabilityMutateWeightShift() > Math.random()) {
            this.mutateWeightShift();
        }
        if (this.neatController.getProbabilityMutateWeightRandom() > Math.random()) {
            this.mutateWeightRandom();
        }
        if (this.neatController.getProbabilityMutateToggleLink() > Math.random()) {
            this.mutateLinkToggle();
        }
    }

    public void mutateLink() {
        for (int i = 0; i < 100; i++) {
            NodeGene nodeGene1 = this.nodes.randomElement();
            NodeGene nodeGene2 = this.nodes.randomElement();
            if (nodeGene1.getX() == nodeGene2.getX()) {
                continue;
            }
            ConnectionGene connectionGene;
            if (nodeGene1.getX() < nodeGene2.getX()) {
                connectionGene = new ConnectionGene(nodeGene1, nodeGene2);
            }
            else {
                connectionGene = new ConnectionGene(nodeGene2, nodeGene1);
            }

            if (this.connections.contains(connectionGene)) {
                continue;
            }

            connectionGene = this.neatController.getConnection(connectionGene.getFrom(), connectionGene.getTo());
            connectionGene.setWeight(
                    (Math.random() * 2 - 1) * this.neatController.getWeightRandomStrength());

            this.connections.addSorted(connectionGene);
            return;
        }
    }

    public void mutateNode() {
        ConnectionGene connectionGene = this.connections.randomElement();
        if (connectionGene == null) return;

        NodeGene from = connectionGene.getFrom();
        NodeGene to = connectionGene.getTo();

        int replaceIndex = this.neatController.getReplaceIndex(from, to);
        NodeGene middle;
        if (replaceIndex == 0) {
            middle = this.neatController.getNode();
            middle.setX((from.getX() + to.getX()) / 2);
            middle.setY((from.getY() + to.getY()) / 2 + Math.random() * 0.1 - 0.05);
            this.neatController.setReplaceIndex(from, to, middle.getInnovationNumber());
        }
        else {
            middle = this.neatController.getNode(replaceIndex);
        }

        ConnectionGene connectionGene1 = this.neatController.getConnection(from, middle);
        ConnectionGene connectionGene2 = this.neatController.getConnection(middle, to);
        connectionGene1.setWeight(1);
        connectionGene2.setWeight(connectionGene.getWeight());
        connectionGene2.setEnabled(connectionGene.isEnabled());
        this.connections.remove(connectionGene);
        this.connections.add(connectionGene1);
        this.connections.add(connectionGene2);
        this.nodes.add(middle);
    }

    public void mutateWeightShift() {
        ConnectionGene connectionGene = this.connections.randomElement();
        if (connectionGene != null) {
            connectionGene.setWeight(
                    connectionGene.getWeight() + (Math.random() * 2 - 1) * this.neatController.getWeightShiftStrength());
        }
    }

    public void mutateWeightRandom() {
        ConnectionGene connectionGene = this.connections.randomElement();
        if (connectionGene != null) {
            connectionGene.setWeight(
                    (Math.random() * 2 - 1) * this.neatController.getWeightRandomStrength());
        }
    }

    public void mutateLinkToggle() {
        ConnectionGene connectionGene = this.connections.randomElement();
        if (connectionGene != null) {
            connectionGene.setEnabled(!connectionGene.isEnabled());
        }
    }

    public RandomHashSet<ConnectionGene> getConnections() {
        return this.connections;
    }

    public RandomHashSet<NodeGene> getNodes() {
        return this.nodes;
    }

    public NEATController getNeatController() {
        return this.neatController;
    }

}
