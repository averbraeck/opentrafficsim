package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;

/**
 * Abstract base class representing an executable action state within a maneuver pattern.
 * <p>
 * Action states define concrete, time-continuous vehicle behavior during a specific phase of a maneuver (e.g., preparation,
 * lane change, completion). They form the atomic units of the Finite State Machine (FSM) representing procedural knowledge in
 * <b>Layer 4 (Procedure & Action)</b> of the MiRoVA architecture.
 * </p>
 * <p>
 * Each ActionState is responsible for:
 * <ul>
 * <li>Executing control logic (e.g., car-following, gap maintenance) by returning a {@link SimpleOperationalPlan}</li>
 * <li>Evaluating transition conditions to the next state</li>
 * <li>Detecting abort conditions (if the maneuver is no longer feasible)</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public abstract class ActionState
{

    /** Reference to the parent maneuver pattern. */
    protected final ManeuverPattern maneuverPattern;

    /** Associated vehicle (retrieved from the maneuver's knowledge chunk). */
    protected final MirovaTacticalPlanner vehicle;

    /** Indicates whether this state is currently active. */
    protected boolean active = false;

    /** Optional cached operational plan for the current time step. */
    protected SimpleOperationalPlan operationalPlan;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Initializes a new ActionState for the given maneuver pattern. Sets this state as the currently active action state in the
     * pattern.
     * @param maneuverPattern the parent maneuver pattern this state belongs to
     */
    public ActionState(final ManeuverPattern maneuverPattern)
    {
        this.maneuverPattern = maneuverPattern;
        this.vehicle = maneuverPattern.getMirovaTacticalPlanner();
        this.maneuverPattern.setCurrentActionState(this);
        this.maneuverPattern.setRunning(true);
    }

    // ----------------------------------------------------------------------
    // Core execution cycle
    // ----------------------------------------------------------------------

    /**
     * Executes a full update step for this state:
     * <ol>
     * <li>Checks abort conditions via {@link #abort()}</li>
     * <li>Checks transitions via {@link #next()}</li>
     * <li>Performs control logic via {@link #executeControl()}</li>
     * </ol>
     * * @return the resulting operational plan for this time step
     * @throws ParameterException if a parameter required for control logic cannot be found
     * @throws NullPointerException if required contextual data is missing
     * @throws IllegalArgumentException if invalid arguments are passed during plan generation
     * @throws GtuException if an error occurs within the GTU state
     * @throws NetworkException if a network-related error occurs during lookup
     */
    public SimpleOperationalPlan update()
            throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
    {

        // 1. Abort check
        this.operationalPlan = this.abort();
        if (this.operationalPlan != null)
        {
            return this.operationalPlan;
        }

        // 2. Transition check
        this.operationalPlan = this.next();
        if (this.operationalPlan != null)
        {
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
     * * @return the operational plan representing the control output for this step
     * @throws ParameterException if a parameter required for control logic cannot be found
     * @throws OperationalPlanException if the generation of the operational plan fails
     * @throws GtuException if an error occurs within the GTU state
     * @throws NetworkException if a network-related error occurs
     */
    public abstract SimpleOperationalPlan executeControl()
            throws ParameterException, OperationalPlanException, GtuException, NetworkException;

    /**
     * Checks transition conditions to the next action state. Should call {@link #transitionTo(ActionState)} if a transition
     * occurs. * @return an operational plan if a transition occurred, or null if the state remains active
     * @throws OperationalPlanException if the generation of the operational plan fails
     * @throws ParameterException if parameter retrieval fails during transition checks
     * @throws NullPointerException if required contextual data is missing
     * @throws IllegalArgumentException if invalid arguments are passed during plan generation
     * @throws GtuException if an error occurs within the GTU state
     * @throws NetworkException if a network-related error occurs
     */
    public abstract SimpleOperationalPlan next() throws OperationalPlanException, ParameterException, NullPointerException,
            IllegalArgumentException, GtuException, NetworkException;

    /**
     * Checks whether this state should be aborted (e.g., if the maneuver became infeasible). * @return an operational plan if
     * the maneuver was aborted, or null if execution can continue
     * @throws ParameterException if parameter retrieval fails during abort checks
     * @throws OperationalPlanException if the generation of the operational plan fails
     * @throws NullPointerException if required contextual data is missing
     * @throws IllegalArgumentException if invalid arguments are passed
     * @throws GtuException if an error occurs within the GTU state
     * @throws NetworkException if a network-related error occurs
     */
    public abstract SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException, NullPointerException,
            IllegalArgumentException, GtuException, NetworkException;

    /**
     * Calculates and returns the utility of this specific action state.
     * <p>
     * The utility represents the motivation or fitness of the maneuver at this exact stage. Higher values indicate a stronger
     * need or better suitability to execute or continue this state compared to concurrently proposed plans. This score is
     * utilized by the tactical planner's arbitration layer.
     * </p>
     * * @return double; the evaluated utility score for this specific action state
     */
    public double getUtility()
    {
        // Default implementation returns a neutral utility score.
        // Subclasses should override this method to provide context-specific utility evaluations.
        return 0.0;
    }

    // ----------------------------------------------------------------------
    // Helper and lifecycle methods
    // ----------------------------------------------------------------------

    /**
     * Transitions to the specified next state. * @param nextState the new active action state to transition into
     * @return the operational plan generated by the new state's update cycle
     * @throws ParameterException if parameter retrieval fails in the new state
     * @throws NullPointerException if required contextual data is missing in the new state
     * @throws IllegalArgumentException if invalid arguments are passed in the new state
     * @throws GtuException if an error occurs within the GTU state
     * @throws NetworkException if a network-related error occurs
     */
    protected SimpleOperationalPlan transitionTo(final ActionState nextState)
            throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException
    {
        this.vehicle.releaseActionLock();
        this.active = false;
        nextState.active = true;
        this.vehicle.setCurrentActionState(nextState);
        return nextState.update();
    }

    /**
     * Finalizes the maneuver, resetting vehicle state and stopping the maneuver pattern. * @return an operational plan resuming
     * normal driving behavior (e.g., car-following)
     * @throws ParameterException if required parameters cannot be retrieved
     * @throws GtuException if an error occurs within the GTU state
     * @throws NetworkException if a network-related error occurs
     */
    protected SimpleOperationalPlan finishManeuver() throws ParameterException, GtuException, NetworkException
    {
        this.vehicle.releaseActionLock();
        this.maneuverPattern.setRunning(false);
        EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);
        return new SimpleOperationalPlan(egoCtx.getCurrentCarFollowingAcceleration(),
                this.maneuverPattern.getPatternSpecificTimestep());
    }

    /**
     * Returns whether this state is currently active. * @return true if active, false otherwise
     */
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * Returns the vehicle executing this action. * @return the MiRoVA tactical planner associated with the vehicle
     */
    public MirovaTacticalPlanner getVehicle()
    {
        return this.vehicle;
    }

    /**
     * Returns the parent maneuver pattern orchestrating this state. * @return the parent maneuver pattern
     */
    public ManeuverPattern getManeuverPattern()
    {
        return this.maneuverPattern;
    }
}
