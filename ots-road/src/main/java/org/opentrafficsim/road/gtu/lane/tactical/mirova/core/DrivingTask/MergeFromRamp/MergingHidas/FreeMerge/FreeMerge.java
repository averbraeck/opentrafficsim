package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.base.parameters.ParameterException;

/**
 * Implements the Free Merge maneuver pattern according to the Hidas (2005) ramp merging model.
 * <p>
 * This maneuver pattern is responsible for evaluating and executing a free merge from a ramp onto a main lane. It defines the
 * initial action state, the direction of the lane change, and the desire value for the maneuver. The pattern is used by the
 * tactical planner to determine when and how to initiate a free merge, based on the current traffic situation and the merging
 * model parameters.
 * </p>
 */
public class FreeMerge extends ManeuverPattern
{
    /**
     * The current desire value for this maneuver pattern. This value indicates the urgency or motivation for the vehicle to
     * perform a free merge.
     */
    private double desire;

    /**
     * The direction of the lane change for the free merge maneuver. For ramp merging, this is typically
     * {@link LateralDirectionality#LEFT}.
     */
    private final LateralDirectionality laneChangeDirection = LateralDirectionality.LEFT;

    /**
     * The initial action state for the free merge maneuver. This state represents the starting point of the maneuver in the
     * tactical planner's state machine.
     */
    private final ActionState initialActionState;

    /**
     * The driving task associated with this maneuver pattern. Provides access to the tactical context and parameters required
     * for the merge.
     */
    private final MergingHidas drivingTask;

    /**
     * Constructs a new FreeMerge maneuver pattern for the specified driving task. Initializes the desire value and sets up the
     * initial action state.
     * @param drivingTask the merging driving task context for this maneuver pattern
     */
    public FreeMerge(final MergingHidas drivingTask)
    {

        this.drivingTask = drivingTask;
        this.desire = 3.0;
        this.initialActionState = new StartFreeMergeToTargetGap(this.drivingTask);
    }

    /**
     * Calculates and updates the desire value for this maneuver pattern. This method should be called to re-evaluate the
     * urgency of the maneuver based on the current situation.
     * @throws ParameterException if required parameters cannot be retrieved
     */
    @Override
    public void calculateDesire() throws ParameterException
    {
        this.desire = 3.0;
    }

}
