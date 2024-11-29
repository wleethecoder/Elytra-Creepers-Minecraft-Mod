package com.leecrafts.elytracreepers.neat.controller;

import com.leecrafts.elytracreepers.neat.datastructures.RandomHashSet;
import com.leecrafts.elytracreepers.neat.genome.Genome;

import java.util.Comparator;

public class Species {

    private RandomHashSet<Client> clients = new RandomHashSet<>();
    private Client representative;
    private double score;

    public Species(Client representative) {
        this.representative = representative;
        this.representative.setSpecies(this);
        this.clients.add(this.representative);
    }

    public boolean put(Client client) {
        if (client.distance(this.representative) < this.representative.getGenome().getNeatController().getCP()) {
            client.setSpecies(this);
            this.clients.add(client);
            return true;
        }
        return false;
    }

    public void forcePut(Client client) {
        client.setSpecies(this);
        this.clients.add(client);
    }

    public void goExtinct() {
        for (Client client : this.clients.getData()) {
            client.setSpecies(null);
        }
    }

    public void evaluateScore() {
        double v = 0;
        for (Client client : this.clients.getData()) {
            v += client.getScore();
        }
        this.score = v / this.clients.size();
    }

    public void reset() {
        this.representative = this.clients.randomElement();
        for (Client client : this.clients.getData()) {
            client.setSpecies(null);
        }

        this.clients.clear();
        this.clients.add(this.representative);
        this.representative.setSpecies(this);
        this.score = 0;
    }

    public void kill(double percentage) {
        this.clients.getData().sort(
                new Comparator<Client>() {
                    @Override
                    public int compare(Client o1, Client o2) {
                        return Double.compare(o1.getScore(), o2.getScore());
                    }
                }
        );

        double amount = percentage * this.clients.size();
        for (int i = 0; i < amount; i++) {
            this.clients.get(0).setSpecies(null);
            this.clients.remove(0);
        }
    }

    public Genome breed() {
        Client client1 = this.clients.randomElement();
        Client client2 = this.clients.randomElement();
        if (client1.getScore() > client2.getScore()) {
            return Genome.crossOver(client1.getGenome(), client2.getGenome());
        }
        return Genome.crossOver(client2.getGenome(), client1.getGenome());
    }

    public int size() {
        return this.clients.size();
    }

    public RandomHashSet<Client> getClients() {
        return this.clients;
    }

    public Client getRepresentative() {
        return this.representative;
    }

    public double getScore() {
        return this.score;
    }

}
