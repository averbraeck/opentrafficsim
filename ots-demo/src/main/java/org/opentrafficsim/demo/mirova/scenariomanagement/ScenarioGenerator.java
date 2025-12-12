package org.opentrafficsim.demo.mirova.scenariomanagement;

import java.util.Map;
import java.util.Set;
import java.io.File;
import java.lang.reflect.Parameter;
import java.security.Policy.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuTemplate;
import org.opentrafficsim.road.gtu.generator.headway.HeadwayGenerator;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.LoopDetector;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.od.OdMatrix;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * ScenarioGenerator
 * -----------------
 *
 * Abstract base class that encapsulates all components required to fully
 * define a simulation scenario in OTS/Mirova. A concrete scenario defines:
 *
 *  - The road network
 *  - GTU templates (car, truck, etc.)
 *  - Desired speed distributions per vehicle type
 *  - Route definitions
 *  - Demand definitions (OD matrix or direct generators)
 *  - Traffic sampling configuration
 *  - All simulation parameters relevant for this scenario
 *
 * A ScenarioManager will request all scenario components and run multiple
 * replications with different random seeds.
 */
public abstract class ScenarioGenerator {

    /** Human-readable name of the scenario. */
    protected final String scenarioName;

    /** map as container for routes for this scenario (keyed by route name). */
    protected Map<String, Route> routes = new HashMap<>();

    /** GTU templates for this scenario (keyed by GTU type). */
    protected Map<GtuType, LaneBasedGtuTemplate> gtuTemplates = new HashMap<>();

    /** OD matrix for demand definition. */
    protected OdMatrix odMatrix = null;

    /** Headway generator for direct generation (alternative to OD matrix). */
    protected HeadwayGenerator headwayGenerator = null;

    /** The road network for this scenario. */
    protected RoadNetwork network = null;


    /** Random stream for this scenario. */
    protected StreamInterface stream = null;

    /** Initial longitudinal positions for generated GTUs (optional). */
    protected Set<LanePosition> initialLongitudinalPositions = new LinkedHashSet<>();

    /** All lanes in the network (cached after network creation). */
    protected List<Lane> listAllLanes = new ArrayList<>();

    /** Road samplers for data collection in this scenario. */
    protected List<RoadSampler> listRoadSamplers = new ArrayList<>();

    /** Default parameters for this scenario. */
    protected ScenarioParameters defaultParameters = new ScenarioParameters();

    /** Output configuration for this scenario. */
    protected ScenarioOutputConfiguration outputConfiguration = new ScenarioOutputConfiguration();

    /** Loop detectors in this scenario. */
    protected List<LoopDetector> listLoopDetectors = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name scenario name
     */
    protected ScenarioGenerator(final String name) {
        this.scenarioName = name;
        setDefaultParameters();
    }

    /** Returns the scenario name. */
    public String getScenarioName() {
        return this.scenarioName;
    }

    // ----------------------------------------------------------------------
    // Network definition
    // ----------------------------------------------------------------------

    /**
     * Builds the road network for this scenario.
     * Implementations should define all relevant roads, nodes, and lanes required for the simulation.
     *
     * @param sim the OTS simulator instance to use
     * @throws Exception if network creation fails
     */
    public abstract void buildNetwork(OtsSimulatorInterface sim) throws Exception;

    /**
     * Initializes the simulation with the given simulator using default parameters and output configuration.
     *
     * @param sim the OTS simulator instance
     * @return the initialized RoadNetwork object
     * @throws Exception if initialization fails
     */
    public RoadNetwork setupSimulation(final OtsSimulatorInterface sim) throws Exception {
        return setupSimulation(sim, this.defaultParameters);
    }


    /**
     * Initializes the simulation with the given simulator, scenario parameters, and output configuration.
     * This method should build the network, configure all relevant components, and prepare the simulation to start.
     *
     * @param sim the OTS simulator instance
     * @param params scenario-specific parameters, e.g., simulation duration or vehicle parameters
     * @param output configuration for output data to be collected
     * @return the initialized RoadNetwork object
     * @throws Exception if initialization fails
     */
    public abstract RoadNetwork setupSimulation(
            OtsSimulatorInterface sim,
            ScenarioParameters params
            ) throws Exception;


    // ----------------------------------------------------------------------
    // GTU Templates (vehicle types, desired speed distributions, routes)
    // ----------------------------------------------------------------------

    /**
     * Builds the GTU templates (vehicle types) for generation.
     * Implementations should define all required vehicle types, their properties, and distributions.
     *
     * @param sim the OTS simulator instance
     * @throws Exception if template creation fails
     */
    public abstract void buildGtuTemplates(OtsSimulatorInterface sim) throws Exception;

    /**
     * Creates the route definitions for the scenario.
     * This method should define all available routes in the network, used for vehicle generation or OD matrix.
     *
     * @throws Exception if route creation fails
     */
    public abstract void buildRoutes() throws Exception;

    // ----------------------------------------------------------------------
    // Demand definitions
    // ----------------------------------------------------------------------

    /**
     * Defines the demand using an OD matrix.
     * Implementations can create an OD matrix describing traffic flows between origin and destination nodes.
     * If not used, this method can return null.
     *
     * @param sim the OTS simulator instance
     * @throws Exception if OD matrix creation fails
     */
    public void buildOdMatrix(final OtsSimulatorInterface sim) throws Exception {
    }

    /**
     * Defines a direct headway generator as an alternative to the OD matrix.
     * Implementations can configure a generator that directly determines vehicle headways.
     */
    public void buildHeadwayGenerator() {
    }

    /**
     * Builds the road samplers for data collection in this scenario.
     * Implementations should define all relevant sampling points and metrics to be collected during simulation.
     * Gets added to the listRoadSamplers attribute.
     * @throws NetworkException if sampler creation fails
     */
    public void buildRoadSamplers() throws NetworkException {
    }


    /**
     * Returns the origin locations (nodes or lane positions) where vehicles can be generated.
     *
     * @param network the road network in which to search for origins
     * @return list of origin nodes or lane positions
     */
    public abstract List<Node> getOrigins(RoadNetwork network);

    /**
     * Returns the destination locations (nodes) to which vehicles can travel in the scenario.
     *
     * @param network the road network in which to search for destinations
     * @return list of destination nodes
     */
    public abstract List<Node> getDestinations(RoadNetwork network);


    /**
     * Returns all lanes in the network.
     * @return list of all lanes
     */
    public List<Lane> getAllLanes() {
        return this.listAllLanes;
    }

    // ----------------------------------------------------------------------
    // Output configuration
    // ----------------------------------------------------------------------

    /**
     * Configures the output data sampling for this scenario.
     * Implementations should define which data to sample, at what frequency, and any custom output writers.
     * @return output configuration for data sampling
     */
    public ScenarioOutputConfiguration buildOutputConfiguration() {
        this.outputConfiguration = new ScenarioOutputConfiguration();
        return this.outputConfiguration; // default: empty config
    }

    /**
     * @return
     */
    public ScenarioOutputConfiguration getOutputConfiguration() {
        return this.outputConfiguration;
    }
    // ----------------------------------------------------------------------
    // Parameter overrides (scenario-specific global parameters)
    // ----------------------------------------------------------------------

    /**
     * Returns the default parameters for this scenario.
     * @return scenario-specific parameters
     *
     */
    public ScenarioParameters getDefaultParameters()
    {
        return this.defaultParameters;
    }

    /**
     * Sets the output directory for this scenario.
     * @param outputDirectory directory to store output data
     */
    public void setOutputDirectory(final File outputDirectory) {
        this.outputConfiguration.setOutputDirectory(outputDirectory.getAbsolutePath());
    }

    /**
     * Returns the output directory for this scenario.
     * @return output directory
     */
    public File getOutputDirectory() {
        return new File(this.outputConfiguration.getOutputDirectory());
    }

    /**
     * Sets the default parameters for this scenario.
     * Implementations should define all relevant default parameters here.
     */
    public abstract void setDefaultParameters();

    /**
     * Builds the simulation script for this scenario with given parameters and output configuration.
     * @param params scenario-specific parameters
     * @param outputConfig output configuration for data sampling
     * @return simulation script instance
     */
    public ScenarioSimulationScript buildSimulationScript(
            final ScenarioParameters params
            )
    {
        return new ScenarioSimulationScript(this, params);
    }

    /**
     * Builds the simulation script for this scenario with default parameters and output configuration.
     * @return simulation script instance
     */
    public ScenarioSimulationScript buildSimulationScript()
    {
        return buildSimulationScript(this.defaultParameters);
    }
    /**
     * Starts a simple simulation for this scenario with given parameters and output directory.
     * @param params scenario-specific parameters
     * @param outputDirectory directory to store output data
     * @throws Exception if simulation fails to start
     */
    public void startSimpleSimulation(final ScenarioParameters params, final File outputDirectory) throws Exception {
        ScenarioSimulationScript script = buildSimulationScript(params);
        script.setOutputDirectory(outputDirectory.getAbsolutePath());
        script.setGuiEnabled(true);

        script.start();

    }

    /**
     * Starts a simple simulation for this scenario with default parameters and given output directory.
     * @param outputDirectory directory to store output data
     * @throws Exception if simulation fails to start
     */
    public void startSimpleSimulation(final File outputDirectory) throws Exception {
        startSimpleSimulation(this.defaultParameters, outputDirectory);
    }

    /**
     * Returns the list of loop detectors in this scenario.
     * @return list of loop detectors
     */
    public List<LoopDetector> getLoopDetectors() {
        return this.listLoopDetectors;
    }

    /**
     * Adds a loop detector to this scenario.
     * @param detector loop detector to add
     */
    public void addLoopDetector(final LoopDetector detector) {
        this.listLoopDetectors.add(detector);
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    @Override
    public String toString() {
        return "ScenarioGenerator[" + this.scenarioName + "]";
    }
}
