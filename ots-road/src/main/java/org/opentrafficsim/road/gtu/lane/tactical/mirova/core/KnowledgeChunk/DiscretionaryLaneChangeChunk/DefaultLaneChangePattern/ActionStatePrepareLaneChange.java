package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.DiscretionaryLaneChangeChunk.DefaultLaneChangePattern;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;

/**
 * {@code ActionStatePrepareLaneChange}
 * <p>
 * First phase of the lane-change maneuver.
 * The vehicle maintains normal car-following behavior while continuously
 * checking whether a lane change in the desired direction becomes feasible.
 * </p>
 *
 * <p><b>Transitions:</b></p>
 * <ul>
 *   <li><b>→ PerformLaneChange</b> if the safety conditions (ego + follower deceleration) are met.</li>
 *   <li><b>→ AbortLaneChange</b> if feasibility fails or the desire drops significantly below threshold.</li>
 * </ul>
 */
public class ActionStatePrepareLaneChange extends ActionState {

    /** Target direction of the lane change (LEFT or RIGHT). */
    private final LateralDirectionality direction;

    /** Desire drop hysteresis to prevent oscillations. */
    private static final double DESIRE_HYSTERESIS = 0.1;

    public ActionStatePrepareLaneChange(final ManeuverPattern pattern, final LateralDirectionality direction) {
        super(pattern);
        this.direction = direction;
    }

    /**
     * Executes standard car-following control.
     * <p>
     * During the preparation phase, no lateral motion is yet performed. The vehicle adapts
     * its longitudinal acceleration using the MIROVA car-following controller.
     * </p>
     * @throws NetworkException
     * @throws GtuException
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
        // Delegate to the MIROVA vehicle’s car-following behavior

        Acceleration acceleration = this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getCurrentCarFollowingAcceleration();
        SimpleOperationalPlan plan = new SimpleOperationalPlan(
                acceleration,
            this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT)
        );

        if (this.direction == LateralDirectionality.LEFT) {
            plan.setIndicatorIntentLeft();;
        } else if (this.direction == LateralDirectionality.RIGHT) {
            plan.setIndicatorIntentRight();
        }
        return plan;

    }

    /**
     * Evaluates transition to the next lane-change phase.
     * <p>
     * The state transitions to {@code ActionStatePerformLaneChange} if all feasibility
     * conditions are met (ego and follower decelerations above threshold, lane not ending too soon).
     * </p>
     * @return
     * @throws NetworkException
     * @throws GtuException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    @Override
    public SimpleOperationalPlan next() throws ParameterException, NullPointerException, IllegalArgumentException, GtuException, NetworkException {
        if (checkFeasibility()) {
            ActionState nextState =
                    new ActionStatePerformLaneChange(this.maneuverPattern, this.direction);
            return transitionTo(nextState);
        }
        return null; // remain in current state
    }

    /**
     * Checks whether the maneuver should be aborted.
     * <p>
     * The state aborts if the feasibility check fails or the lane-change desire
     * drops significantly below the discretionary threshold.
     * </p>
     * @return
     */
    @Override
    public SimpleOperationalPlan abort() throws ParameterException, OperationalPlanException {
        if (!checkFeasibility() ||
            this.vehicle.getDesire() < this.vehicle.getDFree() - DESIRE_HYSTERESIS) {
            this.vehicle.setRunningManeuver(false);
            this.active = false;
        }
        return null;
    }

    /**
     * Determines if the current traffic situation allows safe initiation of the lane change.
     * <p>
     * The check is based on the NeighborsContext and current deceleration capabilities
     * of both ego and follower vehicles on the target lane.
     * </p>
     *
     * @return true if the lane change can be safely initiated
     * @throws ParameterException if model parameters cannot be retrieved
     */
    private boolean checkFeasibility() throws ParameterException {
        NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
        InfrastructureContext infra = this.vehicle.getContext(InfrastructureContext.class);
        EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

        Acceleration bDes = this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.B).neg();
        Acceleration egoDecel = neighbors.getEgoDeceleration(this.direction);
        Acceleration follDecel = neighbors.getFollowerDeceleration(this.direction);

        Length desiredRearHeadway = egoCtx.getDesiredRearHeadway(this.direction);
        Length rearHeadway = neighbors.getRearGapDistance(this.direction);

        Length desiredFrontHeadway = egoCtx.getDesiredFrontHeadway(this.direction);
        Length frontHeadway = neighbors.getFrontGapDistance(this.direction);


        return egoDecel.gt(bDes) && follDecel.gt(bDes) && rearHeadway.gt(desiredRearHeadway) && frontHeadway.gt(desiredFrontHeadway);

    }

    @Override
    public String toString() {
        return "ActionStatePrepareLaneChange[" + this.direction + "]";
    }
}
