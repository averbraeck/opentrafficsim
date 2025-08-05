package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.base.parameters.ParameterException;

/**
 * Implements the Forced Merge maneuver pattern according to the Hidas (2005) ramp merging model.
 * <p>
 * This maneuver pattern is responsible for evaluating and executing a forced merge from a ramp onto a main lane,
 * typically when a free merge is not possible. It defines the initial action state, the direction of the lane change,
 * and the desire value for the maneuver.
 * </p>
 */
public class ForcedMerge extends ManeuverPattern
{
    /**
     * The current desire value for this maneuver pattern.
     * This value indicates the urgency or motivation for the vehicle to perform a forced merge.
     */
    private double desire;

    /**
     * The direction of the lane change for the forced merge maneuver.
     * For ramp merging, this is typically {@link LateralDirectionality#LEFT}.
     */
    private final LateralDirectionality laneChangeDirection;

    /**
     * The initial action state for the forced merge maneuver.
     * This state represents the starting point of the maneuver in the tactical planner's state machine.
     */
    private final ActionState initialActionState;

    /**
     * The driving task associated with this maneuver pattern.
     * Provides access to the tactical context and parameters required for the merge.
     */
    private final MergingHidas drivingTask;

    /**
     * Constructs a new ForcedMerge maneuver pattern for the specified driving task.
     * Initializes the desire value and sets up the initial action state.
     *
     * @param drivingTask the merging driving task context for this maneuver pattern
     */
    public ForcedMerge(final MergingHidas drivingTask)
    {
        this.drivingTask = drivingTask;
        this.laneChangeDirection = LateralDirectionality.LEFT;
        this.initialActionState = new StartForcedMerge(drivingTask);
        this.desire = 5.0; // Example value, adjust as needed
    }

    /**
     * Returns the current desire value for this maneuver pattern.
     *
     * @return the current desire value
     */
    @Override
    public double getDesire()
    {
        return this.desire;
    }

    /**
     * Returns the direction of the lane change for this maneuver pattern.
     *
     * @return the lane change direction
     */
    @Override
    public LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection;
    }

    /**
     * Calculates and updates the desire value for this maneuver pattern.
     * This method should be called to re-evaluate the urgency of the maneuver based on the current situation.
     *
     * @throws ParameterException if required parameters cannot be retrieved
     */
    @Override
    public void calculateDesire() throws ParameterException
    {
        // Example: set a fixed desire or implement logic based on traffic situation
        this.desire = 5.0;
    }

    /**
     * Returns the initial action state for this maneuver pattern.
     *
     * @return the initial action state
     */
    @Override
    public ActionState getInitialActionState()
    {
        return this.initialActionState;
    }

    /**
     * Returns the driving task associated with this maneuver pattern.
     *
     * @return the driving task
     */
    @Override
    public DrivingTask getDrivingTask()
    {
        return this.drivingTask;
    }
}
