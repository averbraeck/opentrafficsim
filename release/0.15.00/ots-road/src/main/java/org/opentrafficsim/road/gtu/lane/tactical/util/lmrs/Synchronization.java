package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.Set;
import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Different forms of synchronization, which includes cooperation.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum Synchronization implements LmrsParameters
{

    /** Synchronization that only includes stopping for a dead-end. */
    NONE
    {

        @Override
        Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData)
                throws ParameterException, OperationalPlanException
        {

            Acceleration a = Acceleration.POSITIVE_INFINITY;
            // stop for end
            Length remainingDist = null;
            for (InfrastructureLaneChangeInfo ili : perception.getPerceptionCategory(InfrastructurePerception.class)
                    .getInfrastructureLaneChangeInfo(RelativeLane.CURRENT))
            {
                if (remainingDist == null || remainingDist.gt(ili.getRemainingDistance()))
                {
                    remainingDist = ili.getRemainingDistance();
                }
            }
            if (remainingDist != null)
            {
                Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
                Acceleration bCrit = bc.getParameter(ParameterTypes.BCRIT);
                try
                {
                    remainingDist = remainingDist.minus(bc.getParameter(ParameterTypes.S0))
                            .minus(perception.getGtu().getFront().getDx());
                }
                catch (GTUException exception)
                {
                    throw new OperationalPlanException("Could not obtain GTU from perception.", exception);
                }
                // TODO replace this hack with something that properly accounts for overshoot
                remainingDist = remainingDist.minus(Length.createSI(10));
                if (remainingDist.le0())
                {
                    if (speed.gt0())
                    {
                        a = Acceleration.min(a, bCrit.neg());
                    }
                    else
                    {
                        a = Acceleration.min(a, Acceleration.ZERO);
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
            }

            return a;
        }

        @Override
        Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {
            return Acceleration.POSITIVE_INFINITY;
        }

    },

    /** Synchronization that only includes stopping for a dead-end and simple cooperation. */
    COOPERATION
    {

        @Override
        Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData)
                throws ParameterException, OperationalPlanException
        {
            // stop for dead-end
            return NONE.synchronize(perception, bc, sli, cfm, desire, lat, lmrsData);
        }

        @Override
        Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {
            if ((lat.isLeft() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
                    || (lat.isRight() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT)))
            {
                return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            }
            Acceleration b = bc.getParameter(ParameterTypes.B);
            Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            double dCoop = bc.getParameter(DCOOP);
            Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            for (HeadwayGTU leader : removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane), perception,
                    relativeLane), perception, RelativeLane.CURRENT))
            {
                BehavioralCharacteristics bc2 = leader.getBehavioralCharacteristics();
                double desire = lat.equals(LateralDirectionality.LEFT) && bc2.contains(DRIGHT) ? bc2.getParameter(DRIGHT)
                        : lat.equals(LateralDirectionality.RIGHT) && bc2.contains(DLEFT) ? bc2.getParameter(DLEFT) : 0;
                if (desire >= dCoop && (leader.getSpeed().gt0() || leader.getDistance().gt0()))
                {
                    Acceleration aSingle = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(),
                            desire, bc, sli, cfm);
                    a = Acceleration.min(a, aSingle);
                }
            }

            return Acceleration.max(a, b.neg());
        }

    },

    /** Synchronization where current leaders are taken. */
    PASSIVE
    {

        @Override
        Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            double dCoop = bc.getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            SortedSet<
                    HeadwayGTU> set =
                            removeAllUpstreamOfConflicts(
                                    removeAllUpstreamOfConflicts(perception.getPerceptionCategory(NeighborsPerception.class)
                                            .getLeaders(relativeLane), perception, relativeLane),
                                    perception, RelativeLane.CURRENT);
            HeadwayGTU leader = null;
            if (set != null)
            {
                if (desire >= dCoop && !set.isEmpty())
                {
                    leader = set.first();
                }
                else
                {
                    for (HeadwayGTU gtu : set)
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
                Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
                Acceleration aSingle =
                        LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                a = Acceleration.min(a, aSingle);
            }
            a = gentleUrgency(a, desire, bc);

            // dead end
            a = Acceleration.min(a, NONE.synchronize(perception, bc, sli, cfm, desire, lat, lmrsData));

            return a;

        }

        @Override
        Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {
            return COOPERATION.cooperate(perception, bc, sli, cfm, lat, ownDesire);
        }

    },

    /** Synchronization where a suitable leader is actively targeted, in relation to infrastructure. */
    ACTIVE
    {

        @Override
        Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData)
                throws ParameterException, OperationalPlanException
        {

            Acceleration b = bc.getParameter(ParameterTypes.B);
            Duration tMin = bc.getParameter(ParameterTypes.TMIN);
            Duration tMax = bc.getParameter(ParameterTypes.TMAX);
            Speed vCong = bc.getParameter(ParameterTypes.VCONG);
            Length x0 = bc.getParameter(ParameterTypes.LOOKAHEAD);
            Duration t0 = bc.getParameter(ParameterTypes.T0);
            Duration lc = bc.getParameter(ParameterTypes.LCDUR);
            Speed tagSpeed = x0.divideBy(t0);
            double dCoop = bc.getParameter(DCOOP);
            Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
            Length ownLength = perception.getPerceptionCategory(EgoPerception.class).getLength();
            Length dx;
            try
            {
                dx = perception.getGtu().getFront().getDx();
            }
            catch (GTUException exception)
            {
                throw new OperationalPlanException(exception);
            }

            // get xMergeSync, the distance within which a gap is pointless as the lane change is not possible
            InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
            SortedSet<InfrastructureLaneChangeInfo> info = infra.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT);
            Length xMerge = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat).minus(dx);
            xMerge = xMerge.lt0() ? xMerge.neg() : Length.ZERO; // zero, or positive value where lane change is not possible
            int nCur = 0;
            Length xCur = Length.POSITIVE_INFINITY;
            for (InfrastructureLaneChangeInfo lcInfo : info)
            {
                int nCurTmp = lcInfo.getRequiredNumberOfLaneChanges();
                // subtract minimum lane change distance per lane change
                Length xCurTmp = lcInfo.getRemainingDistance().minus(ownLength.multiplyBy(2.0 * nCurTmp)).minus(dx);
                if (xCurTmp.lt(xCur))
                {
                    nCur = nCurTmp;
                    xCur = xCurTmp;
                }
            }

            // for short ramps, include braking distance, i.e. we -do- select a gap somewhat upstream of the merge point;
            // should we abandon this gap, we still have braking distance and minimum lane change distance left
            Length xMergeSync = xCur.minus(Length.createSI(.5 * ownSpeed.si * ownSpeed.si / b.si));
            xMergeSync = Length.min(xMerge, xMergeSync);

            // abandon the gap if the sync vehicle is no longer adjacent, in congestion within xMergeSync, or too far
            NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
            RelativeLane lane = new RelativeLane(lat, 1);
            SortedSet<HeadwayGTU> leaders =
                    removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(neighbors.getLeaders(lane), perception, lane),
                            perception, RelativeLane.CURRENT);
            HeadwayGTU syncVehicle = lmrsData.getSyncVehicle(leaders);
            if (syncVehicle != null && ((syncVehicle.getSpeed().lt(vCong) && syncVehicle.getDistance().lt(xMergeSync))
                    || syncVehicle.getDistance().gt(xCur)))
            {
                syncVehicle = null;
            }

            // if there is no sync vehicle, select the first one to which current deceleration < b (it may become larger later)
            if (leaders != null && syncVehicle == null)
            {
                Length maxDistance = Length.min(x0, xCur);
                for (HeadwayGTU leader : leaders)
                {
                    if (leader.getDistance().lt(maxDistance))
                    {
                        if ((leader.getDistance().gt(xMergeSync) || leader.getSpeed().gt(vCong))
                                && tagAlongAcceleration(leader, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm)
                                        .gt(b.neg()))
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
            HeadwayGTU up;
            SortedSet<HeadwayGTU> followers =
                    removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(neighbors.getFollowers(lane), perception, lane),
                            perception, RelativeLane.CURRENT);
            HeadwayGTU follower = followers == null || followers.isEmpty() ? null
                    : followers.first().moved(
                            followers.first().getDistance().plus(ownLength).plus(followers.first().getLength()).neg(),
                            followers.first().getSpeed(), followers.first().getAcceleration());
            boolean upOk;
            if (syncVehicle == null)
            {
                up = null;
                upOk = false;
            }
            else
            {
                up = getFollower(syncVehicle, leaders, follower, ownLength);
                upOk = up == null ? false
                        : tagAlongAcceleration(up, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm).gt(b.neg());
            }
            while (syncVehicle != null
                    && up != null && (upOk || (!canBeAhead(up, xCur, nCur, ownSpeed, ownLength, tagSpeed, dCoop, b, tMin, tMax,
                            x0, t0, lc, desire) && desire > dCoop))
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
                up = getFollower(syncVehicle, leaders, follower, ownLength);
                upOk = up == null ? false
                        : tagAlongAcceleration(up, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm).gt(b.neg());
            }
            lmrsData.setSyncVehicle(syncVehicle);

            // actual synchronization
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            if (syncVehicle != null)
            {
                a = gentleUrgency(tagAlongAcceleration(syncVehicle, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm),
                        desire, bc);
            }
            else if (nCur > 0 && (follower != null || (leaders != null && !leaders.isEmpty())))
            {
                // no gap to synchronize with, but there is a follower to account for
                if (follower != null && !canBeAhead(follower, xCur, nCur, ownSpeed, ownLength, tagSpeed, dCoop, b, tMin, tMax,
                        x0, t0, lc, desire))
                {
                    // get behind follower
                    double c = requiredBufferSpace(ownSpeed, nCur, x0, t0, lc, dCoop).si;
                    double t = (xCur.si - follower.getDistance().si - c) / follower.getSpeed().si;
                    double xGap = ownSpeed.si * (tMin.si + desire * (tMax.si - tMin.si));
                    Acceleration acc = Acceleration.createSI(2 * (xCur.si - c - ownSpeed.si * t - xGap) / (t * t));
                    if (follower.getSpeed().eq0() || acc.si < -ownSpeed.si / t || t < 0)
                    {
                        // inappropriate to get behind
                        // note: if minimum lane change space is more than infrastructure, deceleration will simply be limited
                        a = stopForEnd(xCur, xMerge, bc, ownSpeed, cfm, sli);
                    }
                    else
                    {
                        a = gentleUrgency(acc, desire, bc);
                    }
                }
                else if (!LmrsUtil.acceptGapNeighbors(perception, bc, sli, cfm, desire, ownSpeed, lat))
                {
                    a = stopForEnd(xCur, xMerge, bc, ownSpeed, cfm, sli);
                    // but no stronger than getting behind the leader
                    if (leaders != null && !leaders.isEmpty())
                    {
                        double c = requiredBufferSpace(ownSpeed, nCur, x0, t0, lc, dCoop).si;
                        double t = (xCur.si - leaders.first().getDistance().si - c) / leaders.first().getSpeed().si;
                        double xGap = ownSpeed.si * (tMin.si + desire * (tMax.si - tMin.si));
                        Acceleration acc = Acceleration.createSI(2 * (xCur.si - c - ownSpeed.si * t - xGap) / (t * t));
                        if (!(leaders.first().getSpeed().eq0() || acc.si < -ownSpeed.si / t || t < 0))
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
                            : xCur.minus(xMerge).divideBy(t0.multiplyBy((1 - dCoop) * (nCur - 1)).plus(lc));
                    vMerge = Speed.max(vMerge, x0.divideBy(t0));
                    a = Acceleration.min(a, CarFollowingUtil.approachTargetSpeed(cfm, bc, ownSpeed, sli, xMerge, vMerge));
                }
                else
                {
                    // slow down by b if our speed is too high beyond the merge point
                    Length c = requiredBufferSpace(ownSpeed, nCur, x0, t0, lc, dCoop);
                    if (xCur.lt(c))
                    {
                        a = Acceleration.min(a, b.neg());
                    }
                }
            }

            return a;
        }

        @Override
        Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {

            if ((lat.isLeft() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
                    || (lat.isRight() && !perception.getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT)))
            {
                return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            }
            Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            double dCoop = bc.getParameter(DCOOP);
            Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            for (HeadwayGTU leader : removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane), perception,
                    relativeLane), perception, RelativeLane.CURRENT))
            {
                BehavioralCharacteristics bc2 = leader.getBehavioralCharacteristics();
                double desire = lat.equals(LateralDirectionality.LEFT) && bc2.contains(DRIGHT) ? bc2.getParameter(DRIGHT)
                        : lat.equals(LateralDirectionality.RIGHT) && bc2.contains(DLEFT) ? bc2.getParameter(DLEFT) : 0;
                if (desire >= dCoop && leader.getDistance().gt0())
                {
                    Acceleration aSingle = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(),
                            desire, bc, sli, cfm);
                    a = Acceleration.min(a, gentleUrgency(aSingle, desire, bc));
                }
            }
            return a;
        }

    };

    /**
     * Determine acceleration for synchronization.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param lat lateral direction for synchronization
     * @param lmrsData LMRS data
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    abstract Acceleration synchronize(LanePerception perception, BehavioralCharacteristics bc, SpeedLimitInfo sli,
            CarFollowingModel cfm, double desire, LateralDirectionality lat, LmrsData lmrsData)
            throws ParameterException, OperationalPlanException;

    /**
     * Determine acceleration for cooperation.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param lat lateral direction for cooperation
     * @param ownDesire own lane change desire
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    abstract Acceleration cooperate(LanePerception perception, BehavioralCharacteristics bc, SpeedLimitInfo sli,
            CarFollowingModel cfm, LateralDirectionality lat, Desire ownDesire)
            throws ParameterException, OperationalPlanException;

    /**
     * Removes all GTUs from the set, that are found upstream on the conflicting lane of a conflict in the current lane.
     * @param set set of GTUs
     * @param perception perception
     * @param relativeLane relative lane
     * @return the input set, for chained use
     * @throws OperationalPlanException if the {@code IntersectionPerception} category is not present
     */
    static SortedSet<HeadwayGTU> removeAllUpstreamOfConflicts(final SortedSet<HeadwayGTU> set, final LanePerception perception,
            final RelativeLane relativeLane) throws OperationalPlanException
    {
        if (!perception.contains(IntersectionPerception.class))
        {
            return set;
        }
        Set<HeadwayConflict> conflicts =
                perception.getPerceptionCategory(IntersectionPerception.class).getConflicts(relativeLane);
        if (conflicts != null)
        {
            for (HeadwayConflict conflict : conflicts)
            {
                if (conflict.isCrossing() || conflict.isMerge())
                {
                    for (HeadwayGTU conflictGtu : conflict.getUpstreamConflictingGTUs())
                    {
                        for (HeadwayGTU gtu : set)
                        {
                            if (conflictGtu.getId().equals(gtu.getId()))
                            {
                                set.remove(gtu);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return set;
    }

    /**
     * Return limited deceleration. Deceleration is limited to {@code b} for {@code d < dCoop}. Beyond {@code dCoop} the limit
     * is a linear interpolation between {@code b} and {@code bCrit}.
     * @param a acceleration to limit
     * @param desire lane change desire
     * @param bc behavioral characteristics
     * @return limited deceleration
     * @throws ParameterException when parameter is no available or value out of range
     */
    static Acceleration gentleUrgency(final Acceleration a, final double desire, final BehavioralCharacteristics bc)
            throws ParameterException
    {
        Acceleration b = bc.getParameter(ParameterTypes.B);
        if (a.si > -b.si)
        {
            return a;
        }
        double dCoop = bc.getParameter(DCOOP);
        if (desire < dCoop)
        {
            return b.neg();
        }
        Acceleration bCrit = bc.getParameter(ParameterTypes.BCRIT);
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
    static HeadwayGTU getFollower(final HeadwayGTU gtu, final SortedSet<HeadwayGTU> leaders, final HeadwayGTU follower,
            final Length ownLength)
    {
        HeadwayGTU last = null;
        for (HeadwayGTU leader : leaders)
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
     * @param leader leader
     * @param followerSpeed follower speed
     * @param followerLength follower length
     * @param tagSpeed maximum tag along speed
     * @param desire lane change desire
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @return acceleration by following an adjacent vehicle including tagging along
     * @throws ParameterException if a parameter is not present
     */
    @SuppressWarnings("checkstyle:parameternumber")
    static Acceleration tagAlongAcceleration(final HeadwayGTU leader, final Speed followerSpeed, final Length followerLength,
            final Speed tagSpeed, final double desire, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
            final CarFollowingModel cfm) throws ParameterException
    {
        double dCoop = bc.getParameter(DCOOP);
        double tagV = followerSpeed.lt(tagSpeed) ? 1.0 - followerSpeed.si / tagSpeed.si : 0.0;
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
        Length headwayAdjustment = bc.getParameter(ParameterTypes.S0)
                .plus(Length.min(followerLength, leader.getLength()).multiplyBy(0.5)).multiplyBy(tagExtent);
        Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance().plus(headwayAdjustment), followerSpeed,
                leader.getSpeed(), desire, bc, sli, cfm);
        return a;
    }

    /**
     * Returns whether a driver estimates it can be ahead of an adjacent vehicle for merging.
     * @param adjacentVehicle adjacent vehicle
     * @param xCur remaining distance
     * @param nCur number of lane changes to perform
     * @param ownSpeed own speed
     * @param ownLength own length
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
    static boolean canBeAhead(final HeadwayGTU adjacentVehicle, final Length xCur, final int nCur, final Speed ownSpeed,
            final Length ownLength, final Speed tagSpeed, final double dCoop, final Acceleration b, final Duration tMin,
            final Duration tMax, final Length x0, final Duration t0, final Duration lc, final double desire)
            throws ParameterException
    {

        // always true if adjacent vehicle is behind and i) both vehicles very slow, or ii) cooperation assumed and possible
        boolean tmp = LmrsUtil
                .singleAcceleration(adjacentVehicle.getDistance().neg().minus(adjacentVehicle.getLength()).minus(ownLength),
                        adjacentVehicle.getSpeed(), ownSpeed, desire, adjacentVehicle.getBehavioralCharacteristics(),
                        adjacentVehicle.getSpeedLimitInfo(), adjacentVehicle.getCarFollowingModel())
                .gt(b.neg());
        if (adjacentVehicle.getDistance().lt(ownLength.neg())
                && ((desire > dCoop && tmp) || (ownSpeed.lt(tagSpeed) && adjacentVehicle.getSpeed().lt(tagSpeed))))
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
        Length c = requiredBufferSpace(ownSpeed, nCur, x0, t0, lc, dCoop);
        double t = (xCur.si - c.si) / ownSpeed.si;
        double xGap = adjacentVehicle.getSpeed().si * (tMin.si + desire * (tMax.si - tMin.si));
        return 0.0 < t && t < (xCur.si - adjacentVehicle.getDistance().si - ownLength.si - adjacentVehicle.getLength().si - c.si
                - xGap) / adjacentVehicle.getSpeed().si;
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
        Length xCrit = speed.multiplyBy(t0);
        xCrit = Length.max(xCrit, x0);
        return speed.multiplyBy(lc).plus(xCrit.multiplyBy((nCur - 1.0) * (1.0 - dCoop)));
    }

    /**
     * Calculates acceleration to stop for a split or dead-end, accounting for infrastructure.
     * @param xCur remaining distance to end
     * @param xMerge distance until merge point
     * @param bc behavioral characteristics
     * @param ownSpeed own speed
     * @param cfm car-following model
     * @param sli speed limit info
     * @return acceleration to stop for a split or dead-end, accounting for infrastructure
     * @throws ParameterException if parameter is not defined
     */
    static Acceleration stopForEnd(final Length xCur, final Length xMerge, final BehavioralCharacteristics bc,
            final Speed ownSpeed, final CarFollowingModel cfm, final SpeedLimitInfo sli) throws ParameterException
    {
        if (xCur.lt0())
        {
            // missed our final lane change spot, but space remains
            return Acceleration.max(bc.getParameter(ParameterTypes.BCRIT).neg(),
                    CarFollowingUtil.stop(cfm, bc, ownSpeed, sli, xMerge));
        }
        LmrsUtil.setDesiredHeadway(bc, 1.0);
        Acceleration a = CarFollowingUtil.stop(cfm, bc, ownSpeed, sli, xCur);
        if (a.lt0())
        {
            // decelerate even more if still comfortable, leaving space for acceleration later
            a = Acceleration.min(a, bc.getParameter(ParameterTypes.B).neg());
            // but never decelerate such that stand-still is reached within xMerge
            if (xMerge.gt0())
            {
                a = Acceleration.max(a, CarFollowingUtil.stop(cfm, bc, ownSpeed, sli, xMerge));
            }
        }
        else
        {
            a = Acceleration.POSITIVE_INFINITY;
        }
        LmrsUtil.resetDesiredHeadway(bc);
        return a;
    }

    /**
     * Returns the leader of one gtu from a set.
     * @param gtu gtu
     * @param leaders leaders
     * @return leader of one gtu from a set
     */
    static HeadwayGTU getTargetLeader(final HeadwayGTU gtu, final SortedSet<HeadwayGTU> leaders)
    {
        for (HeadwayGTU leader : leaders)
        {
            if (leader.getDistance().gt(gtu.getDistance()))
            {
                return leader;
            }
        }
        return null;
    }

}