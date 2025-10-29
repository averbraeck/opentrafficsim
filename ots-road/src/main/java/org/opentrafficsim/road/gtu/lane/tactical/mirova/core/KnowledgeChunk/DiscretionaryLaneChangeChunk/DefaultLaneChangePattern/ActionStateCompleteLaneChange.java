package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.DiscretionaryLaneChangeChunk.DefaultLaneChangePattern;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;

/**
 * {@code ActionStateCompleteLaneChange}
 * <p>
 * Final state of a lane-change maneuver.
 * This state performs no control actions but resets the vehicle’s tactical state
 * and flags the maneuver as finished. The next simulation step will therefore
 * return to the default tactical control logic (e.g., standard car-following).
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Reset maneuver status flags and active ActionState.</li>
 *   <li>Mark the lane change as completed in the tactical planner.</li>
 *   <li>Ensure that no residual acceleration or lateral control persists.</li>
 * </ul>
 */
public class ActionStateCompleteLaneChange extends ActionState {

    /** Direction of the completed lane change (LEFT or RIGHT). */
    private final LateralDirectionality direction;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    public ActionStateCompleteLaneChange(final ManeuverPattern pattern, final LateralDirectionality direction) {
        super(pattern);
        this.direction = direction;
    }

    // ----------------------------------------------------------------------
    // Core behavior
    // ----------------------------------------------------------------------

    /**
     * Performs no control action.
     * Instead, this method ensures that the vehicle returns to its standard
     * control loop in the next tactical update step.
     *
     * @return a neutral operational plan with zero acceleration (optional placeholder)
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException {
        // Immediately finalize maneuver
        finalizeManeuver();
        return null;
    }

    /**
     * No transition follows — this is the terminal state.
     */
    @Override
    public void next() {
        // No next state: this is terminal
    }

    /**
     * No abort possible — lane change already completed successfully.
     */
    @Override
    public void abort() {
        // No abort logic after completion
    }

    // ----------------------------------------------------------------------
    // Helper logic
    // ----------------------------------------------------------------------

    /**
     * Marks the maneuver as completed and resets the tactical vehicle state.
     * This ensures that subsequent updates revert to normal tactical reasoning.
     */
    private void finalizeManeuver() {
        this.vehicle.setRunningManeuver(false);
        this.vehicle.setCurrentActionState(null);
        this.active = false;

    }

    @Override
    public String toString() {
        return "ActionStateCompleteLaneChange[" + this.direction + "]";
    }
}
