package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge.EndFreeMergeToTargetGap;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Represents the state where the vehicle performs a forced lane change to enter the target gap. This state is responsible for
 * initiating and executing the lane change maneuver to position the vehicle within the target gap. It ensures that the vehicle
 * transitions smoothly to the target lane while maintaining safe distances to other vehicles. Responsibilities: - Executes the
 * lane change by setting the desired lane for the vehicle. - Controls the vehicle's acceleration to ensure a safe and efficient
 * lane change. - Transitions to the next state once the lane change is completed. Methods: - executeControl(): Sets the desired
 * lane and adjusts the vehicle's acceleration. - next(): Transitions to the end_forced_merge state if the lane change is
 * completed. Attributes: - drivingTask: The driving task associated with the merge maneuver. - originLaneId: The ID of the lane
 * the vehicle is currently in. - targetLaneId: The ID of the lane the vehicle is merging into. Transitions: - To
 * end_forced_merge if the lane change is successfully completed.
 */
public class ForcedMergeLaneChange extends StartForcedMerge
{

    /** The lane from which the lane change is initiated. */
    private final Lane originLane;

    public ForcedMergeLaneChange(final MergingHidas drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.originLane = this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane();
        this.update();
    }

    @Override
    public SimpleOperationalPlan executeControl() throws OperationalPlanException, ParameterException
    {
        SortedSet<HeadwayGtu> headwayGtuSecondLane;
        LateralDirectionality lateralDirectionality;
        Double minAcceleration = null;
        if (this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane())
        {
            lateralDirectionality = LateralDirectionality.NONE;
        }
        else
        {
            lateralDirectionality = LateralDirectionality.LEFT;
        }

        if (this.originLane.equals(this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()))
        {
            RelativeLane secondLane = this.drivingTask.getAbstractMirovaVehicle().getLaneChange()
                    .getSecondLane(this.drivingTask.getAbstractMirovaVehicle().getGtu());
            headwayGtuSecondLane =
                    this.drivingTask.getNeighborsPerception().getFirstLeaders(secondLane.getLateralDirectionality());

            for (HeadwayGtu leader : headwayGtuSecondLane)
            {
                minAcceleration = Math.min(minAcceleration,
                        this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(leader).si);
            }

        }
        else
        {
            Headway headwayGtuCurrentLane = this.drivingTask.getDirectDefaultSimplePerception().getForwardHeadwayGtu();
            minAcceleration =
                    this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(headwayGtuCurrentLane).si;

        }
        return new SimpleOperationalPlan(new Acceleration(minAcceleration, AccelerationUnit.SI),
                this.drivingTask.getParameters().getParameter(ParameterTypes.DT), lateralDirectionality);
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        // If lane change is finished and the vehicle is no longer on the origin lane, transition to the next action
        if (this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane() == false
                && this.originLane.equals(this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()) == false)
        {
            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new EndForcedMerge(this.drivingTask));
        }
    }
}
