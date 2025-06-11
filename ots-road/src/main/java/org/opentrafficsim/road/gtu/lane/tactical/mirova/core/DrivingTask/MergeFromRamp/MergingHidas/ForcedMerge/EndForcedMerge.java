package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * Represents the final state of the forced merge maneuver.
 */
public class EndForcedMerge extends StartForcedMerge {

    public EndForcedMerge(final MergingHidas drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        this.drivingTask.getContextVehicle().setRunningManeuver(false);
    }

    @Override
    public void next() {
        // No further transitions
    }
}
