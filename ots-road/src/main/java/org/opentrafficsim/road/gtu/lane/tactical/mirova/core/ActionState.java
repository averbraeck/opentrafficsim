package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;
//package mirova.scripts.model.prototype.abstract_classes;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

public abstract class ActionState {
    /**
     * Abstract base class representing an action state in a driving task.
     *
     * This class serves as the foundation for implementing specific action states within a
     * driving task. Action states define the behavior of a vehicle during a specific phase
     * of a maneuver, such as acceleration, lane changes, or merging. Each action state is
     * responsible for controlling the vehicle, updating its state, and managing transitions
     * to subsequent states.
     *
     * Responsibilities:
     * - Execute control logic to adjust the vehicle's behavior (e.g., speed, position).
     * - Update the state at each simulation timestep or customized discretization.
     * - Check and handle transitions to subsequent action states.
     * - Abort the current state if the maneuver is no longer appropriate.
     *
     * Attributes:
     * - drivingTask: The driving task associated with this action state.
     * - active: Indicates whether the action state is currently active.
     */

    protected DrivingTask drivingTask;
    private SimpleOperationalPlan operationalPlan;

    public ActionState(final DrivingTask drivingTask) {
        this.drivingTask = drivingTask;

    }

    /**
     * Executes the control logic for the action state.
     * This method adjusts the vehicle's behavior, such as speed or lateral position,
     * based on the logic defined in the specific action state.
     * @throws OperationalPlanException
     * @throws ParameterException
     */
    public abstract SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException;

    /**
     * Updates the ActionState at each simulation timestep or customized discretization.
     * This method performs the following steps:
     * - Updates the vehicle's state in the simulation by setting the current action state.
     * - Checks for transitions to subsequent action states using the next method.
     * - Checks if the current maneuver is still appropriate using the abort method.
     * - Executes the control logic for the current state using the executeControl method.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    public SimpleOperationalPlan update() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException {
        this.drivingTask.getAbstractMirovaVehicle().setRunningManeuver(true);
        this.next();
        this.abort();
        return this.executeControl();
    }

    /**
     * Checks whether the conditions for transitioning to the next action state are met.
     * If the transition conditions are satisfied, this method triggers the transition
     * to the subsequent action state.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    public abstract void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException;

    /**
     * Checks whether the current maneuver is still appropriate.
     * If the conditions for continuing the current maneuver are no longer valid,
     * this method handles the logic for aborting the current state.
     * @throws ParameterException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws OperationalPlanException
     */
    public abstract void abort() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException;
}