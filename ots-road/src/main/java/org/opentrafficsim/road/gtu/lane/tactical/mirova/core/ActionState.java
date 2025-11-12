package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Abstract base class representing an executable action state within a maneuver pattern.
 * <p>
 * Action states define concrete, time-continuous vehicle behavior during a specific
 * phase of a maneuver (e.g., preparation, lane change, completion).
 * Each ActionState is responsible for:
 * <ul>
 *   <li>executing control logic (e.g., car-following, gap maintenance) by returning a SimpleOperationalPlan</li>
 *   <li>evaluating transition conditions to the next state</li>
 *   <li>detecting abort conditions (if the maneuver is no longer feasible)</li>
 * </ul>
 * </p>
 */
public abstract class ActionState {

    /** Reference to the parent maneuver pattern. */
    protected final ManeuverPattern maneuverPattern;

    /** Associated vehicle (retrieved from the maneuver’s knowledge chunk). */
    protected final MirovaTacticalPlanner vehicle;

    /** Indicates whether this state is currently active. */
    protected boolean active = false;

    /** Optional cached operational plan for the current time step. */
    protected SimpleOperationalPlan operationalPlan;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    public ActionState(final ManeuverPattern maneuverPattern) {
        this.maneuverPattern = maneuverPattern;
        this.vehicle = maneuverPattern.getKnowledgeChunk().getMirovaTacticalPlanner();
    }

    // ----------------------------------------------------------------------
    // Core execution cycle
    // ----------------------------------------------------------------------

    /**
     * Executes a full update step for this state:
     * <ol>
     *   <li>Performs control logic via {@link #executeControl()}</li>
     *   <li>Checks transitions via {@link #next()}</li>
     *   <li>Checks abort conditions via {@link #abort()}</li>
     * </ol>
     * @return the resulting operational plan for this time step
     * @throws NetworkException
     * @throws GtuException
     */
    public SimpleOperationalPlan update()
            throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {

        this.vehicle.setRunningManeuver(true);

        // 1. Abort check
        this.operationalPlan = this.abort();

        if (this.operationalPlan != null) {
            return this.operationalPlan;
        }

        // 2. Transition check
        this.operationalPlan = this.next();
        if (this.operationalPlan != null) {
            return this.operationalPlan;
        }

        // 3. Execute control logic (produces operational plan)
        this.operationalPlan = this.executeControl();

        return this.operationalPlan;
    }

    // ----------------------------------------------------------------------
    // Abstract responsibilities
    // ----------------------------------------------------------------------

    /**
     * Executes the vehicle control logic for this action state.
     * <p>
     * Example: car-following, cooperative adaptation, or lane-change execution.
     * </p>
     * @return operational plan representing the control output for this step
     * @throws NetworkException
     * @throws GtuException
     */
    public abstract SimpleOperationalPlan executeControl()
            throws ParameterException, OperationalPlanException, GtuException, NetworkException;

    /**
     * Checks transition conditions to the next action state.
     * Should call {@link #transitionTo(ActionState)} if a transition occurs.
     * @throws NetworkException
     * @throws GtuException
     */
    public abstract SimpleOperationalPlan next()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException;

    /**
     * Checks whether this state should be aborted (e.g. if the maneuver became infeasible).
     */
    public abstract SimpleOperationalPlan abort()
            throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException;

    // ----------------------------------------------------------------------
    // Helper and lifecycle methods
    // ----------------------------------------------------------------------

    /**
     * Transitions to the specified next state.
     * @param nextState the new active state
     * @return
     * @throws NetworkException
     * @throws GtuException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     */
    protected SimpleOperationalPlan transitionTo(final ActionState nextState) throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
        this.active = false;
        nextState.active = true;
        this.vehicle.setCurrentActionState(nextState);
        return nextState.update();
    }

    /** Returns whether this state is currently active.
     * @return */
    public boolean isActive() {
        return this.active;
    }

    /** Returns the vehicle executing this action.
     * @return */
    public MirovaTacticalPlanner getVehicle() {
        return this.vehicle;
    }

    /** Returns the parent maneuver pattern.
     * @return */
    public ManeuverPattern getManeuverPattern() {
        return this.maneuverPattern;
    }
}
