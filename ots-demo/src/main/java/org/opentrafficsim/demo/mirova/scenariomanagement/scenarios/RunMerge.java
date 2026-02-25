package org.opentrafficsim.demo.mirova.scenariomanagement.scenarios;

import java.io.File;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioGenerator;
import org.opentrafficsim.demo.mirova.scenariomanagement.ScenarioSimulationScript;

public class RunMerge
{
    public static void main(final String[] args) throws Exception {

        File outputDir = new File("D:\\Mitarbeitende\\gw2128\\repositories\\mirova\\output\\ots\\bodegraven");
        ScenarioGenerator scenario = new MergeScenario("MergeScenario");
        outputDir.mkdirs();
        scenario.setOutputDirectory(outputDir);
        ScenarioSimulationScript script = scenario.buildSimulationScript();
        script.setSimulationTime(Duration.instantiateSI(7200));
        script.setGuiEnabled(true);
        script.start();

    }
}
