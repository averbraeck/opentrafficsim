package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge.LaneChangeToTargetGap;

/**
 * Represents the state where the vehicle waits for a suitable condition to perform a forced merge.
 *
 * This state is responsible for monitoring the traffic conditions and waiting until the
 * required gaps in the traffic stream are available to safely initiate a forced lane change.
 * During this state, the vehicle maintains its acceleration and continuously evaluates
 * whether the conditions for a forced merge are met.
 *
 * Responsibilities:
 * - Monitors traffic conditions to determine when a forced merge can be initiated.
 * - Maintains the vehicle's acceleration while waiting for suitable conditions.
 * - Transitions to the forced_merge_lane_change state when the conditions are met.
 *
 * Methods:
 * - executeControl(): Maintains the vehicle's acceleration while waiting.
 * - next(): Transitions to the forced_merge_lane_change state if the conditions for a forced merge are met.
 *
 * Attributes:
 * - drivingTask: The driving task associated with the merge maneuver.
 *
 * Transitions:
 * - To forced_merge_lane_change if the conditions for a forced merge are met.
 */
public class WaitForForcedMerge extends StartForcedMerge {

    /** Time until end of lane. */
    private Duration timeToEndOfLane;

    /** Ego vehicle deceleration for lane change. */
    private Acceleration egoDecel;

    /** Follower vehicle deceleration for lane change. */
    private Acceleration rearDecel;

    public WaitForForcedMerge(final MergingHidas drivingTask) throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException {
        super(drivingTask);
        this.update();
    }

    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
     // Berechne und speichere die Werte als Attribute
        this.timeToEndOfLane = this.drivingTask.getTimeUntilEndOfLane();
        this.egoDecel = this.drivingTask.getAbstractMirovaVehicle()
                .getLaneChangeEgoDeceleration(LateralDirectionality.LEFT);
        this.rearDecel = this.drivingTask.getAbstractMirovaVehicle()
                .getLaneChangeFollowerDeceleration(LateralDirectionality.LEFT);
        return super.update();
    }

    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException {
        this.drivingTask.getAbstractMirovaVehicle().setDesire(1.0,
                this.drivingTask.getParameters().getParameter(ParameterTypes.TAU));
        return null; // No operational plan is executed in this state
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException {
        if (this.timeToEndOfLane.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.LCDUR).times(0.5)))
        {
            if (this.timeToEndOfLane.le(this.drivingTask.getParameters().getParameter(ParameterTypes.LCDUR))
                    && this.rearDecel.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.BCRIT).neg())
                    && this.egoDecel.ge(this.drivingTask.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
            {
                this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new ForcedMergeLaneChange(this.drivingTask));
            }
        }
    }
}
