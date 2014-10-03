package org.opentrafficsim.car.following;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.opentrafficsim.car.Car;
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
 * @param <Line>
 */
public class IDMPlus<Line, C extends Car> implements CarFollowingModel<C>
{
    /** Preferred net longitudinal distance when stopped [m]. */
    protected final DoubleScalar.Rel<LengthUnit> s0 = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);

    /** Longitudinal acceleration [m/s^2]. */
    protected final DoubleScalar.Abs<AccelerationUnit> a = new DoubleScalar.Abs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Regular longitudinal acceleration [m/s^2]. */
    protected final DoubleScalar.Abs<AccelerationUnit> aMin = new DoubleScalar.Abs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    protected final DoubleScalar.Abs<AccelerationUnit> b = new DoubleScalar.Abs<AccelerationUnit>(2.09,
            AccelerationUnit.METER_PER_SECOND_2);

    /**
     * Maximum deceleration when actual speed is more than desired speed (v &gt; vdes). (Should be a positive value even though
     * it is a <b>de</b>celeration.) [m/s^2]
     */
    protected final DoubleScalar.Abs<AccelerationUnit> b0 = new DoubleScalar.Abs<AccelerationUnit>(0.5,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal regular following headway [s]. */
    protected final DoubleScalar.Rel<TimeUnit> tMax = new DoubleScalar.Rel<TimeUnit>(1.2, TimeUnit.SECOND);

    /** LMRS free lane change threshold. (Value between 0 and dSync) */
    protected final double dFree = .365;

    /** LMRS synchronized lane change threshold. (Value between dFree and dCoop) */
    protected final double dSync = .577;

    /** LMRS cooperative lane change threshold. (Value between dSync and 1.0) */
    protected final double dCoop = .788;

    /** LMRS mandatory lane change time [s]. */
    protected final DoubleScalar.Rel<TimeUnit> t0 = new DoubleScalar.Rel<TimeUnit>(43, TimeUnit.SECOND);

    /** LMRS mandatory lane change distance [m]. */
    protected final DoubleScalar.Rel<LengthUnit> x0 = new DoubleScalar.Rel<LengthUnit>(295, LengthUnit.METER);

    /** LMRS speed gain [m/s] for full desire. (The paper specifies this value in [km/h]) */
    protected final DoubleScalar.Abs<SpeedUnit> vGain = new DoubleScalar.Abs<SpeedUnit>(69.6, SpeedUnit.KM_PER_HOUR);

    /** LMRS critical speed [m/s] for a speed gain in the right lane. (The paper specifies this value in [km/h]) */
    protected final DoubleScalar.Abs<SpeedUnit> vCong = new DoubleScalar.Abs<SpeedUnit>(60, SpeedUnit.KM_PER_HOUR);

    /** Safe time headway. */
    protected final DoubleScalar.Rel<TimeUnit> tSafe = new DoubleScalar.Rel<TimeUnit>(1.6, TimeUnit.SECOND);

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the speed
     * limit, etc.).
     */
    protected final double delta = 1.0;

    /**
     * Time slot size used by IDMPlus (not defined in the paper, but 0.5s is a reasonable trade-off between computational speed
     * and accuracy).
     */
    protected final DoubleScalar.Rel<TimeUnit> stepSize = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND);

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param car Car; the Car whose desired speed must be returned
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private DoubleScalar.Rel<SpeedUnit> vDes(final C car, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        return new DoubleScalar.Rel<SpeedUnit>(Math.min(this.delta * speedLimit.getValueSI(), car.vMax().getValueSI()),
                SpeedUnit.METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final CarFollowingModelResult computeAcceleration(final C car, final Collection<C> leaders,
            final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = car.getNextEvaluationTime();
        // System.out.println("evaluation time is " + thisEvaluationTime);
        // System.out.println("vDes is " + vDes);
        DoubleScalar.Abs<LengthUnit> myFrontPosition = car.positionOfFront(thisEvaluationTime);
        // System.out.println("myFrontPosition is " + myFrontPosition);
        DoubleScalar.Rel<LengthUnit> shortestHeadway = new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        C closestLeader = null;
        for (C leader : leaders)
        {
            DoubleScalar.Rel<LengthUnit> s =
                    MutableDoubleScalar.minus(leader.positionOfRear(thisEvaluationTime), myFrontPosition).immutable();
            // System.out.println("s is " + s);
            if (s.getValueSI() < 0)
            {
                continue; // Ignore cars that are behind this car
            }
            if (s.getValueSI() < shortestHeadway.getValueSI())
            {
                shortestHeadway = s;
                closestLeader = leader;
            }
        }
        // System.out.println("shortestHeadway is " + shortestHeadway);
        DoubleScalar.Rel<SpeedUnit> myCurrentSpeed = car.getVelocity(thisEvaluationTime);
        double speedIncentive = 1 - Math.pow(myCurrentSpeed.getValueSI() / vDes(car, speedLimit).getValueSI(), 4);
        // System.out.println("speedIncentive is " + speedIncentive);
        MutableDoubleScalar.Rel<AccelerationUnit> logWeightedAverageSpeedTimes2 =
                new MutableDoubleScalar.Rel<AccelerationUnit>(Math.sqrt(this.a.getValueSI() * this.b.getValueSI()),
                        AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAverageSpeedTimes2.multiply(2); // don't forget the times 2
        DoubleScalar.Rel<SpeedUnit> dV =
                (null == closestLeader) ? new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND) : MutableDoubleScalar
                        .minus(car.getVelocity(thisEvaluationTime), closestLeader.getVelocity(thisEvaluationTime)).immutable();
        // System.out.println("dV is " + dV);
        // System.out.println(" v is " + car.speed(thisEvaluationTime));
        // System.out.println("s0 is " + this.s0);
        DoubleScalar.Rel<LengthUnit> sStar =
                MutableDoubleScalar.plus(
                        LengthUnit.METER,
                        this.s0,
                        Calc.speedTimesTime(car.getVelocity(thisEvaluationTime), this.tSafe),
                        Calc.speedTimesTime(dV,
                                Calc.speedDividedByAcceleration(myCurrentSpeed, logWeightedAverageSpeedTimes2.immutable())))
                        .immutable();
        if (sStar.getValueSI() < 0) // Negative value should be treated as 0
        {
            sStar = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        // System.out.println("s* is " + sStar);
        double distanceIncentive = 1 - Math.pow(sStar.getValueSI() / shortestHeadway.getValueSI(), 2);
        MutableDoubleScalar.Abs<AccelerationUnit> newAcceleration = new MutableDoubleScalar.Abs<AccelerationUnit>(this.a);
        newAcceleration.multiply(Math.min(speedIncentive, distanceIncentive));
        // System.out.println("distanceIncentive is " + distanceIncentive);
        // System.out.println("newAcceleration is " + newAcceleration);
        MutableDoubleScalar.Abs<TimeUnit> nextEvaluationTime = thisEvaluationTime.mutable();
        nextEvaluationTime.add(this.stepSize);
        return new CarFollowingModelResult(newAcceleration.immutable(), nextEvaluationTime.immutable(), 0);
    }

    /** {@inheritDoc} 
     * @throws RemoteException */
    @Override
    public final CarFollowingModelResult computeLaneChangeAndAcceleration(final C car, final Collection<C> sameLaneCars,
            final Collection<C> preferredLaneCars, final Collection<C> nonPreferredLaneCars,
            final DoubleScalar.Abs<SpeedUnit> speedLimit, final double preferredLaneRouteIncentive,
            final double nonPreferredLaneRouteIncentive) throws RemoteException
    {
        System.out.println(String.format(
                "Route desire to merge to preferredLane: %.3f, route desire to merge to overtakingLane: %.3f",
                preferredLaneRouteIncentive, nonPreferredLaneRouteIncentive));
        DoubleScalar.Abs<SpeedUnit> vAntStraight = anticipatedSpeed(speedLimit, car, sameLaneCars);
        double aGain =
                (this.a.getValueSI() - Math
                        .max(computeAcceleration(car, sameLaneCars, speedLimit).acceleration.getValueSI(), 0))
                        / this.a.getValueSI();
        System.out.println(String.format("aGain: %.3f", aGain));
        double nonPreferredLaneSpeedIncentive = 0;
        if (null != nonPreferredLaneCars)
        {
            nonPreferredLaneSpeedIncentive =
                    aGain
                            * MutableDoubleScalar.minus(anticipatedSpeed(speedLimit, car, nonPreferredLaneCars), vAntStraight)
                                    .getValueSI() / this.vGain.getValueSI();
        }
        double dBias = 0;
        double preferredLaneSpeedIncentive = 0;
        if (null != preferredLaneCars)
        {
            DoubleScalar.Abs<SpeedUnit> vAnt = anticipatedSpeed(speedLimit, car, preferredLaneCars);
            if (vAnt.getValueSI() > this.vCong.getValueSI())
            {
                preferredLaneSpeedIncentive =
                        MutableDoubleScalar.minus(vAnt, vAntStraight).getValueSI() / this.vGain.getValueSI();
                if (preferredLaneSpeedIncentive > 0)
                {
                    preferredLaneSpeedIncentive = 0; // changing lane to overtake "on the right" is not permitted
                }
            }
            else
            {
                preferredLaneSpeedIncentive =
                        aGain * MutableDoubleScalar.minus(vAnt, vAntStraight).getValueSI() / this.vGain.getValueSI();
            }
            // FIXME: comparing double values for equality is not "reliable"
            if (preferredLaneRouteIncentive >= 0 && vAnt.getValueSI() == vDes(car, speedLimit).getValueSI())
            {
                dBias = this.dFree;
            }
        }
        System.out.println(String.format(
                "Speed desire to merge to preferredLane: %.3f, speed desire to merge to overtakingLane: %.3f",
                preferredLaneSpeedIncentive, nonPreferredLaneSpeedIncentive));
        double preferredLaneChangeDesire = totalDesire(preferredLaneRouteIncentive, preferredLaneSpeedIncentive, dBias);
        double nonPreferredLaneChangeDesire = totalDesire(nonPreferredLaneRouteIncentive, nonPreferredLaneSpeedIncentive, 0);
        boolean preferredLaneChangeOK = checkLaneChange(car, preferredLaneCars, preferredLaneChangeDesire, speedLimit);
        CarFollowingModelResult straight = computeAcceleration(car, sameLaneCars, speedLimit);
        System.out.println(String.format(
                "Total desire to merge to preferredLane: %.3f, total desire to merge to overtakingLane: %.3f",
                preferredLaneChangeDesire, nonPreferredLaneChangeDesire));
        return null;
    }

    /**
     * Check if a lane change can be executed safely.
     * @param car Car; the Car that considers changing lane
     * @param carsInOtherLane Collection&lt;Car&gt; the car in the adjacent lane
     * @param desire double; the desire to change into the adjacent lane
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit in the adjacent lane
     * @return boolean; true if the lane change can be performed; false if the lane change should not be performed
     * @throws RemoteException 
     */
    private boolean checkLaneChange(final C car, final Collection<C> carsInOtherLane, final double desire,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        // Find the new leader and follower
        C leader = null;
        DoubleScalar.Rel<LengthUnit> leaderHeadway = null;
        C follower = null;
        DoubleScalar.Rel<LengthUnit> followerHeadway = null;
        for (C c : carsInOtherLane)
        {
            DoubleScalar.Rel<LengthUnit> headway = car.headway(c);
            if (headway.getValueSI() > 0)
            {
                if (null == leader || headway.getValueSI() < leaderHeadway.getValueSI())
                {
                    leader = c;
                    leaderHeadway = headway;
                }
            }
            else
            {
                if (null == follower || headway.getValueSI() > followerHeadway.getValueSI())
                {
                    follower = c;
                    followerHeadway = headway;
                }
            }
        }
        Collection<C> leaders = new ArrayList<C>();
        if (null != leader)
        {
            leaders.add(leader);
        }
        DoubleScalar.Abs<AccelerationUnit> carAcceleration = computeAcceleration(car, leaders, speedLimit).acceleration;
        if (carAcceleration.getValueSI() < this.b.getValueSI())
        {
            return false; // leader would be too close
        }
        if (null != follower)
        {
            Collection<C> referenceCarGroup = new ArrayList<C>();
            referenceCarGroup.add(car);
            DoubleScalar.Abs<AccelerationUnit> otherCarAcceleration =
                    computeAcceleration(follower, referenceCarGroup, speedLimit).acceleration;
            // This assumes that the follower also uses IDMPlus (which is not unreasonable)
            if (otherCarAcceleration.getValueSI() < this.b.getValueSI())
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
     * Compute the anticipated speed if driving in a lane among a set of other cars.
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speedLimit in the lane
     * @param car Car; the reference car (only the current position of the reference car is used)
     * @param leaders Collection&lt;Car&gt;; the set of other cars
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the anticipated speed
     */
    private DoubleScalar.Abs<SpeedUnit> anticipatedSpeed(final DoubleScalar.Abs<SpeedUnit> speedLimit, final C car,
            final Collection<C> leaders)
    {
        DoubleScalar.Abs<SpeedUnit> result = speedLimit;
        DoubleScalar.Abs<LengthUnit> frontPositionOfCar = car.positionOfFront(car.getNextEvaluationTime());
        for (C leader : leaders)
        {
            DoubleScalar.Rel<LengthUnit> headway =
                    MutableDoubleScalar.minus(leader.positionOfRear(car.getNextEvaluationTime()), frontPositionOfCar)
                            .immutable();
            if (headway.getValueSI() < 0)
            {
                continue;
            }
            if (headway.getValueSI() > this.x0.getValueSI())
            {
                continue;
            }
            DoubleScalar.Rel<SpeedUnit> leaderSpeed = leader.getVelocity(car.getNextEvaluationTime());
            if (leaderSpeed.getValueSI() < result.getValueSI())
            {
                result = new DoubleScalar.Abs<SpeedUnit>(leaderSpeed.getValueSI(), leaderSpeed.getUnit());
            }
        }
        return result;
    }

}
