package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;


import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge.*;

/**
 * Represents the initial state for a forced merge maneuver.
 *
 * This state is responsible for initiating the forced merging process by updating the
 * operational values of the merge gap and calculating the required acceleration to position
 * the vehicle within the target gap. It also determines the next state based on the progress
 * of the merge.
 *
 * Responsibilities:
 * - Updates the operational values of the merge gap, such as distances, speeds, and accelerations
 *   of the leading and following vehicles in the gap.
 * - Calculates the required acceleration to reach the target gap.
 * - Transitions to the next state based on the progress of the merge.
 *
 * Methods:
 * - update(): Updates the merge gap values and calculates the required acceleration.
 * - executeControl(): Placeholder for controlling the vehicle's behavior in this state
 */
public class StartForcedMerge extends ActionState {

    public StartForcedMerge(final MergingHidas drivingTask) {
        super(drivingTask);
    }

    @Override
    public void abort() {
        super.abort();
    }

    @Override
    public void next() {
        if (this.drivingTask.getContextVehicle().checkOperationalLaneChange(
                0.5 * this.drivingTask.getContextVehicle().getContextDriverDevice().getMinimumTimeHeadway(),
                0.5 * this.drivingTask.getContextVehicle().getContextDriverDevice().getMinimumTimeHeadway(),
                -4, -4, 1)) {
            this.drivingTask.getContextVehicle().setLastExecutedActionState(
                new ForcedMergeLaneChange((MergingHidas) this.drivingTask)
            );
        } else {
            this.drivingTask.getContextVehicle().setLastExecutedActionState(
                new WaitForForcedMerge((MergingHidas) this.drivingTask)
            );
        }
    }

    @Override
    public void executeControl() {
        // Placeholder
    }

    @Override
    public void update() {
        super.update();
    }
}
