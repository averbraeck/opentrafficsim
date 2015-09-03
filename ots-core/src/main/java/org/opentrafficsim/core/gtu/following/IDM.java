package org.opentrafficsim.core.gtu.following;

import org.djunits.unit.AccelerationUnit;
import org.opentrafficsim.core.units.calc.Calc;

/**
 * The Intelligent Driver Model by Treiber, Hennecke and Helbing.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 19 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDM extends AbstractGTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final Length.Rel s0;

    /** Maximum longitudinal acceleration [m/s^2]. */
    private final Acceleration.Abs a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final Acceleration.Abs b;

    /** Safe time headway. */
    private final Time.Rel tSafe;

    /**
     * Time slot size used by IDM (not defined in the paper, but 0.5s is a reasonable trade-off between computational speed and
     * accuracy).
     */
    private final Time.Rel stepSize = new Time.Rel(0.5, SECOND);

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
        this.a = new Acceleration.Abs(1.56, METER_PER_SECOND_2);
        this.b = new Acceleration.Abs(2.09, METER_PER_SECOND_2);
        this.s0 = new Length.Rel(3, METER);
        this.tSafe = new Time.Rel(1.2, SECOND);
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
    public IDM(final Acceleration.Abs a, final Acceleration.Abs b, final Length.Rel s0, final Time.Rel tSafe,
        final double delta)
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
    private Speed.Rel vDes(final Speed.Abs speedLimit, final Speed.Abs followerMaximumSpeed)
    {
        return new Speed.Rel(Math.min(this.delta * speedLimit.getSI(), followerMaximumSpeed.getSI()), METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    public final Acceleration.Abs computeAcceleration(final Speed.Abs followerSpeed, final Speed.Abs followerMaximumSpeed,
        final Speed.Abs leaderSpeed, final Length.Rel headway, final Speed.Abs speedLimit)
    {
        // System.out.println("Applying IDM for " + follower + " headway is " + headway);
        // dV is the approach speed
        Speed.Rel dV = followerSpeed.minus(leaderSpeed);
        Acceleration.Abs aFree =
            new Acceleration.Abs(this.a.getSI()
                * (1 - Math.pow(followerSpeed.getSI() / vDes(speedLimit, followerMaximumSpeed).getSI(), 4)),
                METER_PER_SECOND_2);
        if (Double.isNaN(aFree.getSI()))
        {
            aFree = new Acceleration.Abs(0, AccelerationUnit.SI);
        }
        Acceleration.Rel logWeightedAccelerationTimes2 =
            new Acceleration.Rel(Math.sqrt(this.a.getSI() * this.b.getSI()), METER_PER_SECOND_2).multiplyBy(2); // don't forget
                                                                                                                // the times 2
        // TODO compute logWeightedAccelerationTimes2 only once per run
        /*
         * Length.Rel sStar = DoubleScalar.plus( DoubleScalar.plus(this.s0,
         * Calc.speedTimesTime(follower.getLongitudinalVelocity(thisEvaluationTime), this.tSafe)) , Calc.speedTimesTime( dV,
         * Calc.speedDividedByAcceleration(followerCurrentSpeed, logWeightedAccelerationTimes2)));
         */
        Length.Rel right =
            Calc.speedTimesTime(followerSpeed, this.tSafe).plus(
                Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(followerSpeed, logWeightedAccelerationTimes2)));
        if (right.getSI() < 0)
        {
            // System.out.println("Fixing negative right");
            right = new Length.Rel(0, METER);
        }
        Length.Rel sStar = this.s0.plus(right);
        if (sStar.getSI() < 0) // Negative value should be treated as 0
        {
            System.out.println("sStar is negative");
            sStar = new Length.Rel(0, METER);
        }
        // System.out.println("s* is " + sStar);
        Acceleration.Rel aInteraction =
            new Acceleration.Rel(-Math.pow(this.a.getSI() * sStar.getSI() / headway.getSI(), 2), METER_PER_SECOND_2);
        Acceleration.Abs newAcceleration = aFree.plus(aInteraction);
        if (newAcceleration.getSI() * this.stepSize.getSI() + followerSpeed.getSI() < 0)
        {
            // System.out.println("Limiting deceleration to prevent moving backwards");
            newAcceleration = new Acceleration.Abs(-followerSpeed.getSI() / this.stepSize.getSI(), METER_PER_SECOND_2);
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
    public final Acceleration.Abs maximumSafeDeceleration()
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
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(), this.a
            .getSI(), this.b.getSI(), this.s0.getSI(), this.tSafe.getSI(), this.delta);
    }

}
