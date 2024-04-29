package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Different forms of synchronization.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Synchronization extends LmrsParameters
{

    /** Synchronization where current leaders are taken. */
    Synchronization PASSIVE = new Synchronization()
    {
        /** {@inheritDoc} */
        @Override
        public Acceleration synchronize(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData,
                final LaneChange laneChange, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            double dCoop = params.getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);

            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> set =
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane);
            HeadwayGtu leader = null;
            if (set != null)
            {
                if (desire >= dCoop && !set.isEmpty())
                {
                    leader = set.first();
                }
                else
                {
                    for (HeadwayGtu gtu : set)
                    {
                        if (gtu.getSpeed().gt0())
                        {
                            leader = gtu;
                            break;
                        }
                    }
                }
            }
            Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
            if (leader != null)
            {
                Length headway = headwayWithLcSpace(leader, params, laneChange);
                Acceleration aSingle =
                        LmrsUtil.singleAcceleration(headway, ownSpeed, leader.getSpeed(), desire, params, sli, cfm);
                a = Acceleration.min(a, aSingle);
                a = gentleUrgency(a, desire, params);
            }
            // keep some space ahead to perform lane change
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
            if (!leaders.isEmpty() && leaders.first().getSpeed().lt(params.getParameter(ParameterTypes.VCONG)))
            {
                Length headway = leaders.first().getDistance().minus(laneChange.getMinimumLaneChangeDistance());
                Acceleration aSingle =
                        LmrsUtil.singleAcceleration(headway, ownSpeed, leaders.first().getSpeed(), desire, params, sli, cfm);
                aSingle = gentleUrgency(aSingle, desire, params);
                a = Acceleration.min(a, aSingle);
            }

            // check merge distance
            Length xMerge =
                    getMergeDistance(perception, lat).minus(perception.getPerceptionCategory(EgoPerception.class).getLength());
            if (xMerge.gt0())
            {
                Acceleration aMerge = LmrsUtil.singleAcceleration(xMerge, ownSpeed, Speed.ZERO, desire, params, sli, cfm);
                a = Acceleration.max(a, aMerge);
            }
            return a;
        }

        /** {@inheritDoc} */
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
        /** {@inheritDoc} */
        @Override
        public Acceleration synchronize(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData,
                final LaneChange laneChange, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            EgoPerception<?, ?> ego = perception.getPerceptionCategory(EgoPerception.class);
            Speed ownSpeed = ego.getSpeed();
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(relativeLane);
            if (!leaders.isEmpty())
            {
                HeadwayGtu leader = leaders.first();
                Length gap = leader.getDistance();
                LmrsUtil.setDesiredHeadway(params, desire);
                PerceptionCollectable<HeadwayGtu, LaneBasedGtu> followers =
                        perception.getPerceptionCategory(NeighborsPerception.class).getFollowers(relativeLane);
                if (!followers.isEmpty())
                {
                    HeadwayGtu follower = followers.first();
                    Length netGap = leader.getDistance().plus(follower.getDistance()).times(0.5);
                    gap = Length.max(gap, leader.getDistance().minus(netGap).plus(cfm.desiredHeadway(params, ownSpeed)));
                }
                a = CarFollowingUtil.followSingleLeader(cfm, params, ownSpeed, sli, gap, leader.getSpeed());
                LmrsUtil.resetDesiredHeadway(params);
                // limit deceleration based on desire
                a = gentleUrgency(a, desire, params);
            }
            // never stop before we can actually merge
            Length xMerge = getMergeDistance(perception, lat);
            if (xMerge.gt0())
            {
                Acceleration aMerge = LmrsUtil.singleAcceleration(xMerge, ownSpeed, Speed.ZERO, desire, params, sli, cfm);
                a = Acceleration.max(a, aMerge);
            }
            return a;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ALIGN_GAP";
        }
    };

    /** Synchronization where current leaders are taken. Synchronization is disabled for d_sync&lt;d&lt;d_coop at low speeds. */
    Synchronization PASSIVE_MOVING = new Synchronization()
    {
        /** {@inheritDoc} */
        @Override
        public Acceleration synchronize(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData,
                final LaneChange laneChange, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {
            double dCoop = params.getParameter(DCOOP);
            Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
            if (desire < dCoop && ownSpeed.si < params.getParameter(ParameterTypes.LOOKAHEAD).si
                    / params.getParameter(ParameterTypes.T0).si)
            {
                return Acceleration.POSITIVE_INFINITY;
            }
            return PASSIVE.synchronize(perception, params, sli, cfm, desire, lat, lmrsData, laneChange, initiatedLaneChange);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "PASSIVE_MOVING";
        }
    };

    /** Synchronization where a suitable leader is actively targeted, in relation to infrastructure. */
    Synchronization ACTIVE = new Synchronization()
    {
        /** {@inheritDoc} */
        @Override
        public Acceleration synchronize(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
                final CarFollowingModel cfm, final double desire, final LateralDirectionality lat, final LmrsData lmrsData,
                final LaneChange laneChange, final LateralDirectionality initiatedLaneChange)
                throws ParameterException, OperationalPlanException
        {

            Acceleration b = params.getParameter(ParameterTypes.B);
            Duration tMin = params.getParameter(ParameterTypes.TMIN);
            Duration tMax = params.getParameter(ParameterTypes.TMAX);
            Speed vCong = params.getParameter(ParameterTypes.VCONG);
            Length x0 = params.getParameter(ParameterTypes.LOOKAHEAD);
            Duration t0 = params.getParameter(ParameterTypes.T0);
            Duration lc = params.getParameter(ParameterTypes.LCDUR);
            Speed tagSpeed = x0.divide(t0);
            double dCoop = params.getParameter(DCOOP);
            EgoPerception<?, ?> ego = perception.getPerceptionCategory(EgoPerception.class);
            Speed ownSpeed = ego.getSpeed();
            Length ownLength = ego.getLength();
            Length dx;
            try
            {
                dx = perception.getGtu().getFront().getDx();
            }
            catch (GtuException exception)
            {
                throw new OperationalPlanException(exception);
            }

            // get xMergeSync, the distance within which a gap is pointless as the lane change is not possible
            InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
            SortedSet<InfrastructureLaneChangeInfo> info = infra.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT);
            // Length xMerge = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat).minus(dx);
            // xMerge = xMerge.lt0() ? xMerge.neg() : Length.ZERO; // zero or positive value where lane change is not possible
            Length xMerge = getMergeDistance(perception, lat);
            int nCur = 0;
            Length xCur = Length.POSITIVE_INFINITY;
            for (InfrastructureLaneChangeInfo lcInfo : info)
            {
                int nCurTmp = lcInfo.getRequiredNumberOfLaneChanges();
                // subtract minimum lane change distance per lane change
                Length xCurTmp = lcInfo.getRemainingDistance().minus(ownLength.times(2.0 * nCurTmp)).minus(dx);
                if (xCurTmp.lt(xCur))
                {
                    nCur = nCurTmp;
                    xCur = xCurTmp;
                }
            }

            // for short ramps, include braking distance, i.e. we -do- select a gap somewhat upstream of the merge point;
            // should we abandon this gap, we still have braking distance and minimum lane change distance left
            Length xMergeSync = xCur.minus(Length.instantiateSI(.5 * ownSpeed.si * ownSpeed.si / b.si));
            xMergeSync = Length.min(xMerge, xMergeSync);

            // abandon the gap if the sync vehicle is no longer adjacent, in congestion within xMergeSync, or too far
            NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
            RelativeLane lane = new RelativeLane(lat, 1);
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders = neighbors.getLeaders(lane);
            HeadwayGtu syncVehicle = lmrsData.getSyncVehicle(leaders);
            if (syncVehicle != null && ((syncVehicle.getSpeed().lt(vCong) && syncVehicle.getDistance().lt(xMergeSync))
                    || syncVehicle.getDistance().gt(xCur)))
            {
                syncVehicle = null;
            }

            // if there is no sync vehicle, select the first one to which current deceleration < b (it may become larger later)
            if (leaders != null && syncVehicle == null)
            {
                Length maxDistance = Length.min(x0, xCur);
                for (HeadwayGtu leader : leaders)
                {
                    if (leader.getDistance().lt(maxDistance))
                    {
                        if ((leader.getDistance().gt(xMergeSync) || leader.getSpeed().gt(vCong))
                                && tagAlongAcceleration(leader, ownSpeed, ownLength, tagSpeed, desire, params, sli, cfm)
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
            HeadwayGtu up;
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> followers = neighbors.getFollowers(lane);
            HeadwayGtu follower = followers == null || followers.isEmpty() ? null
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
                        : tagAlongAcceleration(up, ownSpeed, ownLength, tagSpeed, desire, params, sli, cfm).gt(b.neg());
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
                        : tagAlongAcceleration(up, ownSpeed, ownLength, tagSpeed, desire, params, sli, cfm).gt(b.neg());
            }
            lmrsData.setSyncVehicle(syncVehicle);

            // actual synchronization
            Acceleration a = Acceleration.POSITIVE_INFINITY;
            if (syncVehicle != null)
            {
                a = gentleUrgency(tagAlongAcceleration(syncVehicle, ownSpeed, ownLength, tagSpeed, desire, params, sli, cfm),
                        desire, params);
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
                    Acceleration acc = Acceleration.instantiateSI(2 * (xCur.si - c - ownSpeed.si * t - xGap) / (t * t));
                    if (follower.getSpeed().eq0() || acc.si < -ownSpeed.si / t || t < 0)
                    {
                        // inappropriate to get behind
                        // note: if minimum lane change space is more than infrastructure, deceleration will simply be limited
                        a = stopForEnd(xCur, xMerge, params, ownSpeed, cfm, sli);
                    }
                    else
                    {
                        a = gentleUrgency(acc, desire, params);
                    }
                }
                else if (!LmrsUtil.acceptLaneChange(perception, params, sli, cfm, desire, ownSpeed, Acceleration.ZERO, lat,
                        lmrsData.getGapAcceptance(), laneChange))
                {
                    a = stopForEnd(xCur, xMerge, params, ownSpeed, cfm, sli);
                    // but no stronger than getting behind the leader
                    if (leaders != null && !leaders.isEmpty())
                    {
                        double c = requiredBufferSpace(ownSpeed, nCur, x0, t0, lc, dCoop).si;
                        double t = (xCur.si - leaders.first().getDistance().si - c) / leaders.first().getSpeed().si;
                        double xGap = ownSpeed.si * (tMin.si + desire * (tMax.si - tMin.si));
                        Acceleration acc = Acceleration.instantiateSI(2 * (xCur.si - c - ownSpeed.si * t - xGap) / (t * t));
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
                            : xCur.minus(xMerge).divide(t0.times((1 - dCoop) * (nCur - 1)).plus(lc));
                    vMerge = Speed.max(vMerge, x0.divide(t0));
                    a = Acceleration.min(a, CarFollowingUtil.approachTargetSpeed(cfm, params, ownSpeed, sli, xMerge, vMerge));
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

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ACTIVE";
        }
    };

    /**
     * Returns the distance to the next merge, stopping within this distance is futile for a lane change.
     * @param perception LanePerception; perception
     * @param lat LateralDirectionality; lateral direction
     * @return Length; distance to the next merge
     * @throws OperationalPlanException if there is no infrastructure perception
     */
    public static Length getMergeDistance(final LanePerception perception, final LateralDirectionality lat)
            throws OperationalPlanException
    {
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        Length dx = Try.assign(() -> perception.getGtu().getFront().getDx(), "Could not obtain GTU.");
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
     * @param perception LanePerception; perception
     * @param params Parameters; parameters
     * @param sli SpeedLimitInfo; speed limit info
     * @param cfm CarFollowingModel; car-following model
     * @param desire double; level of lane change desire
     * @param lat LateralDirectionality; lateral direction for synchronization
     * @param lmrsData LmrsData; LMRS data
     * @param laneChange LaneChange; lane change
     * @param initiatedLaneChange LateralDirectionality; lateral direction of initiated lane change
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    Acceleration synchronize(LanePerception perception, Parameters params, SpeedLimitInfo sli, CarFollowingModel cfm,
            double desire, LateralDirectionality lat, LmrsData lmrsData, LaneChange laneChange,
            LateralDirectionality initiatedLaneChange) throws ParameterException, OperationalPlanException;

    /**
     * Returns a headway (length) to allow space to perform a lane change at low speeds.
     * @param headway Headway; headway
     * @param parameters Parameters; parameters
     * @param laneChange LaneChange; lane change
     * @return Length; distance to allow space to perform a lane change at low speeds
     * @throws ParameterException if parameter VCONG is not available
     */
    static Length headwayWithLcSpace(final Headway headway, final Parameters parameters, final LaneChange laneChange)
            throws ParameterException
    {
        if (headway.getSpeed().gt(parameters.getParameter(ParameterTypes.VCONG)))
        {
            return headway.getDistance();
        }
        return headway.getDistance().minus(laneChange.getMinimumLaneChangeDistance());
    }

    /**
     * Return limited deceleration. Deceleration is limited to {@code b} for {@code d < dCoop}. Beyond {@code dCoop} the limit
     * is a linear interpolation between {@code b} and {@code bCrit}.
     * @param a Acceleration; acceleration to limit
     * @param desire double; lane change desire
     * @param params Parameters; parameters
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
     * @param gtu HeadwayGtu; gtu
     * @param leaders PerceptionCollectable&lt;HeadwayGtu,LaneBasedGtu&gt;; leaders of own vehicle
     * @param follower HeadwayGtu; following vehicle of own vehicle
     * @param ownLength Length; own vehicle length
     * @return upstream gtu of the given gtu
     */
    static HeadwayGtu getFollower(final HeadwayGtu gtu, final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders,
            final HeadwayGtu follower, final Length ownLength)
    {
        HeadwayGtu last = null;
        for (HeadwayGtu leader : leaders)
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
     * @param leader HeadwayGtu; leader
     * @param followerSpeed Speed; follower speed
     * @param followerLength Length; follower length
     * @param tagSpeed Speed; maximum tag along speed
     * @param desire double; lane change desire
     * @param params Parameters; parameters
     * @param sli SpeedLimitInfo; speed limit info
     * @param cfm CarFollowingModel; car-following model
     * @return acceleration by following an adjacent vehicle including tagging along
     * @throws ParameterException if a parameter is not present
     */
    @SuppressWarnings("checkstyle:parameternumber")
    static Acceleration tagAlongAcceleration(final HeadwayGtu leader, final Speed followerSpeed, final Length followerLength,
            final Speed tagSpeed, final double desire, final Parameters params, final SpeedLimitInfo sli,
            final CarFollowingModel cfm) throws ParameterException
    {
        double dCoop = params.getParameter(DCOOP);
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
        Length headwayAdjustment = params.getParameter(ParameterTypes.S0)
                .plus(Length.min(followerLength, leader.getLength()).times(0.5)).times(tagExtent);
        Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance().plus(headwayAdjustment), followerSpeed,
                leader.getSpeed(), desire, params, sli, cfm);
        return a;
    }

    /**
     * Returns whether a driver estimates it can be ahead of an adjacent vehicle for merging.
     * @param adjacentVehicle HeadwayGtu; adjacent vehicle
     * @param xCur Length; remaining distance
     * @param nCur int; number of lane changes to perform
     * @param ownSpeed Speed; own speed
     * @param ownLength Length; own length
     * @param tagSpeed Speed; maximum tag along speed
     * @param dCoop double; cooperation threshold
     * @param b Acceleration; critical deceleration
     * @param tMin Duration; minimum headway
     * @param tMax Duration; normal headway
     * @param x0 Length; anticipation distance
     * @param t0 Duration; anticipation time
     * @param lc Duration; lane change duration
     * @param desire double; lane change desire
     * @return whether a driver estimates it can be ahead of an adjacent vehicle for merging
     * @throws ParameterException if parameter is not defined
     */
    static boolean canBeAhead(final HeadwayGtu adjacentVehicle, final Length xCur, final int nCur, final Speed ownSpeed,
            final Length ownLength, final Speed tagSpeed, final double dCoop, final Acceleration b, final Duration tMin,
            final Duration tMax, final Length x0, final Duration t0, final Duration lc, final double desire)
            throws ParameterException
    {

        // always true if adjacent vehicle is behind and i) both vehicles very slow, or ii) cooperation assumed and possible
        boolean tmp = LmrsUtil
                .singleAcceleration(adjacentVehicle.getDistance().neg().minus(adjacentVehicle.getLength()).minus(ownLength),
                        adjacentVehicle.getSpeed(), ownSpeed, desire, adjacentVehicle.getParameters(),
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
     * @param speed Speed; representative speed
     * @param nCur int; number of required lane changes
     * @param x0 Length; anticipation distance
     * @param t0 Duration; anticipation time
     * @param lc Duration; lane change duration
     * @param dCoop double; cooperation threshold
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
     * @param xCur Length; remaining distance to end
     * @param xMerge Length; distance until merge point
     * @param params Parameters; parameters
     * @param ownSpeed Speed; own speed
     * @param cfm CarFollowingModel; car-following model
     * @param sli SpeedLimitInfo; speed limit info
     * @return acceleration to stop for a split or dead-end, accounting for infrastructure
     * @throws ParameterException if parameter is not defined
     */
    static Acceleration stopForEnd(final Length xCur, final Length xMerge, final Parameters params, final Speed ownSpeed,
            final CarFollowingModel cfm, final SpeedLimitInfo sli) throws ParameterException
    {
        if (xCur.lt0())
        {
            // missed our final lane change spot, but space remains
            return Acceleration.max(params.getParameter(ParameterTypes.BCRIT).neg(),
                    CarFollowingUtil.stop(cfm, params, ownSpeed, sli, xMerge));
        }
        LmrsUtil.setDesiredHeadway(params, 1.0);
        Acceleration a = CarFollowingUtil.stop(cfm, params, ownSpeed, sli, xCur);
        if (a.lt0())
        {
            // decelerate even more if still comfortable, leaving space for acceleration later
            a = Acceleration.min(a, params.getParameter(ParameterTypes.B).neg());
            // but never decelerate such that stand-still is reached within xMerge
            if (xMerge.gt0())
            {
                a = Acceleration.max(a, CarFollowingUtil.stop(cfm, params, ownSpeed, sli, xMerge));
            }
        }
        else
        {
            a = Acceleration.POSITIVE_INFINITY;
        }
        LmrsUtil.resetDesiredHeadway(params);
        return a;
    }

    /**
     * Returns the leader of one gtu from a set.
     * @param gtu HeadwayGtu; gtu
     * @param leaders SortedSet&lt;HeadwayGtu&gt;; leaders
     * @return leader of one gtu from a set
     */
    static HeadwayGtu getTargetLeader(final HeadwayGtu gtu, final SortedSet<HeadwayGtu> leaders)
    {
        for (HeadwayGtu leader : leaders)
        {
            if (leader.getDistance().gt(gtu.getDistance()))
            {
                return leader;
            }
        }
        return null;
    }

}
