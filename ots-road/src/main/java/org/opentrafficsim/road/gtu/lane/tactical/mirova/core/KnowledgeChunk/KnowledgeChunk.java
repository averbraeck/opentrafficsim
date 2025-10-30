package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;

/**
 * Abstract base class representing a declarative KnowledgeChunk in the cognitive driving model.
 * <p>
 * A KnowledgeChunk corresponds to an ACT-R "chunk" of declarative knowledge.
 * It provides mechanisms to:
 * <ul>
 *   <li>Evaluate whether this knowledge is currently applicable (context relevance).</li>
 *   <li>Compute an activation level representing how "available" or "salient" this knowledge is.</li>
 *   <li>Contribute to the overall driving desire (e.g. LMRS-style components: route following, overtaking, keeping right, cooperation).</li>
 * </ul>
 * Each KnowledgeChunk can be specialized for different behavioral components.
 * </p>
 */
public abstract class KnowledgeChunk
{
    /** Vehicle context. */
    protected final MirovaTacticalPlanner vehicle;

    /** Last computed desire. */
    protected Desire desire;

    /** Perception categories. */
    private final InfrastructurePerception infrastructurePerception;
    private final TrafficPerception trafficPerception;
    private final EgoPerception egoPerception;
    private final NeighborsPerception neighborsPerception;
    private final DirectDefaultSimplePerception directDefaultSimplePerception;

    /** Vehicle parameters. */
    private final Parameters parameters;

    /** Procedural knowledge: maneuver pattern factories associated with this chunk. */
    protected final List<Supplier<ManeuverPattern>> maneuverPatterns;

    /**
     * @param vehicle the vehicle owning this knowledge chunk
     * @throws OperationalPlanException when perception categories cannot be accessed
     */
    public KnowledgeChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        this.vehicle = vehicle;
        this.desire = Desire.zero();
        this.maneuverPatterns = new ArrayList<>();
        this.infrastructurePerception =
                vehicle.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        this.trafficPerception =
                vehicle.getPerception().getPerceptionCategory(TrafficPerception.class);
        this.egoPerception =
                vehicle.getPerception().getPerceptionCategory(EgoPerception.class);
        this.neighborsPerception =
                vehicle.getPerception().getPerceptionCategory(NeighborsPerception.class);
        this.directDefaultSimplePerception =
                vehicle.getPerception().getPerceptionCategory(DirectDefaultSimplePerception.class);
        this.parameters = vehicle.getGtu().getParameters();
    }

    // ----------------------------------------------------------------------
    // CORE COGNITIVE INTERFACE
    // ----------------------------------------------------------------------

    /**
     * Checks if this knowledge chunk is currently relevant given the driving context.
     * For example, knowledge about signalized intersections is not applicable on a motorway.
     * @return true if this knowledge is relevant in the current context
     * @throws ParameterException if a parameter lookup fails
     */
    public abstract boolean isApplicable() throws ParameterException;

    /**
     * Computes the contribution of this chunk to the current driving desires
     * (LMRS-style components for route following, overtaking, keeping right, cooperation, etc.).
     * @return a {@link Desire} object representing directional preferences
     * @throws ParameterException if parameters cannot be read
     * @throws OperationalPlanException
     * @throws NetworkException
     * @throws GtuException
     */
    public abstract Desire computeDesire() throws ParameterException, OperationalPlanException, GtuException, NetworkException;

 // ----------------------------------------------------------------------
    // Procedural knowledge interface
    // ----------------------------------------------------------------------

    /**
     * Returns the procedural patterns (factories) linked to this knowledge.
     * These can be requested by a higher-level tactical planner to decide which
     * maneuvers are available when this knowledge becomes active.
     * @return list of maneuver pattern suppliers
     */
    public List<Supplier<ManeuverPattern>> getManeuverPatterns() {
        return this.maneuverPatterns;
    }

    /**
     * Registers an additional maneuver pattern associated with this chunk.
     * @param patternFactory supplier that creates a fresh ManeuverPattern instance
     */
    public void addManeuverPattern(final Supplier<ManeuverPattern> patternFactory) {
        this.maneuverPatterns.add(patternFactory);
    }


    // ----------------------------------------------------------------------
    // ACCESSORS
    // ----------------------------------------------------------------------

    public MirovaTacticalPlanner getAbstractMirovaVehicle() { return this.vehicle; }

    public InfrastructurePerception getInfrastructurePerception() { return this.infrastructurePerception; }

    public TrafficPerception getTrafficPerception() { return this.trafficPerception; }

    public EgoPerception getEgoPerception() { return this.egoPerception; }

    public NeighborsPerception getNeighborsPerception() { return this.neighborsPerception; }

    public DirectDefaultSimplePerception getDirectDefaultSimplePerception() { return this.directDefaultSimplePerception; }

    public Parameters getParameters() { return this.parameters; }

    public Desire getDesire() { return this.desire; }

}
