package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeDeceleration;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the state where the vehicle decelerates to open a gap for cooperative merging.
 */
public class DecelerateOpenGap extends StartCooperativeDecelerationManeuver {

    public DecelerateOpenGap(final DrivingTask drivingTask) {
        super(drivingTask);
        this.drivingTask.setCooperativeDeceleration(null);
        this.update();
    }

    @Override
    public void executeControl() {
        var vehicle = this.drivingTask.getContextVehicle();
        vehicle.updateLanechangeFrontValues(-1);

        double desiredGapHeadway = vehicle.getContextDriverDevice().getMinimumTimeHeadway()
                * vehicle.getCurrentSpeed() / 3.6;

        double anticipatedGapHeadwayWithoutDeceleration = vehicle.getFrontSpaceHeadway()
                - (vehicle.getCurrentSpeed() / 3.6)
                + (vehicle.getCurrentSpeed() - vehicle.getFrontSpeedDelta()) / 3.6;

        if (desiredGapHeadway < anticipatedGapHeadwayWithoutDeceleration) {
            vehicle.setUpdatedAcceleration(this.drivingTask.getCooperativeDeceleration());
        } else {
            vehicle.setUpdatedAcceleration(0);
        }
    }

    @Override
    public void next() {
        var vehicle = this.drivingTask.getContextVehicle();
        String leadTargNo = (String) vehicle.getContextDriverDevice().getVissimVehicle().AttValue("LEADTARGNO");
        String targetId = this.drivingTask.getTargetAdjacentVehicleId();

        boolean conditionTargetLaneChange = vehicle.getCurrentLaneId() ==
            (int) vehicle.getContextDriverDevice().getVissimConnection().getNet().getVehicles()
                .ItemByKey(targetId).AttValue("LANE\\INDEX");

        if (leadTargNo != null && leadTargNo.equals(targetId)) {
            vehicle.setLastExecutedActionState(new EndCooperativeDecelerationManeuver(this.drivingTask));
        } else if (conditionTargetLaneChange) {
            vehicle.setLastExecutedActionState(new EndCooperativeDecelerationManeuver(this.drivingTask));
        } else if (vehicle.getCurrentPosition() >
            (double) vehicle.getContextDriverDevice().getVissimConnection().getNet().getVehicles()
                .ItemByKey(targetId).AttValue("POS")) {
            vehicle.setLastExecutedActionState(new EndCooperativeDecelerationManeuver(this.drivingTask));
        }
    }

    @Override
    public void update() {
        super.update();
    }
}
