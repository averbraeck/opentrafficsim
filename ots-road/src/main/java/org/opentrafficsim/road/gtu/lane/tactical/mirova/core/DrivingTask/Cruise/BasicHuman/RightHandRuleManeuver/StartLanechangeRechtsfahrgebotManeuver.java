package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.RightHandRuleManeuver;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

// start_lanechange_rechtsfahrgebot_maneuver.java
public class StartLanechangeRechtsfahrgebotManeuver extends ActionState

{
    protected BasicHuman drivingTask;

    public StartLanechangeRechtsfahrgebotManeuver(final BasicHuman drivingTask)
    {
        super(drivingTask);
    }

    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException
    {
        return null;// No execution
    }

    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        return super.update();
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new LanechangeRight(this.drivingTask));
    }

    @Override
    public void abort()
    {
        // No execution
    }
}
