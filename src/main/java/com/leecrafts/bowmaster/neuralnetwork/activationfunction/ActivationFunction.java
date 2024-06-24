package com.leecrafts.bowmaster.neuralnetwork.activationfunction;

public interface ActivationFunction {
    double activate(double input);
    double[] activate(double[] inputs);
}
