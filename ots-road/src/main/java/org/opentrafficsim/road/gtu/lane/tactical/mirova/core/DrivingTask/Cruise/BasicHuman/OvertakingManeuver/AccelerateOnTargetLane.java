package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Execution: weiter Beschleunigen auf Zielfahrtstreifen
 *
 * Transition: vWunsch erreicht oder Vorderfahrzeug
 *
 * @param drivingTask the driving task context
 */
public class AccelerateOnTargetLane extends StartOvertakingManeuver {
    public AccelerateOnTargetLane(final DrivingTask drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        this.drivingTask.getContextVehicle().setUpdatedAcceleration(
            this.drivingTask.getContextVehicle().getMaximumAcceleration()
        );
    }

    @Override
    public void next() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        if (v.getCurrentSpeed() >= v.getContextDriverDevice().getDesiredSpeed()
            || v.getCurrentTimeHeadway() <= 2 * v.getContextDriverDevice().getMinimumTimeHeadway()) {
            this.active = false;
            v.setLastExecutedActionState(new EndOvertakingManeuver(this.drivingTask));
        }
    }
}
