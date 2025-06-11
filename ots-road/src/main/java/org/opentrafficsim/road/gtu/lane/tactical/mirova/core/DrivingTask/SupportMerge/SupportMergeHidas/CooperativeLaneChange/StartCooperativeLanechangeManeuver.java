package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeLaneChange;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the initial state of a cooperative lane change maneuver.
 *
 * This state is responsible for initiating the cooperative lane change process. It prepares
 * the vehicle for the lane change by updating the operational values and transitioning to
 * the execution phase of the maneuver.
 *
 * Responsibilities:
 * - Initialize the cooperative lane change maneuver.
 * - Transition to the execute_cooperative_lanechange_maneuver state.
 *
 * Methods:
 * - executeControl(): Placeholder for controlling the vehicle's behavior during this state.
 * - update(): Updates the operational values required for the lane change.
 * - next(): Transitions to the execute_cooperative_lanechange_maneuver state.
 * - abort(): Handles the abort logic if the lane change maneuver is canceled.
 *
 * Attributes:
 * - drivingTask: The driving task associated with the cooperative lane change maneuver.
 *
 * Transitions:
 * - To execute_cooperative_lanechange_maneuver to begin the lane change execution.
 */
public class StartCooperativeLanechangeManeuver extends ActionState {

    public StartCooperativeLanechangeManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
    }

    @Override
    public void executeControl() {
        super.executeControl();
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void next() {
        this.drivingTask.getContextVehicle().setRunningManeuver(true);
        this.drivingTask.getContextVehicle().setLastExecutedActionState(
            new ExecuteCooperativeLanechangeManeuver(this.drivingTask)
        );
    }

    @Override
    public void abort() {
        super.abort();
    }
}
