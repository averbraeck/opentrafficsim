package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePatternOld.ActionStateCompleteLaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;


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
        FREE_LC,            // immediately executable lane change
        TACTICAL_LC,        // lane change requiring tactical preparation
        COOPERATIVE,        // cooperative or yielding maneuver
        EXCLUSIVE,          // exclusive maneuvers
        PARALLEL            // parallel maneuvers
    }

    /** The initial action state that starts the state machine of this maneuver. */
    protected Supplier<ActionState> initialActionState;

    /** The current action state of this maneuver pattern. */
    protected ActionState currentActionState;

    /** Context categories required for this pattern to be applicable. */
    protected final Set<String> requiredContextKeys = new HashSet<>();

    /** The high-level category of this maneuver pattern. */
    protected final PatternType type;
    /** Reference to the ego vehicle executing this maneuver. */
    protected MirovaTacticalPlanner vehicle;

    /** Pattern-specific timestep. Defaults to global DT, but can be customized */
    protected Duration patternSpecificTimestep = null;

    protected Boolean isRunning = false;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    protected ManeuverPattern(final PatternType type, final MirovaTacticalPlanner vehicle) {
        this.type = type;
        this.vehicle = vehicle;
        try
        {
            this.patternSpecificTimestep = this.vehicle.getParameters().getParameter(ParameterTypes.DT);
        }
        catch (ParameterException exception)
        {
            exception.printStackTrace();
        }

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


    /**
     * Updates the internal state machine of this maneuver pattern.
     *
     * @return the updated {@link SimpleOperationalPlan}
     * @throws ParameterException if perception parameters cannot be retrieved
     * @throws NullPointerException if an expected object is null
     * @throws IllegalArgumentException if an argument is invalid
     * @throws GtuException if GTU-related errors occur
     * @throws NetworkException if network-related errors occur
     */
    public SimpleOperationalPlan update() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
        if (this.isRunning()==false) {
            this.setCurrentActionState(this.initialActionState.get());
        }
        return this.currentActionState.update();
    }


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
        return this.initialActionState.get();
    }

    /**
     * Returns the current action state of this maneuver pattern.
     *
     * @return current {@link ActionState}
     */
    public ActionState getCurrentActionState() {
        return this.currentActionState;
    }

    /**
     * Sets the current action state of this maneuver pattern.
     *
     * @param actionState the new current {@link ActionState}
     */
    public void setCurrentActionState(final ActionState actionState) {
        this.currentActionState = actionState;
    }

    /**
     * Resets the current action state to the initial action state.
     */
    public void resetCurrentActionState() {
        this.currentActionState = this.initialActionState.get();
    }


    /**
     * Returns the pattern-specific timestep.
     * If not explicitly set, this defaults to the global simulation timestep set in the vehicle parameters.
     *
     * @return the pattern-specific {@link Duration} timestep
     */
    public Duration getPatternSpecificTimestep()
    {
        return this.patternSpecificTimestep;
    }

    /**
     * Returns whether this maneuver pattern is currently running.
     *
     * @return true if running, false otherwise
     */
    public Boolean isRunning() {
        return this.isRunning;
    }
    /**
     * Sets whether this maneuver pattern is currently running.
     *
     * @param running true if running, false otherwise
     */
    public void setRunning(final Boolean running) {
        this.isRunning = running;
    }


    public MirovaTacticalPlanner getMirovaTacticalPlanner() {
        return this.vehicle;
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
