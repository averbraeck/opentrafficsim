package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeLaneChange;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the final state of a cooperative lane change maneuver.
 *
 * This state marks the completion of the cooperative lane change. It resets the relevant
 * flags and ensures that the maneuver is finalized, allowing the vehicle to return to its
 * normal driving behavior.
 *
 * Responsibilities:
 * - Finalize the cooperative lane change maneuver.
 * - Reset the running_maneuver flag to indicate the maneuver is complete.
 *
 * Methods:
 * - executeControl(): Placeholder for any final control logic (currently does nothing).
 * - next(): Finalizes the maneuver and resets the active state.
 *
 * Attributes:
 * - drivingTask: The driving task associated with the cooperative lane change maneuver.
 *
 * Transitions:
 * - None (this is the end state of the cooperative lane change maneuver).
 */
public class EndCooperativeLanechangeManeuver extends StartCooperativeLanechangeManeuver {

    public EndCooperativeLanechangeManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        // No-op
    }

    @Override
    public void next() {
        this.drivingTask.getContextVehicle().setRunningManeuver(false);
    }
}
