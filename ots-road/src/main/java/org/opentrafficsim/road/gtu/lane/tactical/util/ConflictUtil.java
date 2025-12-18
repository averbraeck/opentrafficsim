package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
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
import org.opentrafficsim.road.gtu.lane.Break;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu.Maneuver;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu.Signals;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtuBase;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtuSimple;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.conflict.BusStopConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * This class implements default behavior for intersection conflicts for use in tactical planners.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
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
            Duration.ofSI(3.0), ConstraintInterface.POSITIVE);

    /** Time step for free acceleration anticipation. */
    private static final Duration TIME_STEP = new Duration(0.5, DurationUnit.SI);

    /** Cross standing vehicles on crossings. We allow this to prevent dead-locks. A better model should render this useless. */
    private static final boolean CROSSSTANDING = true;

    /**
     * Do not instantiate.
     */
    private ConflictUtil()
    {
        //
    }

    /**
     * Approach conflicts by applying appropriate acceleration (or deceleration). The model may yield for a vehicle even while
     * having priority. Such a plan is remembered in {@link ConflictPlans}. By forwarding the same {@code ConflictPlans} for a
     * GTU consistency of such plans is provided. If any conflict is not accepted to pass, stopping before a more upstream
     * conflict is applied if there is not sufficient stopping length in between conflicts.
     * @param parameters parameters
     * @param conflicts set of conflicts to approach
     * @param leaders leading vehicles
     * @param carFollowingModel car-following model
     * @param vehicleLength length of vehicle
     * @param vehicleWidth width of vehicle
     * @param speed current speed
     * @param acceleration current acceleration
     * @param speedLimitInfo speed limit info
     * @param conflictPlans set of plans for conflict
     * @param gtu gtu
     * @param lane lane
     * @return acceleration appropriate for approaching the conflicts
     * @throws GtuException in case of an unsupported conflict rule
     * @throws ParameterException if a parameter is not defined or out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    // @docs/06-behavior/tactical-planner/#modular-utilities (..., final ConflictPlans conflictPlans, ...)
    public static Acceleration approachConflicts(final Parameters parameters, final Iterable<PerceivedConflict> conflicts,
            final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders, final CarFollowingModel carFollowingModel,
            final Length vehicleLength, final Length vehicleWidth, final Speed speed, final Acceleration acceleration,
            final SpeedLimitInfo speedLimitInfo, final ConflictPlans conflictPlans, final LaneBasedGtu gtu,
            final RelativeLane lane) throws GtuException, ParameterException
    {
        conflictPlans.cleanPlans();
        boolean blocking = false;

        // Ignore conflicts if we are beyond a stopping distance
        Acceleration a = Acceleration.POS_MAXVALUE;
        Length stoppingDistance = Length.ofSI(
                parameters.getParameter(S0).si + vehicleLength.si + .5 * speed.si * speed.si / parameters.getParameter(B).si);
        Iterator<PerceivedConflict> it = conflicts.iterator();
        if (it.hasNext() && it.next().getDistance().gt(stoppingDistance))
        {
            conflictPlans.setBlocking(blocking);
            return a;
        }

        // Maintain lists of info per conflict required for consistency in a plan to cross (part of) an intersection
        List<Length> prevStarts = new ArrayList<>();
        List<Length> prevEnds = new ArrayList<>();
        List<Class<? extends ConflictRule>> conflictRuleTypes = new ArrayList<>();

        // Distance until first stationary leader, minus spaces required for stationary intermediate vehicle and minus ego
        Space space = leaders.collect(new AvailableSpace());
        Length availableSpace = space.availableSpace().minus(passableDistance(vehicleLength, parameters));

        for (PerceivedConflict conflict : conflicts)
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
                    // this is probably evaluation for a lane-change as this it not on the current lane
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
                // TODO priority rules of busses should be handled differently, this also makes a GTU type unnecessary here
                if (gtu.getStrategicalPlanner().getRoute() instanceof BusSchedule && gtu.getType().isOfType(DefaultsNl.BUS)
                        && conflict.getConflictRuleType().equals(BusStopConflictRule.class))
                {
                    BusSchedule busSchedule = (BusSchedule) gtu.getStrategicalPlanner().getRoute();
                    Duration actualDeparture = busSchedule.getActualDepartureConflict(conflict.getId());
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

            // zip-merging
            boolean stop = false;
            if (conflict.isMerge() && conflict.getConflictPriority().isPriority())
            {
                if (conflict.getUpstreamConflictingGTUs().isEmpty()
                        || !conflictPlans.isZipGtu(conflict.getUpstreamConflictingGTUs().first().getId()))
                {
                    conflictPlans.clearZipGtu();
                }
                else
                {
                    stop = true;
                }
            }

            // determine if we need to stop by available space downstream
            if (!stop)
            {
                Length d = conflict.isCrossing() ? conflict.getDistance().plus(conflict.getLength()) : conflict.getDistance();
                stop = !conflict.getConflictType().isSplit() && d.lt(space.firstStationary()) && availableSpace.lt(d);

                // trigger zip-merging
                /*
                 * Note that when a vehicle is fully on a merge, only vehicles from the same direction consider it regarding
                 * available space. Vehicles from the other direction do not have the vehicle as one of their regular leaders.
                 * One vehicle from the other direction will thus put the nose on the merge or close to it. If this is on the
                 * merge, zip behavior automatically results. If it is close to the merge, and the vehicle on the conflict was
                 * from the priority direction, the priority vehicle upstream of the conflict needs to remember to let the other
                 * vehicle go. Otherwise, as soon as traffic starts to move and the available space heuristic is lifted, regular
                 * priority behavior results. This may cause the non-priority direction to never flow.
                 */
                if (stop && conflict.isMerge() && conflict.getConflictPriority().isPriority() && conflict.getDistance().gt0()
                        && !conflict.getUpstreamConflictingGTUs().isEmpty())
                {
                    conflictPlans.setZipGtu(conflict.getUpstreamConflictingGTUs().first().getId());
                }
            }

            if (!stop)
            {
                switch (conflict.getConflictPriority())
                {
                    case PRIORITY:
                    {
                        // available space consideration and zip-merging allow no further action
                        break;
                    }
                    case YIELD:
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
                        continue;
                    }
                    default:
                    {
                        throw new GtuException("Unsupported conflict rule encountered while approaching conflicts.");
                    }
                }
            }

            // stop if required, account for upstream conflicts to keep clear
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

                // stop for j'th conflict, if deceleration is too strong, for next one
                parameters.setParameterResettable(S0, parameters.getParameter(S0_CONF));
                Acceleration bCrit = parameters.getParameter(ParameterTypes.BCRIT).neg();
                Acceleration aConflict = Acceleration.ofSI(-Double.MAX_VALUE);
                while (aConflict.si < bCrit.si && j < prevStarts.size())
                {
                    if (prevStarts.get(j).lt(parameters.getParameter(S0_CONF)))
                    {
                        // critical deceleration once GTU is within s0_conf
                        // otherwise car-following model may generate unreasonably large decelerations
                        aConflict = Acceleration.max(aConflict, bCrit);
                    }
                    else
                    {
                        Acceleration aStop =
                                CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo, prevStarts.get(j));
                        if (conflictRuleTypes.get(j).equals(BusStopConflictRule.class) && aStop.lt(bCrit))
                        {
                            // as it may suddenly switch state, i.e. ignore like a yellow traffic light
                            aStop = Acceleration.POS_MAXVALUE;
                        }
                        aConflict = Acceleration.max(aConflict, aStop);
                    }
                    j++;
                }
                parameters.resetParameter(S0);
                a = Acceleration.min(a, aConflict);
                break;
            }

            // remember info to keep conflict clear (when stopping for another conflict)
            if (conflict.isCrossing())
            {
                prevStarts.add(conflict.getDistance());
                conflictRuleTypes.add(conflict.getConflictRuleType());
                prevEnds.add(conflict.getDistance().plus(conflict.getLength()));
            }
        }
        conflictPlans.setBlocking(blocking);

        if (a.si < -6.0 && speed.si > 5.0 / 3.6)
        {
            Logger.ots().info("Deceleration from conflict util stronger than 6m/s^2.");
            // return Acceleration.POSITIVE_INFINITY;
        }
        return a;
    }

    /**
     * Determines acceleration for following conflicting vehicles <i>on</i> a merge or split conflict.
     * @param conflict merge or split conflict
     * @param parameters parameters
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @param vehicleWidth own width
     * @return acceleration for following conflicting vehicles <i>on</i> a merge or split conflict
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    private static Acceleration followConflictingLeaderOnMergeOrSplit(final PerceivedConflict conflict,
            final Parameters parameters, final CarFollowingModel carFollowingModel, final Speed speed,
            final SpeedLimitInfo speedLimitInfo, final Length vehicleWidth) throws ParameterException
    {
        // ignore if no conflicting GTU's, or if first is downstream of conflict
        PerceptionIterable<PerceivedGtu> downstreamGTUs = conflict.getDownstreamConflictingGTUs();
        if (downstreamGTUs.isEmpty() || downstreamGTUs.first().getKinematics().getOverlap().isAhead())
        {
            return Acceleration.POS_MAXVALUE;
        }
        // get the most upstream GTU to consider
        PerceivedGtu c = null;
        Length virtualHeadway = null;
        if (conflict.getDistance().gt0())
        {
            c = downstreamGTUs.first();
            virtualHeadway = conflict.getDistance().plus(c.getKinematics().getOverlap().getOverlapRear());
        }
        else
        {
            for (PerceivedGtu con : downstreamGTUs)
            {
                if (con.getKinematics().getOverlap().isAhead())
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
                virtualHeadway = conflict.getDistance().plus(con.getKinematics().getOverlap().getOverlapRear());
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
     * @param parameters parameters
     * @param conflict conflict
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @return acceleration required to avoid a collision
     * @throws ParameterException if parameter is not defined
     */
    private static Acceleration avoidCrossingCollision(final Parameters parameters, final PerceivedConflict conflict,
            final CarFollowingModel carFollowingModel, final Speed speed, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException
    {
        // gather relevant GTUs (first up, and downstream on)
        List<PerceivedGtu> conflictingGTUs = new ArrayList<>();
        for (PerceivedGtu gtu : conflict.getUpstreamConflictingGTUs())
        {
            if (conflict.getConflictingVisibility().lt(gtu.getDistance()))
            {
                break;
            }
            if (isOnRoute(conflict.getConflictingLink(), gtu))
            {
                // first upstream vehicle on route to this conflict
                conflictingGTUs.add(gtu);
                break;
            }
        }
        for (PerceivedGtu gtu : conflict.getDownstreamConflictingGTUs())
        {
            if (gtu.getKinematics().getOverlap().isParallel())
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
        for (PerceivedGtu conflictingGTU : conflictingGTUs)
        {
            // time till enter, conflicting vehicle, no acceleration
            AnticipationInfo tteCz;
            Length distance;
            if (conflictingGTU.getKinematics().getOverlap().isParallel())
            {
                tteCz = new AnticipationInfo(Duration.ZERO, conflictingGTU.getSpeed());
                distance = conflictingGTU.getKinematics().getOverlap().getOverlapRear().abs()
                        .plus(conflictingGTU.getKinematics().getOverlap().getOverlap())
                        .plus(Length.max(Length.ZERO, conflictingGTU.getKinematics().getOverlap().getOverlapFront().neg()));
            }
            else
            {
                tteCz = AnticipationInfo.anticipateMovement(conflictingGTU.getDistance(), conflictingGTU.getSpeed(),
                        Acceleration.ZERO);
                distance = conflictingGTU.getDistance().plus(conflict.getLength()).plus(conflictingGTU.getLength());
            }
            // time till clear (rear past conflict), conflicting vehicle, no acceleration
            AnticipationInfo ttcCz =
                    AnticipationInfo.anticipateMovement(distance, conflictingGTU.getSpeed(), Acceleration.ZERO);
            // time till enter, own vehicle, free acceleration
            AnticipationInfo tteOa = AnticipationInfo.anticipateMovementFreeAcceleration(conflict.getDistance(), speed,
                    parameters, carFollowingModel, speedLimitInfo, TIME_STEP);
            // enter before cleared (tteCz < tteOa < ttcCz)
            // TODO safety factor?
            if (tteCz.duration().lt(tteOa.duration()) && tteOa.duration().lt(ttcCz.duration()))
            {
                if (!conflictingGTU.getSpeed().eq0() || !CROSSSTANDING)
                {
                    double t = ttcCz.duration().si;
                    // solve parabolic speed profile s = v*t + .5*a*t*t, a =
                    double acc = 2.0 * (conflict.getDistance().si - speed.si * t) / (t * t);
                    // time till zero speed > time to avoid conflict?
                    if (speed.si / -acc > ttcCz.duration().si)
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
     * @param parameters parameters
     * @param conflict conflict
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @return acceleration required to avoid a collision
     * @throws ParameterException if parameter is not defined
     */
    private static Acceleration avoidMergeCollision(final Parameters parameters, final PerceivedConflict conflict,
            final CarFollowingModel carFollowingModel, final Speed speed, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException
    {
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> conflicting = conflict.getUpstreamConflictingGTUs();
        // parallel, followConflictingLeaderOnMergeOrSplit?
        if (conflicting.isEmpty() || conflicting.first().getKinematics().getOverlap().isParallel())
        {
            return Acceleration.POS_MAXVALUE;
        }
        // TODO: this check is simplistic, designed quick and dirty, it just adds 3s as a safe gap
        PerceivedGtu conflictingGtu = conflicting.first();
        double tteC = conflictingGtu.getDistance().si / conflictingGtu.getSpeed().si;
        if (tteC < conflict.getDistance().si / speed.si + 3.0)
        {
            return CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo, conflict.getDistance());
        }
        return Acceleration.POS_MAXVALUE;
    }

    /**
     * Approach a give-way conflict.
     * @param conflict conflict
     * @param leaders leaders
     * @param speed current speed
     * @param acceleration current acceleration
     * @param vehicleLength vehicle length
     * @param parameters parameters
     * @param speedLimitInfo speed limit info
     * @param carFollowingModel car-following model
     * @param bType parameter type for considered deceleration
     * @param prevEnd distance to end of previous conflict that should not be blocked, {@code null} if none
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings({"checkstyle:parameternumber", "checkstyle:methodlength"})
    public static boolean stopForGiveWayConflict(final PerceivedConflict conflict,
            final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders, final Speed speed, final Acceleration acceleration,
            final Length vehicleLength, final Parameters parameters, final SpeedLimitInfo speedLimitInfo,
            final CarFollowingModel carFollowingModel, final ParameterTypeAcceleration bType, final Length prevEnd)
            throws ParameterException
    {
        // Account for limited visibility and traffic light
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> conflictingVehiclesCollectable =
                conflict.getUpstreamConflictingGTUs();
        Iterable<PerceivedGtu> conflictingVehicles;
        if (conflictingVehiclesCollectable.isEmpty())
        {
            if (conflict.getConflictingTrafficLightDistance() == null)
            {
                // none within visibility, assume a conflicting vehicle just outside of visibility driving at speed limit
                Length length = Length.ofSI(4.0);
                PerceivedGtuSimple conflictGtu = new PerceivedGtuSimple("virtual " + UUID.randomUUID().toString(),
                        DefaultsNl.CAR, length, new Length(2.0, LengthUnit.SI),
                        Kinematics.dynamicBehind(conflict.getConflictingVisibility(), conflict.getConflictingSpeedLimit(),
                                Acceleration.ZERO, true, length, conflict.getLength()),
                        Signals.NONE, Maneuver.NONE);
                conflictingVehicles = Set.of(conflictGtu);
            }
            else
            {
                // no conflicting vehicles
                return false;
            }
        }
        else
        {
            PerceivedGtu conflicting = conflictingVehiclesCollectable.first();
            if (conflict.getConflictingTrafficLightDistance() != null && conflicting.getKinematics().getOverlap().isAhead()
                    && conflict.getConflictingTrafficLightDistance().lt(conflicting.getDistance())
                    && (conflicting.getSpeed().eq0() || conflicting.getAcceleration().lt0()))
            {
                // conflicting traffic upstream of traffic light
                return false;
            }
            conflictingVehicles = conflictingVehiclesCollectable;
        }

        // Get data independent of conflicting vehicle
        Acceleration b = parameters.getParameter(bType).neg();
        double f = parameters.getParameter(TIME_FACTOR);
        Duration gap = parameters.getParameter(MIN_GAP);
        Length passable = passableDistance(vehicleLength, parameters);
        Length distance = conflict.getDistance().plus(vehicleLength);
        if (conflict.isCrossing())
        {
            distance = distance.plus(conflict.getLength()); // merge is cleared at start, crossing at end
        }

        // time till clear (i.e. rear leaves conflict), own vehicle, free acceleration
        AnticipationInfo ttcOa = AnticipationInfo.anticipateMovementFreeAcceleration(distance, speed, parameters,
                carFollowingModel, speedLimitInfo, TIME_STEP);

        // Loop over conflicting vehicles
        boolean first = true;
        for (PerceivedGtu conflictingVehicle : conflictingVehicles)
        {
            // skip if not on route
            if (!isOnRoute(conflict.getConflictingLink(), conflictingVehicle))
            {
                continue;
            }

            // do not stop if first conflicting vehicle is standing still
            if (first && conflictingVehicle.getSpeed().eq0() && conflictingVehicle.getKinematics().getOverlap().isAhead())
            {
                return false;
            }

            // time till enter, conflict vehicle, free acceleration
            AnticipationInfo tteCa;
            if (conflictingVehicle instanceof PerceivedGtuSimple)
            {
                // fixed acceleration for simple as it provides no behavioral information
                tteCa = AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(),
                        conflictingVehicle.getAcceleration());
            }
            else
            {
                Parameters params = conflictingVehicle.getBehavior().getParameters();
                SpeedLimitInfo sli = conflictingVehicle.getBehavior().getSpeedLimitInfo();
                CarFollowingModel cfm = conflictingVehicle.getBehavior().getCarFollowingModel();
                // Constant acceleration creates inf at stand still, triggering passing trough a congested stream
                if (conflictingVehicle.getKinematics().getOverlap().isAhead())
                {
                    tteCa = AnticipationInfo.anticipateMovementFreeAcceleration(conflictingVehicle.getDistance(),
                            conflictingVehicle.getSpeed(), params, cfm, sli, TIME_STEP);
                }
                else
                {
                    tteCa = new AnticipationInfo(Duration.ZERO, conflictingVehicle.getSpeed());
                }
            }

            // check gap
            if (conflict.isMerge())
            {
                /*
                 * At a merge the conflicting vehicle will become ego's follower. Hence time might be needed to overcome a speed
                 * difference. We assume that the speed difference at the moment the ego vehicle clears the conflict, relative
                 * to the current speed of the conflicting vehicle, must be removed by deceleration b. This will require
                 * additional time, within which we need to have moved sufficiently assuming the speed we have when we clear the
                 * conflict. Sufficient space is when the follower can perform the deceleration over that space. Then, also
                 * space for a car-following headway and the gap time is required. Note that tteCa assumes free acceleration of
                 * the conflicting vehicle until it enters the conflict. The ego vehicle however clears the conflict earlier, so
                 * the resulting speed of that acceleration is not the right speed to consider for the speed difference after
                 * the conflict. The conflicting vehicle's speed at the moment ego clear the conflict is not known. Hence, the
                 * current conflicting vehicle's speed is used. This is compensated by not assuming further acceleration of ego
                 * after clearing the conflict.
                 */
                double vSelf = ttcOa.endSpeed().si;
                double speedDiff = conflictingVehicle.getSpeed().si - vSelf;
                speedDiff = speedDiff > 0 ? speedDiff : 0;
                Duration additionalTime = Duration.ofSI(speedDiff / -b.si);
                double followerFront = conflictingVehicle.getSpeed().si * (ttcOa.duration().si + additionalTime.si)
                        - conflictingVehicle.getDistance().si + 0.5 * b.si * additionalTime.si * additionalTime.si;
                double ownRear = vSelf * additionalTime.si;
                Duration tMax = parameters.getParameter(ParameterTypes.TMAX);
                Length s0 = parameters.getParameter(S0);
                // 1) will clear the conflict after the conflict vehicle enters
                // 2) conflict vehicle will be too near after adjusting speed
                if (ttcOa.duration().times(f).plus(gap).gt(tteCa.duration()) || (!Double.isInfinite(tteCa.duration().si)
                        && tteCa.duration().si > 0.0 && ownRear < (followerFront + (tMax.si + gap.si) * vSelf + s0.si) * f))
                {
                    return true;
                }
            }
            else if (conflict.isCrossing())
            {
                // time till passible, downstream, zero acceleration
                AnticipationInfo ttpDz = null;
                if (!leaders.isEmpty())
                {
                    distance = conflict.getDistance().minus(leaders.first().getDistance()).plus(conflict.getLength())
                            .plus(passable);
                    ttpDz = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), Acceleration.ZERO);
                }
                else
                {
                    // no leader so conflict is passable within a duration of 0
                    ttpDz = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
                }
                // 1) downstream vehicle must supply sufficient space before conflict vehicle will enter
                // 2) must clear the conflict before the conflict vehicle will enter
                if (ttpDz.duration().times(f).plus(gap).gt(tteCa.duration())
                        || ttcOa.duration().times(f).plus(gap).gt(tteCa.duration()))
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
     * @param conflict conflict
     * @param leaders leaders
     * @param speed current speed
     * @param acceleration current acceleration
     * @param vehicleLength vehicle length
     * @param parameters parameters
     * @param speedLimitInfo speed limit info
     * @param carFollowingModel car-following model
     * @param bType parameter type for considered deceleration
     * @param prevEnd distance to end of previous conflict that should not be blocked, {@code null} if none
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static boolean stopForStopConflict(final PerceivedConflict conflict,
            final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders, final Speed speed, final Acceleration acceleration,
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
     * @param conflict conflict to approach
     * @param conflictPlans set of plans for conflict
     * @return whether to stop for this conflict
     */
    public static boolean stopForAllStopConflict(final PerceivedConflict conflict, final ConflictPlans conflictPlans)
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
     * @param conflictingLink conflicting link
     * @param gtu gtu
     * @return whether the conflict is on the route of the given gtu
     */
    private static boolean isOnRoute(final CrossSectionLink conflictingLink, final PerceivedGtu gtu)
    {
        try
        {
            Route route = gtu.getBehavior().getRoute();
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
     * Returns distance needed behind the leader to completely pass the conflict.
     * @param vehicleLength vehicle length
     * @param parameters parameters
     * @return distance needed behind the leader to completely pass the conflict
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
     * abandoned. Decelerations above what is considered safe deceleration may result due to numerical overshoot or other
     * factors coming into play in car-following models. Many other examples exist where a driver sticks to a certain plan.
     */
    public static final class ConflictPlans implements Blockable
    {

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

        /** Id of GTU that we allow priority (although it has not) for zip-merging at congested merge conflict. */
        private String zipGtuId;

        /**
         * Constructor.
         */
        public ConflictPlans()
        {
            //
        }

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
         * @param gtu GTU
         * @param time estimated arrival time
         */
        void setArrivalTime(final PerceivedGtuBase gtu, final Time time)
        {
            this.arrivalTimes.put(gtu.getId(), time);
        }

        /**
         * Returns the estimated arrival time of given GTU.
         * @param gtu GTU
         * @return estimated arrival time of given GTU
         */
        Time getArrivalTime(final PerceivedGtuBase gtu)
        {
            return this.arrivalTimes.get(gtu.getId());
        }

        /**
         * Sets the current phase to 'approach' for the given stop line.
         * @param stopLine stop line
         */
        void setStopPhaseApproach(final PerceivedObject stopLine)
        {
            this.stopPhases.put(stopLine.getId(), StopPhase.APPROACH);
        }

        /**
         * Sets the current phase to 'yield' for the given stop line.
         * @param stopLine stop line
         * @throws OtsRuntimeException if the phase was not set to approach before
         */
        void setStopPhaseYield(final PerceivedObject stopLine)
        {
            Throw.when(
                    !this.stopPhases.containsKey(stopLine.getId())
                            || !this.stopPhases.get(stopLine.getId()).equals(StopPhase.APPROACH),
                    OtsRuntimeException.class, "Yield stop phase is set for stop line that was not approached.");
            this.stopPhases.put(stopLine.getId(), StopPhase.YIELD);
        }

        /**
         * Sets the current phase to 'run' for the given stop line.
         * @param stopLine stop line
         * @throws OtsRuntimeException if the phase was not set to approach before
         */
        void setStopPhaseRun(final PerceivedObject stopLine)
        {
            Throw.when(!this.stopPhases.containsKey(stopLine.getId()), OtsRuntimeException.class,
                    "Run stop phase is set for stop line that was not approached.");
            this.stopPhases.put(stopLine.getId(), StopPhase.YIELD);
        }

        /**
         * Return whether plan is in approach stop line phase.
         * @param stopLine stop line
         * @return whether the current phase is 'approach' for the given stop line
         */
        boolean isStopPhaseApproach(final PerceivedObject stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId())
                    && this.stopPhases.get(stopLine.getId()).equals(StopPhase.APPROACH);
        }

        /**
         * Returns whether yielding was planned for the stop line.
         * @param stopLine stop line
         * @return whether the current phase is 'yield' for the given stop line
         */
        boolean isStopPhaseYield(final PerceivedObject stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId())
                    && this.stopPhases.get(stopLine.getId()).equals(StopPhase.YIELD);
        }

        /**
         * Returns whether running was planned for the stop line.
         * @param stopLine stop line
         * @return whether the current phase is 'run' for the given stop line
         */
        boolean isStopPhaseRun(final PerceivedObject stopLine)
        {
            return this.stopPhases.containsKey(stopLine.getId()) && this.stopPhases.get(stopLine.getId()).equals(StopPhase.RUN);
        }

        /**
         * Return indicator intent.
         * @return indicatorIntent.
         */
        public TurnIndicatorIntent getIndicatorIntent()
        {
            return this.indicatorIntent;
        }

        /**
         * Returns distance to indicator determining object.
         * @return distance to indicator determining object.
         */
        public Length getIndicatorObjectDistance()
        {
            return this.indicatorObjectDistance;
        }

        /**
         * Set indicator intent.
         * @param intent indicator intent
         * @param distance distance to object pertaining to the turn indicator intent
         */
        public void setIndicatorIntent(final TurnIndicatorIntent intent, final Length distance)
        {
            if (this.indicatorObjectDistance == null || this.indicatorObjectDistance.gt(distance))
            {
                this.indicatorIntent = intent;
                this.indicatorObjectDistance = distance;
            }
        }

        @Override
        public boolean isBlocking()
        {
            return this.blocking;
        }

        /**
         * Sets the GTU as blocking conflicts or not.
         * @param blocking whether the GTU is blocking conflicts
         */
        public void setBlocking(final boolean blocking)
        {
            this.blocking = blocking;
        }

        /**
         * Sets the id of GTU that we allow priority (although it has not) for zip-merging at congested merge conflict.
         * @param zipGtuId id of GTU that we allow priority
         */
        void setZipGtu(final String zipGtuId)
        {
            this.zipGtuId = zipGtuId;
        }

        /**
         * Clear the id of GTU that we allow priority (although it has not) for zip-merging at congested merge conflict.
         */
        void clearZipGtu()
        {
            this.zipGtuId = null;
        }

        /**
         * Check whether the given id is of the GTU for which we allow priority.
         * @param id GTU id
         * @return whether the given id is of the GTU for which we allow priority
         */
        boolean isZipGtu(final String id)
        {
            return id.equals(this.zipGtuId);
        }

        @Override
        public String toString()
        {
            return "ConflictPlans";
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

    /**
     * Returns the net available space when all moving GTUs will become stationary behind the first stationary GTU.
     */
    private static final class AvailableSpace implements PerceptionCollector<Space, LaneBasedGtu, Space>
    {
        @Override
        public Supplier<Space> getIdentity()
        {
            return () -> new Space();
        }

        @Override
        public PerceptionAccumulator<LaneBasedGtu, Space> getAccumulator()
        {
            return (i, u, h) ->
            {
                if (i.getObject().addVehicle(u, h))
                {
                    i.stop();
                }
                return i;
            };
        }

        @Override
        public Function<Space, Space> getFinalizer()
        {
            return (l) -> l;
        }
    }

    /**
     * Intermediate result for {@link AvailableSpace} collector.
     */
    private static final class Space
    {
        /** Accumulated required space for vehicles up to stationary leader. */
        private Length cumulativeRequiredSpace = Length.ZERO;

        /** Distance to first stationary leader. */
        private Length firstStationary;

        /**
         * Adds a vehicle.
         * @param gtu GTU
         * @param h distance to GTU
         * @return whether the accumulator can stop as the vehicles is stationary
         */
        public boolean addVehicle(final LaneBasedGtu gtu, final Length h)
        {
            if (gtu.getSpeed().eq0())
            {
                this.firstStationary = h;
                return true;
            }
            Length s0;
            try
            {
                s0 = gtu.getParameters().getParameter(ParameterTypes.S0);
            }
            catch (ParameterException ex)
            {
                s0 = Length.ofSI(3.0);
            }
            this.cumulativeRequiredSpace = this.cumulativeRequiredSpace.plus(gtu.getLength()).plus(s0);
            return false;
        }

        /**
         * Returns the available space up to first stationary leader.
         * @return available space up to first stationary leader
         */
        public Length availableSpace()
        {
            return this.firstStationary == null ? Length.POSITIVE_INFINITY
                    : this.firstStationary.minus(this.cumulativeRequiredSpace);
        }

        /**
         * Returns the distance to first stationary leader.
         * @return distance to first stationary leader
         */
        public Length firstStationary()
        {
            return this.firstStationary == null ? Length.POSITIVE_INFINITY : this.firstStationary;
        }
    }

}
