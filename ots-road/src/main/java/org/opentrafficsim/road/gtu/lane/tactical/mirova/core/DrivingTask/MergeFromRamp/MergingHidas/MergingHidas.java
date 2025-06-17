package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;

/**
 * Implements the ramp merging behavior based on the methodology described by Hidas (2005).
 * <p>
 * This class models the merging process of a vehicle transitioning from an on-ramp to a freeway.
 * It evaluates the merging conditions, identifies suitable gaps, and determines the appropriate
 * actions to safely and efficiently complete the merge. The approach is inspired by the driver
 * behavior model for freeway–ramp merges as outlined in Hidas (2005).
 * </p>
 *
 * <h3>Key Concepts from Hidas (2005):</h3>
 * <ul>
 *   <li><b>Anticipatory behavior</b>: Drivers anticipate the actions of surrounding vehicles to adjust
 *   their speed and position for merging.</li>
 *   <li><b>Gap acceptance</b>: Drivers evaluate the available gaps in the traffic stream and decide
 *   whether to merge based on the feasibility of the gap.</li>
 *   <li><b>Cooperative behavior</b>: Leading and following vehicles in the target gap may adjust their
 *   behavior to facilitate the merging process.</li>
 * </ul>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Evaluate the feasibility of merging based on traffic conditions and gap availability.</li>
 *   <li>Identify the target gap and calculate the required actions for merging.</li>
 *   <li>Transition between different states of the merging process, such as acceleration, lane change,
 *   and finalization.</li>
 *   <li>Handle situations where merging is not feasible by transitioning to abort or forced merge states.</li>
 * </ul>
 *
 * <h3>Attributes:</h3>
 * <ul>
 *   <li>currentActionState (ActionState): The current state of the merging process.</li>
 *   <li>decreasedDesiredHeadwayFactor (double): A factor to reduce the desired headway during merging.</li>
 *   <li>timeAnticipation (double): The time (in seconds) the vehicle anticipates for merging.</li>
 *   <li>nextLink (int): The ID of the next link the vehicle is merging into.</li>
 *   <li>nextLaneRightmost (int): The rightmost lane ID of the next link.</li>
 *   <li>routeLaneOffset (int): The difference between the current lane and the target lane.</li>
 *   <li>vehicleIdGapFollower (int): The ID of the following vehicle in the target gap.</li>
 *   <li>vehicleIdGapLeader (int): The ID of the leading vehicle in the target gap.</li>
 *   <li>startTimeMerge (Double): The simulation time when the merge process starts.</li>
 * </ul>
 *
 * <h3>Reference:</h3>
 * Hidas, P. (2005). Modeling Driver Behavior at Freeway–Ramp Merges. Transportation Research Part C:
 * Emerging Technologies, 13(5-6), 321-336.
 */
public class MergingHidas extends DrivingTask {
    private ActionState currentActionState;
    private Double currentCompleteGapSizeLeft;
    private Double proportionAccLaneUsed;
    private Double freewayDensityVehPerMilePerLane;
    private Double rampAccelerationFtS2;

    private final double decreasedDesiredHeadwayFactor = 0.5;
    private final double timeAnticipation = 3.0; // seconds

    private int nextLink;
    private int nextLaneRightmost;
    private int routeLaneOffset;
    private int vehicleIdGapFollower;
    private int vehicleIdGapLeader;
    private Double startTimeMerge;

    /**
     * Constructor for MergeFromRamp.
     * @param contextVehicle The vehicle associated with this merging task.
     */
    public MergingHidas(final AbstractMirovaVehicle contextVehicle) {
        super(contextVehicle);
    }

    /**
     * Placeholder for calculating the utility of the merging maneuver.
     * <p>
     * This method can be extended to evaluate the benefits or costs of the merging maneuver
     * based on various factors such as traffic density, gap size, and vehicle speed.
     * </p>
     * @return the utility value (currently always 0.0)
     */
    @Override
    public double calculateUtility() {
        return 0.0;
    }

    /**
     * Determines whether the merging task should be activated.
     * <p>
     * This method checks if the vehicle needs to merge based on its current lane position
     * and the target lane on the freeway.
     * </p>
     * @return true if the merging task should be activated, false otherwise.
     */
    @Override
    public boolean getActivation() {
        this.nextLink = this.AbstractMirovaVehicle.getContextDriverDevice().getVissimVehicle().AttValue("NEXTLINK");
        this.nextLaneRightmost = Integer.parseInt(
            this.AbstractMirovaVehicle.getContextDriverDevice()
                .getVissimConnection().getNet().getLinks().ItemByKey(this.nextLink)
                .AttValue("MIN:LANES\\FROMLANES").split("-")[1]
        );
        this.routeLaneOffset = this.AbstractMirovaVehicle.getCurrentLaneId() - this.nextLaneRightmost;
        return this.routeLaneOffset < 0;
    }

    /**
     * Decides the appropriate maneuver advice for merging.
     * <p>
     * This method evaluates the time until the end of the lane and checks the feasibility
     * of merging into the available gaps. If a suitable gap is found, it returns an
     * ActionAdvice object with the desired lane change and initial action state.
     * </p>
     * @return The advice for the merging maneuver, or null if no suitable gap is found.
     */
    @Override
    public ActionAdvice decideManeuverAdvice() {
        double timeUntilEndOfLane = (
            this.AbstractMirovaVehicle.getContextDriverDevice().getVissimVehicle().AttValue("LANE\\LINK\\LENGTH2D")
            - this.AbstractMirovaVehicle.getCurrentPosition()
        ) / (this.AbstractMirovaVehicle.getCurrentSpeed() / 3.6);

        if (timeUntilEndOfLane > this.timeAnticipation && timeUntilEndOfLane > 3) {
            MergeGapInfo gapInfo = this.AbstractMirovaVehicle.updateMergeGap(0, 1);
            this.vehicleIdGapFollower = gapInfo.vehicleIdGapFollower;
            this.vehicleIdGapLeader = gapInfo.vehicleIdGapLeader;
            if (checkFreeMergeHidas(gapInfo)) {
                this.startTimeMerge = this.AbstractMirovaVehicle.getContextDriverDevice()
                    .getVissimConnection().getSimulation().AttValue("SimSec");
                return new ActionAdvice(
                    3,
                    ActionStateFactory.startFreeMergeToTargetGap(this)
                );
            } else {
                gapInfo = this.AbstractMirovaVehicle.updateMergeGap(-1, 1);
                this.vehicleIdGapFollower = gapInfo.vehicleIdGapFollower;
                this.vehicleIdGapLeader = gapInfo.vehicleIdGapLeader;
                if (checkFreeMergeHidas(gapInfo)) {
                    this.startTimeMerge = this.AbstractMirovaVehicle.getContextDriverDevice()
                        .getVissimConnection().getSimulation().AttValue("SimSec");
                    return new ActionAdvice(
                        3,
                        ActionStateFactory.startFreeMergeToTargetGap(this)
                    );
                } else {
                    gapInfo = this.AbstractMirovaVehicle.updateMergeGap(-2, 1);
                    this.vehicleIdGapFollower = gapInfo.vehicleIdGapFollower;
                    this.vehicleIdGapLeader = gapInfo.vehicleIdGapLeader;
                    if (checkFreeMergeHidas(gapInfo)) {
                        this.startTimeMerge = this.AbstractMirovaVehicle.getContextDriverDevice()
                            .getVissimConnection().getSimulation().AttValue("SimSec");
                        return new ActionAdvice(
                            3,
                            ActionStateFactory.startFreeMergeToTargetGap(this)
                        );
                    } else {
                        return new ActionAdvice(
                            3,
                            ActionStateFactory.startForcedMerge(this)
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks the feasibility of a free merge based on the Hidas (2005) model.
     * <p>
     * This method evaluates whether the merging maneuver can be performed safely by
     * comparing the desired and anticipated space headways for the leading and following
     * vehicles in the target gap.
     * </p>
     * @param gapInfo Information about the current gap and surrounding vehicles.
     * @return true if the merge is feasible, false otherwise.
     */
    private boolean checkFreeMergeHidas(final MergeGapInfo gapInfo) {
        double desiredSpaceHeadwayGapLeader = this.AbstractMirovaVehicle.getContextDriverDevice().getMinimumTimeHeadway()
            * this.decreasedDesiredHeadwayFactor
            * this.AbstractMirovaVehicle.getCurrentSpeed();
        double desiredSpaceHeadwayGapFollower = this.AbstractMirovaVehicle.getContextDriverDevice().getMinimumTimeHeadway()
            * this.decreasedDesiredHeadwayFactor
            * this.AbstractMirovaVehicle.getCurrentSpeed();

        double anticipatedDesiredSpaceHeadwayGapLeader =
            gapInfo.currentSpaceHeadwayGapLeader
            - (this.AbstractMirovaVehicle.getCurrentSpeed() * this.timeAnticipation / 3.6
                - this.AbstractMirovaVehicle.getMaximumAcceleration() / (2 * Math.pow(this.timeAnticipation, 2)))
            + gapInfo.currentSpeedGapLeader * this.timeAnticipation / 3.6;

        double anticipatedDesiredSpaceHeadwayGapFollower =
            (gapInfo.currentSpaceHeadwayGapFollower
                - (gapInfo.currentSpeedGapFollower * this.timeAnticipation / 3.6
                    - (gapInfo.currentAccelerationGapFollower / (2 * Math.pow(this.timeAnticipation, 2)))))
            + (this.AbstractMirovaVehicle.getCurrentSpeed() * this.timeAnticipation / 3.6)
            - this.AbstractMirovaVehicle.getMaximumAcceleration() / (2 * Math.pow(this.timeAnticipation, 2));

        return desiredSpaceHeadwayGapFollower <= anticipatedDesiredSpaceHeadwayGapFollower
            && desiredSpaceHeadwayGapLeader <= anticipatedDesiredSpaceHeadwayGapLeader;
    }
}
