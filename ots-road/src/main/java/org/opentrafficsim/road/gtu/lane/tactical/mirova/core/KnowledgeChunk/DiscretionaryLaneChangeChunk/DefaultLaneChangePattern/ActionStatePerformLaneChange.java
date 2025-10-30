package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.DiscretionaryLaneChangeChunk.DefaultLaneChangePattern;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.*;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.*;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * {@code ActionStatePerformLaneChange}
 * <p>
 * Represents the active execution phase of a lane-change maneuver.
 * The ego vehicle transitions laterally to the target lane while maintaining
 * longitudinal safety using a simplified Two-Leader-Car-Following logic.
 * </p>
 *
 * <p><b>Transitions:</b></p>
 * <ul>
 *   <li><b>→ CompleteLaneChange</b> once the lane-change is completed.</li>
 *   <li><b>→ AbortLaneChange</b> if feasibility or desire constraints are violated mid-change.</li>
 * </ul>
 */
public class ActionStatePerformLaneChange extends ActionState {

    /** Target direction of the lane change (LEFT or RIGHT). */
    private final LateralDirectionality direction;

    /** Desire hysteresis for abort stability. */
    private static final double DESIRE_HYSTERESIS = 0.1;

    /** Cached origin lane to detect completion. */
    private final Lane originLane;


    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /** ActionStatePerformLaneChange constructor.
     * @param pattern
     * @param direction
     */
    public ActionStatePerformLaneChange(final ManeuverPattern pattern, final LateralDirectionality direction) {
        super(pattern);
        this.direction = direction;
        this.originLane = this.vehicle.getGtu().getLane();
    }

    // ----------------------------------------------------------------------
    // Core control logic
    // ----------------------------------------------------------------------

    /**
     * Executes longitudinal control using a simplified Two-Leader Car-Following logic.
     * <p>
     * The ego vehicle simultaneously considers the leader on its current lane and
     * the leader on the target lane. The resulting acceleration is the most restrictive
     * (minimum) across these influences.
     * </p>
     * @throws NetworkException
     * @throws GtuException
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
        InfrastructureContext infraCtx = this.vehicle.getContext(InfrastructureContext.class);
        NeighborsContext neighborsCtx = this.vehicle.getContext(NeighborsContext.class);
        EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

        Speed egoSpeed = egoCtx.getEgoSpeed();
        Parameters params = this.vehicle.getGtu().getParameters();

        // Retrieve leaders via context (no direct perception access here!)
        Headway currentLeader = neighborsCtx.getCurrentLeader();


        // Start with relaxed car-following acceleration (already includes Desire effects)
        Acceleration minAcc = this.vehicle.computeLongitudinalAcceleration();


        // Add target-lane leader constraint
        if (this.vehicle.getGtu().getLane() == this.originLane) {
            // Only consider target-lane leader if still on origin lane;
            HeadwayGtu targetLeader = neighborsCtx.getLeaderInDirection(this.direction);
            if (targetLeader != null) {
                Acceleration aTarget = CarFollowingUtil.followSingleLeader(
                        this.vehicle.getCarFollowingModel(),
                        params,
                        egoSpeed,
                        infraCtx.getCurrentSpeedLimit(),
                        targetLeader.getDistance(),
                        targetLeader.getSpeed());
                minAcc = Acceleration.min(minAcc, aTarget);
            }
        }

        if (this.vehicle.getLaneChange().isChangingLane()) {
            // Create operational plan
            return new SimpleOperationalPlan(
                    minAcc,
                    params.getParameter(ParameterTypes.DT),
                    null);
        } else {
            // Create operational plan
            return new SimpleOperationalPlan(
                    minAcc,
                    params.getParameter(ParameterTypes.DT),
                    this.direction);
        }
        }

    // ----------------------------------------------------------------------
    // Transitions
    // ----------------------------------------------------------------------

    /**
     * Proceeds to {@link ActionStateCompleteLaneChange} when the lane change is completed.
     */
    @Override
    public void next() throws ParameterException, OperationalPlanException {
        boolean finished = !this.vehicle.getLaneChange().isChangingLane()
                && !this.originLane.equals(this.vehicle.getGtu().getLane());

        if (finished) {
            ActionState nextState = new ActionStateCompleteLaneChange(this.maneuverPattern, this.direction);
            transitionTo(nextState);
        }
    }

    /**
     * Checks whether the lane-change should be aborted (safety or desire violation).
     */
    @Override
    public void abort() throws ParameterException, OperationalPlanException {
        // abort Lane Change is currently not supported
    //        NeighborsContext neighbors = this.vehicle.getContext(NeighborsContext.class);
    //        Acceleration bDes = this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.B);
    //        Acceleration egoDecel = neighbors.getEgoDeceleration(this.direction);
    //        Acceleration follDecel = neighbors.getFollowerDeceleration(this.direction);
    //
    //        boolean unsafe = egoDecel.lt(bDes) || follDecel.lt(bDes);
    //        boolean lowDesire = this.vehicle.getDesire() < this.vehicle.getDFree() - DESIRE_HYSTERESIS;
    //
    //        if (unsafe || lowDesire) {
    //            ActionState nextState = new ActionStateAbortLaneChange(this.maneuverPattern, this.direction);
    //            transitionTo(nextState);
    //        }
    }

    @Override
    public String toString() {
        return "ActionStatePerformLaneChange[" + this.direction + "]";
    }
}
