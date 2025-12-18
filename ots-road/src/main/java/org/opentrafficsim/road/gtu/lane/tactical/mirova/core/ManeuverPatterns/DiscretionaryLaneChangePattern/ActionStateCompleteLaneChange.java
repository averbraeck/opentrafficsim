package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.DiscretionaryLaneChangePattern;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;

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
     * @throws NetworkException
     * @throws GtuException
     */
    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, GtuException, NetworkException {
        // Immediately finalize maneuver
        finalizeManeuver();
        return new SimpleOperationalPlan(
                this.vehicle.getContextManager().getCategory("Ego", EgoContext.class).getCurrentCarFollowingAcceleration(),
            this.vehicle.getGtu().getParameters().getParameter(ParameterTypes.DT),
            LateralDirectionality.NONE
        );
    }

    /**
     * No transition follows — this is the terminal state.
     * @return
     */
    @Override
    public SimpleOperationalPlan next() {
        // No next state: this is terminal
        return null;
    }

    /**
     * No abort possible — lane change already completed successfully.
     * @return
     */
    @Override
    public SimpleOperationalPlan abort() {
        // No abort logic after completion
        return null;
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
