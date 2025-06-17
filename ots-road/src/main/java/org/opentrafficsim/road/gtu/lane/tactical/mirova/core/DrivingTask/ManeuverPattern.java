package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;

/** * Abstract base class for maneuver patterns in the Mirova tactical planner. This class defines the structure for
 * maneuver patterns, including the initial action state, desire calculation, and lane change directionality.
 * It serves as a foundation for specific maneuver patterns such as lane changes or overtaking maneuvers.
 */
public abstract class ManeuverPattern
{

    /**
     * gets the desire for the maneuver pattern.
     * @param desire The desire value to be get.
     */
    public abstract double getDesire();

    public abstract LateralDirectionality getLaneChangeDirection();

    public abstract void calculateDesire() throws ParameterException;

    public abstract ActionState getInitialActionState();

    public abstract DrivingTask getDrivingTask();
}
