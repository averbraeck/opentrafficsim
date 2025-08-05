package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge;

import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * Represents the final state of the free merge maneuver into the target gap.
 *
 * This state marks the completion of the merging process. It ensures that the vehicle
 * has successfully entered the target gap and finalizes the maneuver by resetting
 * relevant flags and stopping further state transitions.
 *
 * Responsibilities:
 * - Finalizes the merging maneuver.
 * - Marks the maneuver as completed by resetting the runningManeuver flag.
 *
 * Methods:
 * - executeControl(): Resets the runningManeuver flag to indicate the maneuver is complete.
 * - next(): Placeholder for any potential future transitions (currently does nothing).
 * - abort(): Placeholder for abort logic (currently does nothing).
 *
 * Attributes:
 * - drivingTask: The driving task associated with the merge maneuver.
 *
 * Transitions:
 * - None (this is the end state of the merging process).
 */
public class EndFreeMergeToTargetGap extends StartFreeMergeToTargetGap {

    public EndFreeMergeToTargetGap(final MergingHidas drivingTask) {
        super(drivingTask);
    }

    @Override
    public SimpleOperationalPlan executeControl() {
        this.drivingTask.getAbstractMirovaVehicle().setRunningManeuver(false);
        return null; // No operational plan needed in this state
    }

    @Override
    public void next() {
        // No further transitions
    }

    @Override
    public void abort() {
        // No abort logic
    }
}
