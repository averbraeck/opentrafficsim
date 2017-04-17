package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

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
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum Synchronization implements LmrsParameters
{

    /** Synchronization where current leaders are taken. */
    PASSIVE
    {

        @Override
        Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            double dCoop = bc.getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            SortedSet<HeadwayGTU> set = removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane), perception,
                    relativeLane), perception, RelativeLane.CURRENT);
            HeadwayGTU leader = null;
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
            if (leader != null)
            {
                Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
                Acceleration aSingle =
                        LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                a = Acceleration.min(a, aSingle);
            }
            return gentleUrgency(a, desire, bc);
        }

        @Override
        Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final LateralDirectionality lat)
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

    /** Synchronization where a suitable leader is actively targeted, in relation to infrastructure. */
    ACTIVE
    {

        @Override
        Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData)
                throws ParameterException, OperationalPlanException
        {

            Acceleration b = bc.getParameter(ParameterTypes.B);
            Acceleration bCrit = bc.getParameter(ParameterTypes.BCRIT);
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
            int nCur;
            Length xCur;
            if (info.isEmpty())
            {
                nCur = 0;
                xCur = Length.createSI(Double.POSITIVE_INFINITY);
            }
            else
            {
                nCur = info.first().getRequiredNumberOfLaneChanges();
                // subtract minimum lane change distance per lane changes
                xCur = info.first().getRemainingDistance().minus(ownLength.multiplyBy(2.0 * nCur)).minus(dx);
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
            if (syncVehicle == null)
            {
                Length maxDistance = Length.min(x0, xCur);
                for (HeadwayGTU leader : leaders)
                {
                    if ((leader.getDistance().lt(maxDistance) || leader.getSpeed().gt(vCong))
                            && tagAlongAcceleration(leader, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm).gt(b.neg()))
                    {
                        syncVehicle = leader;
                        break;
                    }
                }
            }

            // select upstream vehicle if we can safely follow that, or if we cannot stay ahead of it (infrastructure, in coop)
            HeadwayGTU up;
            SortedSet<HeadwayGTU> followers =
                    removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(neighbors.getFollowers(lane), perception, lane),
                            perception, RelativeLane.CURRENT);
            HeadwayGTU follower = followers.isEmpty() ? null
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
            while (syncVehicle != null && up != null
                    && (upOk || (!canBeAhead(up, xCur, nCur, ownSpeed, ownLength, tagSpeed, dCoop, bCrit, tMin, tMax, x0, t0,
                            lc, desire) && desire > dCoop))
                    && (up.getDistance().plus(up.getLength()).gt(xMergeSync) || up.getSpeed().gt(vCong)))
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

            // actual synchronization
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            if (syncVehicle != null)
            {
                a = gentleUrgency(tagAlongAcceleration(syncVehicle, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm),
                        desire, bc);
            }
            else if (follower != null)
            {
                // no gap to synchronize with, but there is a follower to account for
                if (!canBeAhead(follower, xCur, nCur, ownSpeed, ownLength, tagSpeed, dCoop, bCrit, tMin, tMax, x0, t0, lc,
                        desire))
                {
                    // get behind follower
                    double c = requiredBufferSpace(follower.getSpeed(), nCur, x0, t0, lc, dCoop).si;
                    double t = (xCur.si - follower.getDistance().si - c) / follower.getSpeed().si;
                    Acceleration acc = Acceleration.createSI(2 * (xCur.si - c - ownSpeed.si * t) / (t * t));
                    if (follower.getSpeed().eq0() || acc.si < -ownSpeed.si / t || t < 0)
                    {
                        // inappropriate to get behind
                        a = stopForEnd(xCur, xMerge, bc, ownSpeed, cfm, sli);
                    }
                    else
                    {
                        a = gentleUrgency(acc, desire, bc);
                    }
                }
                else
                {
                    // check gap acceptance based on accelerations
                    boolean acceptFollower;
                    if (followers.first().getDistance().gt0()) // obtain with unadjusted distance
                    {
                        BehavioralCharacteristics bcFollower = follower.getBehavioralCharacteristics();
                        LmrsUtil.setDesiredHeadway(bc, desire);
                        acceptFollower = LmrsUtil.singleAcceleration(followers.first().getDistance(), follower.getSpeed(),
                                ownSpeed, desire, bcFollower, follower.getSpeedLimitInfo(),
                                follower.getCarFollowingModel()).si > -b.si * desire;
                    }
                    else
                    {
                        acceptFollower = false;
                    }
                    boolean acceptSelf;
                    if (!leaders.isEmpty() && leaders.first().getDistance().gt0())
                    {
                        acceptSelf = LmrsUtil.singleAcceleration(leaders.first().getDistance(), ownSpeed,
                                leaders.first().getSpeed(), desire, bc, sli, cfm).si > -b.si * desire;
                    }
                    else
                    {
                        acceptSelf = false;
                    }
                    if (!acceptFollower || !acceptSelf)
                    {
                        // can be ahead of follower, but no suitable leader for synchronization
                        a = stopForEnd(xCur, xMerge, bc, ownSpeed, cfm, sli);
                    }
                }
            }

            // slow down to have sufficient time for further lane changes
            if (nCur > 1)
            {
                if (xMerge.lt0())
                {
                    // achieve speed to have sufficient time as soon as a lane change becomes possible (infrastructure)
                    Speed vMerge = xCur.minus(xMerge).divideBy(t0.multiplyBy((1 - dCoop) * (nCur - 1)).plus(lc));
                    vMerge = Speed.min(vMerge, x0.divideBy(t0));
                    a = Acceleration.min(a, CarFollowingUtil.approachTargetSpeed(cfm, bc, ownSpeed, sli, xMerge, vMerge));
                }
                else
                {
                    // slow down by b if our speed is too high beyond the merge point
                    Speed speed;
                    if (up != null)
                    {
                        speed = up.getSpeed();
                    }
                    else if (syncVehicle != null)
                    {
                        speed = syncVehicle.getSpeed();
                    }
                    else
                    {
                        speed = ownSpeed;
                    }
                    Length c = requiredBufferSpace(speed, nCur, x0, t0, lc, dCoop);
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
                final CarFollowingModel cfm, final LateralDirectionality lat)
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
            Length x0 = bc.getParameter(ParameterTypes.LOOKAHEAD);
            Duration t0 = bc.getParameter(ParameterTypes.T0);
            Speed tagSpeed = x0.divideBy(t0);
            Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
            Length ownLength = perception.getPerceptionCategory(EgoPerception.class).getLength();
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            SortedSet<HeadwayGTU> targetLeaders =
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
            for (HeadwayGTU leader : removeAllUpstreamOfConflicts(removeAllUpstreamOfConflicts(
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane), perception,
                    relativeLane), perception, RelativeLane.CURRENT))
            {
                BehavioralCharacteristics bc2 = leader.getBehavioralCharacteristics();
                double desire = lat.equals(LateralDirectionality.LEFT) && bc2.contains(DRIGHT) ? bc2.getParameter(DRIGHT)
                        : lat.equals(LateralDirectionality.RIGHT) && bc2.contains(DLEFT) ? bc2.getParameter(DLEFT) : 0;
                if (desire >= dCoop && (perception.getPerceptionCategory(EgoPerception.class).getSpeed().gt0()
                        || leader.getSpeed().gt0() || leader.getDistance().gt0()))
                {
                    Acceleration aSingle;
                    if (leader.getSpeed().eq0())
                    {
                        HeadwayGTU targetLeader = getTargetLeader(leader, targetLeaders);
                        if (targetLeader == null || LmrsUtil.singleAcceleration(
                                targetLeader.getDistance().minus(leader.getDistance()).minus(leader.getLength()),
                                leader.getSpeed(), targetLeader.getSpeed(), desire, leader.getBehavioralCharacteristics(),
                                leader.getSpeedLimitInfo(), leader.getCarFollowingModel()).gt(b.neg()))
                        {
                            // cooperate without tagging along, adjacent vehicle stands still due to remaining space
                            aSingle = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc,
                                    sli, cfm);
                        }
                        else
                        {
                            aSingle = tagAlongAcceleration(leader, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm);
                        }
                    }
                    else
                    {
                        aSingle = tagAlongAcceleration(leader, ownSpeed, ownLength, tagSpeed, desire, bc, sli, cfm);
                    }
                    a = Acceleration.min(a, aSingle);
                }
            }

            return Acceleration.max(a, b.neg());
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
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    abstract Acceleration cooperate(LanePerception perception, BehavioralCharacteristics bc, SpeedLimitInfo sli,
            CarFollowingModel cfm, LateralDirectionality lat) throws ParameterException, OperationalPlanException;

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
        for (HeadwayConflict conflict : perception.getPerceptionCategory(IntersectionPerception.class)
                .getConflicts(relativeLane))
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
     * @param bCrit critical deceleration
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
            final Length ownLength, final Speed tagSpeed, final double dCoop, final Acceleration bCrit, final Duration tMin,
            final Duration tMax, final Length x0, final Duration t0, final Duration lc, final double desire)
            throws ParameterException
    {
        // always true if adjacent vehicle is behind and i) both vehicles very slow, or ii) cooperation assumed and possible
        if (adjacentVehicle
                .getDistance().gt(
                        adjacentVehicle.getLength()
                                .neg())
                && ((desire > dCoop && LmrsUtil.singleAcceleration(
                        adjacentVehicle.getDistance().neg().minus(adjacentVehicle.getLength()).minus(ownLength),
                        adjacentVehicle.getSpeed(), ownSpeed, desire, adjacentVehicle.getBehavioralCharacteristics(),
                        adjacentVehicle.getSpeedLimitInfo(), adjacentVehicle.getCarFollowingModel()).gt(bCrit.neg()))
                        || (ownSpeed.lt(tagSpeed) && adjacentVehicle.getSpeed().lt(tagSpeed))))
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
        Length c = requiredBufferSpace(adjacentVehicle.getSpeed(), nCur, x0, t0, lc, dCoop);
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
        LmrsUtil.setDesiredHeadway(bc, 1.0);
        Acceleration a = CarFollowingUtil.stop(cfm, bc, ownSpeed, sli, xCur);
        if (a.lt0())
        {
            // decelerate even more if still comfortable, leaving space for acceleration later
            a = Acceleration.max(a, bc.getParameter(ParameterTypes.B).neg());
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
