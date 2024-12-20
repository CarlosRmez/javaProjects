package com.concurrentprogramming.montecarlo.montecarlo.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class MonteCarloSimulation {
    private final double mean;
    private final double stdDev;
    private final Random random;

    public MonteCarloSimulation(Double[] distribution) {
        this.mean = calculateMean(distribution);
        this.stdDev = calculateStdDev(distribution, mean);
        this.random = new Random();
    }

    // Method to calculate the mean of the distribution
    public double calculateMean(Double[] distribution) {
        double sum = 0.0;
        for (double value : distribution) {
            sum += value;
        }
        return sum / distribution.length;
    }

    // Method to calculate the standard deviation of the distribution
    private double calculateStdDev(Double[] distribution, double mean) {
        double sumSquaredDiffs = 0.0;
        for (double value : distribution) {
            sumSquaredDiffs += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sumSquaredDiffs / distribution.length);
    }

    // Method to perform one simulation
    public List<Double> performMontecarloSimulation(int steps) {
        List<Double> simulation = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            double randomValue = random.nextGaussian() * stdDev + mean;
            simulation.add(randomValue);
        }
        return simulation;
    }

    // Method to perform multiple simulations
    public List<List<Double>> performSimulations(int numSimulations, int steps) {
        List<List<Double>> simulations = new ArrayList<>();
        for (int i = 0; i < numSimulations; i++) {
            simulations.add(performMontecarloSimulation(steps));
        }
        return simulations;
    }

    // Getters for mean and standard deviation
    public double getMean() {
        return mean;
    }

    public double getStdDev() {
        return stdDev;
    }
}
