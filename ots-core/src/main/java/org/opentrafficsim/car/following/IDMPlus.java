package org.opentrafficsim.car.following;

import java.util.ArrayList;
import java.util.Collection;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * IDMPlus implements the <i>Integrated Lane Change Model with Relaxation and Synchronization</i> as published by Wouter
 * J. Schakel, Victor L. Knoop and Bart van Arem in Transportation Research Record No 2316 pp 47-57, Washington D.C.,
 * 2012. <br />
 * There are two nasty type setting errors in equation 7 in this published version of the paper. Both times an equals
 * sign (<cite>=</cite>) after <cite>a<sub>gain</sub></cite> should <b>not</b> be there.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <Line>
 */
public class IDMPlus<Line> implements CarFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    protected final DoubleScalarRel<LengthUnit> s0 = new DoubleScalarRel<LengthUnit>(3, LengthUnit.METER);

    /** Longitudinal acceleration [m/s^2]. */
    protected final DoubleScalarAbs<AccelerationUnit> a = new DoubleScalarAbs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Regular longitudinal acceleration [m/s^2]. */
    protected final DoubleScalarAbs<AccelerationUnit> aMin = new DoubleScalarAbs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    protected final DoubleScalarAbs<AccelerationUnit> b = new DoubleScalarAbs<AccelerationUnit>(2.09,
            AccelerationUnit.METER_PER_SECOND_2);

    /**
     * Maximum deceleration when actual speed is more than desired speed (v > vdes). (Should be a positive value even
     * though it is a <b>de</b>celeration.) [m/s^2]
     */
    protected final DoubleScalarAbs<AccelerationUnit> b0 = new DoubleScalarAbs<AccelerationUnit>(0.5,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal regular following headway [s]. */
    protected final DoubleScalarRel<TimeUnit> tMax = new DoubleScalarRel<TimeUnit>(1.2, TimeUnit.SECOND);

    /** LMRS free lane change threshold. (Value between 0 and dSync) */
    protected final double dFree = .365;

    /** LMRS synchronized lane change threshold. (Value between dFree and dCoop) */
    protected final double dSync = .577;

    /** LMRS cooperative lane change threshold. (Value between dSync and 1.0) */
    protected final double dCoop = .788;

    /** LMRS mandatory lane change time [s]. */
    protected final DoubleScalarRel<TimeUnit> t0 = new DoubleScalarRel<TimeUnit>(43, TimeUnit.SECOND);

    /** LMRS mandatory lane change distance [m]. */
    protected final DoubleScalarRel<LengthUnit> x0 = new DoubleScalarRel<LengthUnit>(295, LengthUnit.METER);

    /** LMRS speed gain [m/s] for full desire. (The paper specifies this value in [km/h]) */
    protected final DoubleScalarAbs<SpeedUnit> vGain = new DoubleScalarAbs<SpeedUnit>(69.6, SpeedUnit.KM_PER_HOUR);

    /** LMRS critical speed [m/s] for a speed gain in the right lane. (The paper specifies this value in [km/h]) */
    protected final DoubleScalarAbs<SpeedUnit> vCong = new DoubleScalarAbs<SpeedUnit>(60, SpeedUnit.KM_PER_HOUR);

    /** Safe time headway. */
    protected final DoubleScalarRel<TimeUnit> tSafe = new DoubleScalarRel<TimeUnit>(1.6, TimeUnit.SECOND);

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the
     * speed limit, etc.).
     */
    protected final double delta = 1.0;

    /**
     * Time slot size used by IDMPlus (not defined in the paper, but 0.5s is a reasonable trade-off between
     * computational speed and accuracy).
     */
    protected final DoubleScalarRel<TimeUnit> stepSize = new DoubleScalarRel<TimeUnit>(0.5, TimeUnit.SECOND);

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param car Car; the Car whose desired speed must be returned
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private DoubleScalarRel<SpeedUnit> vDes(final Car car, final DoubleScalarAbs<SpeedUnit> speedLimit)
    {
        return new DoubleScalarRel<SpeedUnit>(Math.min(this.delta * speedLimit.getValueSI(), car.vMax().getValueSI()),
                SpeedUnit.METER_PER_SECOND);
    }

    /**
     * @see org.opentrafficsim.car.following.CarFollowingModel#computeAcceleration(org.opentrafficsim.car.Car,
     *      java.util.Set, org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs)
     */
    @Override
    public CarFollowingModelResult computeAcceleration(final Car car, final Collection<Car> leaders,
            final DoubleScalarAbs<SpeedUnit> speedLimit)
    {
        DoubleScalarAbs<TimeUnit> thisEvaluationTime = car.getNextEvaluationTime();
        // System.out.println("evaluation time is " + thisEvaluationTime);
        // System.out.println("vDes is " + vDes);
        DoubleScalarAbs<LengthUnit> myFrontPosition = car.positionOfFront(thisEvaluationTime);
        // System.out.println("myFrontPosition is " + myFrontPosition);
        DoubleScalarRel<LengthUnit> shortestHeadway =
                new DoubleScalarRel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        Car closestLeader = null;
        for (Car leader : leaders)
        {
            DoubleScalarRel<LengthUnit> s =
                    DoubleScalar.minus(leader.positionOfRear(thisEvaluationTime), myFrontPosition);
            // System.out.println("s is " + s);
            if (s.getValueSI() < 0)
                continue; // Ignore cars that are behind this car
            if (s.getValueSI() < shortestHeadway.getValueSI())
            {
                shortestHeadway = s;
                closestLeader = leader;
            }
        }
        // System.out.println("shortestHeadway is " + shortestHeadway);
        DoubleScalarRel<SpeedUnit> myCurrentSpeed = car.getVelocity(thisEvaluationTime);
        double speedIncentive = 1 - Math.pow(myCurrentSpeed.getValueSI() / vDes(car, speedLimit).getValueSI(), 4);
        // System.out.println("speedIncentive is " + speedIncentive);
        DoubleScalarRel<AccelerationUnit> logWeightedAverageSpeedTimes2 =
                new DoubleScalarRel<AccelerationUnit>(Math.sqrt(this.a.getValueSI() * this.b.getValueSI()),
                        AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAverageSpeedTimes2.multiply(2); // don't forget the times 2
        DoubleScalarRel<SpeedUnit> dV =
                (null == closestLeader) ? new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND) : DoubleScalar
                        .minus(car.getVelocity(thisEvaluationTime), closestLeader.getVelocity(thisEvaluationTime));
        // System.out.println("dV is " + dV);
        // System.out.println(" v is " + car.speed(thisEvaluationTime));
        // System.out.println("s0 is " + this.s0);
        DoubleScalarRel<LengthUnit> sStar =
                DoubleScalar.plus(
                        LengthUnit.METER,
                        this.s0,
                        Calc.speedTimesTime(car.getVelocity(thisEvaluationTime), this.tSafe),
                        Calc.speedTimesTime(dV,
                                Calc.speedDividedByAcceleration(myCurrentSpeed, logWeightedAverageSpeedTimes2)));
        if (sStar.getValueSI() < 0) // Negative value should be treated as 0
            sStar = new DoubleScalarRel<LengthUnit>(0, LengthUnit.METER);
        // System.out.println("s* is " + sStar);
        double distanceIncentive = 1 - Math.pow(sStar.getValueSI() / shortestHeadway.getValueSI(), 2);
        DoubleScalarAbs<AccelerationUnit> newAcceleration = new DoubleScalarAbs<AccelerationUnit>(this.a);
        newAcceleration.multiply(Math.min(speedIncentive, distanceIncentive));
        // System.out.println("distanceIncentive is " + distanceIncentive);
        // System.out.println("newAcceleration is " + newAcceleration);
        DoubleScalarAbs<TimeUnit> nextEvaluationTime = thisEvaluationTime;
        nextEvaluationTime.add(this.stepSize);
        return new CarFollowingModelResult(newAcceleration, nextEvaluationTime, 0);
    }

    /**
     * @see org.opentrafficsim.car.following.CarFollowingModel#computeLaneChangeAndAcceleration(org.opentrafficsim.car.Car,
     *      java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection,
     *      java.util.Collection, org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs, double, double)
     */
    @Override
    public CarFollowingModelResult computeLaneChangeAndAcceleration(final Car car, final Collection<Car> sameLaneCars,
            final Collection<Car> preferredLaneCars, final Collection<Car> nonPreferredLaneCars,
            final DoubleScalarAbs<SpeedUnit> speedLimit, double preferredLaneRouteIncentive,
            double nonPreferredLaneRouteIncentive)
    {
        System.out.println(String.format(
                "Route desire to merge to preferredLane: %.3f, route desire to merge to overtakingLane: %.3f",
                preferredLaneRouteIncentive, nonPreferredLaneRouteIncentive));
        DoubleScalarAbs<SpeedUnit> vAntStraight = anticipatedSpeed(speedLimit, car, sameLaneCars);
        double aGain =
                (this.a.getValueSI() - Math.max(
                        computeAcceleration(car, sameLaneCars, speedLimit).acceleration.getValueSI(), 0))
                        / this.a.getValueSI();
        System.out.println(String.format("aGain: %.3f", aGain));
        double nonPreferredLaneSpeedIncentive = 0;
        if (null != nonPreferredLaneCars)
            nonPreferredLaneSpeedIncentive =
                    aGain
                            * DoubleScalar.minus(anticipatedSpeed(speedLimit, car, nonPreferredLaneCars), vAntStraight)
                                    .getValueSI() / this.vGain.getValueSI();
        double dBias = 0;
        double preferredLaneSpeedIncentive = 0;
        if (null != preferredLaneCars)
        {
            DoubleScalarAbs<SpeedUnit> vAnt = anticipatedSpeed(speedLimit, car, preferredLaneCars);
            if (vAnt.getValueSI() > this.vCong.getValueSI())
            {
                preferredLaneSpeedIncentive =
                        DoubleScalar.minus(vAnt, vAntStraight).getValueSI() / this.vGain.getValueSI();
                if (preferredLaneSpeedIncentive > 0)
                    preferredLaneSpeedIncentive = 0; // changing lane to overtake "on the right" is not permitted
            }
            else
                preferredLaneSpeedIncentive =
                        aGain * DoubleScalar.minus(vAnt, vAntStraight).getValueSI() / this.vGain.getValueSI();
            // FIXME: comparing double values for equality is not "reliable"
            if (preferredLaneRouteIncentive >= 0 && vAnt.getValueSI() == vDes(car, speedLimit).getValueSI())
                dBias = this.dFree;
        }
        System.out.println(String.format(
                "Speed desire to merge to preferredLane: %.3f, speed desire to merge to overtakingLane: %.3f",
                preferredLaneSpeedIncentive, nonPreferredLaneSpeedIncentive));
        double preferredLaneChangeDesire = totalDesire(preferredLaneRouteIncentive, preferredLaneSpeedIncentive, dBias);
        double nonPreferredLaneChangeDesire =
                totalDesire(nonPreferredLaneRouteIncentive, nonPreferredLaneSpeedIncentive, 0);
        boolean preferredLaneChangeOK =
                checkLaneChange(car, preferredLaneCars, preferredLaneChangeDesire, speedLimit);
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
     */
    private boolean checkLaneChange(Car car, Collection<Car> carsInOtherLane, double desire,
            DoubleScalarAbs<SpeedUnit> speedLimit)
    {
        // Find the new leader and follower
        Car leader = null;
        DoubleScalarRel<LengthUnit> leaderHeadway = null;
        Car follower = null;
        DoubleScalarRel<LengthUnit> followerHeadway = null;
        for (Car c : carsInOtherLane)
        {
            DoubleScalarRel<LengthUnit> headway = car.headway(c);
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
        Collection<Car> leaders = new ArrayList<Car>();
        if (null != leader)
            leaders.add(leader);
        DoubleScalarAbs<AccelerationUnit> carAcceleration = computeAcceleration(car, leaders, speedLimit).acceleration;
        if (carAcceleration.getValueSI() < this.b.getValueSI())
            return false; // leader would be too close
        if (null != follower)
        {
            Collection<Car> referenceCarGroup = new ArrayList<Car>();
            referenceCarGroup.add(car);
            DoubleScalarAbs<AccelerationUnit> otherCarAcceleration =
                    computeAcceleration(follower, referenceCarGroup, speedLimit).acceleration;
            // This assumes that the follower also uses IDMPlus (which is not unreasonable)
            if (otherCarAcceleration.getValueSI() < this.b.getValueSI())
                return false; // follower would be too close
        }
        return true;
    }

    /**
     * Compute the total desire to change lane. <br />
     * Equation 1 in the LMRS paper.
     * @param routeIncentive double; the desire to make the lane change in order to follow the route
     * @param speedIncentive double; the desire to make the lane change in order to gain speed (or avoid reducing speed)
     * @param bias double; the desire to make the lane change in order to get to into the preferred lane
     * @return double; the total desire to change lane
     */
    private double totalDesire(double routeIncentive, double speedIncentive, double bias)
    {
        return routeIncentive + theta(routeIncentive, speedIncentive) * (speedIncentive + bias);
    }

    /**
     * Combine the lane change incentives into one value that describes how <i>voluntary</i> a potential lane change is. <br />
     * Equation 11 in the LRMS paper.
     * @param routeIncentive double; the lane change incentive to follow the intended route
     * @param speedIncentive double; the lane change incentive for gaining (or not so much reducing) speed
     * @return double; a value between 0 (no urge to change lane) and 1 (maximum urge to change lane)
     */
    private double theta(double routeIncentive, double speedIncentive)
    {
        if (routeIncentive * speedIncentive < 0 && Math.abs(routeIncentive) >= this.dCoop)
            return 0;
        else if (routeIncentive * speedIncentive >= 0 || Math.abs(routeIncentive) <= this.dSync)
            return 1;
        else
            return (this.dCoop - Math.abs(routeIncentive)) / (this.dCoop - this.dSync);

    }

    /**
     * Compute the anticipated speed if driving in a lane among a set of other cars.
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speedLimit in the lane
     * @param car Car; the reference car (only the current position of the reference car is used)
     * @param leaders Collection&lt;Car&gt;; the set of other cars
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the anticipated speed
     */
    private DoubleScalarAbs<SpeedUnit> anticipatedSpeed(DoubleScalarAbs<SpeedUnit> speedLimit, Car car,
            Collection<Car> leaders)
    {
        DoubleScalarAbs<SpeedUnit> result = speedLimit;
        DoubleScalarAbs<LengthUnit> frontPositionOfCar = car.positionOfFront(car.getNextEvaluationTime());
        for (Car leader : leaders)
        {
            DoubleScalarRel<LengthUnit> headway =
                    DoubleScalar.minus(leader.positionOfRear(car.getNextEvaluationTime()), frontPositionOfCar);
            if (headway.getValueSI() < 0)
                continue;
            if (headway.getValueSI() > this.x0.getValueSI())
                continue;
            DoubleScalarRel<SpeedUnit> leaderSpeed = leader.getVelocity(car.getNextEvaluationTime());
            if (leaderSpeed.getValueSI() < result.getValueSI())
                result = new DoubleScalarAbs<SpeedUnit>(leaderSpeed.getValueSI(), leaderSpeed.getUnit());
        }
        return result;
    }

}
