package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Different forms of synchronization.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Synchronization extends LmrsParameters
{

    /** Synchronization that only includes stopping for a dead-end. */
    Synchronization DEADEND = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            // stop for end
            Length remainingDist = Length.POSITIVE_INFINITY;
            Speed speed = context.getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
            Acceleration bCrit = context.getParameters().getParameter(ParameterTypes.BCRIT);
            remainingDist = remainingDist.minus(context.getParameters().getParameter(ParameterTypes.S0));
            if (remainingDist.le0())
            {
                if (speed.gt0())
                {
                    a = Acceleration.min(a, bCrit.neg());
                }
                else
                {
                    a = Acceleration.ONE; // prevent dead-lock
                }
            }
            else
            {
                Acceleration bMin = new Acceleration(.5 * speed.si * speed.si / remainingDist.si, AccelerationUnit.SI);
                if (bMin.ge(bCrit))
                {
                    a = Acceleration.min(a, bMin.neg());
                }
            }
            return a;
        }

        @Override
        public String toString()
        {
            return "DEADEND";
        }
    };

    /** Synchronization where current leaders are taken. */
    Synchronization PASSIVE = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = DEADEND.synchronize(context, desire, lat, lmrsData, initiatedLaneChange);
            if (a.lt(context.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
            {
                return a;
            }
            double dCoop = context.getParameters().getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);

            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> set =
                    context.getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane);
            PerceivedGtu leader = null;
            if (set != null)
            {
                if (desire >= dCoop && !set.isEmpty())
                {
                    leader = set.first();
                }
                else
                {
                    for (PerceivedGtu gtu : set)
                    {
                        if (gtu.getSpeed().gt0())
                        {
                            leader = gtu;
                            break;
                        }
                    }
                }
            }
            if (leader != null)
            {
                Length headway = leader.getDistance();
                Acceleration aSingle = LmrsUtil.singleAcceleration(context, headway, leader.getSpeed(), desire);
                a = Acceleration.min(a, aSingle);
                a = Synchronization.gentleUrgency(a, desire, context.getParameters());
            }
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders =
                    context.getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
            if (!leaders.isEmpty() && leaders.first().getSpeed().lt(context.getParameters().getParameter(ParameterTypes.VCONG)))
            {
                Length headway = leaders.first().getDistance();
                Acceleration aSingle = LmrsUtil.singleAcceleration(context, headway, leaders.first().getSpeed(), desire);
                aSingle = Synchronization.gentleUrgency(aSingle, desire, context.getParameters());
                a = Acceleration.min(a, aSingle);
            }

            // check merge distance
            Length xMerge = Synchronization.getMergeDistance(context.getPerception(), lat).minus(context.getLength());
            if (xMerge.gt0())
            {
                Acceleration aMerge = LmrsUtil.singleAcceleration(context, xMerge, Speed.ZERO, desire);
                a = Acceleration.max(a, aMerge);
            }
            return a;
        }

        @Override
        public String toString()
        {
            return "PASSIVE";
        }
    };

    /**
     * Synchronization by following the adjacent leader or aligning with the middle of the gap, whichever allows the largest
     * acceleration. Note that aligning with the middle of the gap then means the gap is too small, as following would cause
     * lower acceleration. Aligning with the middle of the gap will however provide a better starting point for the rest of the
     * process. Mainly, the adjacent follower can decelerate less, allowing more smooth merging.
     */
    Synchronization ALIGN_GAP = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders =
                    context.getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane);
            if (!leaders.isEmpty())
            {
                PerceivedGtu leader = leaders.first();
                Length gap = leader.getDistance();
                LmrsUtil.setDesiredHeadway(context.getParameters(), desire, true);
                PerceptionCollectable<PerceivedGtu, LaneBasedGtu> followers =
                        context.getPerception().getPerceptionCategory(NeighborsPerception.class).getFollowers(relativeLane);
                if (!followers.isEmpty())
                {
                    PerceivedGtu follower = followers.first();
                    Length netGap = leader.getDistance().plus(follower.getDistance()).times(0.5);
                    gap = Length.max(gap, leader.getDistance().minus(netGap)
                            .plus(context.getCarFollowingModel().desiredHeadway(context.getParameters(), context.getSpeed())));
                }
                a = CarFollowingUtil.followSingleLeader(context, gap, leader.getSpeed());
                LmrsUtil.resetDesiredHeadway(context.getParameters());
                // limit deceleration based on desire
                a = Synchronization.gentleUrgency(a, desire, context.getParameters());
            }
            a = Acceleration.min(a, DEADEND.synchronize(context, desire, lat, lmrsData, initiatedLaneChange));
            // never stop before we can actually merge
            Length xMerge = Synchronization.getMergeDistance(context.getPerception(), lat);
            if (xMerge.gt0())
            {
                Acceleration aMerge = LmrsUtil.singleAcceleration(context, xMerge, Speed.ZERO, desire);
                a = Acceleration.max(a, aMerge);
            }
            return a;
        }

        @Override
        public String toString()
        {
            return "ALIGN_GAP";
        }
    };

    /** Synchronization where current leaders are taken. Synchronization is disabled for d_sync&lt;d&lt;d_coop at low speeds. */
    Synchronization PASSIVE_MOVING = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            double dCoop = context.getParameters().getParameter(DCOOP);
            if (desire < dCoop && context.getSpeed().si < context.getParameters().getParameter(ParameterTypes.LOOKAHEAD).si
                    / context.getParameters().getParameter(ParameterTypes.T0).si)
            {
                return DEADEND.synchronize(context, desire, lat, lmrsData, initiatedLaneChange);
            }
            return PASSIVE.synchronize(context, desire, lat, lmrsData, initiatedLaneChange);
        }

        @Override
        public String toString()
        {
            return "PASSIVE_MOVING";
        }
    };

    /** Synchronization where a suitable leader is actively targeted, in relation to infrastructure. */
    Synchronization ACTIVE = new Synchronization()
    {
        @Override
        public Acceleration synchronize(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
                final LmrsData lmrsData, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {

            Acceleration b = context.getParameters().getParameter(ParameterTypes.B);
            Duration tMin = context.getParameters().getParameter(ParameterTypes.TMIN);
            Duration tMax = context.getParameters().getParameter(ParameterTypes.TMAX);
            Speed vCong = context.getParameters().getParameter(ParameterTypes.VCONG);
            Length x0 = context.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
            Duration t0 = context.getParameters().getParameter(ParameterTypes.T0);
            Duration lc = context.getParameters().getParameter(ParameterTypes.LCDUR);
            Speed tagSpeed = x0.divide(t0);
            double dCoop = context.getParameters().getParameter(DCOOP);
            Length dx = context.getPerception().getGtu().getFront().dx();

            // get xMergeSync, the distance within which a gap is pointless as the lane change is not possible
            InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
            SortedSet<LaneChangeInfo> info = infra.getLegalLaneChangeInfo(RelativeLane.CURRENT);
            // Length xMerge = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat).minus(dx);
            // xMerge = xMerge.lt0() ? xMerge.neg() : Length.ZERO; // zero or positive value where lane change is not possible
            Length xMerge = Synchronization.getMergeDistance(context.getPerception(), lat);
            int nCur = 0;
            Length xCur = Length.POSITIVE_INFINITY;
            for (LaneChangeInfo lcInfo : info)
            {
                int nCurTmp = lcInfo.numberOfLaneChanges();
                // subtract minimum lane change distance per lane change
                Length xCurTmp = lcInfo.remainingDistance().minus(context.getLength().times(2.0 * nCurTmp)).minus(dx);
                if (xCurTmp.lt(xCur))
                {
                    nCur = nCurTmp;
                    xCur = xCurTmp;
                }
            }

            // for short ramps, include braking distance, i.e. we -do- select a gap somewhat upstream of the merge point;
            // should we abandon this gap, we still have braking distance and minimum lane change distance left
            Length xMergeSync = xCur.minus(Length.ofSI(.5 * context.getSpeed().si * context.getSpeed().si / b.si));
            xMergeSync = Length.min(xMerge, xMergeSync);

            // abandon the gap if the sync vehicle is no longer adjacent, in congestion within xMergeSync, or too far
            NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
            RelativeLane lane = new RelativeLane(lat, 1);
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(lane);
            PerceivedGtu syncVehicle = lmrsData.getSyncVehicle(leaders);
            if (syncVehicle != null && ((syncVehicle.getSpeed().lt(vCong) && syncVehicle.getDistance().lt(xMergeSync))
                    || syncVehicle.getDistance().gt(xCur)))
            {
                syncVehicle = null;
            }

            // if there is no sync vehicle, select the first one to which current deceleration < b (it may become larger later)
            if (leaders != null && syncVehicle == null)
            {
                Length maxDistance = Length.min(x0, xCur);
                for (PerceivedGtu leader : leaders)
                {
                    if (leader.getDistance().lt(maxDistance))
                    {
                        if ((leader.getDistance().gt(xMergeSync) || leader.getSpeed().gt(vCong))
                                && Synchronization.tagAlongAcceleration(context, leader, tagSpeed, desire).gt(b.neg()))
                        {
                            syncVehicle = leader;
                            break;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }

            // select upstream vehicle if we can safely follow that, or if we cannot stay ahead of it (infrastructure, in coop)
            PerceivedGtu up;
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> followers = neighbors.getFollowers(lane);
            PerceivedGtu follower = followers == null || followers.isEmpty() ? null
                    : followers.first().moved(
                            followers.first().getDistance().plus(context.getLength()).plus(followers.first().getLength()).neg(),
                            followers.first().getSpeed(), followers.first().getAcceleration());
            boolean upOk;
            if (syncVehicle == null)
            {
                up = null;
                upOk = false;
            }
            else
            {
                up = Synchronization.getFollower(syncVehicle, leaders, follower, context.getLength());
                upOk = up == null ? false : Synchronization.tagAlongAcceleration(context, up, tagSpeed, desire).gt(b.neg());
            }
            while (syncVehicle != null
                    && up != null && (upOk || (!Synchronization.canBeAhead(context, up, xCur, nCur, tagSpeed, dCoop, b, tMin,
                            tMax, x0, t0, lc, desire) && desire > dCoop))
                    && (up.getDistance().gt(xMergeSync) || up.getSpeed().gt(vCong)))
            {
                if (up.equals(follower))
                {
                    // no suitable downstream vehicle to follow found
                    syncVehicle = null;
                    up = null;
                    break;
                }
                syncVehicle = up;
                up = Synchronization.getFollower(syncVehicle, leaders, follower, context.getLength());
                upOk = up == null ? false : Synchronization.tagAlongAcceleration(context, up, tagSpeed, desire).gt(b.neg());
            }
            lmrsData.setSyncVehicle(syncVehicle);

            // actual synchronization
            Acceleration a = DEADEND.synchronize(context, desire, lat, lmrsData, initiatedLaneChange);
            if (syncVehicle != null)
            {
                a = Synchronization.gentleUrgency(Synchronization.tagAlongAcceleration(context, syncVehicle, tagSpeed, desire),
                        desire, context.getParameters());
            }
            else if (nCur > 0 && (follower != null || (leaders != null && !leaders.isEmpty())))
            {
                // no gap to synchronize with, but there is a follower to account for
                if (follower != null && !Synchronization.canBeAhead(context, follower, xCur, nCur, tagSpeed, dCoop, b, tMin,
                        tMax, x0, t0, lc, desire))
                {
                    // get behind follower
                    double c = Synchronization.requiredBufferSpace(context.getSpeed(), nCur, x0, t0, lc, dCoop).si;
                    double t = (xCur.si - follower.getDistance().si - c) / follower.getSpeed().si;
                    double xGap = context.getSpeed().si * (tMin.si + desire * (tMax.si - tMin.si));
                    Acceleration acc = Acceleration.ofSI(2 * (xCur.si - c - context.getSpeed().si * t - xGap) / (t * t));
                    if (follower.getSpeed().eq0() || acc.si < -context.getSpeed().si / t || t < 0)
                    {
                        // inappropriate to get behind
                        // note: if minimum lane change space is more than infrastructure, deceleration will simply be limited
                        a = Synchronization.stopForEnd(context, xCur, xMerge);
                    }
                    else
                    {
                        a = Synchronization.gentleUrgency(acc, desire, context.getParameters());
                    }
                }
                else if (!LmrsUtil.acceptLaneChange(context, desire, lat, lmrsData.getGapAcceptance()))
                {
                    a = Synchronization.stopForEnd(context, xCur, xMerge);
                    // but no stronger than getting behind the leader
                    if (leaders != null && !leaders.isEmpty())
                    {
                        double c = Synchronization.requiredBufferSpace(context.getSpeed(), nCur, x0, t0, lc, dCoop).si;
                        double t = (xCur.si - leaders.first().getDistance().si - c) / leaders.first().getSpeed().si;
                        double xGap = context.getSpeed().si * (tMin.si + desire * (tMax.si - tMin.si));
                        Acceleration acc = Acceleration.ofSI(2 * (xCur.si - c - context.getSpeed().si * t - xGap) / (t * t));
                        if (!(leaders.first().getSpeed().eq0() || acc.si < -context.getSpeed().si / t || t < 0))
                        {
                            a = Acceleration.max(a, acc);
                        }
                    }
                }
            }

            // slow down to have sufficient time for further lane changes
            if (nCur > 1)
            {
                if (xMerge.gt0())
                {
                    // achieve speed to have sufficient time as soon as a lane change becomes possible (infrastructure)
                    Speed vMerge = xCur.lt(xMerge) ? Speed.ZERO
                            : xCur.minus(xMerge).divide(t0.times((1 - dCoop) * (nCur - 1)).plus(lc));
                    vMerge = Speed.max(vMerge, x0.divide(t0));
                    a = Acceleration.min(a, CarFollowingUtil.approachTargetSpeed(context, xMerge, vMerge));
                }
                else
                {
                    // slow down by b if our speed is too high beyond the merge point
                    Length c = Synchronization.requiredBufferSpace(context.getSpeed(), nCur, x0, t0, lc, dCoop);
                    if (xCur.lt(c))
                    {
                        a = Acceleration.min(a, b.neg());
                    }
                }
            }
            return a;
        }

        @Override
        public String toString()
        {
            return "ACTIVE";
        }
    };

    /**
     * Returns the distance to the next merge, stopping within this distance is futile for a lane change.
     * @param perception perception
     * @param lat lateral direction
     * @return distance to the next merge
     * @throws OperationalPlanException if there is no infrastructure perception
     */
    static Length getMergeDistance(final LanePerception perception, final LateralDirectionality lat)
            throws OperationalPlanException
    {
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        Length dx = Try.assign(() -> perception.getGtu().getFront().dx(), "Could not obtain GTU.");
        Length xMergeRef = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat);
        if (xMergeRef.gt0() && xMergeRef.lt(dx))
        {
            return Length.ZERO;
        }
        Length xMerge = xMergeRef.minus(dx);
        return xMerge.lt0() ? xMerge.neg() : Length.ZERO; // positive value where lane change is not possible
    }

    /**
     * Determine acceleration for synchronization.
     * @param context tactical information such as parameters and car-following model
     * @param desire level of lane change desire
     * @param lat lateral direction for synchronization
     * @param lmrsData LMRS data
     * @param initiatedLaneChange lateral direction of initiated lane change
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    Acceleration synchronize(TacticalContextEgo context, double desire, LateralDirectionality lat, LmrsData lmrsData,
            LateralDirectionality initiatedLaneChange) throws ParameterException, OperationalPlanException;

    /**
     * Return limited deceleration. Deceleration is limited to {@code b} for {@code d < dCoop}. Beyond {@code dCoop} the limit
     * is a linear interpolation between {@code b} and {@code bCrit}.
     * @param a acceleration to limit
     * @param desire lane change desire
     * @param params parameters
     * @return limited deceleration
     * @throws ParameterException when parameter is no available or value out of range
     */
    static Acceleration gentleUrgency(final Acceleration a, final double desire, final Parameters params)
            throws ParameterException
    {
        Acceleration b = params.getParameter(ParameterTypes.B);
        if (a.si > -b.si)
        {
            return a;
        }
        double dCoop = params.getParameter(DCOOP);
        if (desire < dCoop)
        {
            return b.neg();
        }
        Acceleration bCrit = params.getParameter(ParameterTypes.BCRIT);
        double f = (desire - dCoop) / (1.0 - dCoop);
        Acceleration lim = Acceleration.interpolate(b.neg(), bCrit.neg(), f);
        return Acceleration.max(a, lim);
    }

    /**
     * Returns the upstream gtu of the given gtu.
     * @param gtu gtu
     * @param leaders leaders of own vehicle
     * @param follower following vehicle of own vehicle
     * @param ownLength own vehicle length
     * @return upstream gtu of the given gtu
     */
    static PerceivedGtu getFollower(final PerceivedGtu gtu, final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders,
            final PerceivedGtu follower, final Length ownLength)
    {
        PerceivedGtu last = null;
        for (PerceivedGtu leader : leaders)
        {
            if (leader.equals(gtu))
            {
                return last == null ? follower : last;
            }
            last = leader;
        }
        return null;
    }

    /**
     * Calculates acceleration by following an adjacent vehicle, with tagging along if desire is not very high and speed is low.
     * @param context tactical information such as parameters and car-following model
     * @param leader leader
     * @param tagSpeed maximum tag along speed
     * @param desire lane change desire
     * @return acceleration by following an adjacent vehicle including tagging along
     * @throws ParameterException if a parameter is not present
     */
    @SuppressWarnings("checkstyle:parameternumber")
    static Acceleration tagAlongAcceleration(final TacticalContextEgo context, final PerceivedGtu leader, final Speed tagSpeed,
            final double desire) throws ParameterException
    {
        double dCoop = context.getParameters().getParameter(DCOOP);
        double tagV = context.getSpeed().lt(tagSpeed) ? 1.0 - context.getSpeed().si / tagSpeed.si : 0.0;
        double tagD = desire <= dCoop ? 1.0 : 1.0 - (desire - dCoop) / (1.0 - dCoop);
        double tagExtent = tagV < tagD ? tagV : tagD;

        /*-
         * Maximum extent is half a vehicle length, being the minimum of the own vehicle or adjacent vehicle length. At
         * standstill we get:
         *
         * car>car:    __       car>truck:       ______
         *            __                        __
         *                                                   driving direction -->
         * truck>car:      __   truck>truck:       ______
         *            ______                    ______
         */
        Length headwayAdjustment = context.getParameters().getParameter(ParameterTypes.S0)
                .plus(Length.min(context.getLength(), leader.getLength()).times(0.5)).times(tagExtent);
        Acceleration a =
                LmrsUtil.singleAcceleration(context, leader.getDistance().plus(headwayAdjustment), leader.getSpeed(), desire);
        return a;
    }

    /**
     * Returns whether a driver estimates it can be ahead of an adjacent vehicle for merging.
     * @param context tactical information such as parameters and car-following model
     * @param adjacentVehicle adjacent vehicle
     * @param xCur remaining distance
     * @param nCur number of lane changes to perform
     * @param tagSpeed maximum tag along speed
     * @param dCoop cooperation threshold
     * @param b critical deceleration
     * @param tMin minimum headway
     * @param tMax normal headway
     * @param x0 anticipation distance
     * @param t0 anticipation time
     * @param lc lane change duration
     * @param desire lane change desire
     * @return whether a driver estimates it can be ahead of an adjacent vehicle for merging
     * @throws ParameterException if parameter is not defined
     */
    static boolean canBeAhead(final TacticalContextEgo context, final PerceivedGtu adjacentVehicle, final Length xCur,
            final int nCur, final Speed tagSpeed, final double dCoop, final Acceleration b, final Duration tMin,
            final Duration tMax, final Length x0, final Duration t0, final Duration lc, final double desire)
            throws ParameterException
    {

        // always true if adjacent vehicle is behind and i) both vehicles very slow, or ii) cooperation assumed and possible
        boolean tmp = LmrsUtil.singleAcceleration(adjacentVehicle,
                adjacentVehicle.getDistance().neg().minus(adjacentVehicle.getLength()).minus(context.getLength()),
                context.getSpeed(), desire).gt(b.neg());
        if (adjacentVehicle.getDistance().lt(context.getLength().neg())
                && ((desire > dCoop && tmp) || (context.getSpeed().lt(tagSpeed) && adjacentVehicle.getSpeed().lt(tagSpeed))))
        {
            return true;
        }
        /*-
         * Check that we cover distance (xCur - c) before adjacent vehicle will no longer leave a space of xGap.
         * _______________________________________________________________________________
         *                 ___b           ___b (at +t)
         * _____________ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _______
         *       _____x                              _____x (at +t)                /
         * _______________________________________________________________________/
         *            (---------------------------xCur---------------------------)
         *            (-s-)(l)               (-xGap-)(-l-)(----------c-----------)
         *
         *            (----------------------------------) x should cover this distance before
         *                    (-------------) b covers this distance; then we can be ahead (otherwise, follow b)
         */
        Length c = Synchronization.requiredBufferSpace(context.getSpeed(), nCur, x0, t0, lc, dCoop);
        double t = (xCur.si - c.si) / context.getSpeed().si;
        double xGap = adjacentVehicle.getSpeed().si * (tMin.si + desire * (tMax.si - tMin.si));
        return 0.0 < t && t < (xCur.si - adjacentVehicle.getDistance().si - context.getLength().si
                - adjacentVehicle.getLength().si - c.si - xGap) / adjacentVehicle.getSpeed().si;
    }

    /**
     * Returns the required buffer space to perform a lane change and further lane changes.
     * @param speed representative speed
     * @param nCur number of required lane changes
     * @param x0 anticipation distance
     * @param t0 anticipation time
     * @param lc lane change duration
     * @param dCoop cooperation threshold
     * @return required buffer space to perform a lane change and further lane changes
     */
    static Length requiredBufferSpace(final Speed speed, final int nCur, final Length x0, final Duration t0, final Duration lc,
            final double dCoop)
    {
        Length xCrit = speed.times(t0);
        xCrit = Length.max(xCrit, x0);
        return speed.times(lc).plus(xCrit.times((nCur - 1.0) * (1.0 - dCoop)));
    }

    /**
     * Calculates acceleration to stop for a split or dead-end, accounting for infrastructure.
     * @param context tactical information such as parameters and car-following model
     * @param xCur remaining distance to end
     * @param xMerge distance until merge point
     * @return acceleration to stop for a split or dead-end, accounting for infrastructure
     * @throws ParameterException if parameter is not defined
     */
    static Acceleration stopForEnd(final TacticalContextEgo context, final Length xCur, final Length xMerge)
            throws ParameterException
    {
        if (xCur.lt0())
        {
            // missed our final lane change spot, but space remains
            return Acceleration.max(context.getParameters().getParameter(ParameterTypes.BCRIT).neg(),
                    CarFollowingUtil.stop(context, xMerge));
        }
        LmrsUtil.setDesiredHeadway(context.getParameters(), 1.0, true);
        Acceleration a = CarFollowingUtil.stop(context, xCur);
        if (a.lt0())
        {
            // decelerate even more if still comfortable, leaving space for acceleration later
            a = Acceleration.min(a, context.getParameters().getParameter(ParameterTypes.B).neg());
            // but never decelerate such that stand-still is reached within xMerge
            if (xMerge.gt0())
            {
                a = Acceleration.max(a, CarFollowingUtil.stop(context, xMerge));
            }
        }
        else
        {
            a = Acceleration.POSITIVE_INFINITY;
        }
        LmrsUtil.resetDesiredHeadway(context.getParameters());
        return a;
    }

    /**
     * Returns the leader of one gtu from a set.
     * @param gtu gtu
     * @param leaders leaders
     * @return leader of one gtu from a set
     */
    static PerceivedGtu getTargetLeader(final PerceivedGtu gtu, final SortedSet<PerceivedGtu> leaders)
    {
        for (PerceivedGtu leader : leaders)
        {
            if (leader.getDistance().gt(gtu.getDistance()))
            {
                return leader;
            }
        }
        return null;
    }

}
