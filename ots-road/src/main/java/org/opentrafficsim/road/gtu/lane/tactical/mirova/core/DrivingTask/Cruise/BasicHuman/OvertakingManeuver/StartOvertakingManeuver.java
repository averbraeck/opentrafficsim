package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

/**
 * Starting State of overtaking maneuver -> has no execution and makes immediate transition to next ActionState
 * @param drivingTask the driving task context
 */
public class StartOvertakingManeuver extends ActionState
{

    protected BasicHuman drivingTask;

    /**
     * Constructs the starting state of the overtaking maneuver.
     * @param drivingTask the driving task context for this maneuver
     */
    public StartOvertakingManeuver(final BasicHuman drivingTask)
    {
        super(drivingTask);
    }

    /**
     * This method does not execute any control action in the starting state of the overtaking maneuver.
     * @return always returns {@code null} as no operational plan is executed in this state
     * @throws ParameterException
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException
    {
        return null;
    }

    /**
     * Updates the action state. In this starting state, it delegates to the superclass implementation.
     * @return the result of the superclass update method
     * @throws OperationalPlanException if an operational plan error occurs
     * @throws ParameterException if a parameter error occurs
     * @throws NullPointerException if a required object is null
     * @throws IllegalArgumentException if an argument is invalid
     */
    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        return super.update();
    }

    /**
     * Determines and transitions to the next action state in the overtaking maneuver.
     * <p>
     * If the follower deceleration for a left lane change is non-negative, the vehicle accelerates on the original lane.
     * Otherwise, it initiates a lane change to the left.
     * </p>
     * @throws OperationalPlanException if an operational plan error occurs.
     * @throws ParameterException if a parameter error occurs.
     * @throws NullPointerException if a required object is null.
     * @throws IllegalArgumentException if an argument is invalid.
     */
    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        if (this.drivingTask.getAbstractMirovaVehicle().getLaneChangeFollowerDeceleration(LateralDirectionality.LEFT).si >= 0)
        {
            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new AccelerateOnOriginalLane(this.drivingTask));
        }
        else
        {
            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new ExecuteLaneChangeLeft(this.drivingTask));
        }
    }

    /**
     * Aborts the overtaking maneuver if the desire to overtake is no longer present or if the lane change is not safe. This
     * method transitions to the EndOvertakingManeuver state.
     * @throws ParameterException if there is an issue with parameters.
     * @throws OperationalPlanException if there is an issue with the operational plan.
     * @throws NullPointerException if any required object is null.
     * @throws IllegalArgumentException if any argument is invalid.
     */
    @Override
    public void abort() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException
    {
        if (this.drivingTask.conditionDesireToOvertake() == false
                || this.drivingTask.conditionLanechangeSafety(LateralDirectionality.LEFT, 1.0) == false)
        {
            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new AbortOvertakingManeuver(this.drivingTask));
        }
    }
}
