package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.RightHandRuleManeuver;

import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

public class RightHandRuleManeuver extends ManeuverPattern
{
    protected final LateralDirectionality laneChangeDirection;
    protected final ActionState initialActionState;
    protected final BasicHuman drivingTask;
    private double desire;

    public RightHandRuleManeuver(final BasicHuman drivingTask)
    {
        this.initialActionState = new StartLanechangeRechtsfahrgebotManeuver(drivingTask);
        this.laneChangeDirection = LateralDirectionality.RIGHT; // Example: right for right-hand rule
        this.drivingTask = drivingTask;
        calculateDesire(); // Initialize desire
    }

    @Override
    public ActionState getInitialActionState()
    {
        return this.initialActionState;
    }

    @Override
    public LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection;
    }

    @Override
    public DrivingTask getDrivingTask()
    {
        return this.drivingTask;
    }

    @Override
    public double getDesire()
    {
        return this.desire;
    }

    @Override
    public void calculateDesire()
    {
        this.desire = 1.0; // Placeholder for desire calculation logic
    }

}
