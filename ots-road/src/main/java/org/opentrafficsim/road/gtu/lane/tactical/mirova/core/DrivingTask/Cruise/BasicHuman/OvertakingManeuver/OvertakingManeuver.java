package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

public class OvertakingManeuver extends ManeuverPattern
{
    protected final LateralDirectionality laneChangeDirection;

    protected final ActionState initialActionState;

    protected final BasicHuman drivingTask;

    protected double desire; // Default desire value for overtaking



    public OvertakingManeuver(final BasicHuman drivingTask) throws ParameterException
    {
        this.initialActionState = new StartOvertakingManeuver(drivingTask);
        this.laneChangeDirection = LateralDirectionality.LEFT; // Default to left lane change for overtaking
        this.drivingTask = drivingTask;
        this.calculateDesire(); // Calculate the desire for overtaking based on the driving task
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
    public void calculateDesire() throws ParameterException
    {
        Speed overtakingSpeedGainThreshold = this.drivingTask.getOvertakingSpeedGainThreshold(); // Speed gain threshold for
        // overtaking

        Length overtakingLeftLaneDistanceThreshold = this.drivingTask.getOvertakingLeftLaneDistanceThreshold(); // Distance threshold for

        Speed possibleSpeedGain = this.drivingTask.getPossibleSpeedGain(RelativeLane.LEFT); // Speed gain in left lane

        Length leftLaneDistance = this.drivingTask.getTargetLaneRemainingDistance(LateralDirectionality.LEFT); // Distance to the left lane GTU

        Speed maxSpeedGain = this.drivingTask.getMaxSpeedGain(); // Maximum speed gain for overtaking

        this.desire = 1 + (possibleSpeedGain.si - overtakingSpeedGainThreshold.si)
                / (maxSpeedGain.si - overtakingSpeedGainThreshold.si);

        // TODO: Implement logic to adjust desire based on left lane distance and thresholds
    }

}
