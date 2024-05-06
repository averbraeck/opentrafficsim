package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuSimple;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayStopLine;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.conflict.BusStopConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * This class implements default behavior for intersection conflicts for use in tactical planners.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @see <a href="https://rstrail.nl/wp-content/uploads/2015/02/schakel_2012.pdf">Schakel, W.J., B. van Arem (2012) “An Urban
 *      Traffic Extension of a Freeway Driver Model for use in the OpenTraffic® Open Source Traffic Simulation”, presented at
 *      TRAIL Congress 2012.</a>
 */
// TODO do not ignore vehicles upstream of conflict if they have green
// TODO conflict over multiple lanes (longitudinal in own direction)
// TODO a) yielding while having priority happens only when leaders is standing still on conflict (then its useless...)
// b) two vehicles can remain upstream of merge if vehicle stands on merge but leaves some space to move
// probably 1 is yielding, and 1 is courtesy yielding as the other stands still
// c) they might start moving together and collide further down (no response to negative headway on merge)
public final class ConflictUtil
{

    /** Minimum time gap between events. */
    public static final ParameterTypeDuration MIN_GAP = new ParameterTypeDuration("minGap", "Minimum gap for conflicts",
            new Duration(0.000001, DurationUnit.SECOND), ConstraintInterface.POSITIVE);

    /** Comfortable deceleration. */
    public static final ParameterTypeAcceleration B = ParameterTypes.B;

    /** Critical deceleration. */
    public static final ParameterTypeAcceleration BCRIT = ParameterTypes.BCRIT;

    /** Stopping distance. */
    public static final ParameterTypeLength S0 = ParameterTypes.S0;

    /** Stopping distance at conflicts. */
    public static final ParameterTypeLength S0_CONF = new ParameterTypeLength("s0conf", "Stopping distance at conflicts",
            new Length(1.5, LengthUnit.METER), ConstraintInterface.POSITIVE);

    /** Multiplication factor on time for conservative assessment. */
    public static final ParameterTypeDouble TIME_FACTOR =
            new ParameterTypeDouble("timeFactor", "Safety factor on estimated time", 1.25, ConstraintInterface.ATLEASTONE);

    /** Area before stop line where one is considered arrived at the intersection. */
    public static final ParameterTypeLength STOP_AREA =
            new ParameterTypeLength("stopArea", "Area before stop line where one is considered arrived at the intersection",
                    new Length(4, LengthUnit.METER), ConstraintInterface.POSITIVE);

    /** Parameter of how much time before departure a bus indicates its departure to get priority. */
    public static final ParameterTypeDuration TI = new ParameterTypeDuration("ti", "Indicator time before bus departure",
            Duration.instantiateSI(3.0), ConstraintInterface.POSITIVE);

    /** Time step for free acceleration anticipation. */
    private static final Duration TIME_STEP = new Duration(0.5, DurationUnit.SI);

    /** Cross standing vehicles on crossings. We allow this to prevent dead-locks. A better model should render this useless. */
    private static boolean CROSSSTANDING = true;

    /**
     * Do not instantiate.
     */
    private ConflictUtil()
    {
        //
    }

    /**
     * Approach conflicts by applying appropriate acceleration (or deceleration). The model may yield for a vehicle even while
     * having priority. Such a 'yield plan' is remembered in <code>YieldPlans</code>. By forwarding the same
     * <code>YieldPlans</code> for a GTU consistency of such plans is provided. If any conflict is not accepted to pass,
     * stopping before a more upstream conflict is applied if there is not sufficient stopping length in between conflicts.
     * @param parameters Parameters; parameters
     * @param conflicts PerceptionCollectable&lt;HeadwayConflict,Conflict&gt;; set of conflicts to approach
     * @param leaders PerceptionCollectable&lt;HeadwayGtu,LaneBasedGtu&gt;; leading vehicles
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param vehicleLength Length; length of vehicle
     * @param vehicleWidth Length; width of vehicle
     * @param speed Speed; current speed
     * @param acceleration Acceleration; current acceleration
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param conflictPlans ConflictPlans; set of plans for conflict
     * @param gtu LaneBasedGtu; gtu
     * @param lane RelativeLane; lane
     * @return acceleration appropriate for approaching the conflicts
     * @throws GtuException in case of an unsupported conflict rule
     * @throws ParameterException if a parameter is not defined or out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    // @docs/06-behavior/tactical-planner/#modular-utilities (..., final ConflictPlans conflictPlans, ...)
    public static Acceleration approachConflicts(final Parameters parameters, final Iterable<HeadwayConflict> conflicts,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final CarFollowingModel carFollowingModel,
            final Length vehicleLength, final Length vehicleWidth, final Speed speed, final Acceleration acceleration,
            final SpeedLimitInfo speedLimitInfo, final ConflictPlans conflictPlans, final LaneBasedGtu gtu,
            final RelativeLane lane) throws GtuException, ParameterException
    {

        conflictPlans.cleanPlans();

        Acceleration a = Acceleration.POS_MAXVALUE;
        Length stoppingDistance = Length.instantiateSI(
                parameters.getParameter(S0).si + vehicleLength.si + .5 * speed.si * speed.si / parameters.getParameter(B).si);
        Iterator<HeadwayConflict> it = conflicts.iterator();
        if (it.hasNext() && it.next().getDistance().gt(stoppingDistance))
        {
            conflictPlans.setBlocking(false);
            return a;
        }

        List<Length> prevStarts = new ArrayList<>();
        List<Length> prevEnds = new ArrayList<>();
        List<Class<? extends ConflictRule>> conflictRuleTypes = new ArrayList<>();
        boolean blocking = false;

        for (HeadwayConflict conflict : conflicts)
        {

            // adjust acceleration for situations where stopping might not be required
            if (conflict.isCrossing())
            {
                // avoid collision if crossing is occupied
                a = Acceleration.min(a, avoidCrossingCollision(parameters, conflict, carFollowingModel, speed, speedLimitInfo));
            }
            else
            {
                if (conflict.isMerge() && !lane.isCurrent() && conflict.getConflictPriority().isPriority())
                {
                    // probably evaluation for a lane-change
                    a = Acceleration.min(a,
                            avoidMergeCollision(parameters, conflict, carFollowingModel, speed, speedLimitInfo));
                }
                // follow leading GTUs on merge or split
                a = Acceleration.min(a, followConflictingLeaderOnMergeOrSplit(conflict, parameters, carFollowingModel, speed,
                        speedLimitInfo, vehicleWidth));
            }

            // indicator if bus
            if (lane.isCurrent())
            {
                // TODO: priority rules of busses should be handled differently, this also makes a GTU type unnecessary here
                if (gtu.getStrategicalPlanner().getRoute() instanceof BusSchedule && gtu.getType().isOfType(DefaultsNl.BUS)
                        && conflict.getConflictRuleType().equals(BusStopConflictRule.class))
                {
                    BusSchedule busSchedule = (BusSchedule) gtu.getStrategicalPlanner().getRoute();
                    Time actualDeparture = busSchedule.getActualDepartureConflict(conflict.getId());
                    if (actualDeparture != null
                            && actualDeparture.si < gtu.getSimulator().getSimulatorTime().si + parameters.getParameter(TI).si)
                    {
                        // TODO depending on left/right-hand traffic
                        conflictPlans.setIndicatorIntent(TurnIndicatorIntent.LEFT, conflict.getDistance());
                    }
                }
            }

            // blocking and ignoring
            if (conflict.getDistance().lt0() && lane.isCurrent())
            {
                if (conflict.getConflictType().isCrossing() && !conflict.getConflictPriority().isPriority())
                {
                    // note that we are blocking a conflict
                    blocking = true;
                }
                // ignore conflicts we are on (i.e. negative distance to start of conflict)
                continue;
            }

            // determine if we need to stop
            boolean stop;
            switch (conflict.getConflictPriority())
            {
                case PRIORITY:
                {
                    Length prevEnd = prevEnds.isEmpty() ? null : prevEnds.get(prevEnds.size() - 1);
                    stop = stopForPriorityConflict(conflict, leaders, speed, vehicleLength, parameters, prevEnd);
                    break;
                }
                case YIELD: // TODO depending on rules, we may need to stop and not just yield
                {
                    Length prevEnd = prevEnds.isEmpty() ? null : prevEnds.get(prevEnds.size() - 1);
                    stop = stopForGiveWayConflict(conflict, leaders, speed, acceleration, vehicleLength, parameters,
                            speedLimitInfo, carFollowingModel, blocking ? BCRIT : B, prevEnd);
                    break;
                }
                case STOP:
                {
                    Length prevEnd = prevEnds.isEmpty() ? null : prevEnds.get(prevEnds.size() - 1);
                    stop = stopForStopConflict(conflict, leaders, speed, acceleration, vehicleLength, parameters,
                            speedLimitInfo, carFollowingModel, blocking ? BCRIT : B, prevEnd);
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
                    throw new GtuException("Unsupported conflict rule encountered while approaching conflicts.");
                }
            }

            // stop if required, account for upstream conflicts to keep clear
            if (!conflict.getConflictType().equals(ConflictType.SPLIT))
            {

                if (stop)
                {
                    prevStarts.add(conflict.getDistance());
                    conflictRuleTypes.add(conflict.getConflictRuleType());
                    // stop for first conflict looking upstream of this blocked conflict that allows sufficient space
                    int j = 0; // most upstream conflict if not in between conflicts
                    for (int i = prevEnds.size() - 1; i >= 0; i--) // downstream to upstream
                    {
                        // note, at this point prevStarts contains one more conflict than prevEnds
                        if (prevStarts.get(i + 1).minus(prevEnds.get(i)).gt(passableDistance(vehicleLength, parameters)))
                        {
                            j = i + 1;
                            break;
                        }
                    }
                    if (blocking && j == 0)
                    {
                        // we are blocking a conflict, let's not stop more upstream than the conflict that forces our stop
                        j = prevStarts.size() - 1;
                    }

                    // TODO
                    // if this lowers our acceleration, we need to check if we are able to pass upstream conflicts still in time

                    // stop for j'th conflict, if deceleration is too strong, for next one
                    parameters.setParameterResettable(S0, parameters.getParameter(S0_CONF));
                    Acceleration aCF = new Acceleration(-Double.MAX_VALUE, AccelerationUnit.SI);
                    while (aCF.si < -6.0 && j < prevStarts.size())
                    {
                        if (prevStarts.get(j).lt(parameters.getParameter(S0_CONF)))
                        {
                            // TODO what to do when we happen to be in the stopping distance? Stopping might be reasonable,
                            // while car-following might give strong deceleration due to s < s0.
                            aCF = Acceleration.max(aCF, new Acceleration(-6.0, AccelerationUnit.SI));
                        }
                        else
                        {
                            Acceleration aStop = CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo,
                                    prevStarts.get(j));
                            if (conflictRuleTypes.get(j).equals(BusStopConflictRule.class)
                                    && aStop.lt(parameters.getParameter(ParameterTypes.BCRIT).neg()))
                            {
                                // as it may suddenly switch state, i.e. ignore like a yellow traffic light
                                aStop = Acceleration.POS_MAXVALUE;
                            }
                            aCF = Acceleration.max(aCF, aStop);
                        }
                        j++;
                    }
                    parameters.resetParameter(S0);
                    a = Acceleration.min(a, aCF);
                    break;
                }

                // keep conflict clear (when stopping for another conflict)
                prevStarts.add(conflict.getDistance());
                conflictRuleTypes.add(conflict.getConflictRuleType());
                prevEnds.add(conflict.getDistance().plus(conflict.getLength()));
            }

        }
        conflictPlans.setBlocking(blocking);

        if (a.si < -6.0 && speed.si > 5 / 3.6)
        {
            System.err.println("Deceleration from conflict util stronger than 6m/s^2.");
            // return Acceleration.POSITIVE_INFINITY;
        }
        return a;
    }

    /**
     * Determines acceleration for following conflicting vehicles <i>on</i> a merge or split conflict.
     * @param conflict HeadwayConflict; merge or split conflict
     * @param parameters Parameters; parameters
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param vehicleWidth Length; own width
     * @return acceleration for following conflicting vehicles <i>on</i> a merge or split conflict
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    private static Acceleration followConflictingLeaderOnMergeOrSplit(final HeadwayConflict conflict,
            final Parameters parameters, final CarFollowingModel carFollowingModel, final Speed speed,
            final SpeedLimitInfo speedLimitInfo, final Length vehicleWidth) throws ParameterException
    {
        // ignore if no conflicting GTU's, or if first is downstream of conflict
        PerceptionIterable<HeadwayGtu> downstreamGTUs = conflict.getDownstreamConflictingGTUs();
        if (downstreamGTUs.isEmpty() || downstreamGTUs.first().isAhead())
        {
            return Acceleration.POS_MAXVALUE;
        }
        // get the most upstream GTU to consider
        HeadwayGtu c = null;
        Length virtualHeadway = null;
        if (conflict.getDistance().gt0())
        {
            c = downstreamGTUs.first();
            virtualHeadway = conflict.getDistance().plus(c.getOverlapRear());
        }
        else
        {
            for (HeadwayGtu con : downstreamGTUs)
            {
                if (con.isAhead())
                {
                    // conflict GTU completely downstream of conflict (i.e. regular car-following, ignore here)
                    return Acceleration.POS_MAXVALUE;
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
                virtualHeadway = conflict.getDistance().plus(con.getOverlapRear());
                if (virtualHeadway.gt0())
                {
                    if (conflict.isSplit())
                    {
                        double conflictWidth = conflict.getWidthAtFraction(
                                (-conflict.getDistance().si + virtualHeadway.si) / conflict.getConflictingLength().si).si;
                        double gtuWidth = con.getWidth().si + vehicleWidth.si;
                        if (conflictWidth > gtuWidth)
                        {
                            continue;
                        }
                    }
                    // found first downstream GTU on conflict
                    c = con;
                    break;
                }
            }
        }
        if (c == null)
        {
            // conflict GTU downstream of start of conflict, but upstream of us
            return Acceleration.POS_MAXVALUE;
        }
        // follow leader
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(virtualHeadway, c.getSpeed());
        Acceleration a = CarFollowingUtil.followSingleLeader(carFollowingModel, parameters, speed, speedLimitInfo,
                virtualHeadway, c.getSpeed());
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
            parameters.setParameterResettable(S0, parameters.getParameter(S0_CONF));
            Acceleration aStop =
                    CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo, conflict.getDistance());
            parameters.resetParameter(S0);
            a = Acceleration.max(a, aStop); // max, which ever allows the largest acceleration
        }
        return a;
    }

    /**
     * Determines an acceleration required to avoid a collision with GTUs <i>on</i> a crossing conflict.
     * @param parameters Parameters; parameters
     * @param conflict HeadwayConflict; conflict
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @return acceleration required to avoid a collision
     * @throws ParameterException if parameter is not defined
     */
    private static Acceleration avoidCrossingCollision(final Parameters parameters, final HeadwayConflict conflict,
            final CarFollowingModel carFollowingModel, final Speed speed, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException
    {

        // TODO only within visibility
        List<HeadwayGtu> conflictingGTUs = new ArrayList<>();
        for (HeadwayGtu gtu : conflict.getUpstreamConflictingGTUs())
        {
            if (isOnRoute(conflict.getConflictingLink(), gtu))
            {
                // first upstream vehicle on route to this conflict
                conflictingGTUs.add(gtu);
                break;
            }
        }
        for (HeadwayGtu gtu : conflict.getDownstreamConflictingGTUs())
        {
            if (gtu.isParallel())
            {
                conflictingGTUs.add(gtu);
            }
            else
            {
                // vehicles beyond conflict are not a thread
                break;
            }
        }

        if (conflictingGTUs.isEmpty())
        {
            return Acceleration.POS_MAXVALUE;
        }

        Acceleration a = Acceleration.POS_MAXVALUE;
        for (HeadwayGtu conflictingGTU : conflictingGTUs)
        {
            AnticipationInfo tteC;
            Length distance;
            if (conflictingGTU.isParallel())
            {
                tteC = new AnticipationInfo(Duration.ZERO, conflictingGTU.getSpeed());
                distance = conflictingGTU.getOverlapRear().abs().plus(conflictingGTU.getOverlap())
                        .plus(conflictingGTU.getOverlapFront().abs());
            }
            else
            {
                tteC = AnticipationInfo.anticipateMovement(conflictingGTU.getDistance(), conflictingGTU.getSpeed(),
                        Acceleration.ZERO);
                distance = conflictingGTU.getDistance().plus(conflict.getLength()).plus(conflictingGTU.getLength());
            }
            AnticipationInfo ttcC = AnticipationInfo.anticipateMovement(distance, conflictingGTU.getSpeed(), Acceleration.ZERO);
            AnticipationInfo tteO = AnticipationInfo.anticipateMovementFreeAcceleration(conflict.getDistance(), speed,
                    parameters, carFollowingModel, speedLimitInfo, TIME_STEP);
            // enter before cleared
            // TODO safety factor?
            if (tteC.duration().lt(tteO.duration()) && tteO.duration().lt(ttcC.duration()))
            {
                if (!conflictingGTU.getSpeed().eq0() || !CROSSSTANDING)
                {
                    // solve parabolic speed profile s = v*t + .5*a*t*t, a =
                    double acc = 2 * (conflict.getDistance().si - speed.si * ttcC.duration().si)
                            / (ttcC.duration().si * ttcC.duration().si);
                    // time till zero speed > time to avoid conflict?
                    if (speed.si / -acc > ttcC.duration().si)
                    {
                        a = Acceleration.min(a, new Acceleration(acc, AccelerationUnit.SI));
                    }
                    else
                    {
                        // will reach zero speed ourselves
                        a = Acceleration.min(a, CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo,
                                conflict.getDistance()));
                    }
                }
                // conflicting vehicle stand-still, ignore even at conflict
            }
        }
        return a;
    }

    /**
     * Avoid collision at merge. This method assumes the GTU has priority.
     * @param parameters Parameters; parameters
     * @param conflict HeadwayConflict; conflict
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @return acceleration required to avoid a collision
     * @throws ParameterException if parameter is not defined
     */
    private static Acceleration avoidMergeCollision(final Parameters parameters, final HeadwayConflict conflict,
            final CarFollowingModel carFollowingModel, final Speed speed, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException
    {
        PerceptionCollectable<HeadwayGtu, LaneBasedGtu> conflicting = conflict.getUpstreamConflictingGTUs();
        if (conflicting.isEmpty() || conflicting.first().isParallel())
        {
            return Acceleration.POS_MAXVALUE;
        }
        // TODO: this check is simplistic, designed quick and dirty
        HeadwayGtu conflictingGtu = conflicting.first();
        double tteC = conflictingGtu.getDistance().si / conflictingGtu.getSpeed().si;
        if (tteC < conflict.getDistance().si / speed.si + 3.0)
        {
            return CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo, conflict.getDistance());
        }
        return Acceleration.POS_MAXVALUE;
    }

    /**
     * Approach a priority conflict. Stopping is applied to give way to conflicting traffic in case congestion is present on the
     * own lane. This is courtesy yielding.
     * @param conflict HeadwayConflict; conflict to approach
     * @param leaders PerceptionCollectable&lt;HeadwayGtu,LaneBasedGtu&gt;; leading vehicles in own lane
     * @param speed Speed; current speed
     * @param vehicleLength Length; vehicle length
     * @param parameters Parameters; parameters
     * @param prevEnd Length; distance to end of previous conflict that should not be blocked, {@code null} if none
     * @return whether to stop for this conflict
     * @throws ParameterException if parameter B is not defined
     */
    public static boolean stopForPriorityConflict(final HeadwayConflict conflict,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final Speed speed, final Length vehicleLength,
            final Parameters parameters, final Length prevEnd) throws ParameterException
    {

        // check if we should stop as there is not sufficient space on the merge, to leave a previous conflict unblocked
        Length passable = passableDistance(vehicleLength, parameters);
        if (prevEnd != null && conflict.isMerge() && !conflict.getDownstreamConflictingGTUs().isEmpty())
        {
            HeadwayGtu conflictingGTU = conflict.getDownstreamConflictingGTUs().first();
            Acceleration b = parameters.getParameter(BCRIT);
            double t = conflictingGTU.getSpeed().divide(b).si;
            Length stopDistance = Length.instantiateSI(conflictingGTU.getSpeed().si * t - .5 * b.si * t * t);
            Length room = conflict
                    .getDistance().plus(stopDistance).plus(conflictingGTU.isAhead()
                            ? conflict.getLength().plus(conflictingGTU.getDistance()) : conflictingGTU.getOverlapRear())
                    .minus(prevEnd);
            if (room.lt(passable))
            {
                return true;
            }
        }

        // some quick -no need to stop-'s
        if (leaders.isEmpty())
        {
            // no leader
            return false;
        }
        if (conflict.getUpstreamConflictingGTUs().isEmpty())
        {
            // no conflicting vehicles
            return false;
        }
        else
        {
            HeadwayGtu conflictingGTU = conflict.getUpstreamConflictingGTUs().first();
            if (conflictingGTU.getSpeed().eq0() && conflictingGTU.isAhead()
                    && conflictingGTU.getDistance().gt(parameters.getParameter(S0)))
            {
                // conflicting stationary vehicle too far away
                return false;
            }
        }

        // Stop as long as some leader is standing still, and this leader is not leaving sufficient space yet
        // use start of conflict on merge, end of conflict on crossing
        Length typeCorrection = conflict.isCrossing() ? conflict.getLength() : Length.ZERO;
        // distance ego vehicle has to cover to pass the conflict
        Length required = conflict.getDistance().plus(typeCorrection).plus(passable);
        // distance leader has to cover before we can pass the conflict
        Length requiredLeader = required.minus(leaders.first().getDistance());
        if (requiredLeader.gt0())
        {
            for (HeadwayGtu leader : leaders)
            {
                if (leader.getSpeed().eq0())
                {
                    // first stand-still leader is not fully upstream of the conflict (in that case, ignore), and does not
                    // allow sufficient space for all vehicles in between
                    return leader.getDistance().ge(conflict.getDistance()) && required.ge(leader.getDistance());
                }
                required = required // add required distance for leaders
                        .plus(passableDistance(leader.getLength(), leader.getParameters()));
            }
        }

        // no reason found to stop
        return false;
    }

    /**
     * Approach a give-way conflict.
     * @param conflict HeadwayConflict; conflict
     * @param leaders PerceptionCollectable&lt;HeadwayGtu,LaneBasedGtu&gt;; leaders
     * @param speed Speed; current speed
     * @param acceleration Acceleration; current acceleration
     * @param vehicleLength Length; vehicle length
     * @param parameters Parameters; parameters
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param bType ParameterTypeAcceleration; parameter type for considered deceleration
     * @param prevEnd Length; distance to end of previous conflict that should not be blocked, {@code null} if none
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings({"checkstyle:parameternumber", "checkstyle:methodlength"})
    public static boolean stopForGiveWayConflict(final HeadwayConflict conflict,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final Speed speed, final Acceleration acceleration,
            final Length vehicleLength, final Parameters parameters, final SpeedLimitInfo speedLimitInfo,
            final CarFollowingModel carFollowingModel, final ParameterTypeAcceleration bType, final Length prevEnd)
            throws ParameterException
    {

        // TODO conflicting vehicle on crossing conflict, but will leave sooner then we enter, so no problem?
        // TODO more generally, also upstream conflicting vehicles at crossings may leave the conflict before we enter
        if (conflict.getConflictType().isCrossing() && !conflict.getDownstreamConflictingGTUs().isEmpty()
                && conflict.getDownstreamConflictingGTUs().first().isParallel())
        {
            // vehicle on the conflict
            return true;
        }

        // Get data independent of conflicting vehicle
        // parameters
        Acceleration b = parameters.getParameter(bType).neg();
        double f = parameters.getParameter(TIME_FACTOR);
        Duration gap = parameters.getParameter(MIN_GAP);
        Length passable = passableDistance(vehicleLength, parameters);
        // time till conflict is cleared
        Length distance = conflict.getDistance().plus(vehicleLength);
        if (conflict.isCrossing())
        {
            // merge is cleared at start, crossing at end
            distance = distance.plus(conflict.getLength());
        }
        // based on acceleration, limited by free acceleration
        AnticipationInfo ttcOa = AnticipationInfo.anticipateMovementFreeAcceleration(distance, speed, parameters,
                carFollowingModel, speedLimitInfo, TIME_STEP);
        // time till downstream vehicle will make the conflict passible, under constant speed or safe deceleration
        AnticipationInfo ttpDz = null;
        AnticipationInfo ttpDs = null;
        if (conflict.isCrossing())
        {
            if (!leaders.isEmpty())
            {
                distance =
                        conflict.getDistance().minus(leaders.first().getDistance()).plus(conflict.getLength()).plus(passable);
                ttpDz = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), Acceleration.ZERO);
                ttpDs = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), b);
            }
            else
            {
                // no leader so conflict is passable within a duration of 0
                ttpDz = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
                ttpDs = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
            }
        }
        else if (conflict.isMerge() && prevEnd != null)
        {
            // stop for merge (and previous conflict) if we are likely to stop partially on the previous conflict
            Length preGap = conflict.getDistance().minus(prevEnd);
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> downs = conflict.getDownstreamConflictingGTUs();
            if (!downs.isEmpty() && downs.first().isParallel())
            {
                distance = passable.minus(preGap).minus(downs.first().getOverlapRear());
                ttpDz = AnticipationInfo.anticipateMovement(distance, downs.first().getSpeed(), Acceleration.ZERO);
                if (ttpDz.duration().equals(Duration.POSITIVE_INFINITY))
                {
                    // vehicle on conflict will not leave sufficient space
                    return true;
                }
                ttpDs = AnticipationInfo.anticipateMovement(distance, downs.first().getSpeed(), b);
            }
            else if (!leaders.isEmpty())
            {
                distance = conflict.getDistance().plus(passable).minus(preGap).minus(leaders.first().getDistance());
                ttpDz = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), Acceleration.ZERO);
                if (ttpDz.duration().equals(Duration.POSITIVE_INFINITY))
                {
                    // vehicle on conflict will not leave sufficient space
                    return true;
                }
                ttpDs = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), b);
            }
        }

        PerceptionCollectable<HeadwayGtu, LaneBasedGtu> conflictingVehiclesCollectable = conflict.getUpstreamConflictingGTUs();
        Iterable<HeadwayGtu> conflictingVehicles;
        if (conflictingVehiclesCollectable.isEmpty())
        {
            if (conflict.getConflictingTrafficLightDistance() == null)
            {
                // none within visibility, assume a conflicting vehicle just outside of visibility driving at speed limit
                try
                {
                    HeadwayGtuSimple conflictGtu = new HeadwayGtuSimple("virtual " + UUID.randomUUID().toString(),
                            DefaultsNl.CAR, conflict.getConflictingVisibility(), new Length(4.0, LengthUnit.SI),
                            new Length(2.0, LengthUnit.SI), conflict.getConflictingSpeedLimit(), Acceleration.ZERO, Speed.ZERO);
                    List<HeadwayGtu> conflictingVehiclesList = new ArrayList<>();
                    conflictingVehiclesList.add(conflictGtu);
                    conflictingVehicles = conflictingVehiclesList;
                }
                catch (GtuException exception)
                {
                    throw new RuntimeException("Could not create a virtual conflicting vehicle at visibility range.",
                            exception);
                }
            }
            else
            {
                // no conflicting vehicles
                return false;
            }
        }
        else
        {
            HeadwayGtu conflicting = conflictingVehiclesCollectable.first();
            if (conflict.getConflictingTrafficLightDistance() != null && conflicting.isAhead()
                    && conflict.getConflictingTrafficLightDistance().lt(conflicting.getDistance())
                    && (conflicting.getSpeed().eq0() || conflicting.getAcceleration().lt0()))
            {
                // conflicting traffic upstream of traffic light
                return false;
            }
            conflictingVehicles = conflictingVehiclesCollectable;
        }

        // Loop over conflicting vehicles
        boolean first = true;
        for (HeadwayGtu conflictingVehicle : conflictingVehicles)
        {

            // skip if not on route
            if (!isOnRoute(conflict.getConflictingLink(), conflictingVehicle))
            {
                continue;
            }

            // time till conflict vehicle will enter, under free acceleration and safe deceleration
            AnticipationInfo tteCa;
            AnticipationInfo tteCs;
            if (first && conflictingVehicle.getSpeed().eq0() && conflictingVehicle.isAhead())
            {
                // do not stop if conflicting vehicle is standing still
                return false;
            }
            else
            {
                if (conflictingVehicle instanceof HeadwayGtuSimple)
                {
                    tteCa = AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(),
                            conflictingVehicle.getAcceleration());
                }
                else
                {
                    Parameters params = conflictingVehicle.getParameters();
                    SpeedLimitInfo sli = conflictingVehicle.getSpeedLimitInfo();
                    CarFollowingModel cfm = conflictingVehicle.getCarFollowingModel();
                    // Constant acceleration creates inf at stand still, triggering passing trough a congested stream
                    if (conflictingVehicle.isAhead())
                    {
                        tteCa = AnticipationInfo.anticipateMovementFreeAcceleration(conflictingVehicle.getDistance(),
                                conflictingVehicle.getSpeed(), params, cfm, sli, TIME_STEP);
                    }
                    else
                    {
                        tteCa = new AnticipationInfo(Duration.ZERO, conflictingVehicle.getSpeed());
                    }
                }
                if (conflictingVehicle.isAhead())
                {
                    tteCs = AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(),
                            b);
                }
                else
                {
                    tteCs = new AnticipationInfo(Duration.ZERO, conflictingVehicle.getSpeed());
                }
            }

            // check gap
            if (conflict.isMerge())
            {

                // Merge, will be each others followers, add time to overcome speed difference
                double vSelf = ttcOa.endSpeed().si;
                double speedDiff = conflictingVehicle.getSpeed().si - vSelf;
                speedDiff = speedDiff > 0 ? speedDiff : 0;
                Duration additionalTime = new Duration(speedDiff / -b.si, DurationUnit.SI);
                // check if conflict vehicle will be upstream after that time, position beyond conflict after additional time
                double followerFront = conflictingVehicle.isAhead()
                        ? conflictingVehicle.getSpeed().si * ttcOa.duration().si - conflictingVehicle.getDistance().si
                                + (conflictingVehicle.getSpeed().si * additionalTime.si
                                        + 0.5 * b.si * additionalTime.si * additionalTime.si) // note: b < 0
                        : 0.0;
                double ownRear = vSelf * additionalTime.si; // constant speed after clearing
                Duration tMax = parameters.getParameter(ParameterTypes.TMAX);
                Length s0 = parameters.getParameter(S0);
                // 1) will clear the conflict after the conflict vehicle enters
                // 2) not sufficient time to overcome speed difference
                // 3) conflict vehicle will be too near after adjusting speed
                if (ttcOa.duration().times(f).plus(gap).gt(tteCa.duration())
                        // SKL 2024.02.15: I think this is nonsense, tteCs relates to the location of the conflict, not the
                        // location where the speed difference is resolved
                        // || ttcOa.getDuration().plus(additionalTime).times(f).plus(gap).gt(tteCs.getDuration())
                        || (!Double.isInfinite(tteCa.duration().si) && tteCa.duration().si > 0.0
                                && ownRear < (followerFront + (tMax.si + gap.si) * vSelf + s0.si) * f))
                {
                    return true;
                }

            }
            else if (conflict.isCrossing())
            {

                // Crossing, stop if order of events is not ok
                // Should go before the conflict vehicle
                // 1) downstream vehicle must supply sufficient space before conflict vehicle will enter
                // 2) must clear the conflict before the conflict vehicle will enter
                // 3) if leader decelerates with b, conflict vehicle should be able to safely delay entering conflict
                // 4) conflict vehicle will never leave enough space beyond the conflict
                if (ttpDz.duration().times(f).plus(gap).gt(tteCa.duration())
                        || ttcOa.duration().times(f).plus(gap).gt(tteCa.duration())
                        || ttpDs.duration().times(f).plus(gap).gt(tteCs.duration())
                        || ttpDs.duration().equals(Duration.POSITIVE_INFINITY))
                {
                    return true;
                }

            }
            else
            {
                throw new RuntimeException(
                        "Conflict is of unknown type " + conflict.getConflictType() + ", which is not merge nor a crossing.");
            }

            first = false;
        }

        // No conflict vehicle triggered stopping
        return false;

    }

    /**
     * Approach a stop conflict. Currently this is equal to approaching a give-way conflict.
     * @param conflict HeadwayConflict; conflict
     * @param leaders PerceptionCollectable&lt;HeadwayGtu,LaneBasedGtu&gt;; leaders
     * @param speed Speed; current speed
     * @param acceleration Acceleration; current acceleration
     * @param vehicleLength Length; vehicle length
     * @param parameters Parameters; parameters
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param bType ParameterTypeAcceleration; parameter type for considered deceleration
     * @param prevEnd Length; distance to end of previous conflict that should not be blocked, {@code null} if none
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static boolean stopForStopConflict(final HeadwayConflict conflict,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final Speed speed, final Acceleration acceleration,
            final Length vehicleLength, final Parameters parameters, final SpeedLimitInfo speedLimitInfo,
            final CarFollowingModel carFollowingModel, final ParameterTypeAcceleration bType, final Length prevEnd)
            throws ParameterException
    {
        // TODO stopping
        return stopForGiveWayConflict(conflict, leaders, speed, acceleration, vehicleLength, parameters, speedLimitInfo,
                carFollowingModel, bType, prevEnd);
    }

    /**
     * Approach an all-stop conflict.
     * @param conflict HeadwayConflict; conflict to approach
     * @param conflictPlans ConflictPlans; set of plans for conflict
     * @return whether to stop for this conflict
     */
    public static boolean stopForAllStopConflict(final HeadwayConflict conflict, final ConflictPlans conflictPlans)
    {
        // TODO all-stop behavior

        if (conflictPlans.isStopPhaseRun(conflict.getStopLine()))
        {
            return false;
        }

        return false;
    }

    /**
     * Returns whether the conflicting link is on the route of the given gtu.
     * @param conflictingLink CrossSectionLink; conflicting link
     * @param gtu HeadwayGtu; gtu
     * @return whether the conflict is on the route of the given gtu
     */
    private static boolean isOnRoute(final CrossSectionLink conflictingLink, final HeadwayGtu gtu)
    {
        try
        {
            Route route = gtu.getRoute();
            if (route == null)
            {
                // conservative assumption: it's on the route (gtu should be upstream of the conflict)
                return true;
            }
            Node startNode = conflictingLink.getStartNode();
            Node endNode = conflictingLink.getEndNode();
            return route.contains(startNode) && route.contains(endNode)
                    && Math.abs(route.indexOf(endNode) - route.indexOf(startNode)) == 1;
        }
        catch (UnsupportedOperationException uoe)
        {
            // conservative assumption: it's on the route (gtu should be upstream of the conflict)
            return true;
        }
    }

    /**
     * Returns a speed dependent distance needed behind the leader to completely pass the conflict.
     * @param vehicleLength Length; vehicle length
     * @param parameters Parameters; parameters
     * @return speed dependent distance needed behind the leader to completely pass the conflict
     * @throws ParameterException if parameter is not available
     */
    private static Length passableDistance(final Length vehicleLength, final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(S0).plus(vehicleLength);
    }

    /**
     * Holds the tactical plans of a driver considering conflicts. These are remembered for consistency. For instance, if the
     * decision is made to yield as current deceleration suggests it's safe to do so, but the trajectory for stopping in front
     * of the conflict results in deceleration slightly above what is considered safe deceleration, the plan should not be
     * abandoned. Decelerations above what is considered safe deceleration may result due to numerical overshoot or other factor
     * coming into play in car-following models. Many other examples exist where a driver sticks to a certain plan.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static final class ConflictPlans implements Blockable, Serializable
    {

        /** */
        private static final long serialVersionUID = 20160811L;

        /** Phases of navigating an all-stop intersection per intersection. */
        private final LinkedHashMap<String, StopPhase> stopPhases = new LinkedHashMap<>();

        /** Estimated arrival times of vehicles at all-stop intersection. */
        private final LinkedHashMap<String, Time> arrivalTimes = new LinkedHashMap<>();

        /** Indicator intent. */
        private TurnIndicatorIntent indicatorIntent = TurnIndicatorIntent.NONE;

        /** Distance to object causing turn indicator intent. */
        private Length indicatorObjectDistance = null;

        /** Whether the GTU is blocking conflicts. */
        private boolean blocking;

        /**
         * Clean any yield plan that was no longer kept active in the last evaluation of conflicts.
         */
        void cleanPlans()
        {
            this.indicatorIntent = TurnIndicatorIntent.NONE;
            this.indicatorObjectDistance = null;
        }

        /**
         * Sets the estimated arrival time of a GTU.
         * @param gtu AbstractHeadwayGtu; GTU
         * @param time Time; estimated arrival time
         */
        void setArrivalTime(final AbstractHeadwayGtu gtu, final Time time)
        {
            this.arrivalTimes.put(gtu.getId(), time);
        }

        /**
         * Returns the estimated arrival time of given GTU.
         * @param gtu AbstractHeadwayGtu; GTU
         * @return estimated arrival time of given GTU
         */
        Time getArrivalTime(final AbstractHeadwayGtu gtu)
        {
            return this.arrivalTimes.get(gtu.getId());
        }

        /**
         * Sets the current phase to 'approach' for the given stop line.
         * @param stopLine HeadwayStopLine; stop line
         */
        void setStopPhaseApproach(final HeadwayStopLine stopLine)
        {
            this.stopPhases.put(stopLine.getId(), StopPhase.APPROACH);
        }

        /**
         * Sets the current phase to 'yield' for the given stop line.
         * @param stopLine HeadwayStopLine; stop line
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
         * @param stopLine HeadwayStopLine; stop line
         * @throws RuntimeException if the phase was not set to approach before
         */
        void setStopPhaseRun(final HeadwayStopLine stopLine)
        {
            Throw.when(!this.stopPhases.containsKey(stopLine.getId()), RuntimeException.class,
                    "Run stop phase is set for stop line that was not approached.");
            this.stopPhases.put(stopLine.getId(), StopPhase.YIELD);
        }

        /**
         * @param stopLine HeadwayStopLine; stop line
         * @return whether the current phase is 'approach' for the given stop line
         */
        boolean isStopPhaseApproach(final HeadwayStopLine stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId())
                    && this.stopPhases.get(stopLine.getId()).equals(StopPhase.APPROACH);
        }

        /**
         * @param stopLine HeadwayStopLine; stop line
         * @return whether the current phase is 'yield' for the given stop line
         */
        boolean isStopPhaseYield(final HeadwayStopLine stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId())
                    && this.stopPhases.get(stopLine.getId()).equals(StopPhase.YIELD);
        }

        /**
         * @param stopLine HeadwayStopLine; stop line
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

        /**
         * @return indicatorIntent.
         */
        public TurnIndicatorIntent getIndicatorIntent()
        {
            return this.indicatorIntent;
        }

        /**
         * @return indicatorObjectDistance.
         */
        public Length getIndicatorObjectDistance()
        {
            return this.indicatorObjectDistance;
        }

        /**
         * @param intent TurnIndicatorIntent; indicator intent
         * @param distance Length; distance to object pertaining to the turn indicator intent
         */
        public void setIndicatorIntent(final TurnIndicatorIntent intent, final Length distance)
        {
            if (this.indicatorObjectDistance == null || this.indicatorObjectDistance.gt(distance))
            {
                this.indicatorIntent = intent;
                this.indicatorObjectDistance = distance;
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isBlocking()
        {
            return this.blocking;
        }

        /**
         * Sets the GTU as blocking conflicts or not.
         * @param blocking boolean; whether the GTU is blocking conflicts
         */
        public void setBlocking(final boolean blocking)
        {
            this.blocking = blocking;
        }

    }

    /**
     * Phases of navigating an all-stop intersection.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private enum StopPhase
    {
        /** Approaching stop intersection. */
        APPROACH,

        /** Yielding for stop intersection. */
        YIELD,

        /** Running over stop intersection. */
        RUN;
    }

}
