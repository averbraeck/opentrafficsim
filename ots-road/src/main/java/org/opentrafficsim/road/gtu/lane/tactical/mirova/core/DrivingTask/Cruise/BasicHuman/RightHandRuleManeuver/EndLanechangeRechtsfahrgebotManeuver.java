package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.RightHandRuleManeuver;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

// end_lanechange_rechtsfahrgebot_maneuver.java
public class EndLanechangeRechtsfahrgebotManeuver extends StartLanechangeRechtsfahrgebotManeuver
{
    public EndLanechangeRechtsfahrgebotManeuver(final BasicHuman drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.update();
    }

    @Override
    public SimpleOperationalPlan executeControl()
    {
        this.drivingTask.getAbstractMirovaVehicle().setRunningManeuver(false);
        return null; // No operational plan is executed in this state
    }

    @Override
    public void next()
    {
        // No further action
    }
}
