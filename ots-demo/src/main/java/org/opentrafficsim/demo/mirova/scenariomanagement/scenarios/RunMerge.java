package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.io.File;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioGenerator;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioParameters;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioSimulationScript;

public class RunMerge
{
    public static void main(final String[] args) throws Exception
    {

        for (int run = 0; run < 1; run++)
        {
            System.out.println("Starting simulation run " + (run + 1) + " of 10...");
            File outputDir = new File(
                    "D:\\Mitarbeitende\\gw2128\\repositories\\mirova\\output\\ots\\bodegraven\\lmrs\\run_" + (run + 1));
            ScenarioGenerator scenario = new MergeScenario();
            outputDir.mkdirs();
            scenario.setOutputDirectory(outputDir);
            ScenarioParameters params = new ScenarioParameters();
            params.setSeed(42 + run); // Base seed + run index for different seeds
            params.setSimulationTime(new Duration(2.0, DurationUnit.HOUR));
            params.setTruckShare(0.1); // 10% trucks
            params.setMergeShare(0.2); // 20% on-ramp demand
            ScenarioSimulationScript script = scenario.buildSimulationScript(params);
            script.setGuiEnabled(true);
            script.start();
        }
    }
}
