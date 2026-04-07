package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.io.File;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioManager;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioParameters;

/**
 * Runner class to execute ten parallel simulations of the MergeScenario with varying seeds.
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class RunParallelMergeScenarios
{

    /**
     * Main execution method.
     * * @param args String[]; command line arguments
     */
    public static void main(final String[] args)
    {
        try
        {
            // 1. Define the root output directory for the simulation results
            File outputDirectory = new File("D:\\Mitarbeitende\\gw2128\\repositories\\mirova\\output\\ots\\bodegraven\\parallel_lmrs");

            // 2. Initialize the ScenarioManager
            ScenarioManager scenarioManager = new ScenarioManager(outputDirectory);

            // 3. Register the MergeScenario class under a unique name
            String scenarioName = "MergeScenario_10Runs";
            scenarioManager.addScenario(scenarioName, MergeScenario.class);

            // 4. Define the base parameters
            // The ScenarioManager will automatically add the run index to the base seed
            ScenarioParameters baseParameters = new ScenarioParameters();
            baseParameters.setSeed(100L);       // Initial base seed
            //baseParameters.setDemand(4500.0);   // Base demand in veh/h
            baseParameters.setTruckShare(0.1);  // 10% trucks
            baseParameters.setMergeShare(0.2);  // 20% on-ramp demand
            baseParameters.setSimulationTime(new Duration(2.0, DurationUnit.HOUR));
            // Add this parameter configuration to the scenario
            scenarioManager.addParameterVariation(scenarioName, baseParameters);

            // 5. Set the number of replications (this ensures 10 runs with different seeds)
            int numberOfReplications = 10;
            scenarioManager.setReplications(numberOfReplications);


            // 6. Run all registered scenarios in parallel
            // Parameter 1: Number of parallel threads (10 threads for 10 runs)
            // Parameter 2: enableGUI (set to false for faster, headless execution)
            int parallelThreads = 10;
            boolean enableGUI = false;

            System.out.println("Starting 10 parallel replications of MergeScenario...");
            scenarioManager.runAll(parallelThreads, enableGUI);

        }
        catch (Exception exception)
        {
            System.err.println("An error occurred during the parallel scenario execution:");
            exception.printStackTrace();
        }
    }
}