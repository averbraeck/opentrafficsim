package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Overtaking;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

public class EndOvertakingManeuver extends StartOvertakingManeuver {
    public EndOvertakingManeuver(final DrivingTask drivingTask) {
        super(drivingTask);
        this.update();
    }

    @Override
    public void executeControl() {
        this.active = false;
        this.drivingTask.getContextVehicle().setRunningManeuver(false);
    }

    @Override
    public void next() {
        // No further action
    }
}
