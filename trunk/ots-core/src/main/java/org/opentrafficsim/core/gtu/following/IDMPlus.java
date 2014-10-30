package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * IDMPlus implements the <i>Integrated Lane Change Model with Relaxation and Synchronization</i> as published by Wouter J.
 * Schakel, Victor L. Knoop and Bart van Arem in Transportation Research Record No 2316 pp 47-57, Washington D.C., 2012. <br>
 * There are two nasty type setting errors in equation 7 in this published version of the paper. Both times an equals sign
 * (<cite>=</cite>) after <cite>a<sub>gain</sub></cite> should <b>not</b> be there.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlus implements GTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final DoubleScalar.Rel<LengthUnit> s0 = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);

    /** Longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> a = new DoubleScalar.Abs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Regular longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> aMin = new DoubleScalar.Abs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final DoubleScalar.Abs<AccelerationUnit> b = new DoubleScalar.Abs<AccelerationUnit>(2.09,
            AccelerationUnit.METER_PER_SECOND_2);

    /**
     * Maximum deceleration when actual speed is more than desired speed (v &gt; vdes). (Should be a positive value even though
     * it is a <b>de</b>celeration.) [m/s^2]
     */
    private final DoubleScalar.Abs<AccelerationUnit> b0 = new DoubleScalar.Abs<AccelerationUnit>(0.5,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal regular following headway [s]. */
    private final DoubleScalar.Rel<TimeUnit> tMax = new DoubleScalar.Rel<TimeUnit>(1.2, TimeUnit.SECOND);

    /** LMRS free lane change threshold. (Value between 0 and dSync) */
    private final double dFree = .365;

    /** LMRS synchronized lane change threshold. (Value between dFree and dCoop) */
    private final double dSync = .577;

    /** LMRS cooperative lane change threshold. (Value between dSync and 1.0) */
    private final double dCoop = .788;

    /** LMRS mandatory lane change time [s]. */
    private final DoubleScalar.Rel<TimeUnit> t0 = new DoubleScalar.Rel<TimeUnit>(43, TimeUnit.SECOND);

    /** LMRS mandatory lane change distance [m]. */
    private final DoubleScalar.Rel<LengthUnit> x0 = new DoubleScalar.Rel<LengthUnit>(295, LengthUnit.METER);

    /** LMRS speed gain [m/s] for full desire. (The paper specifies this value in [km/h]) */
    private final DoubleScalar.Abs<SpeedUnit> vGain = new DoubleScalar.Abs<SpeedUnit>(69.6, SpeedUnit.KM_PER_HOUR);

    /** LMRS critical speed [m/s] for a speed gain in the right lane. (The paper specifies this value in [km/h]) */
    private final DoubleScalar.Abs<SpeedUnit> vCong = new DoubleScalar.Abs<SpeedUnit>(60, SpeedUnit.KM_PER_HOUR);

    /** Safe time headway. */
    private final DoubleScalar.Rel<TimeUnit> tSafe = new DoubleScalar.Rel<TimeUnit>(1.6, TimeUnit.SECOND);

    /** the reference to the simulator for the GTUs to use. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the speed
     * limit, etc.).
     */
    private final double delta = 1.0;

    /**
     * Time slot size used by IDMPlus (not defined in the paper, but 0.5s is a reasonable trade-off between computational speed
     * and accuracy).
     */
    private final DoubleScalar.Rel<TimeUnit> stepSize = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND);

    /**
     * @param simulator the simulator.
     */
    public IDMPlus(final OTSDEVSSimulatorInterface simulator)
    {
        super();
        this.simulator = simulator;
    }

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param gtu GTU; the GTU whose desired speed must be returned
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private DoubleScalar.Rel<SpeedUnit> vDes(final GTU<?> gtu, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        return new DoubleScalar.Rel<SpeedUnit>(Math.min(this.delta * speedLimit.getSI(), gtu.getMaximumVelocity().getSI()),
                SpeedUnit.METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> gtu,
            final Collection<? extends LaneBasedGTU<?>> leaders, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = gtu.getNextEvaluationTime();
        // System.out.println("evaluation time is " + thisEvaluationTime);
        // System.out.println("vDes is " + vDes);
        DoubleScalar.Rel<LengthUnit> myFrontPosition = gtu.positionOfFront(thisEvaluationTime).getLongitudinalPosition();
        // System.out.println("myFrontPosition is " + myFrontPosition);
        DoubleScalar.Rel<LengthUnit> shortestHeadway = new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        LaneBasedGTU<?> closestLeader = null;
        for (LaneBasedGTU<?> leader : leaders)
        {
            DoubleScalar.Rel<LengthUnit> s =
                    DoubleScalar.minus(leader.positionOfRear(thisEvaluationTime).getLongitudinalPosition(), myFrontPosition)
                            .immutable();
            // System.out.println("s is " + s);
            if (s.getSI() < 0)
            {
                continue; // Ignore gtus that are behind this gtu
            }
            if (s.getSI() < shortestHeadway.getSI())
            {
                shortestHeadway = s;
                closestLeader = leader;
            }
        }
        // System.out.println("shortestHeadway is " + shortestHeadway);
        DoubleScalar.Abs<SpeedUnit> myCurrentSpeed = gtu.getLongitudinalVelocity(thisEvaluationTime);
        double speedIncentive = 1 - Math.pow(myCurrentSpeed.getSI() / vDes(gtu, speedLimit).getSI(), 4);
        // System.out.println("speedIncentive is " + speedIncentive);
        MutableDoubleScalar.Rel<AccelerationUnit> logWeightedAverageSpeedTimes2 =
                new MutableDoubleScalar.Rel<AccelerationUnit>(Math.sqrt(this.a.getSI() * this.b.getSI()),
                        AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAverageSpeedTimes2.multiply(2); // don't forget the times 2
        DoubleScalar.Rel<SpeedUnit> dV =
                (null == closestLeader) ? new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND) : DoubleScalar
                        .minus(gtu.getLongitudinalVelocity(thisEvaluationTime),
                                closestLeader.getLongitudinalVelocity(thisEvaluationTime)).immutable();
        // System.out.println("dV is " + dV);
        // System.out.println(" v is " + gtu.speed(thisEvaluationTime));
        // System.out.println("s0 is " + this.s0);
        DoubleScalar.Rel<LengthUnit> sStar =
                DoubleScalar.plus(
                        DoubleScalar.plus(this.s0,
                        Calc.speedTimesTime(gtu.getLongitudinalVelocity(thisEvaluationTime), this.tSafe)).immutable(),
                        Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(myCurrentSpeed,
                                logWeightedAverageSpeedTimes2.immutable()))).immutable();
        if (sStar.getSI() < 0) // Negative value should be treated as 0
        {
            sStar = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        // System.out.println("s* is " + sStar);
        double distanceIncentive = 1 - Math.pow(sStar.getSI() / shortestHeadway.getSI(), 2);
        MutableDoubleScalar.Abs<AccelerationUnit> newAcceleration = new MutableDoubleScalar.Abs<AccelerationUnit>(this.a);
        newAcceleration.multiply(Math.min(speedIncentive, distanceIncentive));
        // System.out.println("distanceIncentive is " + distanceIncentive);
        // System.out.println("newAcceleration is " + newAcceleration);
        MutableDoubleScalar.Abs<TimeUnit> nextEvaluationTime = thisEvaluationTime.mutable();
        nextEvaluationTime.incrementBy(this.stepSize);
        return new GTUFollowingModelResult(newAcceleration.immutable(), nextEvaluationTime.immutable(), 0);
    }

    /**
     * {@inheritDoc}
     * @throws RemoteException
     */
    @Override
    public final GTUFollowingModelResult computeLaneChangeAndAcceleration(final LaneBasedGTU<?> gtu,
            final Collection<? extends LaneBasedGTU<?>> sameLaneGTUs,
            final Collection<? extends LaneBasedGTU<?>> preferredLaneGTUs,
            final Collection<? extends LaneBasedGTU<?>> nonPreferredLaneGTUs, final DoubleScalar.Abs<SpeedUnit> speedLimit,
            final double preferredLaneRouteIncentive, final double nonPreferredLaneRouteIncentive) throws RemoteException
    {
        System.out.println(String.format(
                "Route desire to merge to preferredLane: %.3f, route desire to merge to overtakingLane: %.3f",
                preferredLaneRouteIncentive, nonPreferredLaneRouteIncentive));
        DoubleScalar.Abs<SpeedUnit> vAntStraight = anticipatedSpeed(speedLimit, gtu, sameLaneGTUs);
        double aGain =
                (this.a.getSI() - Math.max(computeAcceleration(gtu, sameLaneGTUs, speedLimit).getAcceleration().getSI(), 0))
                        / this.a.getSI();
        System.out.println(String.format("aGain: %.3f", aGain));
        double nonPreferredLaneSpeedIncentive = 0;
        if (null != nonPreferredLaneGTUs)
        {
            nonPreferredLaneSpeedIncentive =
                    aGain
                            * DoubleScalar.minus(anticipatedSpeed(speedLimit, gtu, nonPreferredLaneGTUs), vAntStraight)
                                    .getSI() / this.vGain.getSI();
        }
        double dBias = 0;
        double preferredLaneSpeedIncentive = 0;
        if (null != preferredLaneGTUs)
        {
            DoubleScalar.Abs<SpeedUnit> vAnt = anticipatedSpeed(speedLimit, gtu, preferredLaneGTUs);
            if (vAnt.getSI() > this.vCong.getSI())
            {
                preferredLaneSpeedIncentive = DoubleScalar.minus(vAnt, vAntStraight).getSI() / this.vGain.getSI();
                if (preferredLaneSpeedIncentive > 0)
                {
                    preferredLaneSpeedIncentive = 0; // changing lane to overtake "on the right" is not permitted
                }
            }
            else
            {
                preferredLaneSpeedIncentive = aGain * DoubleScalar.minus(vAnt, vAntStraight).getSI() / this.vGain.getSI();
            }
            // FIXME: comparing double values for equality is not "reliable"
            if (preferredLaneRouteIncentive >= 0 && vAnt.getSI() == vDes(gtu, speedLimit).getSI())
            {
                dBias = this.dFree;
            }
        }
        System.out.println(String.format(
                "Speed desire to merge to preferredLane: %.3f, speed desire to merge to overtakingLane: %.3f",
                preferredLaneSpeedIncentive, nonPreferredLaneSpeedIncentive));
        double preferredLaneChangeDesire = totalDesire(preferredLaneRouteIncentive, preferredLaneSpeedIncentive, dBias);
        double nonPreferredLaneChangeDesire = totalDesire(nonPreferredLaneRouteIncentive, nonPreferredLaneSpeedIncentive, 0);
        boolean preferredLaneChangeOK = checkLaneChange(gtu, preferredLaneGTUs, preferredLaneChangeDesire, speedLimit);
        GTUFollowingModelResult straight = computeAcceleration(gtu, sameLaneGTUs, speedLimit);
        System.out.println(String.format(
                "Total desire to merge to preferredLane: %.3f, total desire to merge to overtakingLane: %.3f",
                preferredLaneChangeDesire, nonPreferredLaneChangeDesire));
        return null;
    }

    /**
     * Check if a lane change can be executed safely.
     * @param gtu GTU; the GTU that considers changing lane
     * @param gtusInOtherLane Collection&lt;GTU&gt; the gtu in the adjacent lane
     * @param desire double; the desire to change into the adjacent lane
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit in the adjacent lane
     * @return boolean; true if the lane change can be performed; false if the lane change should not be performed
     * @throws RemoteException in case simulation time cannot be retrieved
     */
    private boolean checkLaneChange(final LaneBasedGTU<?> gtu, final Collection<? extends LaneBasedGTU<?>> gtusInOtherLane,
            final double desire, final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        // Find the new leader and follower
        LaneBasedGTU<?> leader = null;
        DoubleScalar.Rel<LengthUnit> leaderHeadway = null;
        LaneBasedGTU<?> follower = null;
        DoubleScalar.Rel<LengthUnit> followerHeadway = null;
        for (LaneBasedGTU<?> gtuInOtherLane : gtusInOtherLane)
        {
            DoubleScalar.Rel<LengthUnit> headway = gtu.headway(gtuInOtherLane);
            if (headway.getSI() > 0)
            {
                if (null == leader || headway.getSI() < leaderHeadway.getSI())
                {
                    leader = gtuInOtherLane;
                    leaderHeadway = headway;
                }
            }
            else
            {
                if (null == follower || headway.getSI() > followerHeadway.getSI())
                {
                    follower = gtuInOtherLane;
                    followerHeadway = headway;
                }
            }
        }
        Collection<LaneBasedGTU<?>> leaders = new ArrayList<LaneBasedGTU<?>>();
        if (null != leader)
        {
            leaders.add(leader);
        }
        DoubleScalar.Abs<AccelerationUnit> gtuAcceleration = computeAcceleration(gtu, leaders, speedLimit).getAcceleration();
        if (gtuAcceleration.getSI() < this.b.getSI())
        {
            return false; // leader would be too close
        }
        if (null != follower)
        {
            Collection<LaneBasedGTU<?>> referenceGTUGroup = new ArrayList<LaneBasedGTU<?>>();
            referenceGTUGroup.add(gtu);
            DoubleScalar.Abs<AccelerationUnit> otherGTUAcceleration =
                    computeAcceleration(follower, referenceGTUGroup, speedLimit).getAcceleration();
            // This assumes that the follower also uses IDMPlus (which is not unreasonable)
            if (otherGTUAcceleration.getSI() < this.b.getSI())
            {
                return false; // follower would be too close
            }
        }
        return true;
    }

    /**
     * Compute the total desire to change lane. <br>
     * Equation 1 in the LMRS paper.
     * @param routeIncentive double; the desire to make the lane change in order to follow the route
     * @param speedIncentive double; the desire to make the lane change in order to gain speed (or avoid reducing speed)
     * @param bias double; the desire to make the lane change in order to get to into the preferred lane
     * @return double; the total desire to change lane
     */
    private double totalDesire(final double routeIncentive, final double speedIncentive, final double bias)
    {
        return routeIncentive + theta(routeIncentive, speedIncentive) * (speedIncentive + bias);
    }

    /**
     * Combine the lane change incentives into one value that describes how <i>voluntary</i> a potential lane change is. <br>
     * Equation 11 in the LRMS paper.
     * @param routeIncentive double; the lane change incentive to follow the intended route
     * @param speedIncentive double; the lane change incentive for gaining (or not so much reducing) speed
     * @return double; a value between 0 (no urge to change lane) and 1 (maximum urge to change lane)
     */
    private double theta(final double routeIncentive, final double speedIncentive)
    {
        if (routeIncentive * speedIncentive < 0 && Math.abs(routeIncentive) >= this.dCoop)
        {
            return 0;
        }
        else if (routeIncentive * speedIncentive >= 0 || Math.abs(routeIncentive) <= this.dSync)
        {
            return 1;
        }
        else
        {
            return (this.dCoop - Math.abs(routeIncentive)) / (this.dCoop - this.dSync);
        }
    }

    /**
     * Compute the anticipated speed if driving in a lane among a set of other gtus.
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speedLimit in the lane
     * @param gtu GTU; the reference gtu (only the current position of the reference gtu is used)
     * @param leaders Collection&lt;GTU&gt;; the set of other gtus
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the anticipated speed
     */
    private DoubleScalar.Abs<SpeedUnit> anticipatedSpeed(final DoubleScalar.Abs<SpeedUnit> speedLimit,
            final LaneBasedGTU<?> gtu, final Collection<? extends LaneBasedGTU<?>> leaders) throws RemoteException
    {
        DoubleScalar.Abs<SpeedUnit> result = speedLimit;
        DoubleScalar.Rel<LengthUnit> frontPositionOfGTU =
                gtu.positionOfFront(gtu.getNextEvaluationTime()).getLongitudinalPosition();
        for (LaneBasedGTU<?> leader : leaders)
        {
            DoubleScalar.Rel<LengthUnit> headway =
                    DoubleScalar.minus(leader.positionOfRear(gtu.getNextEvaluationTime()).getLongitudinalPosition(),
                            frontPositionOfGTU).immutable();
            if (headway.getSI() < 0)
            {
                continue;
            }
            if (headway.getSI() > this.x0.getSI())
            {
                continue;
            }
            DoubleScalar.Abs<SpeedUnit> leaderSpeed = leader.getLongitudinalVelocity(gtu.getNextEvaluationTime());
            if (leaderSpeed.getSI() < result.getSI())
            {
                result = new DoubleScalar.Abs<SpeedUnit>(leaderSpeed.getSI(), leaderSpeed.getUnit());
            }
        }
        return result;
    }

    /**
     * @return simulator.
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

}
