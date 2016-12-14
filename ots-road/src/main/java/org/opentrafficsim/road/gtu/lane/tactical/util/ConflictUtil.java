package org.opentrafficsim.road.gtu.lane.tactical.util;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.ATLEASTONE;
import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeLength;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.RoadGTUTypes;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayStopLine;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

import nl.tudelft.simulation.language.Throw;

/**
 * This class implements default behavior for intersection conflicts for use in tactical planners.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 3, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO conflict over multiple lanes (longitudinal in own direction)
public final class ConflictUtil
{

    /** Minimum time gap between events. */
    public static final ParameterTypeDuration MIN_GAP =
            new ParameterTypeDuration("minGap", "Minimum gap for conflicts.", new Duration(0.25, TimeUnit.SECOND), POSITIVE);

    /** Stopping distance at conflicts. */
    public static final ParameterTypeLength S0_CONF =
            new ParameterTypeLength("s0 conf", "Stopping distance at conflicts.", new Length(1.5, LengthUnit.METER), POSITIVE);

    /** Multiplication factor on time for conservative assessment. */
    public static final ParameterTypeDouble TIME_FACTOR =
            new ParameterTypeDouble("timeFactor", "Safety factor on estimated time.", 1.1, ATLEASTONE);

    /** Area before stop line where one is considered arrived at the intersection. */
    public static final ParameterTypeLength STOP_AREA =
            new ParameterTypeLength("stopArea", "Area before stop line where one is considered arrived at the intersection.",
                    new Length(4, LengthUnit.METER), POSITIVE);

    /** Time step for free acceleration anticipation. */
    private static final Duration TIME_STEP = new Duration(0.5, TimeUnit.SI);

    /**
     * Do not instantiate.
     */
    private ConflictUtil()
    {
        //
    }

    /**
     * Approach conflicts by applying appropriate acceleration (or deceleration). The model may yield for a vehicle even while
     * having priority. Such a 'yield plan' is remembered in <tt>YieldPlans</tt>. By forwarding the same <tt>YieldPlans</tt> for
     * a GTU consistency of such plans is provided. If any conflict is not accepted to pass, stopping before a more upstream
     * conflict is applied if there not sufficient stopping length in between conflicts.
     * @param behavioralCharacteristics behavioral characteristics
     * @param conflicts set of conflicts to approach
     * @param leaders leading vehicles
     * @param carFollowingModel car-following model
     * @param vehicleLength length of vehicle
     * @param speed current speed
     * @param acceleration current acceleration
     * @param speedLimitInfo speed limit info
     * @param conflictPlans set of plans for conflict
     * @return acceleration appropriate for approaching the conflicts
     * @throws GTUException in case of an unsupported conflict rule
     * @throws ParameterException if a parameter is not defined or out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Acceleration approachConflicts(final BehavioralCharacteristics behavioralCharacteristics,
            final SortedSet<HeadwayConflict> conflicts, final SortedSet<AbstractHeadwayGTU> leaders,
            final CarFollowingModel carFollowingModel, final Length vehicleLength, final Speed speed,
            final Acceleration acceleration, final SpeedLimitInfo speedLimitInfo, final ConflictPlans conflictPlans)
            throws GTUException, ParameterException
    {

        Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
        if (conflicts.isEmpty())
        {
            return a;
        }

        conflictPlans.cleanYieldPlans();
        Length stopLength = behavioralCharacteristics.getParameter(S0_CONF).plus(vehicleLength);
        List<Length> prevStarts = new ArrayList<>();
        List<Length> prevEnds = new ArrayList<>();

        for (HeadwayConflict conflict : conflicts)
        {

            // adjust acceleration for situations where stopping might not be required
            if (conflict.isCrossing())
            {
                // avoid collision if crossing is occupied
                a = Acceleration.min(a,
                        avoidCrossingCollision(behavioralCharacteristics, conflict, carFollowingModel, speed, speedLimitInfo));
            }
            else
            {
                // follow leading GTUs on merge or split
                a = Acceleration.min(a, followConflictingLeaderOnMergeOrSplit(conflict, behavioralCharacteristics,
                        carFollowingModel, speed, speedLimitInfo));
            }
            if (conflict.getDistance().lt(Length.ZERO))
            {
                // ignore conflicts we are on (i.e. negative distance to start of conflict)
                continue;
            }

            // determine if we need to stop
            boolean stop;
            switch (conflict.getConflictRule())
            {
                case PRIORITY:
                {
                    stop = stopForPriorityConflict(conflict, leaders, speed, stopLength, vehicleLength,
                            behavioralCharacteristics, conflictPlans);
                    break;
                }
                case GIVE_WAY:
                {
                    stop = stopForGiveWayConflict(conflict, leaders, speed, acceleration, stopLength, vehicleLength,
                            behavioralCharacteristics, speedLimitInfo, carFollowingModel);
                    break;
                }
                case STOP:
                {
                    stop = stopForStopConflict(conflict, leaders, speed, acceleration, stopLength, vehicleLength,
                            behavioralCharacteristics, speedLimitInfo, carFollowingModel);
                    break;
                }
                case ALL_STOP:
                {
                    stop = stopForAllStopConflict(conflict, conflictPlans);
                    break;
                }
                case SPLIT:
                {
                    stop = false; // skipped anyway
                    break;
                }
                default:
                {
                    throw new GTUException("Unsupported conflict rule encountered while approaching conflicts.");
                }
            }

            // stop if required, account for upstream conflicts to keep clear
            if (!conflict.getConflictType().equals(ConflictType.SPLIT))
            {
                prevStarts.add(conflict.getDistance());
                if (stop)
                {
                    // stop for first conflict looking upstream of this blocked conflict that allows sufficient space
                    int j = 0; // most upstream conflict if not in between conflicts
                    for (int i = prevEnds.size() - 1; i >= 0; i--) // downstream to upstream
                    {
                        // note, at this point prevStarts contains one more conflict than prevEnds
                        if (prevStarts.get(i + 1).minus(prevEnds.get(i)).gt(stopLength))
                        {
                            j = i + 1;
                            break;
                        }
                    }

                    // TODO
                    // if this lowers our acceleration, we need to check if we are able to pass upstream conflicts still in time

                    // stop for j'th conflict, further conflicts may be ignored
                    // constant acceleration is -.5*v^2/s, where s = distance-s0 > 0
                    behavioralCharacteristics.setParameter(ParameterTypes.S0, behavioralCharacteristics.getParameter(S0_CONF));
                    Acceleration aConstant = CarFollowingUtil.constantAccelerationStop(carFollowingModel,
                            behavioralCharacteristics, speed, prevStarts.get(j));
                    Acceleration aCF = CarFollowingUtil.stop(carFollowingModel, behavioralCharacteristics, speed,
                            speedLimitInfo, prevStarts.get(j));
                    behavioralCharacteristics.resetParameter(ParameterTypes.S0);
                    a = Acceleration.min(a, aCF);//Acceleration.max(aCF, aConstant));
                    break;
                }
                prevEnds.add(conflict.getDistance().plus(conflict.getLength()));
            }

        }

        return a;
    }

    /**
     * Determines acceleration for following conflicting vehicles <i>on</i> a merge or split conflict.
     * @param conflict merge or split conflict
     * @param behavioralCharacteristics behavioral characteristics
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @return acceleration for following conflicting vehicles <i>on</i> a merge or split conflict
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    private static Acceleration followConflictingLeaderOnMergeOrSplit(final HeadwayConflict conflict,
            final BehavioralCharacteristics behavioralCharacteristics, final CarFollowingModel carFollowingModel,
            final Speed speed, final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        // ignore if no conflicting GTU's
        if (conflict.getDownstreamConflictingGTUs().isEmpty())
        {
            return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        }
        // get the most upstream GTU to consider
        AbstractHeadwayGTU c = conflict.getDownstreamConflictingGTUs().first();
        if (c.isAhead())
        {
            // conflict GTU completely downstream of conflict (i.e. regular car-following, ignore here)
            return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        }
        // conflict GTU (partially) on the conflict
        // {@formatter:off}
        // ______________________________________________ 
        //   ___      virtual headway   |  ___  |
        //  |___|(-----------------------)|___|(vehicle from south, on lane from south)
        // _____________________________|_______|________
        //                              /       / 
        //                             /       /
        // {@formatter:on}
        Length virtualHeadway = conflict.getDistance().plus(c.getOverlapRear());
        if (virtualHeadway.le(Length.ZERO))
        {
            // conflict GTU downstream of start of conflict, but upstream of us
            return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        }
        // follow leader
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(virtualHeadway, c.getSpeed());
        Acceleration a = carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaders);
        // if conflicting GTU is partially upstream of the conflict and at (near) stand-still, stop for the conflict rather than
        // following the tail of the conflicting GTU
        if (conflict.isMerge() && virtualHeadway.lt(conflict.getDistance()))
        {
            // {@formatter:off}
            /*
             * ______________________________________________
             *    ___    stop for conflict  |       | 
             *   |___|(--------------------)|   ___ |
             * _____________________________|__/  /_|________ 
             *                              / /__/  /
             *                             /       /
             */
            // {@formatter:on}
            Acceleration aStop = CarFollowingUtil.stop(carFollowingModel, behavioralCharacteristics, speed, speedLimitInfo,
                    conflict.getDistance());
            a = Acceleration.max(a, aStop); // max, which ever allows the largest acceleration
        }
        return a;
    }

    /**
     * Determines an acceleration required to avoid a collision with GTUs <i>on</i> a crossing conflict.
     * @param behavioralCharacteristics behavioral characteristics
     * @param conflict conflict
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @return acceleration required to avoid a collision
     * @throws ParameterException if parameter is not defined
     */
    private static Acceleration avoidCrossingCollision(final BehavioralCharacteristics behavioralCharacteristics,
            final HeadwayConflict conflict, final CarFollowingModel carFollowingModel, final Speed speed,
            final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        if (!conflict.getDownstreamConflictingGTUs().isEmpty())
        {
            AbstractHeadwayGTU conflictingGTU = conflict.getDownstreamConflictingGTUs().first();
            if (conflictingGTU.isParallel())
            {
                Length distance = conflictingGTU.getOverlapRear().abs().plus(conflictingGTU.getOverlap())
                        .plus(conflictingGTU.getOverlapFront().abs());
                AnticipationInfo ttcC =
                        AnticipationInfo.anticipateMovement(distance, conflictingGTU.getSpeed(), Acceleration.ZERO);
                AnticipationInfo tteO = AnticipationInfo.anticipateMovementFreeAcceleration(conflict.getDistance(), speed,
                        behavioralCharacteristics, carFollowingModel, speedLimitInfo, TIME_STEP);
                // enter before cleared
                if (tteO.getDuration().lt(ttcC.getDuration()))
                {
                    if (!conflictingGTU.getSpeed().eq(Speed.ZERO))
                    {
                        // solve parabolic speed profile s = v*t + .5*a*t*t, a =
                        double acc = 2 * (conflict.getDistance().si - speed.si * ttcC.getDuration().si)
                                / (ttcC.getDuration().si * ttcC.getDuration().si);
                        // time till zero speed > time to avoid conflict?
                        if (speed.si / -acc > ttcC.getDuration().si)
                        {
                            return new Acceleration(acc, AccelerationUnit.SI);
                        }
                        // will reach zero speed ourselves
                        return CarFollowingUtil.stop(carFollowingModel, behavioralCharacteristics, speed, speedLimitInfo,
                                conflict.getDistance());
                    }
                    // conflicting vehicle on conflict at stand-still, ignore
                }
            }
        }
        return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
    }

    /**
     * Approach a priority conflict. Stopping is applied to give way to conflicting traffic in case congestion is present on the
     * own lane.
     * @param conflict conflict to approach
     * @param leaders leading vehicles in own lane
     * @param speed current speed
     * @param stopLength length required to stop
     * @param vehicleLength vehicle length
     * @param behavioralCharacteristics behavioral characteristics
     * @param yieldPlans set of plans for yielding with priority
     * @return whether to stop for this conflict
     * @throws ParameterException if parameter B is not defined
     */
    private static boolean stopForPriorityConflict(final HeadwayConflict conflict, final SortedSet<AbstractHeadwayGTU> leaders,
            final Speed speed, final Length stopLength, final Length vehicleLength,
            final BehavioralCharacteristics behavioralCharacteristics, final ConflictPlans yieldPlans) throws ParameterException
    {
        if (leaders.isEmpty() || conflict.getUpstreamConflictingGTUs().isEmpty()
                || conflict.getUpstreamConflictingGTUs().first().getSpeed().eq(Speed.ZERO))
        {
            // no leader, and no (stand-still) conflicting vehicle
            return false;
        }
        // time till enter; will enter the conflict
        // AnticipationInfo tteO = AnticipationInfo.anticipateMovement(conflict.getDistance(), speed, Acceleration.ZERO);
        // time till clear; will clear the conflict
        Length distance = conflict.getDistance().plus(vehicleLength);
        if (conflict.isCrossing())
        {
            // merge is cleared at start, crossing at end
            distance = distance.plus(conflict.getLength());
        }
        AnticipationInfo ttcC = AnticipationInfo.anticipateMovement(distance, speed, Acceleration.ZERO);
        // time till passible; downstream vehicle will leave sufficient room (stopLength) after the conflict
        distance = distance.minus(leaders.first().getDistance()).minus(vehicleLength).plus(stopLength);
        AnticipationInfo ttpD = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), Acceleration.ZERO);
        // MOTUS uses tteO instead of ttcC, but especially for long conflicts this is not appropriate
        if (ttpD.getDuration().ge(ttcC.getDuration())) // might block conflict
        {
            // TODO respond to indicator / expected turn, will the conflict vehicle go over the conflict? Consider further
            // conflicting vehicles if not.

            // at a merge, the vehicle that was yielded for may become the leader, do not yield (but follow)
            if (yieldPlans.isYieldPlan(conflict, leaders.first()))
            {
                return false;
            }
            // In MOTUS these rules are different. Drivers tagged themselves as conflict blocked, which others used. In OTS
            // this information is (rightfully) not available. This tagging is simplified to 'speed = 0'.
            if (!yieldPlans.isYieldPlan(conflict, conflict.getUpstreamConflictingGTUs().first())
                    && conflict.getUpstreamConflictingGTUs().first().getSpeed().equals(Speed.ZERO))
            {
                Acceleration b = behavioralCharacteristics.getParameter(ParameterTypes.B);
                Acceleration bReq = new Acceleration(.5 * speed.si * speed.si / conflict.getDistance().si, AccelerationUnit.SI);
                if (bReq.gt(b))
                {
                    // cannot stop safely, do not initiate plan
                    return false;
                }
            }
            // initiate or keep plan to yield
            yieldPlans.setYieldPlan(conflict, conflict.getUpstreamConflictingGTUs().first());
            return true;
        }
        // if the yield was a plan, it is abandoned as the conflict will not be blocked
        return false;
    }

    /**
     * Approach a give-way conflict.
     * @param conflict conflict
     * @param leaders leaders
     * @param speed current speed
     * @param acceleration current acceleration
     * @param stopLength length required when stopped
     * @param vehicleLength vehicle length
     * @param behavioralCharacteristics behavioral characteristics
     * @param speedLimitInfo speed limit info
     * @param carFollowingModel car-following model
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static boolean stopForGiveWayConflict(final HeadwayConflict conflict, final SortedSet<AbstractHeadwayGTU> leaders,
            final Speed speed, final Acceleration acceleration, final Length stopLength, final Length vehicleLength,
            final BehavioralCharacteristics behavioralCharacteristics, final SpeedLimitInfo speedLimitInfo,
            final CarFollowingModel carFollowingModel) throws ParameterException
    {

        // TODO disregard conflicting vehicles with a route not over the conflict, if known through e.g. indicator

        // Get data independent of conflicting vehicle
        // parameters
        Acceleration b = behavioralCharacteristics.getParameter(ParameterTypes.B).neg();
        double f = behavioralCharacteristics.getParameter(TIME_FACTOR);
        Duration gap = behavioralCharacteristics.getParameter(MIN_GAP);
        // time till conflict is cleared
        Length distance = conflict.getDistance().plus(vehicleLength);
        if (conflict.isCrossing())
        {
            // merge is cleared at start, crossing at end
            distance = distance.plus(conflict.getLength());
        }
        // based on current acceleration, but limited by free acceleration
        AnticipationInfo ttcOa = AnticipationInfo.anticipateMovementFreeAcceleration(distance, speed, behavioralCharacteristics,
                carFollowingModel, speedLimitInfo, TIME_STEP);
        AnticipationInfo ttcO;
        if (acceleration.lt(Acceleration.ZERO))
        {
            AnticipationInfo ttcOb = AnticipationInfo.anticipateMovement(distance, speed, acceleration);
            ttcO = ttcOa.getDuration().gt(ttcOb.getDuration()) ? ttcOa : ttcOb;
        }
        else
        {
            ttcO = ttcOa;
        }
        // time till downstream vehicle will make the conflict passible, under constant speed or safe deceleration
        AnticipationInfo ttpDz = null;
        AnticipationInfo ttpDs = null;
        if (conflict.isCrossing())
        {
            if (!leaders.isEmpty())
            {
                distance =
                        conflict.getDistance().minus(leaders.first().getDistance()).plus(conflict.getLength()).plus(stopLength);
                ttpDz = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), Acceleration.ZERO);
                ttpDs = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), b);
                if (Double.isInfinite(ttpDs.getDuration().si))
                {
                    // if leader decelerates to stand-still, not sufficient space after conflict
                    // stop, even without conflicting vehicles
                    return true;
                }
            }
            else
            {
                // no leader so conflict is passible within a duration of 0
                ttpDz = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
                ttpDs = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
            }
        }

        List<AbstractHeadwayGTU> conflictingVehicles = new ArrayList<>();
        if (!conflict.getUpstreamConflictingGTUs().isEmpty())
        {
            conflictingVehicles.addAll(conflict.getUpstreamConflictingGTUs());
        }
        else
        {
            // none within visibility, assume a conflicting vehicle just outside of visibility driving at speed limit
            try
            {
                HeadwayGTUSimple conflictGtu = new HeadwayGTUSimple("virtual " + UUID.randomUUID().toString(), RoadGTUTypes.CAR,
                        conflict.getConflictingVisibility(), new Length(4.0, LengthUnit.SI),
                        conflict.getConflictingSpeedLimit(), Acceleration.ZERO);
                conflictingVehicles.add(conflictGtu);
            }
            catch (GTUException exception)
            {
                throw new RuntimeException("Could not create a virtual conflicting vehicle at visibility range.", exception);
            }
        }

        // Loop over conflicting vehicles
        for (AbstractHeadwayGTU conflictingVehicle : conflictingVehicles)
        {

            // time till conflict vehicle will enter, under free acceleration and safe deceleration
            AnticipationInfo tteCc;
            if (conflictingVehicle instanceof HeadwayGTUSimple)
            {

                tteCc = AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(),
                        conflictingVehicle.getAcceleration());
            }
            else
            {
                BehavioralCharacteristics bc = conflictingVehicle.getBehavioralCharacteristics();
                SpeedLimitInfo sli = conflictingVehicle.getSpeedLimitInfo();
                CarFollowingModel cfm = conflictingVehicle.getCarFollowingModel();
                // tteCc = AnticipationInfo.anticipateMovement(confDistance.get(i), confSpeed.get(i), confAcceleration.get(i));
                tteCc = AnticipationInfo.anticipateMovementFreeAcceleration(conflictingVehicle.getDistance(),
                        conflictingVehicle.getSpeed(), bc, cfm, sli, TIME_STEP);
            }
            AnticipationInfo tteCs =
                    AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(), b);

            // check gap
            if (conflict.isMerge())
            {

                // Merge, will be each others followers, add time to overcome speed difference
                double vSelf = ttcO.getEndSpeed().si;
                double speedDiff = conflictingVehicle.getSpeed().si - vSelf;
                speedDiff = speedDiff > 0 ? speedDiff : 0;
                Duration additionalTime = new Duration(speedDiff / -b.si, TimeUnit.SI);
                // check if conflict vehicle will be upstream after that time, position beyond conflict after additional time
                double leaderFront = conflictingVehicle.getSpeed().si * ttcO.getDuration().si
                        - conflictingVehicle.getDistance().si + (conflictingVehicle.getSpeed().si * additionalTime.si
                                + 0.5 * b.si * additionalTime.si * additionalTime.si);
                double ownRear = vSelf * additionalTime.si; // constant speed after clearing
                Duration tMax = behavioralCharacteristics.getParameter(ParameterTypes.TMAX);
                Length s0 = behavioralCharacteristics.getParameter(ParameterTypes.S0);
                // 1) will clear the conflict after the conflict vehicle enters
                // 2) not sufficient time to overcome speed difference
                // 3) conflict vehicle will be too near after adjusting speed
                if (ttcO.getDuration().multiplyBy(f).plus(gap).gt(tteCc.getDuration())
                        || ttcO.getDuration().plus(additionalTime).multiplyBy(f).plus(gap).gt(tteCs.getDuration())
                        || ownRear < (leaderFront + (tMax.si + gap.si) * vSelf + s0.si) * f)
                {
                    return true;
                }

            }
            else if (conflict.isCrossing())
            {

                // Crossing, stop if order of events is not ok
                // 1) downstream vehicle must supply sufficient space before conflict vehicle will enter
                // 2) must clear the conflict before the conflict vehicle will enter
                // 3) if leader decelerates with b, conflict vehicle should be able to safely delay entering conflict
                if (ttpDz.getDuration().multiplyBy(f).plus(gap).gt(tteCc.getDuration())
                        || ttcO.getDuration().multiplyBy(f).plus(gap).gt(tteCc.getDuration())
                        || ttpDs.getDuration().multiplyBy(f).plus(gap).gt(tteCs.getDuration()))
                {
                    return true;
                }

            }
            else
            {
                throw new RuntimeException(
                        "Conflict is of unknown type " + conflict.getConflictType() + ", which is not merge nor a crossing.");
            }
        }

        // No conflict vehicle triggered stopping
        return false;

    }

    /**
     * Approach a stop conflict. Currently this is equal to approaching a give-way conflict.
     * @param conflict conflict
     * @param leaders leaders
     * @param speed current speed
     * @param acceleration current acceleration
     * @param stopLength length required when stopped
     * @param vehicleLength vehicle length
     * @param behavioralCharacteristics behavioral characteristics
     * @param speedLimitInfo speed limit info
     * @param carFollowingModel car-following model
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static boolean stopForStopConflict(final HeadwayConflict conflict, final SortedSet<AbstractHeadwayGTU> leaders,
            final Speed speed, final Acceleration acceleration, final Length stopLength, final Length vehicleLength,
            final BehavioralCharacteristics behavioralCharacteristics, final SpeedLimitInfo speedLimitInfo,
            final CarFollowingModel carFollowingModel) throws ParameterException
    {
        return stopForGiveWayConflict(conflict, leaders, speed, acceleration, stopLength, vehicleLength,
                behavioralCharacteristics, speedLimitInfo, carFollowingModel);
    }

    /**
     * Approach an all-stop conflict.
     * @param conflict conflict to approach
     * @param conflictPlans set of plans for conflict
     * @return whether to stop for this conflict
     */
    private static boolean stopForAllStopConflict(final HeadwayConflict conflict, final ConflictPlans conflictPlans)
    {
        // TODO all-stop behavior

        if (conflictPlans.isStopPhaseRun(conflict.getStopLine()))
        {
            return false;
        }

        return false;
    }

    /**
     * Holds the tactical plans of a driver considering conflicts. These are remembered for consistency. For instance, if the
     * decision is made to yield as current deceleration suggests it's safe to do so, but the trajectory for stopping in front
     * of the conflict results in deceleration slightly above what is considered safe deceleration, the plan should not be
     * abandoned. Decelerations above what is considered safe deceleration may result due to numerical overshoot or other factor
     * coming into play in car-following models. Many other examples exist where a driver sticks to a certain plan.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 7, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static final class ConflictPlans implements Serializable
    {

        /** */
        private static final long serialVersionUID = 20160811L;

        /** Set of current plans. */
        private final Map<String, String> yieldPlans = new HashMap<>();

        /** Set of conflicts that are still actively considered for a yield plan. */
        private final Set<String> activeYieldPlans = new HashSet<>();

        /** Phases of navigating an all-stop intersection per intersection. */
        private final HashMap<String, StopPhase> stopPhases = new HashMap<>();

        /** Estimated arrival times of vehicles at all-stop intersection. */
        private final HashMap<String, Time> arrivalTimes = new HashMap<>();

        /**
         * Returns whether a plan exists for yielding at the conflict for the given conflict GTU.
         * @param conflict conflict
         * @param gtu conflicting GTU
         * @return whether a plan exists for yielding at the conflict for the given conflict GTU
         */
        boolean isYieldPlan(final HeadwayConflict conflict, final AbstractHeadwayGTU gtu)
        {
            return this.yieldPlans.containsKey(conflict.getId()) && this.yieldPlans.get(conflict.getId()).equals(gtu.getId());
        }

        /**
         * Sets or maintains the plan to yield at the conflict for the given conflict RSU.
         * @param conflict conflict to yield at
         * @param gtu conflicting GTU
         */
        void setYieldPlan(final HeadwayConflict conflict, final AbstractHeadwayGTU gtu)
        {
            this.yieldPlans.put(conflict.getId(), gtu.getId());
            this.activeYieldPlans.add(conflict.getId());
        }

        /**
         * Clears any yield plan that was no longer kept active in the last evaluation of conflicts.
         */
        void cleanYieldPlans()
        {
            // remove any plan not represented in activePlans
            Iterator<String> iterator = this.yieldPlans.keySet().iterator();
            while (iterator.hasNext())
            {
                String conflictId = iterator.next();
                if (!this.activeYieldPlans.contains(conflictId))
                {
                    iterator.remove();
                }
            }
            // clear the activePlans for the next consideration of conflicts
            this.activeYieldPlans.clear();
        }

        /**
         * Sets the estimated arrival time of a GTU.
         * @param gtu GTU
         * @param time estimated arrival time
         */
        void setArrivalTime(final AbstractHeadwayGTU gtu, final Time time)
        {
            this.arrivalTimes.put(gtu.getId(), time);
        }

        /**
         * Returns the estimated arrival time of given GTU.
         * @param gtu GTU
         * @return estimated arrival time of given GTU
         */
        Time getArrivalTime(final AbstractHeadwayGTU gtu)
        {
            return this.arrivalTimes.get(gtu.getId());
        }

        /**
         * Sets the current phase to 'approach' for the given stop line.
         * @param stopLine stop line
         */
        void setStopPhaseApproach(final HeadwayStopLine stopLine)
        {
            this.stopPhases.put(stopLine.getId(), StopPhase.APPROACH);
        }

        /**
         * Sets the current phase to 'yield' for the given stop line.
         * @param stopLine stop line
         * @throws RuntimeException if the phase was not set to approach before
         */
        void setStopPhaseYield(final HeadwayStopLine stopLine)
        {
            Throw.when(
                    !this.stopPhases.containsKey(stopLine.getId())
                            || !this.stopPhases.get(stopLine.getId()).equals(StopPhase.APPROACH),
                    RuntimeException.class, "Yield stop phase is set for stop line that was not approached.");
            this.stopPhases.put(stopLine.getId(), StopPhase.YIELD);
        }

        /**
         * Sets the current phase to 'run' for the given stop line.
         * @param stopLine stop line
         * @throws RuntimeException if the phase was not set to approach before
         */
        void setStopPhaseRun(final HeadwayStopLine stopLine)
        {
            Throw.when(!this.stopPhases.containsKey(stopLine.getId()), RuntimeException.class,
                    "Run stop phase is set for stop line that was not approached.");
            this.stopPhases.put(stopLine.getId(), StopPhase.YIELD);
        }

        /**
         * @param stopLine stop line
         * @return whether the current phase is 'approach' for the given stop line
         */
        boolean isStopPhaseApproach(final HeadwayStopLine stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId())
                    && this.stopPhases.get(stopLine.getId()).equals(StopPhase.APPROACH);
        }

        /**
         * @param stopLine stop line
         * @return whether the current phase is 'yield' for the given stop line
         */
        boolean isStopPhaseYield(final HeadwayStopLine stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId())
                    && this.stopPhases.get(stopLine.getId()).equals(StopPhase.YIELD);
        }

        /**
         * @param stopLine stop line
         * @return whether the current phase is 'run' for the given stop line
         */
        boolean isStopPhaseRun(final HeadwayStopLine stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId()) && this.stopPhases.get(stopLine.getId()).equals(StopPhase.RUN);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ConflictPlans";
        }

    }

    /**
     * Phases of navigating an all-stop intersection.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 30, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static enum StopPhase
    {
        /** Approaching stop intersection. */
        APPROACH,

        /** Yielding for stop intersection. */
        YIELD,

        /** Running over stop intersection. */
        RUN;
    }

}
