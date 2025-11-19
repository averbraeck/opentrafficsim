package org.opentrafficsim.demo.mirova.scenariomanagement;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;

/**
 * Concrete simulation script used by ScenarioManager to run a scenario.
 *
 * It applies:
 *  - ScenarioParameters (seed, timings, GUI mode, etc.)
 *  - ScenarioOutputConfiguration (samplers, CSV output)
 */
public class ScenarioSimulationScript extends AbstractSimulationScriptBase {

    /** Scenario generator instance. */
    private final ScenarioGenerator scenario;
    /** Scenario parameters to apply. */
    private final ScenarioParameters parameters;
    /** Scenario output configuration to apply. */
    private final ScenarioOutputConfiguration outputConfig;

    /**
     *  Constructor.
     * @param scenario ScenarioGenerator
     * @param params ScenarioParameters
     * @param outputConfig ScenarioOutputConfiguration
     */
    public ScenarioSimulationScript(
            final ScenarioGenerator scenario,
            final ScenarioParameters params,
            final ScenarioOutputConfiguration outputConfig)
    {
        super("Scenario-" + scenario.scenarioName, "Scenario simulation runner");
        this.scenario = scenario;
        this.parameters = params;
        this.outputConfig = outputConfig;

        // Apply scenario parameters to the simulation base class
        applyParameters(params);
    }

    // ------------------------------------------------------------
    // Apply all scenario parameters to the script
    // ------------------------------------------------------------

    /**
     * Applies the given scenario parameters to this simulation script.
     * @param p ScenarioParameters
     */
    private void applyParameters(final ScenarioParameters p) {
//        if (p.getSeed() != null) setSeed(p.getSeed());
//        if (p.getStartTime() != null) setStartTime(p.getStartTime());
//        if (p.getWarmupTime() != null) setWarmupTime(p.getWarmupTime());
//        if (p.getSimulationTime() != null) setSimulationTime(p.getSimulationTime());
//        if (p.getHistoryTime() != null) setHistoryTime(p.getHistoryTime());
//
//        setGuiEnabled(p.isGuiEnabled());
//        setAutorun(!p.isGuiEnabled());    // autorun=headless
    }

    // ------------------------------------------------------------
    // Main entry point to scenario-specific setup
    // ------------------------------------------------------------

    /**
     * Sets up the simulation according to the scenario.
     * @param sim OtsSimulatorInterface
     * @return RoadNetwork
     * @throws Exception when setup fails
     */
    @Override
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim)
            throws Exception
    {
        return this.scenario.setupSimulation(sim, this.parameters, this.outputConfig);
    }

    // ------------------------------------------------------------
    // Optional: Sampling output after simulation end
    // ------------------------------------------------------------

    /**
     * Called when the simulation ends to write output data.
     */
    @Override
    protected void onSimulationEnd() {
        if (this.outputConfig != null) {
            this.outputConfig.writeAllOutputs();
        }
    }

    // GUI-only demo elements (typically not used in batch mode)
    /**
     * Sets up demo elements in the animation panel.
     * @param panel OtsAnimationPanel
     * @param net RoadNetwork
     */
    @Override
    protected void setupDemo(final OtsAnimationPanel panel, final RoadNetwork net) {
        // Nothing by default – scenarios may override
    }
}
