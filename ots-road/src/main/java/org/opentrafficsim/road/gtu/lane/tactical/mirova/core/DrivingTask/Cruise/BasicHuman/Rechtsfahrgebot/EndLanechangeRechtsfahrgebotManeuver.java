package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.Rechtsfahrgebot;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

// end_lanechange_rechtsfahrgebot_maneuver.java
public class EndLanechangeRechtsfahrgebotManeuver extends StartLanechangeRechtsfahrgebotManeuver {
    public EndLanechangeRechtsfahrgebotManeuver(final DrivingTask drivingTask) {
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
