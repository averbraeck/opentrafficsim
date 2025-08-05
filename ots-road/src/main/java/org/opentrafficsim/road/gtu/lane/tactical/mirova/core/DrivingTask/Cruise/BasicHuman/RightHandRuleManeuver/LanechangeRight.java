package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.RightHandRuleManeuver;

import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver.EndOvertakingManeuver;
import org.opentrafficsim.road.network.lane.Lane;

public class LanechangeRight extends StartLanechangeRechtsfahrgebotManeuver
{
    private final Lane originLane = this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane();

    public LanechangeRight(final BasicHuman drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.update();
    }

    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException
    {
        SortedSet<HeadwayGtu> headwayGtuSecondLane;
        LateralDirectionality lateralDirectionality;
        if (this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane())
        {
            RelativeLane secondLane = this.drivingTask.getAbstractMirovaVehicle().getLaneChange()
                    .getSecondLane(this.drivingTask.getAbstractMirovaVehicle().getGtu());
            headwayGtuSecondLane =
                    this.drivingTask.getNeighborsPerception().getFirstLeaders(secondLane.getLateralDirectionality());
            lateralDirectionality = LateralDirectionality.NONE;
        }
        else
        {
            headwayGtuSecondLane = this.drivingTask.getNeighborsPerception().getFirstLeaders(LateralDirectionality.RIGHT);
            lateralDirectionality = LateralDirectionality.RIGHT;
        }

        Headway headwayGtuCurrentLane = this.drivingTask.getDirectDefaultSimplePerception().getForwardHeadwayGtu();
        Double minAcceleration =
                this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(headwayGtuCurrentLane, 0.0).si;

        for (HeadwayGtu leader : headwayGtuSecondLane)
        {
            minAcceleration = Math.min(minAcceleration,
                    this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(leader, 0.0).si);
        }

        return new SimpleOperationalPlan(new Acceleration(minAcceleration, AccelerationUnit.SI),
                this.drivingTask.getParameters().getParameter(ParameterTypes.DT), lateralDirectionality);
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        if (this.originLane != this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()
                && this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane() == false)
        {

            this.drivingTask.getAbstractMirovaVehicle()
                    .setCurrentActionState(new EndLanechangeRechtsfahrgebotManeuver(this.drivingTask));
        }
    }
}
