package com.concurrentprogramming.montecarlo.montecarlo.utils;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortfolioPlotter {

    public static void plotSimulations(List<List<Double>> simulations) {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Portfolio Simulations")
                .xAxisTitle("Days")
                .yAxisTitle("Portfolio Value")
                .build();

        int maxSimulations = Math.min(simulations.size(), 20);

        for (int i = 0; i < maxSimulations; i++) {
            List<Double> simulation = simulations.get(i);

            List<Integer> xData = new ArrayList<>();
            for (int day = 0; day < simulation.size(); day++) {
                xData.add(day + 1);
            }

            List<Double> yData = new ArrayList<>(simulation);

            chart.addSeries("Simulation " + (i + 1), xData, yData);
        }

        new SwingWrapper<>(chart).displayChart();
    }
}
