package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.SupportMerge.SupportMergeHidas;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.AbstractMirovaVehicle;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;
/**
 * Implements the cooperative merging behavior based on the methodology described by Hidas (2005).
 *
 * This class models the cooperative behavior of a vehicle to support merging maneuvers, either
 * by creating gaps for other vehicles or by adjusting its own behavior to facilitate merging.
 * The approach is inspired by the driver behavior model for freeway–ramp merges as outlined in
 * Hidas (2005), emphasizing cooperation and anticipatory behavior.
 *
 * Key Concepts from Hidas (2005):
 * - Cooperative deceleration: Vehicles may decelerate to create gaps for merging vehicles.
 * - Anticipatory behavior: Drivers anticipate the actions of surrounding vehicles to adjust
 *   their speed and position.
 * - Gap acceptance: Drivers evaluate the feasibility of merging based on available gaps.
 *
 * Responsibilities:
 * - Evaluate whether cooperative behavior is required to support merging.
 * - Decide on appropriate actions, such as deceleration or lane changes, to facilitate merging.
 * - Transition between different states of cooperative behavior, such as deceleration or maintaining gaps.
 *
 * Attributes:
 * - decreasedDesiredHeadwayFactor: Factor to reduce the desired headway during cooperative behavior.
 * - timeAnticipation: Time (in seconds) the vehicle anticipates for cooperative maneuvers.
 * - cooperativeDeceleration: Deceleration rate (in m/s²) used for creating gaps.
 *
 * Reference:
 * Hidas, P. (2005). Modeling Driver Behavior at Freeway–Ramp Merges. Transportation Research Part C:
 * Emerging Technologies, 13(5-6), 321-336.
 */
public class SupportMerge extends DrivingTask {

    protected double decreasedDesiredHeadwayFactor = 0.5;
    protected double timeAnticipation = 3.0; // seconds
    protected double cooperativeDeceleration = -2.5; // m/s²

    // These fields are set in getActivation
    protected int nextLaneRightmost;
    protected int routeLaneOffset;
    protected String nextLink;
    protected String targetAdjacentVehicleId;

    public SupportMerge(final AbstractMirovaVehicle contextVehicle) {
        super(contextVehicle);
    }

    /**
     * Determines whether the cooperative merging task should be activated.
     *
     * This method checks if the next link in the route cannot be reached from the rightmost lane,
     * indicating that cooperative behavior is required to support merging.
     *
     * @return true if the task should be activated, false otherwise.
     */
    @Override
    public boolean getActivation() {
        // Example: get next link and lane info from the context vehicle
        this.nextLink = (String) this.AbstractMirovaVehicle.getContextDriverDevice().getVissimVehicle().AttValue("NEXTLINK");
        String minLanes = (String) this.AbstractMirovaVehicle.getContextDriverDevice()
                .getVissimConnection().getNet().getLinks().ItemByKey(this.nextLink)
                .AttValue("MIN:LANES\\FROMLANES");
        this.nextLaneRightmost = Integer.parseInt(minLanes.split("-")[1]);
        this.routeLaneOffset = this.AbstractMirovaVehicle.getCurrentLaneId() - this.nextLaneRightmost;

        return (this.nextLaneRightmost > 1) && (this.routeLaneOffset >= 0);
    }

    /**
     * Placeholder for calculating the utility of the cooperative merging maneuver.
     *
     * This method can be extended to evaluate the benefits or costs of cooperative behavior
     * based on factors such as traffic density, gap size, and vehicle speed.
     *
     * @return the latest utility value calculated for the task.
     */
    @Override
    public double calculateUtility() {
        // Placeholder
        return 0.0;
    }

    /**
     * Decides the appropriate maneuver advice for cooperative merging.
     *
     * This method evaluates the current lane offset and checks whether a lane change or
     * cooperative deceleration is required to support merging. It returns an ActionAdvice
     * object with the recommended action.
     *
     * @return the advice for the cooperative merging maneuver.
     */
    @Override
    public ActionAdvice decideManeuverAdvice() {
        this.targetAdjacentVehicleId = this.AbstractMirovaVehicle.getFrontAdjacentLaneVehicleId();
        if (this.routeLaneOffset > 0) {
            return new ActionAdvice(1.5); // keep_lane_desire=1.5
        } else {
            if (this.AbstractMirovaVehicle.checkOperationalLaneChange(
                    this.AbstractMirovaVehicle.getContextDriverDevice().getMinimumTimeHeadway(),
                    this.AbstractMirovaVehicle.getContextDriverDevice().getMinimumTimeHeadway(),
                    -1, -1, 1)) {
                // Replace with actual state class
                return new ActionAdvice(
                        /* initialActionState= */ new StartCooperativeLanechangeManeuver(this),
                        /* leftLaneDesire= */ 1,
                        /* rightLaneDesire= */ -1
                );
            } else if (checkCooperationDecelerationHidas()) {
                // Replace with actual state class
                return new ActionAdvice(
                        /* initialActionState= */ new StartCooperativeDecelerationManeuver(this),
                        /* keepLaneDesire= */ 1,
                        /* rightLaneDesire= */ -1
                );
            } else {
                return new ActionAdvice(
                        /* rightLaneDesire= */ -1
                );
            }
        }
    }

    /**
     * Checks whether cooperative deceleration is feasible based on the Hidas (2005) model.
     *
     * This method evaluates whether the vehicle can decelerate to create a gap for merging
     * vehicles while maintaining safe distances and speeds.
     *
     * @return true if cooperative deceleration is feasible, false otherwise.
     */
    public boolean checkCooperationDecelerationHidas() {
        this.AbstractMirovaVehicle.updateLanechangeFrontValues(-1);

        double frontSpaceHeadway = this.AbstractMirovaVehicle.getFrontSpaceHeadway();
        double currentSpeed = this.AbstractMirovaVehicle.getCurrentSpeed();
        double frontSpeedDelta = this.AbstractMirovaVehicle.getFrontSpeedDelta();
        double minTimeHeadway = this.AbstractMirovaVehicle.getContextDriverDevice().getMinimumTimeHeadway();
        double currentSpaceHeadway = this.AbstractMirovaVehicle.getCurrentSpaceHeadway();

        double anticipatedGapHeadway = frontSpaceHeadway
                - (currentSpeed * this.timeAnticipation / 3.6
                + (this.cooperativeDeceleration * Math.pow(this.timeAnticipation, 2)) / 2)
                + (currentSpeed - frontSpeedDelta) * this.timeAnticipation / 3.6;

        double anticipatedGapHeadwayWithoutDeceleration = frontSpaceHeadway
                - (currentSpeed * this.timeAnticipation / 3.6)
                + (currentSpeed - frontSpeedDelta) * this.timeAnticipation / 3.6;

        double anticipatedEgoSpeed = currentSpeed + (this.cooperativeDeceleration * this.timeAnticipation * 3.6);

        boolean condCooperationReasonable = anticipatedGapHeadway >= minTimeHeadway * anticipatedEgoSpeed / 3.6;
        boolean condCooperationNotNeeded = anticipatedGapHeadwayWithoutDeceleration >= minTimeHeadway * currentSpeed / 3.6;
        boolean condLeaderCanCooperate = (anticipatedGapHeadway - currentSpaceHeadway) >= minTimeHeadway * anticipatedEgoSpeed / 3.6;

        // Optional: print debug info if needed

        return condCooperationReasonable && !condLeaderCanCooperate && !condCooperationNotNeeded;
    }
}
