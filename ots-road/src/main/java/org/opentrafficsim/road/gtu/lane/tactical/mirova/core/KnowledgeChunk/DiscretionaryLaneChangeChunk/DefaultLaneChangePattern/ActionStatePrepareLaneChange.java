package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.DiscretionaryLaneChangeChunk.DefaultLaneChangePattern;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.base.parameters.ParameterTypes;

/**
 * State 1 – prepares the lane change.
 * <p>
 * Executes standard car-following while checking if the lane-change remains feasible.
 * </p>
 */
public class ActionStatePrepareLaneChange extends ActionState {

    private final LateralDirectionality direction;

    public ActionStatePrepareLaneChange(final ManeuverPattern pattern, final LateralDirectionality direction) {
        super(pattern);
        this.direction = direction;
    }

    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException {
        // Standard car-following behavior until ready to change
        return this.vehicle.desireBasedFollowingAcceleration();
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException {
        if (checkAbility()) {
            // Transition to actual lane-change execution
            ActionStatePerformLaneChange nextState =
                    new ActionStatePerformLaneChange(this.maneuverPattern, this.direction);
            transitionTo(nextState);
        }
    }

    @Override
    public void abort() throws ParameterException, OperationalPlanException {
        // Abort if the desire drops to zero or feasibility fails -> + 0.1 to avoid oscillations
        if (!checkAbility() || this.vehicle.getDesire() < this.vehicle.getDFree() + 0.1 ) {
            this.vehicle.setRunningManeuver(false);
            this.active = false;
        }
    }

    /**
     * Re-checks whether the lane change remains feasible using the NeighborsContext.
     */
    private boolean checkAbility() throws ParameterException {
        NeighborsContext nctx = this.vehicle.getContext(NeighborsContext.class);
        Acceleration bDes = this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.B);
        Acceleration egoDecel = nctx.getEgoDeceleration(this.direction);
        Acceleration follDecel = nctx.getFollowerDeceleration(this.direction);

        return egoDecel.gt(bDes) && follDecel.gt(bDes);
    }
}
