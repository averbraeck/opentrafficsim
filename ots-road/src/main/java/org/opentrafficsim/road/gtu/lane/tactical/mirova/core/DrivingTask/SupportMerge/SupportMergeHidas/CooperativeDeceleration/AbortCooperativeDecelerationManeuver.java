package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeDeceleration;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the abort state for a cooperative deceleration maneuver.
 */
public class AbortCooperativeDecelerationManeuver extends StartCooperativeDecelerationManeuver {

    public AbortCooperativeDecelerationManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        super.executeControl();
    }

    @Override
    public void next() {
        super.next();
    }
}
