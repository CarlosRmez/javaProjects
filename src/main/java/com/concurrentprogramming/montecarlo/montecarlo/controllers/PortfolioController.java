package com.concurrentprogramming.montecarlo.montecarlo.controllers;

import com.concurrentprogramming.montecarlo.montecarlo.model.SimulationResult;
import com.concurrentprogramming.montecarlo.montecarlo.service.PortfolioService;
import com.concurrentprogramming.montecarlo.montecarlo.model.PortfolioRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio-simulation")
public class PortfolioController {
    @Autowired
    private PortfolioService portfolioService;
    @PostMapping("/simulate")
    public SimulationResult simulatePortfolio(@RequestBody PortfolioRequest request) {
        request.updateWeights();
        return portfolioService.performSimulations(
                request.getInitialCapital(),
                request.getStockWeights(),
                request.getDaysToPredict(),
                request.getNumSimulations()
        );
    }
}
