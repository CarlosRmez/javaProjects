package com.concurrentprogramming.montecarlo.montecarlo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class PortfolioRequest {
    private double initialCapital;
    private Map<String, Double> stockWeights;
    private int daysToPredict;
    private int numSimulations;

    public void updateWeights(){
        for (Map.Entry<String, Double> entry : stockWeights.entrySet()) {
            double updatedWeight = entry.getValue() / 100;
            stockWeights.put(entry.getKey(), updatedWeight);
        }
    }

}
