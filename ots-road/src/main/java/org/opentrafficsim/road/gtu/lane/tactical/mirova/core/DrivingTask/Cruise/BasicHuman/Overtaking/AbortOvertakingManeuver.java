package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Overtaking;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

public class AbortOvertakingManeuver extends StartOvertakingManeuver {
    public AbortOvertakingManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        int desLane = (int) v.getContextDriverDevice().getVissimVehicle().AttValue("DesLane");
        v.getContextDriverDevice().getVissimVehicle().SetAttValue("DesLane", desLane - 1);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void next() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        if ("NONE".equals(v.getContextDriverDevice().getVissimVehicle().AttValue("LNCHG"))) {
            v.setLastExecutedActionState(new EndOvertakingManeuver(this.drivingTask));
        }
    }

    @Override
    public void abort() {
        super.abort();
    }
}
