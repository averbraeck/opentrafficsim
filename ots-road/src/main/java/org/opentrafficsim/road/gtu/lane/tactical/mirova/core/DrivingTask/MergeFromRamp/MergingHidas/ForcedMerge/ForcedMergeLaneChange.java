package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * Represents the state where the vehicle performs a forced lane change to enter the target gap.
 *
 * This state is responsible for initiating and executing the lane change maneuver to position
 * the vehicle within the target gap. It ensures that the vehicle transitions smoothly to the
 * target lane while maintaining safe distances to other vehicles.
 *
 * Responsibilities:
 * - Executes the lane change by setting the desired lane for the vehicle.
 * - Controls the vehicle's acceleration to ensure a safe and efficient lane change.
 * - Transitions to the next state once the lane change is completed.
 *
 * Methods:
 * - executeControl(): Sets the desired lane and adjusts the vehicle's acceleration.
 * - next(): Transitions to the end_forced_merge state if the lane change is completed.
 *
 * Attributes:
 * - drivingTask: The driving task associated with the merge maneuver.
 * - originLaneId: The ID of the lane the vehicle is currently in.
 * - targetLaneId: The ID of the lane the vehicle is merging into.
 *
 * Transitions:
 * - To end_forced_merge if the lane change is successfully completed.
 */
public class ForcedMergeLaneChange extends StartForcedMerge {

    private int originLaneId;
    private int targetLaneId;

    public ForcedMergeLaneChange(final MergingHidas drivingTask) {
        super(drivingTask);
        this.originLaneId = drivingTask.getContextVehicle().getCurrentLaneId();
        this.targetLaneId = this.originLaneId + 1;
        this.update();
    }

    @Override
    public void executeControl() {
        var vehicle = this.drivingTask.getContextVehicle();
        vehicle.setTemporaryTimeHeadwayReduction(0.5);
        vehicle.setTemporaryTimeHeadwayReductionRelaxationTime(10);

        double accelerationTargetLane;
        if (vehicle.getCurrentLaneId() != this.targetLaneId) {
            accelerationTargetLane = vehicle.getCarFollowingModel().calculateAccelerationFollowing(
                1,
                vehicle.getContextDriverDevice().getMinimumTimeHeadway() * vehicle.getTemporaryTimeHeadwayReduction()
            );
        } else {
            accelerationTargetLane = 99;
        }

        vehicle.setUpdatedAcceleration(Math.min(accelerationTargetLane, vehicle.getMaximumAcceleration()));
        vehicle.getContextDriverDevice().getVissimVehicle().SetAttValue("DESLANE", this.targetLaneId);
    }

    @Override
    public void next() {
        var vehicle = this.drivingTask.getContextVehicle();
        if (vehicle.getCurrentLaneId() == this.targetLaneId &&
            "NONE".equals(vehicle.getContextDriverDevice().getVissimVehicle().AttValue("LNCHG"))) {
            vehicle.setLastExecutedActionState(new EndForcedMerge((MergingHidas) this.drivingTask));
        }
    }
}
