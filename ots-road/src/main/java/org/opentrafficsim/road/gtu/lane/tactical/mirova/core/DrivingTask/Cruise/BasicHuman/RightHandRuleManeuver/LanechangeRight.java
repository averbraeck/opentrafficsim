package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.RightHandRuleManeuver;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

public class LanechangeRight extends StartLanechangeRechtsfahrgebotManeuver {
    private final int originLaneId;
    private final int targetLaneId;

    public LanechangeRight(final DrivingTask drivingTask) {
        super(drivingTask);
        this.originLaneId = drivingTask.getContextVehicle().getCurrentLaneId();
        this.targetLaneId = this.originLaneId - 1;
        this.update();
    }

    @Override
    public void executeControl() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        v.getContextDriverDevice().getVissimVehicle().SetAttValue("DESLANE", this.targetLaneId);
    }

    @Override
    public void next() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        if (v.getCurrentLaneId() == this.targetLaneId
            && "NONE".equals(v.getContextDriverDevice().getVissimVehicle().AttValue("LNCHG"))) {
            v.setLastExecutedActionState(new EndLanechangeRechtsfahrgebotManeuver(this.drivingTask));
        }
    }
}
