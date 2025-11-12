package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;

/**
 * Abstract base class for all maneuver patterns in the Mirova tactical planner.
 * <p>
 * A {@code ManeuverPattern} represents a structured tactical or cooperative behavior schema
 * (e.g. lane change, merge, cooperative yield). Each concrete subclass defines:
 * <ul>
 *   <li>its type (free, tactical, cooperative, ...),</li>
 *   <li>the contextual conditions under which it may be applied,</li>
 *   <li>the ability checks verifying its feasibility, and</li>
 *   <li>the initial {@link ActionState} that starts the maneuver’s state machine.</li>
 * </ul>
 * </p>
 */
public abstract class ManeuverPattern {

    /** Classification of maneuver type used in the hierarchical selection logic. */
    public enum PatternType {
        FREE_LC,          // immediately executable lane change
        TACTICAL_LC,      // lane change requiring tactical preparation
        COOPERATIVE,      // cooperative or yielding maneuver
    }

    /** The initial action state that starts the state machine of this maneuver. */
    protected ActionState initialActionState;

    /** Context categories required for this pattern to be applicable. */
    protected final Set<String> requiredContextKeys = new HashSet<>();

    /** Knowledge chunk providing perception and tactical context information. */
    protected final KnowledgeChunk knowledgeChunk;

    /** The high-level category of this maneuver pattern. */
    protected final PatternType type;
    /** Reference to the ego vehicle executing this maneuver. */
    protected MirovaTacticalPlanner vehicle;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    protected ManeuverPattern(final PatternType type, final KnowledgeChunk knowledgeChunk) {
        this.type = type;
        this.knowledgeChunk = knowledgeChunk;
        this.vehicle = knowledgeChunk.getMirovaTacticalPlanner();
    }

    // ----------------------------------------------------------------------
    // Abstract evaluation interface
    // ----------------------------------------------------------------------

    /**
     * Checks whether the current contextual situation permits this maneuver pattern
     * (e.g. road type, congestion state, automation level, etc.).
     * <p>
     * Implementations typically query the {@code VehicleContextManager} or similar
     * global context data structures.
     * </p>
     *
     * @return {@code true} if the pattern is suitable in the current context
     * @throws ParameterException if perception parameters cannot be retrieved
     */
    public abstract boolean checkContext() throws ParameterException;

    /**
     * Verifies whether the maneuver can currently be executed from a physical and
     * safety perspective (e.g. gap availability, rule compliance, relative speeds).
     *
     * @return {@code true} if the maneuver is currently feasible
     * @throws ParameterException if perception parameters cannot be retrieved
     */
    public abstract boolean checkAbility() throws ParameterException;

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    /** Returns the set of context categories required by this pattern. */
    public Set<String> getRequiredContextKeys() {
        return this.requiredContextKeys;
    }


    /**
     * Returns the initial action state for this maneuver pattern. This state is used by
     * the tactical planner to start the pattern's internal state machine.
     *
     * @return initial {@link ActionState} of this maneuver
     */
    public ActionState getInitialActionState() {
        return this.initialActionState;
    }

    /**
     * Returns the {@link KnowledgeChunk} associated with this pattern.
     * The chunk provides access to perception categories and parameters relevant
     * for tactical decision-making.
     *
     * @return the associated {@link KnowledgeChunk}
     */
    public KnowledgeChunk getKnowledgeChunk() {
        return this.knowledgeChunk;
    }

    /**
     * Returns the type classification of this maneuver pattern.
     *
     * @return the {@link PatternType}
     */
    public PatternType getType() {
        return this.type;
    }
}
