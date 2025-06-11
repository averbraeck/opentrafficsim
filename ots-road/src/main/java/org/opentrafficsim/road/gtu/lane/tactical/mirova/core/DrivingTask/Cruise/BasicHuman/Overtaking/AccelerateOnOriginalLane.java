package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Overtaking;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Execution: Beschleunigen (const. b) bis 0.7 * CC1 (für beide Vorderfahrzeuge beider Fahrstreifen) oder gewissen Anteil von vWunsch erreicht
 *
 * Transition: zu Vorderfahrzeug aufgeschlossen bzw. vWunsch erreicht und Lücke noch vorhanden
 *
 * @param drivingTask the driving task context
 */
public class AccelerateOnOriginalLane extends StartOvertakingManeuver {
    public AccelerateOnOriginalLane(final DrivingTask drivingTask) {
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
            || v.getCurrentTimeHeadway() <= 0.7 * v.getContextDriverDevice().getMinimumTimeHeadway()
            || v.getRearVehicleDeceleration() >= 0) {
            this.active = false;
            v.setLastExecutedActionState(new ExecuteLaneChangeLeft(this.drivingTask));
        }
    }
}
