package org.opentrafficsim.road.gtu.lane.tactical.mirova.patterns;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.KnowledgeChunk;

/**
 * Default lane-change pattern used as a generic fallback maneuver.
 * <p>
 * This pattern represents a basic, always-applicable lane change.
 * It has no explicit contextual requirements and is considered valid
 * whenever it is requested by the tactical planner.
 * </p>
 * <p>
 * The pattern performs only an {@link #checkAbility()} feasibility check
 * (e.g. sufficient gap, safe relative speed) before execution.
 * </p>
 */
public class DefaultLaneChangePattern extends ManeuverPattern {

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
    public DefaultLaneChangePattern(final KnowledgeChunk knowledgeChunk,
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
        return true;
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
        var neighbors = getKnowledgeChunk().getNeighborsPerception();
        double gap = this.direction.isLeft() ? neighbors.getGapLeft() : neighbors.getGapRight();
        double relV = this.direction.isLeft() ? neighbors.getRelSpeedLeft() : neighbors.getRelSpeedRight();
        return gap > 15.0 && relV > -5.0;
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
            this.initialActionState = new ActionState("PrepareLaneChange(" + this.direction + ")", this);
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
