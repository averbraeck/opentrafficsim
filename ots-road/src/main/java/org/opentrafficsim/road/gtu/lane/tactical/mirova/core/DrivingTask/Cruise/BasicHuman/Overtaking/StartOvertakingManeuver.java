package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Overtaking;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Starting State of overtaking maneuver -> has no execution and makes immediate transition to next ActionState
 *
 * @param drivingTask the driving task context
 */
public class StartOvertakingManeuver extends ActionState {
    public StartOvertakingManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
    }

    @Override
    public void executeControl() {
        // No execution
    }

    @Override
    public void update() {
        this.drivingTask.getContextVehicle().updateLanechangeRearValues(1);
        this.drivingTask.getContextVehicle().updateLanechangeFrontValues(1);
        super.update();
    }

    @Override
    public void next() {
        this.drivingTask.getContextVehicle().setRunningManeuver(true);
        if (this.drivingTask.getContextVehicle().getRearVehicleDeceleration() >= 0) {
            this.drivingTask.getContextVehicle().setLastExecutedActionState(
                new AccelerateOnOriginalLane(this.drivingTask)
            );
        } else {
            this.drivingTask.getContextVehicle().setLastExecutedActionState(
                new ExecuteLaneChangeLeft(this.drivingTask)
            );
        }
    }

    @Override
    public void abort() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        if (v.checkOperationalLaneChange(
                v.getContextDriverDevice().getMinimumTimeHeadway() * 0.75,
                v.getContextDriverDevice().getMinimumTimeHeadway() * 0.75,
                v.getContextDriverDevice().getAcceptedDecelerationEgoVehicle(),
                v.getContextDriverDevice().getAcceptedDecelerationRearVehicle())) {
            // do nothing
        } else if ((int) v.getContextDriverDevice().getVissimVehicle().AttValue("DesLane")
                > v.getCurrentLaneId()) {
            this.active = false;
            v.setLastExecutedActionState(new AbortOvertakingManeuver(this.drivingTask));
        }
    }
}
