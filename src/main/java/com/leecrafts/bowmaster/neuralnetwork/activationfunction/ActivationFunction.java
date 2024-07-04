package com.leecrafts.bowmaster.neuralnetwork.activationfunction;

import java.io.Serializable;

public interface ActivationFunction extends Serializable {
    double activate(double input);
    double[] activate(double[] inputs);
}
