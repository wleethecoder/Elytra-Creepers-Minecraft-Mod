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
        // scores are normalized so that the smallest score is 1 (if the smallest score is < 1)
        // this is to deal with negative scores
        double smallest = Double.MAX_VALUE;
        for (Double score : this.scores) {
            smallest = Math.min(smallest, score);
        }
        double offset = Math.max(1 - smallest, 0);

        double v = Math.random() * (this.totalScore + offset * this.scores.size());
        double c = 0;
        for (int i = 0; i < this.objects.size(); i++) {
            c += this.scores.get(i) + offset;
            if (c >= v) {
                return this.objects.get(i);
            }
        }

        // this code should never run
        assert false;
        return null;
    }

    public void reset() {
        this.objects.clear();
        this.scores.clear();
        this.totalScore = 0;
    }

}
