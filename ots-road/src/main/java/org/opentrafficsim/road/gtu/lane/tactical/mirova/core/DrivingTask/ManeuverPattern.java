package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;

/**
 * * Abstract base class for maneuver patterns in the Mirova tactical planner. This class defines the structure for maneuver
 * patterns, including the initial action state, desire calculation, and lane change directionality. It serves as a foundation
 * for specific maneuver patterns such as lane changes or overtaking maneuvers.
 */
public abstract class ManeuverPattern
{

    /**
     * The current desire value for this maneuver pattern. This value indicates the urgency or motivation for the vehicle to
     * perform a free merge.
     */
    private double desire;

    private final LateralDirectionality laneChangeDirection = null;

    private final ActionState initialActionState = null;

    private final DrivingTask drivingTask = null;

    public abstract void calculateDesire() throws ParameterException;

    /**
     * Returns the initial action state for this maneuver pattern. The initial action state is used by the tactical planner to
     * start the maneuver.
     * @return the initial action state
     */

    public ActionState getInitialActionState()
    {
        return this.initialActionState;
    }

    /**
     * Returns the driving task associated with this maneuver pattern. The driving task provides access to the tactical context
     * and parameters.
     * @return the driving task
     */

    public DrivingTask getDrivingTask()
    {
        return this.drivingTask;
    }

    /**
     * Returns the current desire value for this maneuver pattern. The desire value quantifies the motivation to perform the
     * maneuver, influencing tactical decisions.
     * @return the current desire value
     */
    public double getDesire()
    {
        return this.desire;
    }

    /**
     * Returns the direction of the lane change for this maneuver pattern. For free merge maneuvers, this is typically to the
     * left.
     * @return the lane change direction
     */

    public LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection;
    }
}
