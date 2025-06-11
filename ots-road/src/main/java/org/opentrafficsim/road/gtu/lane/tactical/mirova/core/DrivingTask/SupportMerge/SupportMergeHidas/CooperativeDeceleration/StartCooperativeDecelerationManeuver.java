package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeDeceleration;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the start of a cooperative deceleration maneuver.
 */
public class StartCooperativeDecelerationManeuver extends ActionState {

    public StartCooperativeDecelerationManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
    }

    @Override
    public void executeControl() {
        // No-op
    }

    @Override
    public void update() {
        this.drivingTask.getContextVehicle().setRunningManeuver(true);
        super.update();
    }

    @Override
    public void next() {
        String leadTargNo = (String) this.drivingTask.getContextVehicle()
                .getContextDriverDevice().getVissimVehicle().AttValue("LEADTARGNO");
        String targetId = this.drivingTask.getTargetAdjacentVehicleId();
        if (leadTargNo != null && leadTargNo.equals(targetId)) {
            this.drivingTask.getContextVehicle().setLastExecutedActionState(
                new EndCooperativeDecelerationManeuver(this.drivingTask)
            );
        } else {
            this.drivingTask.getContextVehicle().setLastExecutedActionState(
                new DecelerateOpenGap(this.drivingTask)
            );
        }
    }

    @Override
    public void abort() {
        // No-op
    }
}
