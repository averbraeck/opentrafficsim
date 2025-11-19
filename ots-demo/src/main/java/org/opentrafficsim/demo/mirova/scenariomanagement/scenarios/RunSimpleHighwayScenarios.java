package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.io.File;

import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioGenerator;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioManager;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioParameters;

public class RunSimpleHighwayScenarios {

    public static void main(final String[] args) throws Exception {

        String outputDir = "D:\\Mitarbeitende\\gw2128\\repositories\\mirova\\output\\ots\\simple_highway";
        ScenarioManager manager = new ScenarioManager(new File(outputDir));

        // Create scenario instance
        ScenarioGenerator scenario = new SimpleHighwayScenario();

        // Register it
        manager.addScenario("Highway", scenario);

        // Add a parameter variation (here: a lower demand)
        ScenarioParameters params1 = new ScenarioParameters();
        params1.setSeed(10);

        ScenarioParameters params2 = new ScenarioParameters();
        params2.setSeed(20);

        manager.addParameterVariation("Highway", params1);
        manager.addParameterVariation("Highway", params2);

        // Run each variation with 3 simulation runs (replications)
        manager.setReplications(3);

        // Run in parallel with 2 worker threads
        manager.runAll(2);
    }
}
