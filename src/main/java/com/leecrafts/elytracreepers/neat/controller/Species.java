package com.leecrafts.elytracreepers.neat.controller;

import com.leecrafts.elytracreepers.neat.datastructures.RandomHashSet;
import com.leecrafts.elytracreepers.neat.genome.Genome;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

public class Species implements Serializable {

    @Serial
    private static final long serialVersionUID = 1;

    private final RandomHashSet<Agent> agents = new RandomHashSet<>();
    private Agent representative;
    private double score;

    public Species(Agent representative) {
        this.representative = representative;
        this.representative.setSpecies(this);
        this.agents.add(this.representative);
    }

    public boolean put(Agent agent) {
        if (agent.distance(this.representative) < this.representative.getGenome().getNeatController().getCP()) {
            agent.setSpecies(this);
            this.agents.add(agent);
            return true;
        }
        return false;
    }

    public void forcePut(Agent agent) {
        agent.setSpecies(this);
        this.agents.add(agent);
    }

    public void goExtinct() {
        for (Agent agent : this.agents.getData()) {
            agent.setSpecies(null);
        }
    }

    public void evaluateScore() {
        double v = 0;
        for (Agent agent : this.agents.getData()) {
            v += agent.getScore();
        }
        this.score = v / this.agents.size();
    }

    public void reset() {
        this.representative = this.agents.randomElement();
        for (Agent agent : this.agents.getData()) {
            agent.setSpecies(null);
        }

        this.agents.clear();
        this.agents.add(this.representative);
        this.representative.setSpecies(this);
        this.score = 0;
    }

    public void kill(double percentage) {
        this.agents.getData().sort(
                new Comparator<Agent>() {
                    @Override
                    public int compare(Agent o1, Agent o2) {
                        return Double.compare(o1.getScore(), o2.getScore());
                    }
                }
        );

        double amount = percentage * this.agents.size();
        for (int i = 0; i < amount; i++) {
            this.agents.get(0).setSpecies(null);
            this.agents.remove(0);
        }
    }

    public Genome breed() {
        Agent agent1 = this.agents.randomElement();
        Agent agent2 = this.agents.randomElement();
        if (agent1.getScore() > agent2.getScore()) {
            return Genome.crossOver(agent1.getGenome(), agent2.getGenome());
        }
        return Genome.crossOver(agent2.getGenome(), agent1.getGenome());
    }

    public int size() {
        return this.agents.size();
    }

    public RandomHashSet<Agent> getAgents() {
        return this.agents;
    }

    public Agent getRepresentative() {
        return this.representative;
    }

    public double getScore() {
        return this.score;
    }

}
