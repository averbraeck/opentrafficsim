package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Overtaking;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Execution:
 *
 * Transition: zu Vorderfahrzeug aufgeschlossen bzw. vWunsch erreicht und Lücke noch vorhanden
 *
 * @param drivingTask the driving task context
 */
public class ExecuteLaneChangeLeft extends StartOvertakingManeuver {
    private final int originLaneId;
    private final int targetLaneId;

    public ExecuteLaneChangeLeft(final DrivingTask drivingTask) {
        super(drivingTask);
        this.originLaneId = drivingTask.getContextVehicle().getCurrentLaneId();
        this.targetLaneId = this.originLaneId + 1;
        this.update();
    }

    @Override
    public void executeControl() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        v.setUpdatedAcceleration(v.getMaximumAcceleration());
        v.getContextDriverDevice().getVissimVehicle().SetAttValue("DESLANE", this.targetLaneId);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void next() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        if (v.getCurrentLaneId() == this.targetLaneId
            && "NONE".equals(v.getContextDriverDevice().getVissimVehicle().AttValue("LNCHG"))) {
            this.active = false;
            if (v.getCurrentSpeed() >= v.getContextDriverDevice().getDesiredSpeed()) {
                v.setLastExecutedActionState(new EndOvertakingManeuver(this.drivingTask));
            } else {
                v.setLastExecutedActionState(new AccelerateOnTargetLane(this.drivingTask));
            }
        }
    }
}
