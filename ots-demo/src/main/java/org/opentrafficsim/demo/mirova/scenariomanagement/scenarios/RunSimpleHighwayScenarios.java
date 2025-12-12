package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.io.File;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioGenerator;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioManager;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioParameters;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioSimulationScript;

public class RunSimpleHighwayScenarios {

    /** Main method to run simple highway scenarios.
     */
    public static void main(final String[] args) throws Exception {

        File outputDir = new File("D:\\Mitarbeitende\\gw2128\\repositories\\mirova\\output\\ots\\simple_highway");
//        ScenarioManager manager = new ScenarioManager(outputDir);
//
//        // Create scenario instance
//        ScenarioGenerator scenario = new SimpleHighwayScenario();
//
//        // Register it
//        manager.addScenario("Highway", SimpleHighwayScenario.class);
//
//        // Add a parameter variation (here: a lower demand)
//        ScenarioParameters params1 = new ScenarioParameters();
//        params1.setDemand(2000);
//
//        ScenarioParameters params2 = new ScenarioParameters();
//        params2.setDemand(3000);
//
//        manager.addParameterVariation("Highway", params1);
//        manager.addParameterVariation("Highway", params2);
//
//        // Run each variation with 3 simulation runs (replications)
//        manager.setReplications(4);
//
//        // Run in parallel with 2 worker threads
//        manager.runAll(8, false);

        ScenarioGenerator scenario = new SimpleHighwayScenario();
        outputDir.mkdirs();
        scenario.setOutputDirectory(outputDir);
        ScenarioSimulationScript script = scenario.buildSimulationScript();
        script.setSimulationTime(Duration.instantiateSI(0.5 * 3600));
        script.setGuiEnabled(true);
        script.start();

    }
}
