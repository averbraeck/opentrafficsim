package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Execution: Transition: zu Vorderfahrzeug aufgeschlossen bzw. vWunsch erreicht und Lücke noch vorhanden
 * @param drivingTask the driving task context
 */
public class ExecuteLaneChangeLeft extends StartOvertakingManeuver
{
    private final Lane originLane = this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane();

    /**
     * Constructs an instance of ExecuteLaneChangeLeft, representing the execution phase of a left lane change maneuver.
     * Initializes the maneuver and updates its state.
     * @param drivingTask the driving task context containing vehicle and perception information
     * @throws OperationalPlanException if an error occurs while creating the operational plan
     * @throws ParameterException if a required parameter is missing or invalid
     * @throws NullPointerException if a required object is null
     * @throws IllegalArgumentException if an illegal argument is provided
     */
    public ExecuteLaneChangeLeft(final BasicHuman drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.update();
    }

    /**
     * Executes the control logic for the left lane change maneuver. Determines the minimum acceleration required based on the
     * leaders in the current and target lanes, and returns a corresponding operational plan for the maneuver.
     * @return a SimpleOperationalPlan representing the next step in the lane change maneuver
     * @throws OperationalPlanException if an error occurs while generating the operational plan
     * @throws ParameterException if a required parameter is missing or invalid
     */
    @Override
    public SimpleOperationalPlan executeControl() throws OperationalPlanException, ParameterException
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
            headwayGtuSecondLane = this.drivingTask.getNeighborsPerception().getFirstLeaders(LateralDirectionality.LEFT);
            lateralDirectionality = LateralDirectionality.LEFT;
        }

        // Leader on current lane
        Headway headwayGtuCurrentLane = this.drivingTask.getDirectDefaultSimplePerception().getForwardHeadwayGtu();
        Double minAcceleration =
                this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(headwayGtuCurrentLane, 1.0).si;

        // leaders on target lane
        for (HeadwayGtu leader : headwayGtuSecondLane)
        {
            minAcceleration = Math.min(minAcceleration,
                    this.drivingTask.getAbstractMirovaVehicle().desireBasedFollowingAcceleration(leader, 1.0).si);
        }

        return new SimpleOperationalPlan(new Acceleration(minAcceleration, AccelerationUnit.SI),
                this.drivingTask.getParameters().getParameter(ParameterTypes.DT), lateralDirectionality);

    }

    /**
     * Updates the state of the lane change maneuver. Delegates to the superclass to perform any necessary state updates and
     * returns the updated operational plan.
     * @return the updated SimpleOperationalPlan after state changes
     * @throws OperationalPlanException if an error occurs while updating the operational plan
     * @throws ParameterException if a required parameter is missing or invalid
     * @throws NullPointerException if a required object is null
     * @throws IllegalArgumentException if an illegal argument is provided
     */
    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        return super.update();
    }

    /**
     * Advances the maneuver to the next action state if the lane change is complete. Checks if the vehicle has left the
     * original lane and is no longer changing lanes, then transitions to the AccelerateOnTargetLane state.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        if (this.originLane != this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()
                && this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane() == false)
        {

            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new EndOvertakingManeuver(this.drivingTask));
        }
    }

    @Override
    public void abort() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException
    {
        /* not sure if OTS allows aborting lane change maneuvers */
        // if (this.originLane == this.drivingTask.getAbstractMirovaVehicle().getGtu().getLane()
        // && this.drivingTask.getAbstractMirovaVehicle().getLaneChange().isChangingLane())
        // {
        // if (this.drivingTask.getAbstractMirovaVehicle().getLaneChangeFollowerDeceleration(LateralDirectionality.LEFT)
        // .lt(this.drivingTask.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
        // {
        // // If the lane change follower deceleration is too high, abort the lane change.
        // this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new AbortOvertakingManeuver(this.drivingTask));
        // }
        //
        //
        // }
        // No specific abort logic for this state; it will be handled in the next state if necessary.
    }
}
