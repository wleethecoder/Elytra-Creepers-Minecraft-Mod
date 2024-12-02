package com.leecrafts.elytracreepers.neat.calculations;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Comparable<Node>, Serializable {

    private double x;
    private double output;
    private ArrayList<Connection> connections = new ArrayList<>();

    public Node(double x) {
        this.x = x;
    }

    public void calculate() {
        double s = 0;
        for(Connection connection : this.connections){
            if(connection.isEnabled()){
                s += connection.getWeight() * connection.getFrom().getOutput();
            }
        }
        this.output = this.activationFunction(s);
    }

    // TODO tweak activation function
    private double activationFunction(double x) {
        // if this is an output node (output activation is linear)
        if (this.getX() >= 0.9) {
            return x;
        }
        // for hidden nodes, use tanh
        return Math.tanh(x);
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getX() {
        return x;
    }

    public double getOutput() {
        return output;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }


    @Override
    public int compareTo(Node o) {
        if (this.x > o.x) return -1;
        if (this.x < o.x) return 1;
        return 0;
    }
}