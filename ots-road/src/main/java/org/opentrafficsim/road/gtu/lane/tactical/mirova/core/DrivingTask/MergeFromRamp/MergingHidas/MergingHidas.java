package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas;

import java.util.Iterator;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.ForcedMerge.ForcedMerge;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge.FreeMerge;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Implements the ramp merging behavior based on the methodology described by Hidas (2005).
 * <p>
 * This class models the merging process of a vehicle transitioning from an on-ramp to a freeway. It evaluates the merging
 * conditions, identifies suitable gaps, and determines the appropriate actions to safely and efficiently complete the merge.
 * The approach is inspired by the driver behavior model for freeway–ramp merges as outlined in Hidas (2005).
 * </p>
 * <h3>Key Concepts from Hidas (2005):</h3>
 * <ul>
 * <li><b>Anticipatory behavior</b>: Drivers anticipate the actions of surrounding vehicles to adjust their speed and position
 * for merging.</li>
 * <li><b>Gap acceptance</b>: Drivers evaluate the available gaps in the traffic stream and decide whether to merge based on the
 * feasibility of the gap.</li>
 * <li><b>Cooperative behavior</b>: Leading and following vehicles in the target gap may adjust their behavior to facilitate the
 * merging process.</li>
 * </ul>
 * <h3>Responsibilities:</h3>
 * <ul>
 * <li>Evaluate the feasibility of merging based on traffic conditions and gap availability.</li>
 * <li>Identify the target gap and calculate the required actions for merging.</li>
 * <li>Transition between different states of the merging process, such as acceleration, lane change, and finalization.</li>
 * <li>Handle situations where merging is not feasible by transitioning to abort or forced merge states.</li>
 * </ul>
 * <h3>Attributes:</h3>
 * <ul>
 * <li>currentActionState (ActionState): The current state of the merging process.</li>
 * <li>decreasedDesiredHeadwayFactor (double): A factor to reduce the desired headway during merging.</li>
 * <li>timeAnticipation (double): The time (in seconds) the vehicle anticipates for merging.</li>
 * <li>nextLink (int): The ID of the next link the vehicle is merging into.</li>
 * <li>nextLaneRightmost (int): The rightmost lane ID of the next link.</li>
 * <li>routeLaneOffset (int): The difference between the current lane and the target lane.</li>
 * <li>vehicleIdGapFollower (int): The ID of the following vehicle in the target gap.</li>
 * <li>vehicleIdGapLeader (int): The ID of the leading vehicle in the target gap.</li>
 * <li>startTimeMerge (Double): The simulation time when the merge process starts.</li>
 * </ul>
 * <h3>Reference:</h3> Hidas, P. (2005). Modeling Driver Behavior at Freeway–Ramp Merges. Transportation Research Part C:
 * Emerging Technologies, 13(5-6), 321-336.
 */
public class MergingHidas extends DrivingTask
{
    private ActionState currentActionState;

    private Double currentCompleteGapSizeLeft;

    private Double proportionAccLaneUsed;

    private Double freewayDensityVehPerMilePerLane;

    private Double rampAccelerationFtS2;

    private final double decreasedDesiredHeadwayFactor = 0.5;

    private Duration timeAnticipation = null; // seconds

    private int nextLink;

    private int nextLaneRightmost;

    private int routeLaneOffset;

    private int vehicleIdGapFollower;

    private int vehicleIdGapLeader;

    private Double startTimeMerge;

    private HeadwayGtu gapFollowerHeadway;

    private HeadwayGtu gapLeaderHeadway;

    private LaneBasedGtu gapFollowerGtu;

    private LaneBasedGtu gapLeaderGtu;

    private double desire;

    /**
     * Constructor for MergeFromRamp.
     * @param contextVehicle The vehicle associated with this merging task.
     * @throws OperationalPlanException
     */
    public MergingHidas(final AbstractMirovaVehicle AbstractMirovaVehicle) throws OperationalPlanException
    {
        super(AbstractMirovaVehicle);
    }

    /**
     * Placeholder for calculating the utility of the merging maneuver.
     * <p>
     * This method can be extended to evaluate the benefits or costs of the merging maneuver based on various factors such as
     * traffic density, gap size, and vehicle speed.
     * </p>
     * @return the utility value (currently always 0.0)
     * @throws ParameterException
     */
    @Override
    public double calculateDesire() throws ParameterException
    {   double dout = 0.0;
        Parameters params = getParameters();
        RelativeLane lane = RelativeLane.LEFT;
        for (LaneChangeInfo info : getInfrastructurePerception().getLegalLaneChangeInfo(lane))
        {
            Length x = info.remainingDistance();
            int n = info.numberOfLaneChanges();
            Speed v = getAbstractMirovaVehicle().getGtu().getSpeed();
            double d1 = 1 - x.si / (n * params.getParameter(ParameterTypes.LOOKAHEAD).si);
            double d2 = 1 - (x.si / v.si) / (n * params.getParameter(ParameterTypes.T0).si);
            d1 = d2 > d1 ? d2 : d1;
            dout = Math.max(dout, d1 < 0 ? 0 : d1) ;
        }
        return dout;
    }

    /**
     * Determines whether the merging task should be activated.
     * <p>
     * This method checks if the vehicle needs to merge based on its current lane position and the target lane on the freeway.
     * </p>
     * @return true if the merging task should be activated, false otherwise.
     */
    @Override
    public boolean getActivation()
    {
        SortedSet<LaneChangeInfo> currentLaneLCInfo =
                getInfrastructurePerception().getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentLaneLCRemainingDistance =
                currentLaneLCInfo.isEmpty() || currentLaneLCInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                        : currentLaneLCInfo.first().remainingDistance();
        Boolean currentLaneLCIsDeadend = currentLaneLCInfo.first().deadEnd();

        if (currentLaneLCRemainingDistance.gt(Length.instantiateSI(300)) && currentLaneLCIsDeadend)
        {
            return true; // Activate merging task if the current lane is a dead-end and has sufficient distance
        }
        else
            return false; // Do not activate merging task if the current lane is not a dead-end or has insufficient distance

    }

    /**
     * Decides the appropriate ManeuverPattern for merging.
     * <p>
     * This method evaluates the time until the end of the lane and checks the feasibility of merging into the available gaps.
     * If a suitable gap is found, it returns an ManeuverPattern object with the desired lane change and initial action state.
     * </p>
     * @return The advice for the merging maneuver, or null if no suitable gap is found.
     * @throws ParameterException
     */
    @Override
    public ManeuverPattern decideManeuverPattern() throws ParameterException
    {
        Duration timeUntilEndOfLane = Duration
                .instantiateSI(getRemainingDistanceBeforeLaneChange().si / getAbstractMirovaVehicle().getGtu().getSpeed().si);
        this.timeAnticipation = Duration.instantiateSI(3.0);

        if (timeUntilEndOfLane.ge(Duration.instantiateSI(10.0)))
        {
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> targetGapFollowers =
                    getNeighborsPerception().getFollowers(RelativeLane.LEFT);
            Iterator<LaneBasedGtu> iteratorGtuFollowers = targetGapFollowers.underlying();
            Iterator<LaneBasedGtu> iteratorGtuLeaders = getNeighborsPerception().getLeaders(RelativeLane.LEFT).underlying();
            this.gapLeaderHeadway = getNeighborsPerception().getFirstLeaders(LateralDirectionality.LEFT).first();
            this.gapLeaderGtu = iteratorGtuLeaders.hasNext() ? iteratorGtuLeaders.next() : null;


            for (HeadwayGtu targetGapFollower : targetGapFollowers)
            {
               this.gapFollowerHeadway = targetGapFollower;
               this.gapFollowerGtu = iteratorGtuFollowers.hasNext() ? iteratorGtuFollowers.next() : null;

               if (checkFreeMerge(this.gapLeaderHeadway, this.gapFollowerHeadway)){
                   return new FreeMerge(this);
                   }
               else
               {
                   this.gapLeaderHeadway = targetGapFollower;
                   this.gapLeaderGtu = this.gapFollowerGtu;
                }
            }

        }
        else
        {
            return new ForcedMerge(this);
        }
           return null; // No suitable gap found, return null
    }

    /**
     * Evaluates whether a free merge maneuver is feasible according to the Hidas (2005) ramp merging model.
     * <p>
     * This method calculates and compares the desired and anticipated space headways for both the leader and follower vehicles
     * in the target gap. The desired space headway is based on the minimum time headway and the current speed of the merging
     * vehicle. The anticipated space headway accounts for the predicted positions and speeds of the involved vehicles after a
     * specified anticipation time, considering their current speeds and accelerations.
     * </p>
     * <p>
     * The merge is considered feasible if both the follower and leader in the target gap will have sufficient space headway
     * after the merge, i.e., the anticipated space headways are greater than or equal to the desired values.
     * </p>
     * <ul>
     * <li><b>Leader:</b> The vehicle ahead in the target lane gap.</li>
     * <li><b>Follower:</b> The vehicle behind in the target lane gap.</li>
     * </ul>
     * <p>
     * The method retrieves the current distances, speeds, and accelerations of the relevant vehicles using the perception
     * system, computes the required headways, and returns whether the merge can be performed safely.
     * </p>
     * @return {@code true} if both the leader and follower have sufficient anticipated space headway for a safe merge;
     *         {@code false} otherwise.
     * @throws ParameterException if required parameters cannot be retrieved.
     */
    public boolean checkFreeMerge(final HeadwayGtu gapLeader, final HeadwayGtu gapFollower) throws ParameterException
    {
        LaneBasedGtu egoGtu = this.AbstractMirovaVehicle.getGtu();
        Length desiredSpaceHeadwayGapLeader =
                Length.instantiateSI(this.getParameters().getParameter(ParameterTypes.TMIN).si * egoGtu.getSpeed().si);
        Length desiredSpaceHeadwayGapFollower =
                Length.instantiateSI(this.getParameters().getParameter(ParameterTypes.TMIN).si * egoGtu.getSpeed().si);

        Length currentSpaceHeadwayGapLeader = gapLeader.getDistance();

        Speed currentSpeedGapLeader = gapLeader.getSpeed();

        Length currentSpaceHeadwayGapFollower = gapFollower.getDistance();

        Speed currentSpeedGapFollower = gapFollower.getSpeed();

        Acceleration currentAccelerationGapFollower = gapFollower.getAcceleration();

        Length anticipatedDesiredSpaceHeadwayGapLeader = Length.instantiateSI(currentSpaceHeadwayGapLeader.si
                - (egoGtu.getSpeed().si * this.timeAnticipation.si
                        - egoGtu.getMaximumAcceleration().si / (2 * Math.pow(this.timeAnticipation.si, 2)))
                + (currentSpeedGapLeader.si * this.timeAnticipation.si));

        Length anticipatedDesiredSpaceHeadwayGapFollower =
                Length.instantiateSI((currentSpaceHeadwayGapFollower.si - (currentSpeedGapFollower.si * this.timeAnticipation.si
                        - (currentAccelerationGapFollower.si / (2 * Math.pow(this.timeAnticipation.si, 2)))))
                        + (egoGtu.getSpeed().si * this.timeAnticipation.si)
                        - egoGtu.getMaximumAcceleration().si / (2 * Math.pow(this.timeAnticipation.si, 2)));

        return desiredSpaceHeadwayGapFollower.le(anticipatedDesiredSpaceHeadwayGapFollower)
                && desiredSpaceHeadwayGapLeader.le(anticipatedDesiredSpaceHeadwayGapLeader);
    }

    public Length getRemainingDistanceBeforeLaneChange()
    {
        SortedSet<LaneChangeInfo> currentLaneLCInfo =
                getInfrastructurePerception().getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentLaneLCRemainingDistance =
                currentLaneLCInfo.isEmpty() || currentLaneLCInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                        : currentLaneLCInfo.first().remainingDistance();

        return currentLaneLCRemainingDistance;
    }

    public HeadwayGtu getGapLeaderHeadway()
    {
        return this.gapLeaderHeadway;
    }

    public LaneBasedGtu getGapLeaderGtu()
    {
        return this.gapLeaderGtu;
    }

    public HeadwayGtu getGapFollowerHeadway()
    {
        return this.gapFollowerHeadway;
    }

    public LaneBasedGtu getGapFollowerGtu()
    {
        return this.gapFollowerGtu;
    }

    public Duration getTimeUntilEndOfLane()
    {
        return Duration
                .instantiateSI(getRemainingDistanceBeforeLaneChange().si / getAbstractMirovaVehicle().getGtu().getSpeed().si);
    }

    public void updateTimeAnticipation() throws ParameterException
    {
        this.timeAnticipation = this.timeAnticipation.minus(getParameters().getParameter(ParameterTypes.DT));
    }
}
