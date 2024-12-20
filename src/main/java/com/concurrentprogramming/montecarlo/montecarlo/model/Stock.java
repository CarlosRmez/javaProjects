package com.concurrentprogramming.montecarlo.montecarlo.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Stock {
    private final String ticker;
    public final TreeMap<LocalDate, Double> stockData;
    private final TreeMap<LocalDate, Double> dailyPctChange;
    private Double[] logReturns;
    private final List<List<Double>> allSimulations;
    private final List<Double> finalPrices;

    public String getTicker() {
        return ticker;
    }

    public Stock(String ticker) {
        this.ticker = ticker;
        this.stockData = getStockData(ticker);
        this.dailyPctChange = new TreeMap<>();
        this.allSimulations = new ArrayList<>();
        this.finalPrices = new ArrayList<>();
    }

    public Double[] getLogReturns() {
        if (this.logReturns == null || this.logReturns.length == 0) {
            calculateLogReturns();
        }
        return this.logReturns;
    }

    public List<Double> performSimulation(int daysToPredict) {
        if (stockData.isEmpty()) {
            throw new IllegalStateException("No historical stock data available for prediction.");
        }
        if (this.logReturns == null || this.logReturns.length == 0) {
            calculateLogReturns();
        }

        MonteCarloSimulation simulation = new MonteCarloSimulation(logReturns);
        var simulatedLogReturns = simulation.performMontecarloSimulation(daysToPredict);
        List<Double> simulatedReturns = simulatedLogReturns.stream()
                .map(Math::exp)
                .toList();

        List<Double> simulatedPrices = new ArrayList<>();
        Double currentPrice = this.stockData.lastEntry().getValue();
        simulatedPrices.add(currentPrice);
        for (Double ret : simulatedReturns) {
            currentPrice = currentPrice * ret;
            simulatedPrices.add(currentPrice);
        }
        return simulatedPrices;
    }

    public void performMultipleSimulations(int daysToPredict, int numSimulations) {
        allSimulations.clear();
        finalPrices.clear();

        for (int i = 0; i < numSimulations; i++) {
            List<Double> simulatedPrices = performSimulation(daysToPredict);
            allSimulations.add(simulatedPrices);
            finalPrices.add(simulatedPrices.get(simulatedPrices.size() - 1));
        }
    }

    public List<Double> getFinalPrices() {
        return finalPrices;
    }

    public double getMeanFinalPrice() {
        return finalPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public double getPercentageChangeFromInitialToMean() {
        double initialPrice = stockData.lastEntry().getValue();
        double meanFinalPrice = getMeanFinalPrice();
        return ((meanFinalPrice - initialPrice) / initialPrice) * 100;
    }

    private void calculateDailyPctChange() {
        Double previousPrice = null;
        for (Map.Entry<LocalDate, Double> entry : this.stockData.entrySet()) {
            LocalDate date = entry.getKey();
            Double currentPrice = entry.getValue();
            if (previousPrice != null) {
                double pctChange = ((currentPrice - previousPrice) / previousPrice);
                this.dailyPctChange.put(date, pctChange);
            }
            previousPrice = currentPrice;
        }
    }

    private void calculateLogReturns() {
        if (this.dailyPctChange.isEmpty()) {
            calculateDailyPctChange();
        }
        this.logReturns = new Double[this.dailyPctChange.size()];
        int k = 0;
        for (Map.Entry<LocalDate, Double> entry : this.dailyPctChange.entrySet()) {
            Double pctChange = entry.getValue();
            this.logReturns[k] = Math.log(1 + pctChange);
            k++;
        }
    }

    private TreeMap<LocalDate, Double> getStockData(String ticker) {
        String filePath = "src/main/java/com/concurrentprogramming/montecarlo/montecarlo/model/data/"
                + ticker + ".csv";
        TreeMap<LocalDate, Double> data = new TreeMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                LocalDate date = formatDate(values[0]);
                Double price = Double.parseDouble(values[1]);
                data.put(date, price);
            }
        } catch (IOException e) {
            System.err.println("Error reading stock data for ticker: " + ticker);
            e.printStackTrace();
        }
        return data;
    }

    private LocalDate formatDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
        try {
            return LocalDate.parse(date, formatter);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + date);
            throw e;
        }
    }

    public static void main(String[] args) {
        Stock apple = new Stock("MSFT");

        int daysToPredict = 60;
        int numSimulations = 100_000;

        apple.performMultipleSimulations(daysToPredict, numSimulations);

        double initialPrice = apple.stockData.lastEntry().getValue();
        double meanFinalPrice = apple.getMeanFinalPrice();
        double percentageChange = apple.getPercentageChangeFromInitialToMean();

        System.out.printf("Initial Price: %.2f%n", initialPrice);
        System.out.printf("Mean Final Price: %.2f%n", meanFinalPrice);
        System.out.printf("Percentage Change from Initial to Mean: %.2f%%%n", percentageChange);
    }
}
