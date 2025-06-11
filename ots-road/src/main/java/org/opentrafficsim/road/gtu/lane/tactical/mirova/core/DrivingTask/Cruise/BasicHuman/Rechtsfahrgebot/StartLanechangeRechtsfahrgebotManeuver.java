package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Rechtsfahrgebot;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

// start_lanechange_rechtsfahrgebot_maneuver.java
public class StartLanechangeRechtsfahrgebotManeuver extends ActionState {
    public StartLanechangeRechtsfahrgebotManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
    }

    @Override
    public void executeControl() {
        // No execution
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void next() {
        AbstractMirovaVehicle v = this.drivingTask.getContextVehicle();
        v.setRunningManeuver(true);
        v.updateLanechangeRearValues(-1);
        v.updateLanechangeFrontValues(-1);
        if (v.getFrontFreeflowtime() >= v.getContextDriverDevice().getMinimumFrontFreeflowtime()
            && (v.getRearVehicleDeceleration() >= 0 || v.getRearTimeHeadway() >= 5)) {
            v.setLastExecutedActionState(new LanechangeRight(this.drivingTask));
        }
    }

    @Override
    public void abort() {
        // No execution
    }
}
