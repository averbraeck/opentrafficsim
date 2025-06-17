package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.BasicHuman;

/**
 * Represents the acceleration phase on the original lane during an overtaking maneuver.
 * <p>
 * The vehicle accelerates until either the forward time headway falls below the minimum threshold,
 * the follower deceleration for a left lane change is non-negative, or the desired speed is reached.
 * </p>
 */
public class AccelerateOnOriginalLane extends StartOvertakingManeuver
{
    /**
     * Constructs the acceleration phase state for the overtaking maneuver.
     *
     * @param drivingTask the driving task context
     * @throws OperationalPlanException if an operational plan error occurs
     * @throws ParameterException if a parameter error occurs
     * @throws NullPointerException if a required object is null
     * @throws IllegalArgumentException if an argument is invalid
     */
    public AccelerateOnOriginalLane(final BasicHuman drivingTask)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        super(drivingTask);
        this.update();
    }

    /**
     * Executes the control action for the acceleration phase.
     * <p>
     * If the current time headway exceeds the minimum, it is reset to the minimum.
     * No operational plan is executed in this state.
     * </p>
     *
     * @return always returns {@code null} as no operational plan is executed in this state
     * @throws ParameterException if a parameter error occurs
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException
    {
        Duration minimumTimeHeadway = this.drivingTask.getParameters().getParameter(ParameterTypes.TMIN);
        if (this.drivingTask.getParameters().getParameter(ParameterTypes.T).gt(minimumTimeHeadway))
        {
            this.drivingTask.getParameters().setParameterResettable(ParameterTypes.T, minimumTimeHeadway);
        }
        return null; // No control action executed in this state

    }

    /**
     * Determines and transitions to the next action state during the acceleration phase of the overtaking maneuver.
     * <p>
     * If the forward time headway falls below the minimum threshold, the follower deceleration for a left lane change is non-negative,
     * or the ego vehicle reaches its desired speed, the maneuver transitions to executing a lane change to the left.
     * </p>
     *
     * @throws ParameterException if a parameter error occurs
     * @throws OperationalPlanException if an operational plan error occurs
     * @throws NullPointerException if a required object is null
     * @throws IllegalArgumentException if an argument is invalid
     */
    @Override
    public void next() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException
    {
        Length forwardDistanceHeadway =
                this.drivingTask.getDirectDefaultSimplePerception().getForwardHeadwayGtu().getDistance();
        Speed egoSpeed = this.drivingTask.getEgoPerception().getSpeed();

        Duration forwardTimeHeadway = new Duration(forwardDistanceHeadway.si / egoSpeed.si, DurationUnit.SI);

        if (forwardTimeHeadway.si <= this.drivingTask.getParameters().getParameter(ParameterTypes.TMIN).si
                || this.drivingTask.getAbstractMirovaVehicle()
                        .getLaneChangeFollowerDeceleration(LateralDirectionality.LEFT).si >= 0
                || egoSpeed.ge(this.drivingTask.getAbstractMirovaVehicle().getGtu().getDesiredSpeed()))
        {
            this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new ExecuteLaneChangeLeft(this.drivingTask));
        }

    }
}
