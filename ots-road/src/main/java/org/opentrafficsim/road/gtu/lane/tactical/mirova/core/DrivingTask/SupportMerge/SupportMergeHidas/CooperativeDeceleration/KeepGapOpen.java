package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeDeceleration;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the state where the vehicle keeps a gap open for merging.
 */
public class KeepGapOpen extends StartCooperativeDecelerationManeuver {

    private final double timeAnticipation = 1.0;

    public KeepGapOpen(final DrivingTask drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        var vehicle = this.drivingTask.getContextVehicle();
        vehicle.updateLanechangeFrontValues(-1);

        double cooperativeDeceleration = (
            vehicle.getContextDriverDevice().getMinimumTimeHeadway()
            * vehicle.getCurrentSpeed() / 3.6
            - vehicle.getFrontSpaceHeadway()
            + vehicle.getFrontSpeedDelta() * this.timeAnticipation / 3.6
        ) * 2 / (this.timeAnticipation * this.timeAnticipation);

        vehicle.setUpdatedAcceleration(Math.max(cooperativeDeceleration, -1.5));
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
        }
    }
}
