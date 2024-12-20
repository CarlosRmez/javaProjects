document.addEventListener("DOMContentLoaded", () => {
    const stockTicker = document.getElementById("stockTicker");
    const stockWeight = document.getElementById("stockWeight");
    const addStockButton = document.getElementById("addStockButton");
    const selectedStocksList = document.getElementById("selectedStocks");
    const runSimulationButton = document.getElementById("runSimulationButton");
    const initialCapitalInput = document.getElementById("initialCapital");
    const daysToPredictInput = document.getElementById("daysToPredict");
    const numSimulationsInput = document.getElementById("numSimulations");
    const sequentialMeanFinalValue = document.getElementById("sequentialMeanFinalValue");
    const sequentialStandardDeviation = document.getElementById("sequentialStandardDeviation");
    const sequentialExecutionTime = document.getElementById("sequentialExecutionTime");
    const parallelMeanFinalValue = document.getElementById("parallelMeanFinalValue");
    const parallelStandardDeviation = document.getElementById("parallelStandardDeviation");
    const parallelExecutionTime = document.getElementById("parallelExecutionTime");
    const chartCtx = document.getElementById("chart").getContext("2d");

    const selectedStocks = {};
    let chart;

    addStockButton.addEventListener("click", () => {
        const ticker = stockTicker.value;
        const weight = parseFloat(stockWeight.value);

        if (!ticker || isNaN(weight) || weight <= 0 || weight > 100) {
            alert("Please enter a valid stock weight between 0 and 100.");
            return;
        }

        selectedStocks[ticker] = weight;

        renderSelectedStocks();
    });

    const renderSelectedStocks = () => {
        selectedStocksList.innerHTML = "";
        for (const [ticker, weight] of Object.entries(selectedStocks)) {
            const li = document.createElement("li");
            li.textContent = `${ticker}: ${weight}%`;
            selectedStocksList.appendChild(li);
        }
    };

    const updateChart = (chartCtx, simulations) => {
        const colorPalette = [
            "#FF5733", "#33FF57", "#3357FF", "#F3FF33", "#FF33F3",
            "#33FFF3", "#FF8C33", "#8C33FF", "#33FF8C", "#FF338C",
        ];

        const datasets = simulations.map((simulation, index) => ({
            data: simulation,
            borderColor: colorPalette[index % colorPalette.length],
            borderWidth: 2,
            tension: 0.4,
            pointRadius: 0,
            hoverRadius: 3,
        }));

        return new Chart(chartCtx, {
            type: "line",
            data: {
                labels: Array.from({ length: simulations[0].length }, (_, i) => i + 1),
                datasets: datasets,
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                },
                scales: {
                    x: { title: { display: true, text: "Days" } },
                    y: { title: { display: true, text: "Portfolio Value" } },
                },
            },
        });
    };

    runSimulationButton.addEventListener("click", async (event) => {
        event.preventDefault();

        const initialCapital = parseFloat(initialCapitalInput.value);
        const daysToPredict = parseInt(daysToPredictInput.value);
        const numSimulations = parseInt(numSimulationsInput.value);

        if (isNaN(initialCapital) || isNaN(daysToPredict) || isNaN(numSimulations)) {
            alert("Please fill out all fields with valid numbers.");
            return;
        }

        const stockWeights = selectedStocks;

        if (Object.keys(stockWeights).length === 0) {
            alert("Please add at least one stock.");
            return;
        }

        const requestData = { initialCapital, stockWeights, daysToPredict, numSimulations };

        try {
            const response = await fetch("http://localhost:8080/portfolio-simulation/simulate", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                throw new Error("Failed to fetch simulation results.");
            }

            const result = await response.json();

            sequentialMeanFinalValue.textContent = result.sequentialMean.toFixed(2);
            sequentialStandardDeviation.textContent = result.sequentialStdDev.toFixed(2);
            sequentialExecutionTime.textContent = result.sequentialExecutionTime;

            parallelMeanFinalValue.textContent = result.parallelMean.toFixed(2);
            parallelStandardDeviation.textContent = result.parallelStdDev.toFixed(2);
            parallelExecutionTime.textContent = result.parallelExecutionTime;

            if (chart) chart.destroy();

            chart = updateChart(chartCtx, result.allSimulations.slice(0, Math.min(30, result.allSimulations.length)));
        } catch (error) {
            alert(`An error occurred: ${error.message}`);
        }
    });
});
