package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;

/**
 * Represents the cruise driving task for freeway driving.
 *
 * This task is always active as long as the vehicle is on the freeway.
 * It considers overtaking and right-hand rule maneuvers based on the current traffic situation.
 */
public class BasicHuman extends DrivingTask {

    public BasicHuman(final AbstractMirovaVehicle contextVehicle) {
        super(contextVehicle);
    }

    /**
     * Returns boolean if driving task should be further considered in decision over next action.
     * Always on as long as vehicle is on freeway (returns true, because we do not consider other scenarios at the moment).
     */
    @Override
    public boolean getActivation() {
        return true;
    }

    /**
     * Placeholder for utility calculation.
     */
    @Override
    public double calculateUtility() {
        // Not implemented
        return 0.0;
    }

    /**
     * Decides the appropriate maneuver advice for cruising.
     * Considers overtaking and right-hand rule maneuvers.
     */
    @Override
    public ActionAdvice decideManeuverAdvice() {
        AbstractMirovaVehicle v = this.AbstractMirovaVehicle;

        v.updateLanechangeFrontValues(
            1,
            v.getContextDriverDevice().getDistancePerceptionThreshold()
        );

        if (conditionOvertaking()) {
            v.updateLanechangeRearValues(
                1,
                v.getContextDriverDevice().getDistancePerceptionThreshold()
            );
            if (conditionLanechangeLeftSafety()) {
                double lcDesire = 1 + (
                    v.getDesiredSpeedDelta()
                    - v.getContextDriverDevice().getMinimumDeltaDesiredSpeed()
                ) / (
                    v.getContextDriverDevice().getMaximumDeltaDesiredSpeed()
                    - v.getContextDriverDevice().getMinimumDeltaDesiredSpeed()
                );
                return new ActionAdvice(
                    new StartOvertakingManeuver(this),
                    lcDesire,
                    null
                );
            }
        } else if (conditionRightHandRule()) {
            return new ActionAdvice(
                new StartLanechangeRechtsfahrgebotManeuver(this),
                null,
                1.0
            );
        } else {
            return null;
        }
        return null;
    }

    /**
     * Checks if overtaking conditions are met.
     */
    private boolean conditionOvertaking() {
        AbstractMirovaVehicle v = this.AbstractMirovaVehicle;
        if (0 < v.getCurrentSpaceHeadway()
            && v.getCurrentSpaceHeadway() < v.getContextDriverDevice().getDistancePerceptionThreshold()
            && v.getDesiredSpeedDelta() > v.getContextDriverDevice().getMinimumDeltaDesiredSpeed()
            && ((int) v.getContextDriverDevice().getVissimVehicle().AttValue("LANE\\INDEX")
                < (int) v.getContextDriverDevice().getVissimVehicle().AttValue("LANE\\LINK\\COUNT:LANES"))
        ) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a left lane change is safe.
     */
    private boolean conditionLanechangeLeftSafety() {
        AbstractMirovaVehicle v = this.AbstractMirovaVehicle;
        return v.checkOperationalLaneChange(
            v.getContextDriverDevice().getMinimumTimeHeadway() * 0.75,
            v.getContextDriverDevice().getMinimumTimeHeadway() * 0.75,
            v.getContextDriverDevice().getAcceptedDecelerationEgoVehicle(),
            v.getContextDriverDevice().getAcceptedDecelerationRearVehicle()
        );
    }

    /**
     * Checks if the right-hand rule applies for a lane change to the right.
     */
    private boolean conditionRightHandRule() {
        AbstractMirovaVehicle v = this.AbstractMirovaVehicle;
        v.updateLanechangeFrontValues(
            -1,
            v.getContextDriverDevice().getDistancePerceptionThreshold()
        );
        v.updateLanechangeRearValues(
            -1,
            v.getContextDriverDevice().getDistancePerceptionThreshold()
        );
        boolean canChange = v.checkOperationalLaneChange(
            v.getContextDriverDevice().getMinimumTimeHeadway() * 0.75,
            v.getContextDriverDevice().getMinimumTimeHeadway() * 0.75,
            v.getContextDriverDevice().getMinimumTimeHeadway(),
            v.getContextDriverDevice().getMinimumTimeHeadway()
        );
        boolean enoughFreeflow = v.getFrontFreeflowtime() >= v.getContextDriverDevice().getMinimumFrontFreeflowtime();
        boolean notLeftmost = ((int) v.getContextDriverDevice().getVissimVehicle().AttValue("LANE\\INDEX") > 1);

        return canChange && enoughFreeflow && notLeftmost;
    }
}
