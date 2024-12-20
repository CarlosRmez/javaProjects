package com.concurrentprogramming.montecarlo.montecarlo.model;

import java.util.List;

public class SimulationResult {
    private final double sequentialMean;
    private final double sequentialStdDev;
    private final long sequentialExecutionTime;

    private final double parallelMean;
    private final double parallelStdDev;
    private final long parallelExecutionTime;

    private final List<List<Double>> allSimulations;

    public SimulationResult(
            double sequentialMean,
            double sequentialStdDev,
            long sequentialExecutionTime,
            double parallelMean,
            double parallelStdDev,
            long parallelExecutionTime,
            List<List<Double>> allSimulations
    ) {
        this.sequentialMean = sequentialMean;
        this.sequentialStdDev = sequentialStdDev;
        this.sequentialExecutionTime = sequentialExecutionTime;
        this.parallelMean = parallelMean;
        this.parallelStdDev = parallelStdDev;
        this.parallelExecutionTime = parallelExecutionTime;
        this.allSimulations = allSimulations;
    }

    public double getSequentialMean() {
        return sequentialMean;
    }

    public double getSequentialStdDev() {
        return sequentialStdDev;
    }

    public long getSequentialExecutionTime() {
        return sequentialExecutionTime;
    }

    public double getParallelMean() {
        return parallelMean;
    }

    public double getParallelStdDev() {
        return parallelStdDev;
    }

    public long getParallelExecutionTime() {
        return parallelExecutionTime;
    }

    public List<List<Double>> getAllSimulations() {
        return allSimulations;
    }
}
