package com.leecrafts.elytracreepers.neat.datastructures;

import java.util.ArrayList;

public class RandomSelector<T> {

    // helper class for random selection of agents based on fitness score
    // higher-scoring agents are more likely to be selected

    private final ArrayList<T> objects = new ArrayList<>();
    private final ArrayList<Double> scores = new ArrayList<>();
    private double totalScore = 0;

    public void add(T object, double score) {
        this.objects.add(object);
        this.scores.add(score);
        this.totalScore += score;
    }

    // higher scoring objects are assigned a higher weight for random selection
    public T random() {
        double v = Math.random() * this.totalScore;
        double c = 0;
        for (int i = 0; i < this.objects.size(); i++) {
            c += this.scores.get(i);
            if (c >= v) {
                return this.objects.get(i);
            }
        }
        return null;
    }

    public void reset() {
        this.objects.clear();
        this.scores.clear();
        this.totalScore = 0;
    }

}
