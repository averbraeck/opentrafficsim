package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeDeceleration;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the end state of a cooperative deceleration maneuver.
 */
public class EndCooperativeDecelerationManeuver extends StartCooperativeDecelerationManeuver {

    public EndCooperativeDecelerationManeuver(final DrivingTask drivingTask) {
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
