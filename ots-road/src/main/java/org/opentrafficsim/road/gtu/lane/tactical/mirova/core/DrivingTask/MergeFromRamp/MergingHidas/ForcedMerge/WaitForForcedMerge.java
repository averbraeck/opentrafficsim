package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * Represents the state where the vehicle waits for a suitable condition to perform a forced merge.
 *
 * This state is responsible for monitoring the traffic conditions and waiting until the
 * required gaps in the traffic stream are available to safely initiate a forced lane change.
 * During this state, the vehicle maintains its acceleration and continuously evaluates
 * whether the conditions for a forced merge are met.
 *
 * Responsibilities:
 * - Monitors traffic conditions to determine when a forced merge can be initiated.
 * - Maintains the vehicle's acceleration while waiting for suitable conditions.
 * - Transitions to the forced_merge_lane_change state when the conditions are met.
 *
 * Methods:
 * - executeControl(): Maintains the vehicle's acceleration while waiting.
 * - next(): Transitions to the forced_merge_lane_change state if the conditions for a forced merge are met.
 *
 * Attributes:
 * - drivingTask: The driving task associated with the merge maneuver.
 *
 * Transitions:
 * - To forced_merge_lane_change if the conditions for a forced merge are met.
 */
public class WaitForForcedMerge extends StartForcedMerge {

    public WaitForForcedMerge(final MergingHidas drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        var vehicle = this.drivingTask.getContextVehicle();
        vehicle.setTemporaryTimeHeadwayReduction(0.5);
        vehicle.setTemporaryTimeHeadwayReductionRelaxationTime(10);

        double distanceEndOfLane = vehicle.getContextDriverDevice().getVissimVehicle().AttValue("LANE\\LINK\\LENGTH2D")
                - vehicle.getCurrentPosition();

        double decelerationYield = vehicle.getCarFollowingModel().calculateDecelerationYield(
            distanceEndOfLane, 0, 10, vehicle.getMaximumDeceleration()
        );

        // Uncomment if you want to use decelerationYield logic
        // if (decelerationYield < vehicle.getMaximumDeceleration() * 0.9) {
        //     vehicle.setUpdatedAcceleration(decelerationYield);
        // } else {
        vehicle.setUpdatedAcceleration(Math.min(vehicle.getMaximumAcceleration(), vehicle.getMaximumAcceleration()));
        // }
    }

    @Override
    public void next() {
        var vehicle = this.drivingTask.getContextVehicle();
        if (vehicle.checkOperationalLaneChange(
                0.5 * vehicle.getContextDriverDevice().getMinimumTimeHeadway(),
                0.5 * vehicle.getContextDriverDevice().getMinimumTimeHeadway(),
                -4, -4, 1)) {
            vehicle.setLastExecutedActionState(new ForcedMergeLaneChange((MergingHidas) this.drivingTask));
        }
    }
}
