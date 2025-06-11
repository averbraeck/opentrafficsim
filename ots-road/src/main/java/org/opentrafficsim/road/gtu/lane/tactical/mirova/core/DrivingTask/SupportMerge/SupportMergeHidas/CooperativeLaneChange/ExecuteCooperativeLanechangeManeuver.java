package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas.CooperativeLaneChange;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the execution phase of a cooperative lane change maneuver.
 *
 * This state is responsible for performing the actual lane change. It sets the target lane
 * for the vehicle and monitors the progress of the lane change until it is completed.
 *
 * Responsibilities:
 * - Execute the cooperative lane change by setting the target lane.
 * - Monitor the lane change progress and ensure it is completed safely.
 * - Transition to the end_cooperative_lanechange_maneuver state upon completion.
 *
 * Attributes:
 * - originLaneId: The ID of the lane the vehicle is currently in.
 * - targetLaneId: The ID of the lane the vehicle is changing to.
 * - drivingTask: The driving task associated with the cooperative lane change maneuver.
 *
 * Transitions:
 * - To end_cooperative_lanechange_maneuver when the lane change is completed.
 */
public class ExecuteCooperativeLanechangeManeuver extends StartCooperativeLanechangeManeuver {

    private final int originLaneId;
    private final int targetLaneId;

    public ExecuteCooperativeLanechangeManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
        this.originLaneId = drivingTask.getContextVehicle().getCurrentLaneId();
        this.targetLaneId = this.originLaneId + 1;
        this.update();
    }

    @Override
    public void executeControl() {
        this.drivingTask.getContextVehicle().getContextDriverDevice()
            .getVissimVehicle().SetAttValue("DESLANE", this.targetLaneId);
    }

    @Override
    public void next() {
        var vehicle = this.drivingTask.getContextVehicle();
        if (vehicle.getCurrentLaneId() == this.targetLaneId &&
            "NONE".equals(vehicle.getContextDriverDevice().getVissimVehicle().AttValue("LNCHG"))) {
            vehicle.setLastExecutedActionState(
                new EndCooperativeLanechangeManeuver(this.drivingTask)
            );
        }
    }
}
