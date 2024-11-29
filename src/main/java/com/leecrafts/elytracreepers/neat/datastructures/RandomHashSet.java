package com.leecrafts.elytracreepers.neat.datastructures;

import com.leecrafts.elytracreepers.neat.genome.Gene;

import java.util.ArrayList;
import java.util.HashSet;

public class RandomHashSet<T> {

    // using both a hashset and arraylist
    // reason: use hashing to detect if data is in it or not (O(1) operation)
    // arraylist stores actual data, which can be accessed by indexing
    private final HashSet<T> set;
    private final ArrayList<T> data;

    public RandomHashSet() {
        this.set = new HashSet<>();
        this.data = new ArrayList<>();
    }

    public boolean contains(T object) {
        return this.set.contains(object);
    }

    public T randomElement() {
        if (!this.set.isEmpty()) {
            return this.data.get((int) (Math.random() * this.size()));
        }
        return null;
    }

    // in theory, data and set should have the same size.
    public int size() {
        return this.data.size();
    }

    public void add(T object) {
        if (!this.set.contains(object)) {
            this.set.add(object);
            this.data.add(object);
        }
    }

    public void addSorted(Gene gene) {
        for (int i = 0; i < this.size(); i++) {
            int innovationNumber = ((Gene) this.data.get(i)).getInnovationNumber();
            if (gene.getInnovationNumber() < innovationNumber) {
                this.set.add((T) gene);
                this.data.add(i, (T) gene);
                return;
            }
        }
        this.add((T) gene);
    }

    public void clear() {
        this.set.clear();
        this.data.clear();
    }

    public T get(int index) {
        if (index < 0 || index >= this.size()) {
            return null;
        }
        return this.data.get(index);
    }

    public T get(T template) {
        return this.data.get(this.data.indexOf(template));
    }

    public void remove(int index) {
        if (index < 0 || index >= this.size()) {
            return;
        }
        this.set.remove(this.data.get(index));
        this.data.remove(index);
    }

    public void remove(T object) {
        this.set.remove(object);
        this.data.remove(object);
    }

    public ArrayList<T> getData() {
        return this.data;
    }

}
