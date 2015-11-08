package org.opentrafficsim.road.gtu.following;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;

/**
 * The Intelligent Driver Model by Treiber, Hennecke and Helbing.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1408 $, $LastChangedDate: 2015-09-24 15:17:25 +0200 (Thu, 24 Sep 2015) $, by $Author: pknoppers $,
 *          initial version 19 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDM extends AbstractGTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final Length.Rel s0;

    /** Maximum longitudinal acceleration [m/s^2]. */
    private final Acceleration a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final Acceleration b;

    /** Safe time headway. */
    private final Time.Rel tSafe;

    /**
     * Time slot size used by IDM (not defined in the paper, but 0.5s is a reasonable trade-off between computational speed and
     * accuracy).
     */
    private final Time.Rel stepSize = new Time.Rel(0.5, TimeUnit.SECOND);

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the speed
     * limit, etc.).
     */
    private final double delta;

    /**
     * Construct a new IDM car following model with reasonable values (reasonable for passenger cars).
     */
    public IDM()
    {
        this.a = new Acceleration(1.56, AccelerationUnit.METER_PER_SECOND_2);
        this.b = new Acceleration(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new Length.Rel(3, LengthUnit.METER);
        this.tSafe = new Time.Rel(1.2, TimeUnit.SECOND);
        this.delta = 1d;
    }

    /**
     * Construct a new IDM car following model.
     * @param a DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum acceleration of a stationary vehicle (normal value is 1
     *            m/s/s)
     * @param b DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum deemed-safe deceleration (this is a positive value).
     *            Normal value is 1.5 m/s/s.
     * @param s0 DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum stationary headway (normal value is 2 m)
     * @param tSafe DoubleScalar.Rel&lt;TimeUnit&gt;; the minimum time-headway (normal value is 1s)
     * @param delta double; the speed limit adherence (1.0; mean free speed equals the speed limit; 1.1: mean free speed equals
     *            110% of the speed limit; etc.)
     */
    public IDM(final Acceleration a, final Acceleration b, final Length.Rel s0, final Time.Rel tSafe, final double delta)
    {
        this.a = a;
        this.b = b;
        this.s0 = s0;
        this.tSafe = tSafe;
        this.delta = delta;
    }

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit
     * @param followerMaximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the maximum speed that the follower can drive
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private Speed vDes(final Speed speedLimit, final Speed followerMaximumSpeed)
    {
        return new Speed(Math.min(this.delta * speedLimit.getSI(), followerMaximumSpeed.getSI()), SpeedUnit.SI);
    }

    /** {@inheritDoc} */
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
        final Speed leaderSpeed, final Length.Rel headway, final Speed speedLimit)
    {
        // System.out.println("Applying IDM for " + follower + " headway is " + headway);
        // dV is the approach speed
        Speed dV = followerSpeed.minus(leaderSpeed);
        Acceleration aFree =
            new Acceleration(this.a.getSI()
                * (1 - Math.pow(followerSpeed.getSI() / vDes(speedLimit, followerMaximumSpeed).getSI(), 4)),
                AccelerationUnit.SI);
        if (Double.isNaN(aFree.getSI()))
        {
            aFree = new Acceleration(0, AccelerationUnit.SI);
        }
        Acceleration logWeightedAccelerationTimes2 =
            new Acceleration(Math.sqrt(this.a.getSI() * this.b.getSI()), AccelerationUnit.SI).multiplyBy(2);
        // don't forget the times 2

        // TODO compute logWeightedAccelerationTimes2 only once per run
        Length.Rel right =
            followerSpeed.multiplyBy(this.tSafe).plus(
                dV.multiplyBy(followerSpeed.divideBy(logWeightedAccelerationTimes2)));
        /*-
            Calc.speedTimesTime(followerSpeed, this.tSafe).plus(
                Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(followerSpeed, logWeightedAccelerationTimes2)));
         */
        if (right.getSI() < 0)
        {
            // System.out.println("Fixing negative right");
            right = new Length.Rel(0, LengthUnit.SI);
        }
        Length.Rel sStar = this.s0.plus(right);
        if (sStar.getSI() < 0) // Negative value should be treated as 0
        {
            System.out.println("sStar is negative");
            sStar = new Length.Rel(0, LengthUnit.SI);
        }
        // System.out.println("s* is " + sStar);
        Acceleration aInteraction =
            new Acceleration(-Math.pow(this.a.getSI() * sStar.getSI() / headway.getSI(), 2), AccelerationUnit.SI);
        Acceleration newAcceleration = aFree.plus(aInteraction);
        if (newAcceleration.getSI() * this.stepSize.getSI() + followerSpeed.getSI() < 0)
        {
            // System.out.println("Limiting deceleration to prevent moving backwards");
            newAcceleration = new Acceleration(-followerSpeed.getSI() / this.stepSize.getSI(), AccelerationUnit.SI);
        }
        // System.out.println("newAcceleration is " + newAcceleration);
        return newAcceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Rel getStepSize()
    {
        return new Time.Rel(this.stepSize);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration maximumSafeDeceleration()
    {
        return this.b;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "IDM";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(),
            this.a.getSI(), this.b.getSI(), this.s0.getSI(), this.tSafe.getSI(), this.delta);
    }

}
