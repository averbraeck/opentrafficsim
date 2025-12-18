package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePattern;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;

/**
 * Default lane-change pattern used as a generic fallback maneuver.
 * <p>
 * This pattern represents a basic lane change pattern which is applicable if requirements on
 * resulting deceleration are met.
 * It has no explicit contextual requirements.
 * </p>
 * <p>
 * The pattern performs only an {@link #checkAbility()} feasibility check
 * (e.g. sufficient gap, safe relative speed) before execution.
 * </p>
 */
public class DiscretionaryLaneChangePattern extends ManeuverPattern {

    /** Direction of the lane change (LEFT or RIGHT). */
    private final LateralDirectionality direction;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a default lane-change pattern.
     *
     * @param knowledgeChunk reference to the associated knowledge chunk
     * @param direction desired lateral direction (left or right)
     */
    public DiscretionaryLaneChangePattern(final KnowledgeChunk knowledgeChunk,
                                    final LateralDirectionality direction) {
        super(PatternType.FREE_LC, knowledgeChunk);
        this.direction = direction;
    }

    // ----------------------------------------------------------------------
    // Evaluation interface
    // ----------------------------------------------------------------------

    /**
     * Always applicable — this pattern has no explicit contextual constraints.
     * @return true
     */
    @Override
    public boolean checkContext() {
        if (this.knowledgeChunk.getMirovaTacticalPlanner().getLaneChangeDesire().dominantDirection().equals(this.direction)) {
//            System.out.println("DefaultLaneChangePattern: checkContext true for direction " + this.direction);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Evaluates whether a lane change in the specified direction is currently feasible.
     * <p>
     * This default implementation uses simple heuristics: sufficient gap and acceptable relative speed.
     * Concrete subclasses may override this for more advanced feasibility checks.
     * </p>
     *
     * @return {@code true} if the maneuver is physically feasible
     * @throws ParameterException if perception data cannot be accessed
     */
    @Override
    public boolean checkAbility() throws ParameterException {
        NeighborsContext nctx = this.vehicle.getContext(NeighborsContext.class);
        Acceleration bDes = this.knowledgeChunk.getParameters().getParameter(ParameterTypes.B).neg();
        EgoContext egoCtx = this.vehicle.getContext(EgoContext.class);

        Length desiredRearHeadway = egoCtx.getDesiredRearHeadway(this.direction);
        Length rearHeadway = nctx.getRearGapDistance(this.direction);

        Length desiredFrontHeadway = egoCtx.getDesiredFrontHeadway(this.direction);
        Length frontHeadway = nctx.getFrontGapDistance(this.direction);

        if (nctx.getEgoDeceleration(this.direction).gt(bDes) && nctx.getFollowerDeceleration(this.direction).gt(bDes)
                && rearHeadway.gt(desiredRearHeadway) && frontHeadway.gt(desiredFrontHeadway))
                {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Initializes and returns the first {@link ActionState} for this maneuver.
     * Typically this is a "prepare lane change" state.
     *
     * @return initial action state
     */
    @Override
    public ActionState getInitialActionState() {
        if (this.initialActionState == null) {
            this.initialActionState = new ActionStatePrepareLaneChange(this, this.direction);
        }
        return this.initialActionState;
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the lane-change direction.
     *
     * @return {@link LateralDirectionality}
     */
    public LateralDirectionality getDirection() {
        return this.direction;
    }

    @Override
    public String toString() {
        return "DefaultLaneChangePattern[" + this.direction + "]";
    }
}
