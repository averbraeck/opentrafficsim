package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;

import nl.tudelft.simulation.language.Throw;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayStopLine;
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
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 3, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
    public static final ParameterTypeDuration MIN_GAP = new ParameterTypeDuration("minGap", "Minimum gap for conflicts.",
            new Duration(0.000001, DurationUnit.SECOND), ConstraintInterface.POSITIVE);

    /** Stopping distance at conflicts. */
    public static final ParameterTypeLength S0_CONF = new ParameterTypeLength("s0 conf", "Stopping distance at conflicts.",
            new Length(1.5, LengthUnit.METER), ConstraintInterface.POSITIVE);

    /** Multiplication factor on time for conservative assessment. */
    public static final ParameterTypeDouble TIME_FACTOR = new ParameterTypeDouble("timeFactor",
            "Safety factor on estimated time.", 1.25, ConstraintInterface.ATLEASTONE);

    /** Area before stop line where one is considered arrived at the intersection. */
    public static final ParameterTypeLength STOP_AREA = new ParameterTypeLength("stopArea",
            "Area before stop line where one is considered arrived at the intersection.", new Length(4, LengthUnit.METER),
            ConstraintInterface.POSITIVE);

    /** Parameter of how much time before departure a bus indicates its departure to get priority. */
    public static final ParameterTypeDuration TI = new ParameterTypeDuration("ti", "Indicator time before departure.",
            Duration.createSI(3.0), ConstraintInterface.POSITIVE);

    /** Time step for free acceleration anticipation. */
    private static final Duration TIME_STEP = new Duration(0.5, DurationUnit.SI);

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
     * @param parameters parameters
     * @param conflicts set of conflicts to approach
     * @param leaders leading vehicles
     * @param carFollowingModel car-following model
     * @param vehicleLength length of vehicle
     * @param speed current speed
     * @param acceleration current acceleration
     * @param speedLimitInfo speed limit info
     * @param conflictPlans set of plans for conflict
     * @param gtu gtu
     * @return acceleration appropriate for approaching the conflicts
     * @throws GTUException in case of an unsupported conflict rule
     * @throws ParameterException if a parameter is not defined or out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Acceleration approachConflicts(final Parameters parameters, final SortedSet<HeadwayConflict> conflicts,
            final SortedSet<HeadwayGTU> leaders, final CarFollowingModel carFollowingModel, final Length vehicleLength,
            final Speed speed, final Acceleration acceleration, final SpeedLimitInfo speedLimitInfo,
            final ConflictPlans conflictPlans, final LaneBasedGTU gtu) throws GTUException, ParameterException
    {

        conflictPlans.cleanPlans();

        Acceleration a = Acceleration.POS_MAXVALUE;
        if (conflicts.isEmpty())
        {
            return a;
        }

        List<Length> prevStarts = new ArrayList<>();
        List<Length> prevEnds = new ArrayList<>();
        List<Class<? extends ConflictRule>> conflictRuleTypes = new ArrayList<>();

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
                // follow leading GTUs on merge or split
                a =
                        Acceleration.min(
                                a,
                                followConflictingLeaderOnMergeOrSplit(conflict, parameters, carFollowingModel, speed,
                                        speedLimitInfo));
            }
            if (conflict.getDistance().lt0())
            {
                // ignore conflicts we are on (i.e. negative distance to start of conflict)
                continue;
            }

            // indicator if bus
            if (gtu.getStrategicalPlanner().getRoute() instanceof BusSchedule && gtu.getGTUType().isOfType(GTUType.BUS)
                    && conflict.getConflictRuleType().equals(BusStopConflictRule.class))
            {
                BusSchedule busSchedule = (BusSchedule) gtu.getStrategicalPlanner().getRoute();
                Time actualDeparture = busSchedule.getActualDepartureConflict(conflict.getId());
                if (actualDeparture != null
                        && actualDeparture.si < gtu.getSimulator().getSimulatorTime().getTime().si
                                + parameters.getParameter(TI).si)
                {
                    // TODO depending on left/right-hand traffic
                    conflictPlans.setIndicatorIntent(TurnIndicatorIntent.LEFT, conflict.getDistance());
                }
            }

            // determine if we need to stop
            boolean stop;
            switch (conflict.getConflictPriority())
            {
                case PRIORITY:
                {
                    stop = stopForPriorityConflict(conflict, leaders, speed, vehicleLength, parameters, conflictPlans);
                    break;
                }
                case GIVE_WAY:
                {
                    stop =
                            stopForGiveWayConflict(conflict, leaders, speed, acceleration, vehicleLength, parameters,
                                    speedLimitInfo, carFollowingModel);
                    break;
                }
                case STOP:
                {
                    stop =
                            stopForStopConflict(conflict, leaders, speed, acceleration, vehicleLength, parameters,
                                    speedLimitInfo, carFollowingModel);
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

                    // TODO
                    // if this lowers our acceleration, we need to check if we are able to pass upstream conflicts still in time

                    // stop for j'th conflict, if deceleration is too strong, for next one
                    parameters.setParameter(ParameterTypes.S0, parameters.getParameter(S0_CONF));
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
                            Acceleration aStop =
                                    CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo,
                                            prevStarts.get(j));
                            if (conflictRuleTypes.get(j).equals(BusStopConflictRule.class)
                                    && aStop.lt(parameters.getParameter(ParameterTypes.BCRIT).neg()))
                            {
                                aStop = Acceleration.POS_MAXVALUE;
                            }
                            aCF = Acceleration.max(aCF, aStop);
                        }
                        j++;
                    }
                    parameters.resetParameter(ParameterTypes.S0);
                    a = Acceleration.min(a, aCF);
                    break;
                }

                // keep conflict clear (when stopping for another conflict), if there are conflicting vehicles
                if (!conflict.getUpstreamConflictingGTUs().isEmpty())
                {
                    prevStarts.add(conflict.getDistance());
                    conflictRuleTypes.add(conflict.getConflictRuleType());
                    prevEnds.add(conflict.getDistance().plus(conflict.getLength()));
                }
            }

        }

        if (a.si < -6.0)
        {
            System.err.println("Deceleration from conflict util stronger than 6m/s^2.");
            // return IGNORE;
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
     * @return acceleration for following conflicting vehicles <i>on</i> a merge or split conflict
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    private static Acceleration followConflictingLeaderOnMergeOrSplit(final HeadwayConflict conflict,
            final Parameters parameters, final CarFollowingModel carFollowingModel, final Speed speed,
            final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        // ignore if no conflicting GTU's
        if (conflict.getDownstreamConflictingGTUs().isEmpty())
        {
            return Acceleration.POS_MAXVALUE;
        }
        // get the most upstream GTU to consider
        HeadwayGTU c = conflict.getDownstreamConflictingGTUs().first();
        if (c.isAhead())
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
        Length virtualHeadway = conflict.getDistance().plus(c.getOverlapRear());
        if (virtualHeadway.le0() && conflict.getDistance().le0())
        {
            // TODO what about long conflicts where we need to follow the second conflicting downstream vehicle?
            // conflict GTU downstream of start of conflict, but upstream of us
            return Acceleration.POS_MAXVALUE;
        }
        // follow leader
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(virtualHeadway, c.getSpeed());
        Acceleration a = carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, leaders);
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
            parameters.setParameter(ParameterTypes.S0, parameters.getParameter(S0_CONF));
            Acceleration aStop =
                    CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo, conflict.getDistance());
            parameters.resetParameter(ParameterTypes.S0);
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
    private static Acceleration avoidCrossingCollision(final Parameters parameters, final HeadwayConflict conflict,
            final CarFollowingModel carFollowingModel, final Speed speed, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException
    {

        // TODO only within visibility
        List<HeadwayGTU> conflictingGTUs = new ArrayList<>();
        for (HeadwayGTU gtu : conflict.getUpstreamConflictingGTUs())
        {
            if (isOnRoute(conflict.getConflictingLink(), gtu))
            {
                // first upstream vehicle on route to this conflict
                conflictingGTUs.add(gtu);
                break;
            }
        }
        for (HeadwayGTU gtu : conflict.getDownstreamConflictingGTUs())
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
        for (HeadwayGTU conflictingGTU : conflictingGTUs)
        {
            AnticipationInfo tteC;
            Length distance;
            if (conflictingGTU.isParallel())
            {
                tteC = new AnticipationInfo(Duration.ZERO, conflictingGTU.getSpeed());
                distance =
                        conflictingGTU.getOverlapRear().abs().plus(conflictingGTU.getOverlap())
                                .plus(conflictingGTU.getOverlapFront().abs());
            }
            else
            {
                tteC =
                        AnticipationInfo.anticipateMovement(conflictingGTU.getDistance(), conflictingGTU.getSpeed(),
                                Acceleration.ZERO);
                distance = conflictingGTU.getDistance().plus(conflict.getLength()).plus(conflictingGTU.getLength());
            }
            AnticipationInfo ttcC = AnticipationInfo.anticipateMovement(distance, conflictingGTU.getSpeed(), Acceleration.ZERO);
            AnticipationInfo tteO =
                    AnticipationInfo.anticipateMovementFreeAcceleration(conflict.getDistance(), speed, parameters,
                            carFollowingModel, speedLimitInfo, TIME_STEP);
            // enter before cleared
            // TODO safety factor?
            if (tteC.getDuration().lt(tteO.getDuration()) && tteO.getDuration().lt(ttcC.getDuration()))
            {
                if (!conflictingGTU.getSpeed().eq0())
                {
                    // solve parabolic speed profile s = v*t + .5*a*t*t, a =
                    double acc =
                            2 * (conflict.getDistance().si - speed.si * ttcC.getDuration().si)
                                    / (ttcC.getDuration().si * ttcC.getDuration().si);
                    // time till zero speed > time to avoid conflict?
                    if (speed.si / -acc > ttcC.getDuration().si)
                    {
                        a = Acceleration.min(a, new Acceleration(acc, AccelerationUnit.SI));
                    }
                    else
                    {
                        // will reach zero speed ourselves
                        a =
                                Acceleration.min(
                                        a,
                                        CarFollowingUtil.stop(carFollowingModel, parameters, speed, speedLimitInfo,
                                                conflict.getDistance()));
                    }
                }
                // conflicting vehicle stand-still, ignore even at conflict
            }
        }
        return a;
    }

    /**
     * Approach a priority conflict. Stopping is applied to give way to conflicting traffic in case congestion is present on the
     * own lane. This is courtesy yielding.
     * @param conflict conflict to approach
     * @param leaders leading vehicles in own lane
     * @param speed current speed
     * @param vehicleLength vehicle length
     * @param parameters parameters
     * @param yieldPlans set of plans for yielding with priority
     * @return whether to stop for this conflict
     * @throws ParameterException if parameter B is not defined
     */
    public static boolean stopForPriorityConflict(final HeadwayConflict conflict, final SortedSet<HeadwayGTU> leaders,
            final Speed speed, final Length vehicleLength, final Parameters parameters, final ConflictPlans yieldPlans)
            throws ParameterException
    {

        if (leaders.isEmpty() || conflict.getUpstreamConflictingGTUs().isEmpty())
        {
            // no leader, or no conflicting vehicle
            return false;
        }

        // Stop as long as some leader is standing still, and leader is not leaving sufficient space yet
        // use start of conflict on merge, end of conflict on crossing
        Length typeCorrection = conflict.isCrossing() ? conflict.getLength() : Length.ZERO;
        // distance leader has to cover before we can pass the conflict
        Length distance =
                conflict.getDistance().minus(leaders.first().getDistance()).plus(passableDistance(vehicleLength, parameters))
                        .plus(typeCorrection);
        if (distance.gt0())
        {
            Length required = conflict.getDistance().plus(typeCorrection).plus(passableDistance(vehicleLength, parameters)); // for
                                                                                                                             // ourselves
            for (HeadwayGTU leader : leaders)
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
        return false;

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
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static boolean stopForGiveWayConflict(final HeadwayConflict conflict, final SortedSet<HeadwayGTU> leaders,
            final Speed speed, final Acceleration acceleration, final Length vehicleLength, final Parameters parameters,
            final SpeedLimitInfo speedLimitInfo, final CarFollowingModel carFollowingModel) throws ParameterException
    {

        // TODO conflicting vehicle on crossing conflict, but will leave sooner then we enter, so no problem?
        // TODO more generally, also upstream conflicting vehicles at crossings may leave the conflict before we enter
        if (conflict.getConflictType().equals(ConflictType.CROSSING) && !conflict.getDownstreamConflictingGTUs().isEmpty()
                && conflict.getDownstreamConflictingGTUs().first().isParallel())
        {
            // vehicle on the conflict
            return true;
        }

        // Get data independent of conflicting vehicle
        // parameters
        Acceleration b = parameters.getParameter(ParameterTypes.B).neg();
        double f = parameters.getParameter(TIME_FACTOR);
        Duration gap = parameters.getParameter(MIN_GAP);
        // time till conflict is cleared
        Length distance = conflict.getDistance().plus(vehicleLength);
        if (conflict.isCrossing())
        {
            // merge is cleared at start, crossing at end
            distance = distance.plus(conflict.getLength());
        }
        // based on acceleration, limited by free acceleration
        AnticipationInfo ttcOa =
                AnticipationInfo.anticipateMovementFreeAcceleration(distance, speed, parameters, carFollowingModel,
                        speedLimitInfo, TIME_STEP);
        // time till downstream vehicle will make the conflict passible, under constant speed or safe deceleration
        AnticipationInfo ttpDz = null;
        AnticipationInfo ttpDs = null;
        if (conflict.isCrossing())
        {
            if (!leaders.isEmpty())
            {
                distance =
                        conflict.getDistance().minus(leaders.first().getDistance()).plus(conflict.getLength())
                                .plus(passableDistance(vehicleLength, parameters));
                ttpDz = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), Acceleration.ZERO);
                ttpDs = AnticipationInfo.anticipateMovement(distance, leaders.first().getSpeed(), b);
            }
            else
            {
                // no leader so conflict is passible within a duration of 0
                ttpDz = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
                ttpDs = new AnticipationInfo(Duration.ZERO, Speed.ZERO);
            }
        }

        List<HeadwayGTU> conflictingVehicles = new ArrayList<>();
        if (!conflict.getUpstreamConflictingGTUs().isEmpty())
        {
            for (HeadwayGTU vehicle : conflict.getUpstreamConflictingGTUs())
            {
                if (conflict.getConflictingTrafficLightDistance() != null && !conflict.isPermitted()
                        && vehicle.getDistance().gt(conflict.getConflictingTrafficLightDistance()))
                {
                    break;
                }
                if (isOnRoute(conflict.getConflictingLink(), vehicle))
                {
                    conflictingVehicles.add(vehicle);
                }
            }
        }
        else if (conflict.getConflictingTrafficLightDistance() == null)
        {
            // none within visibility, assume a conflicting vehicle just outside of visibility driving at speed limit
            try
            {
                HeadwayGTUSimple conflictGtu =
                        new HeadwayGTUSimple("virtual " + UUID.randomUUID().toString(), GTUType.CAR,
                                conflict.getConflictingVisibility(), new Length(4.0, LengthUnit.SI),
                                conflict.getConflictingSpeedLimit(), Acceleration.ZERO);
                conflictingVehicles.add(conflictGtu);
            }
            catch (GTUException exception)
            {
                throw new RuntimeException("Could not create a virtual conflicting vehicle at visibility range.", exception);
            }
        }

        // Do not stop if conflicting vehicle is standing still
        if (conflictingVehicles.isEmpty() || conflictingVehicles.get(0).getSpeed().eq0())
        {
            return false;
        }

        // Loop over conflicting vehicles
        for (HeadwayGTU conflictingVehicle : conflictingVehicles)
        {

            // time till conflict vehicle will enter, under free acceleration and safe deceleration
            AnticipationInfo tteCa;
            if (conflictingVehicle instanceof HeadwayGTUSimple)
            {
                tteCa =
                        AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(),
                                conflictingVehicle.getAcceleration());
            }
            else
            {
                Parameters params = conflictingVehicle.getParameters();
                SpeedLimitInfo sli = conflictingVehicle.getSpeedLimitInfo();
                CarFollowingModel cfm = conflictingVehicle.getCarFollowingModel();
                // Constant acceleration creates inf at stand still, triggering passing trough a congested stream
                tteCa =
                        AnticipationInfo.anticipateMovementFreeAcceleration(conflictingVehicle.getDistance(),
                                conflictingVehicle.getSpeed(), params, cfm, sli, TIME_STEP);
            }
            AnticipationInfo tteCs =
                    AnticipationInfo.anticipateMovement(conflictingVehicle.getDistance(), conflictingVehicle.getSpeed(), b);

            // check gap
            if (conflict.isMerge())
            {

                // Merge, will be each others followers, add time to overcome speed difference
                double vSelf = ttcOa.getEndSpeed().si;
                double speedDiff = conflictingVehicle.getSpeed().si - vSelf;
                speedDiff = speedDiff > 0 ? speedDiff : 0;
                Duration additionalTime = new Duration(speedDiff / -b.si, DurationUnit.SI);
                // check if conflict vehicle will be upstream after that time, position beyond conflict after additional time
                double followerFront =
                        conflictingVehicle.getSpeed().si
                                * ttcOa.getDuration().si
                                - conflictingVehicle.getDistance().si
                                + (conflictingVehicle.getSpeed().si * additionalTime.si + 0.5 * b.si * additionalTime.si
                                        * additionalTime.si);
                double ownRear = vSelf * additionalTime.si; // constant speed after clearing
                Duration tMax = parameters.getParameter(ParameterTypes.TMAX);
                Length s0 = parameters.getParameter(ParameterTypes.S0);
                // 1) will clear the conflict after the conflict vehicle enters
                // 2) not sufficient time to overcome speed difference
                // 3) conflict vehicle will be too near after adjusting speed
                if (ttcOa.getDuration().multiplyBy(f).plus(gap).gt(tteCa.getDuration())
                        || ttcOa.getDuration().plus(additionalTime).multiplyBy(f).plus(gap).gt(tteCs.getDuration())
                        || ownRear < (followerFront + (tMax.si + gap.si) * vSelf + s0.si) * f)
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
                if (ttpDz.getDuration().multiplyBy(f).plus(gap).gt(tteCa.getDuration())
                        || ttcOa.getDuration().multiplyBy(f).plus(gap).gt(tteCa.getDuration())
                        || ttpDs.getDuration().multiplyBy(f).plus(gap).gt(tteCs.getDuration()))
                {
                    return true;
                }

            }
            else
            {
                throw new RuntimeException("Conflict is of unknown type " + conflict.getConflictType()
                        + ", which is not merge nor a crossing.");
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
     * @param vehicleLength vehicle length
     * @param parameters parameters
     * @param speedLimitInfo speed limit info
     * @param carFollowingModel car-following model
     * @return whether to stop for this conflict
     * @throws ParameterException if a parameter is not defined
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static boolean stopForStopConflict(final HeadwayConflict conflict, final SortedSet<HeadwayGTU> leaders,
            final Speed speed, final Acceleration acceleration, final Length vehicleLength, final Parameters parameters,
            final SpeedLimitInfo speedLimitInfo, final CarFollowingModel carFollowingModel) throws ParameterException
    {
        return stopForGiveWayConflict(conflict, leaders, speed, acceleration, vehicleLength, parameters, speedLimitInfo,
                carFollowingModel);
    }

    /**
     * Approach an all-stop conflict.
     * @param conflict conflict to approach
     * @param conflictPlans set of plans for conflict
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
     * @param gtu HeadwayGTU; gtu
     * @return whether the conflict is on the route of the given gtu
     */
    private static boolean isOnRoute(final CrossSectionLink conflictingLink, final HeadwayGTU gtu)
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
        catch (@SuppressWarnings("unused") UnsupportedOperationException uoe)
        {
            // conservative assumption: it's on the route (gtu should be upstream of the conflict)
            return true;
        }
    }

    /**
     * Returns a speed dependent distance needed behind the leader to completely pass the conflict.
     * @param vehicleLength vehicle length
     * @param parameters parameters
     * @return speed dependent distance needed behind the leader to completely pass the conflict
     * @throws ParameterException if parameter is not available
     */
    private static Length passableDistance(final Length vehicleLength, final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(ParameterTypes.S0).plus(vehicleLength);
    }

    /**
     * Holds the tactical plans of a driver considering conflicts. These are remembered for consistency. For instance, if the
     * decision is made to yield as current deceleration suggests it's safe to do so, but the trajectory for stopping in front
     * of the conflict results in deceleration slightly above what is considered safe deceleration, the plan should not be
     * abandoned. Decelerations above what is considered safe deceleration may result due to numerical overshoot or other factor
     * coming into play in car-following models. Many other examples exist where a driver sticks to a certain plan.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

        /** Phases of navigating an all-stop intersection per intersection. */
        private final HashMap<String, StopPhase> stopPhases = new HashMap<>();

        /** Estimated arrival times of vehicles at all-stop intersection. */
        private final HashMap<String, Time> arrivalTimes = new HashMap<>();

        /** Indicator intent. */
        private TurnIndicatorIntent indicatorIntent = TurnIndicatorIntent.NONE;

        /** Distance to object causing turn indicator intent. */
        private Length indicatorObjectDistance = null;

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
                            || !this.stopPhases.get(stopLine.getId()).equals(StopPhase.APPROACH), RuntimeException.class,
                    "Yield stop phase is set for stop line that was not approached.");
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

    }

    /**
     * Phases of navigating an all-stop intersection.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 30, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
