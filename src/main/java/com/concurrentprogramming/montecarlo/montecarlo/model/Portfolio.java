package com.concurrentprogramming.montecarlo.montecarlo.model;

import com.concurrentprogramming.montecarlo.montecarlo.utils.PortfolioPlotter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Portfolio {
    private final Map<Stock, Double> stockWeights;
    private final double initialCapital;
    private final List<List<Double>> allSimulations;
    private final List<Double> finalValues;

    public Portfolio(double initialCapital, Map<String, Double> tickerWeights) {
        this.initialCapital = initialCapital;
        this.stockWeights = new HashMap<>();
        this.allSimulations = new ArrayList<>();
        this.finalValues = new ArrayList<>();

        for (Map.Entry<String, Double> entry : tickerWeights.entrySet()) {
            String ticker = entry.getKey();
            Double weight = entry.getValue();
            Stock stock = new Stock(ticker);
            this.stockWeights.put(stock, weight);
        }
    }

    public List<Double> simulatePortfolio(int daysToPredict) {
        Map<Stock, List<Double>> stockSimulations = new HashMap<>();
        for (Stock stock : stockWeights.keySet()) {
            stockSimulations.put(stock, stock.performSimulation(daysToPredict));
        }

        List<Double> portfolioSimulation = new ArrayList<>();
        for (int day = 0; day <= daysToPredict; day++) {
            double portfolioValue = 0.0;
            for (Map.Entry<Stock, Double> entry : stockWeights.entrySet()) {
                Stock stock = entry.getKey();
                double weight = entry.getValue();
                double initialInvestment = initialCapital * weight;
                double stockPriceRelative = stockSimulations.get(stock).get(day) / stock.stockData.lastEntry().getValue();
                portfolioValue += initialInvestment * stockPriceRelative;
            }
            portfolioSimulation.add(portfolioValue);
        }
        return portfolioSimulation;
    }

    public void performMultipleSimulations(int daysToPredict, int numSimulations) {
        allSimulations.clear();
        finalValues.clear();
        for (int i = 0; i < numSimulations; i++) {
            List<Double> portfolioSimulation = simulatePortfolio(daysToPredict);
            allSimulations.add(portfolioSimulation);
            finalValues.add(portfolioSimulation.get(portfolioSimulation.size() - 1));
        }
    }

    public void performMultipleSimulationsInParallel(int daysToPredict, int numSimulations) {
        allSimulations.clear();
        finalValues.clear();

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("The number of available processors is :" + availableProcessors);
        int threadsToUse = Math.min(availableProcessors, numSimulations);

        List<Thread> threads = new ArrayList<>();
        List<List<Double>> concurrentSimulations = new CopyOnWriteArrayList<>();
        List<Double> concurrentFinalValues = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadsToUse; i++) {
            Thread thread = new Thread(() -> {
                int simulationsPerThread = numSimulations / threadsToUse;
                for (int j = 0; j < simulationsPerThread; j++) {
                    List<Double> portfolioSimulation = simulatePortfolio(daysToPredict);
                    concurrentSimulations.add(portfolioSimulation);
                    concurrentFinalValues.add(portfolioSimulation.getLast());
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        allSimulations.addAll(concurrentSimulations);
        finalValues.addAll(concurrentFinalValues);
    }

    public List<Double> getFinalValuesDistribution() {
        return finalValues;
    }

    public double getMeanFinalValue() {
        return finalValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public double getStandardDeviationFinalValue() {
        double mean = getMeanFinalValue();
        return Math.sqrt(finalValues.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .average()
                .orElse(0.0));
    }

    public List<List<Double>> getAllSimulations() {
        return allSimulations;
    }

    public void plotSimulations() {
        PortfolioPlotter.plotSimulations(allSimulations);
    }

    public static void main(String[] args) {
        Map<String, Double> tickerWeights = Map.of(
                "AAPL", 0.6,
                "MSFT", 0.4
        );

        double initialCapital = 10;

        Portfolio portfolio = new Portfolio(initialCapital, tickerWeights);

        int daysToPredict = 30;
        int numSimulations = 10_000;

        long startSequential = System.currentTimeMillis();
        portfolio.performMultipleSimulations(daysToPredict, numSimulations);
        long sequentialTime = System.currentTimeMillis() - startSequential;

        double sequentialMean = portfolio.getMeanFinalValue();
        double sequentialStdDev = portfolio.getStandardDeviationFinalValue();

        portfolio.allSimulations.clear();
        portfolio.finalValues.clear();

        long startParallel = System.currentTimeMillis();
        portfolio.performMultipleSimulationsInParallel(daysToPredict, numSimulations);
        long parallelTime = System.currentTimeMillis() - startParallel;

        double parallelMean = portfolio.getMeanFinalValue();
        double parallelStdDev = portfolio.getStandardDeviationFinalValue();

        double improvementPercentage = ((double) (sequentialTime - parallelTime) / sequentialTime) * 100;

        System.out.println("Sequential Execution:");
        System.out.printf("Mean Final Value: %.2f%n", sequentialMean);
        System.out.printf("Standard Deviation: %.2f%n", sequentialStdDev);
        System.out.printf("Execution Time: %d ms%n", sequentialTime);

        System.out.println("\nParallel Execution:");
        System.out.printf("Mean Final Value: %.2f%n", parallelMean);
        System.out.printf("Standard Deviation: %.2f%n", parallelStdDev);
        System.out.printf("Execution Time: %d ms%n", parallelTime);

        System.out.println("\nPerformance Improvement:");
        System.out.printf("Parallel Execution is %.2f%% faster than Sequential Execution.%n", improvementPercentage);

        //portfolio.plotSimulations();
    }
}
