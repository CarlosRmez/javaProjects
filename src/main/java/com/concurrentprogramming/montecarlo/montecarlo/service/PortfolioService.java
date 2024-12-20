package com.concurrentprogramming.montecarlo.montecarlo.service;

import com.concurrentprogramming.montecarlo.montecarlo.model.Portfolio;
import com.concurrentprogramming.montecarlo.montecarlo.model.SimulationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    public SimulationResult performSimulations(double initialCapital, Map<String, Double> stockWeights, int daysToPredict, int numSimulations) {

        Portfolio portfolio = new Portfolio(initialCapital, stockWeights);

        // Sequential simulation
        long startSequential = System.currentTimeMillis();
        portfolio.performMultipleSimulations(daysToPredict, numSimulations);
        long sequentialExecutionTime = System.currentTimeMillis() - startSequential;

        double sequentialMean = portfolio.getMeanFinalValue();
        double sequentialStdDev = portfolio.getStandardDeviationFinalValue();

        // Parallel simulation
        portfolio.getAllSimulations().clear();
        portfolio.getFinalValuesDistribution().clear();

        long startParallel = System.currentTimeMillis();
        portfolio.performMultipleSimulationsInParallel(daysToPredict, numSimulations);
        long parallelExecutionTime = System.currentTimeMillis() - startParallel;

        double parallelMean = portfolio.getMeanFinalValue();
        double parallelStdDev = portfolio.getStandardDeviationFinalValue();

        // Prepare results
        return new SimulationResult(
                sequentialMean,
                sequentialStdDev,
                sequentialExecutionTime,
                parallelMean,
                parallelStdDev,
                parallelExecutionTime,
                portfolio.getAllSimulations()
        );
    }
}
