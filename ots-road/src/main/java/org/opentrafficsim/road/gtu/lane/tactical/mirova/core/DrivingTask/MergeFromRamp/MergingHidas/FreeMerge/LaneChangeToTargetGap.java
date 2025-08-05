package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge;

import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Handles the lane change maneuver to a target gap during a free merge from a ramp.
 * <p>
 * This class manages the operational plan for changing lanes, considering leaders on both the current and target lanes, and
 * ensures safe merging by calculating the minimum required acceleration.
 * </p>
 */
public class LaneChangeToTargetGap extends StartFreeMergeToTargetGap
{
    /** The lane from which the lane change is initiated. */
    private final Lane originLane;

    /**
     * Constructs a LaneChangeToTargetGap action.
     * @param drivingTask the merging driving task context
     * @throws OperationalPlanException if the operational plan cannot be created
     * @throws ParameterException if parameters are invalid
     * @throws NullPointerException if required objects are null
     * @throws IllegalArgumentException if arguments are invalid
     */
    public LaneChangeToTargetGap(final MergingHidas drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        // Store the lane where the maneuver starts
        this.originLane = this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane();
        this.update();
    }

    /**
     * Executes the control logic for the lane change maneuver. Calculates the minimum acceleration required based on leaders in
     * both lanes and remaining distance.
     * @return the operational plan for the current time step
     * @throws ParameterException if parameters are invalid
     * @throws OperationalPlanException if the plan cannot be created
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException
    {
        SortedSet<HeadwayGtu> headwayGtuSecondLane;
        LateralDirectionality lateralDirectionality;
        this.drivingTask.getAbstractMirovaVehicle().setDesire(1.0, new Duration(15.0, DurationUnit.SI));
        // Determine if a lane change is in progress and select the appropriate lane and leaders
        if (this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane())
        {
            // If changing lane, get leaders on the second lane
            RelativeLane secondLane = this.drivingTask.getAbstractMirovaVehicle().getLaneChange()
                    .getSecondLane(this.drivingTask.getAbstractMirovaVehicle().getGtu());
            headwayGtuSecondLane =
                    this.drivingTask.getNeighborsPerception().getFirstLeaders(secondLane.getLateralDirectionality());
            lateralDirectionality = LateralDirectionality.NONE;
        }
        else
        {
            // If not changing lane, get leaders on the left (target) lane
            headwayGtuSecondLane = this.drivingTask.getNeighborsPerception().getFirstLeaders(LateralDirectionality.LEFT);
            lateralDirectionality = LateralDirectionality.LEFT;
        }

        // Get leader on the current lane
        Headway headwayGtuCurrentLane = this.drivingTask.getDirectDefaultSimplePerception().getForwardHeadwayGtu();
        Double minAcceleration = null;
        if (headwayGtuCurrentLane != null)
        {
            // Calculate desired following acceleration for the current lane leader
            minAcceleration =
                    this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(headwayGtuCurrentLane).si;
        }
        Length remainingDistanceBeforeLaneChange = this.drivingTask.getRemainingDistanceBeforeLaneChange();

        // If still on the origin lane, consider stopping if close to the end and leaders on the second lane
        if (this.originLane.equals(this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()))
        {
            // If close to the end of the lane, consider stopping
            if (remainingDistanceBeforeLaneChange.lt(Length.instantiateSI(250.0)))
            {
                // Temporarily set time gap to minimum for stopping calculation
                this.drivingTask.getParameters().setParameterResettable(ParameterTypes.T,
                        this.drivingTask.getParameters().getParameter(ParameterTypes.TMIN));
                Double stopAcceleration =
                        CarFollowingUtil.stop(this.drivingTask.getAbstractMirovaVehicle().getCarFollowingModel(),
                                this.drivingTask.getParameters(), getEgoSpeed(),
                                this.drivingTask.getInfrastructurePerception().getSpeedLimitProspect(RelativeLane.CURRENT)
                                        .getSpeedLimitInfo(Length.ZERO),
                                this.drivingTask.getRemainingDistanceBeforeLaneChange()).si;
                this.drivingTask.getParameters().resetParameter(ParameterTypes.T);
                minAcceleration = Math.min(minAcceleration, stopAcceleration);
            }
            // Consider leaders on the second lane
            for (HeadwayGtu leader : headwayGtuSecondLane)
            {
                minAcceleration = Math.min(minAcceleration,
                        this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(leader).si);
            }
        }
        // Return the operational plan with the calculated minimum acceleration and directionality
        return new SimpleOperationalPlan(new Acceleration(minAcceleration, AccelerationUnit.SI),
                this.drivingTask.getParameters().getParameter(ParameterTypes.DT), lateralDirectionality);
    }

    /**
     * Advances the action to the next state if the lane change is completed.
     * @throws ParameterException if parameters are invalid
     * @throws OperationalPlanException if the plan cannot be created
     */
    @Override
    public void next() throws ParameterException, OperationalPlanException
    {
        // If lane change is finished and the vehicle is no longer on the origin lane, transition to the next action
        if (this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane() == false
                && this.originLane.equals(this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()) == false)
        {
            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new EndFreeMergeToTargetGap(this.drivingTask));
        }
    }

    /**
     * Aborts the lane change action. Implement abort logic if needed.
     */
    @Override
    public void abort()
    {
        // Implement abort logic if needed
    }
}
