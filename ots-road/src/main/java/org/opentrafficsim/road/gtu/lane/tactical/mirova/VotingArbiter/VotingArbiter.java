package org.opentrafficsim.road.gtu.lane.tactical.mirova.VotingArbiter;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;

import java.util.ArrayList;
import java.util.List;

/**
 * The VotingArbiter class is responsible for aggregating and evaluating multiple ActionAdvice objects.
 * It processes the advices, removes vetoed actions, and determines the most preferred action based on voting logic.
 */
public class VotingArbiter {
    private List<ActionAdvice> advices;

    /**
     * Initializes a new instance of the VotingArbiter class.
     */
    public VotingArbiter() {
        this.advices = new ArrayList<>();
    }

    /**
     * Executes the voting process on a list of ActionAdvice objects.
     *
     * @param listAdvices A list of ActionAdvice objects containing action recommendations.
     * @return The initial ActionState of the highest voted advice, or null if no valid advice is found.
     */
    public ActionState execute(final List<ActionAdvice> listAdvices) {
        this.advices = listAdvices;
        List<ActionAdvice> filteredAdvices = deleteVetos(this.advices);
        // combineVotes(); // Not yet implemented
        return vote(filteredAdvices);
    }

    /**
     * Removes advices where a veto (negative desire) is present.
     * Only actions with non-negative desires are considered for voting.
     *
     * @param advices The list of ActionAdvice objects.
     * @return A filtered list of ActionAdvice objects without vetoes.
     */
    private List<ActionAdvice> deleteVetos(final List<ActionAdvice> advices) {
        List<ActionAdvice> allowed = new ArrayList<>();
        for (ActionAdvice advice : advices) {
            boolean leftAllowed = advice.getLeftLaneDesire() == null || advice.getLeftLaneDesire() >= 0;
            boolean keepAllowed = advice.getKeepLaneDesire() == null || advice.getKeepLaneDesire() >= 0;
            boolean rightAllowed = advice.getRightLaneDesire() == null || advice.getRightLaneDesire() >= 0;
            if (leftAllowed && keepAllowed && rightAllowed) {
                allowed.add(advice);
            }
        }
        return allowed;
    }

    /**
     * Combines the votes from all ActionAdvice objects to determine the overall urge for lateral and longitudinal control.
     * (Not yet implemented.)
     */
    private void combineVotes() {
        // Not yet implemented
    }

    /**
     * Handles situations where conflicting votes are present among the ActionAdvice objects.
     * (Not yet implemented.)
     */
    private void handleConflictingVotes() {
        // Not yet implemented
    }

    /**
     * Calculates the preferred next action by summing up the desires for each action.
     * Returns the initial ActionState of the advice with the highest total desire.
     *
     * @param advices The list of ActionAdvice objects.
     * @return The initial ActionState of the highest voted advice, or null if no valid advice is found.
     */
    private ActionState vote(final List<ActionAdvice> advices) {
        double maxDesire = Double.NEGATIVE_INFINITY;
        ActionState bestActionState = null;
        for (ActionAdvice advice : advices) {
            double sum = 0.0;
            if (advice.getLeftLaneDesire() != null) sum += advice.getLeftLaneDesire();
            if (advice.getKeepLaneDesire() != null) sum += advice.getKeepLaneDesire();
            if (advice.getRightLaneDesire() != null) sum += advice.getRightLaneDesire();
            if (sum > maxDesire) {
                maxDesire = sum;
                bestActionState = advice.getInitialActionState();
            }
        }
        return bestActionState;
    }
}