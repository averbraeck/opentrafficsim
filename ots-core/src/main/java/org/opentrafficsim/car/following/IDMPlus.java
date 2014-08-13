package org.opentrafficsim.car.following;

import java.util.Collection;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
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
     * though it is a <b>de</b>celeration.)
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
     * @see org.opentrafficsim.car.following.CarFollowingModel#computeAcceleration(org.opentrafficsim.car.Car,
     *      java.util.Set, org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs)
     */
    @Override
    public CarFollowingModelResult computeAcceleration(final Car car, final Collection<Car> leaders,
            final DoubleScalarAbs<SpeedUnit> speedLimit)
    {
        DoubleScalarAbs<TimeUnit> thisEvaluationTime = car.getNextEvaluationTime();
        //System.out.println("evaluation time is " + thisEvaluationTime);
        DoubleScalarRel<SpeedUnit> vDes =
                new DoubleScalarRel<SpeedUnit>(Math.min(this.delta * speedLimit.getValueSI(), car.vMax().getValueSI()),
                        SpeedUnit.METER_PER_SECOND);
        //System.out.println("vDes is " + vDes);
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
        //System.out.println("shortestHeadway is " + shortestHeadway);
        DoubleScalarRel<SpeedUnit> myCurrentSpeed = car.getVelocity(thisEvaluationTime);
        double speedIncentive = 1 - Math.pow(myCurrentSpeed.getValueSI() / vDes.getValueSI(), 4);
        //System.out.println("speedIncentive is " + speedIncentive);
        DoubleScalarRel<AccelerationUnit> logWeightedAverageSpeedTimes2 =
                new DoubleScalarRel<AccelerationUnit>(Math.sqrt(this.a.getValueSI() * this.b.getValueSI()),
                        AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAverageSpeedTimes2.multiply(2); // don't forget the times 2
        DoubleScalarRel<SpeedUnit> dV =
                (null == closestLeader) ? new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND) : DoubleScalar
                        .minus(car.getVelocity(thisEvaluationTime), closestLeader.getVelocity(thisEvaluationTime));
        //System.out.println("dV is " + dV);
        //System.out.println(" v is " + car.speed(thisEvaluationTime));
        //System.out.println("s0 is " + this.s0);
        DoubleScalarRel<LengthUnit> sStar =
                DoubleScalar.plus(LengthUnit.METER, this.s0,
                        Calc.speedTimesTime(car.getVelocity(thisEvaluationTime), this.tSafe),
                        Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(myCurrentSpeed, logWeightedAverageSpeedTimes2)));
        if (sStar.getValueSI() < 0) // Negative value should be treated as 0
            sStar = new DoubleScalarRel<LengthUnit>(0, LengthUnit.METER);
        //System.out.println("s* is " + sStar);
        double distanceIncentive = 1 - Math.pow(sStar.getValueSI() / shortestHeadway.getValueSI(), 2);
        DoubleScalarAbs<AccelerationUnit> newAcceleration = new DoubleScalarAbs<AccelerationUnit>(this.a);
        newAcceleration.multiply(Math.min(speedIncentive, distanceIncentive));
        //System.out.println("distanceIncentive is " + distanceIncentive);
        //System.out.println("newAcceleration is " + newAcceleration);
        DoubleScalarAbs<TimeUnit> nextEvaluationTime = thisEvaluationTime;
        nextEvaluationTime.add(this.stepSize);
        return new CarFollowingModelResult(newAcceleration, nextEvaluationTime);
    }

}
