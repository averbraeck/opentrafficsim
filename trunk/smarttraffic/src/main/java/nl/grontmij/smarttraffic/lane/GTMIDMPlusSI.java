package nl.grontmij.smarttraffic.lane;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.gtu.following.AbstractGTUFollowingModel;

/**
 * IDMPlus implements the <i>Integrated Lane Change Model with Relaxation and Synchronization</i> as published by Wouter J.
 * Schakel, Bart van Arem, Member, IEEE, and Bart D. Netten. 2012. <br>
 * There are two nasty type setting errors in equation 7 in this published version of the paper. Both times an equals sign
 * (<cite>=</cite>) after <cite>a<sub>gain</sub></cite> should <b>not</b> be there.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTMIDMPlusSI extends AbstractGTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final double s0;

    /** Longitudinal acceleration [m/s^2]. */
    private final double a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final double b;

    /** Safe time headway. */
    private final double tSafe;

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean free speed equals 110% of the speed
     * limit, etc.).
     */
    private final double delta;

    /**
     * Time slot size used by IDMPlus by (not defined in the paper, but 0.5s is a reasonable trade-off between computational
     * speed and accuracy).
     */
    private final double stepSize = 0.5;

    /** store 2 * sqrt(a*b). */
    private final double logWeightedAccelerationTimes2;

    /**
     * Construct a new IDM+ car following model with reasonable values (reasonable for passenger cars). <br>
     * These values are from <b>Integrated Lane Change Model with Relaxation and Synchronization</b> by Wouter J. Schakel,
     * Victor L. Knoop, and Bart van Arem, published in Transportation Research Record: Journal of the Transportation Research
     * Board, No. 2316, Transportation Research Board of the National Academies, Washington, D.C., 2012, pp. 47–57.
     */
    public GTMIDMPlusSI()
    {
        this.a = 2.09; // 1.56
        this.b = 2.09;
        this.s0 = 1.5; // 3
        this.tSafe = 0.5; // 1.2
        this.delta = 1d;
        this.logWeightedAccelerationTimes2 = 2.0 * Math.sqrt(this.a * this.b);
    }

    /**
     * Construct a new IDMPlus car following model.
     * @param a DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum acceleration of a stationary vehicle (normal value is 1
     *            m/s/s)
     * @param b DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum deemed-safe deceleration (this is a positive value)
     * @param s0 DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum stationary headway
     * @param tSafe DoubleScalar.Rel&lt;TimeUnit&gt;; the minimum time-headway
     * @param delta double; the speed limit adherence (1.0; mean free speed equals the speed limit; 1.1: mean free speed equals
     *            110% of the speed limit; etc.)
     */
    public GTMIDMPlusSI(final Acceleration.Abs a, final Acceleration.Abs b,
        final Length.Rel s0, final DoubleScalar.Rel<TimeUnit> tSafe, final double delta)
    {
        this.a = a.getSI();
        this.b = b.getSI();
        this.s0 = s0.getSI();
        this.tSafe = tSafe.getSI();
        this.delta = delta;
        this.logWeightedAccelerationTimes2 = 2.0 * Math.sqrt(this.a * this.b);
    }

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param speedLimit in SI; the speed limit
     * @param followerMaximumSpeed in SI; the maximum speed that the follower can drive
     * @return the desired speed in SI units
     */
    private double vDes(final double speedLimit, final double followerMaximumSpeed)
    {
        return Math.min(this.delta * speedLimit, followerMaximumSpeed);
    }

    /** {@inheritDoc} */
    public final Acceleration.Abs computeAcceleration(final Speed.Abs followerSpeed,
        final Speed.Abs followerMaximumSpeed, final Speed.Abs leaderSpeed,
        final Length.Rel headway, final Speed.Abs speedLimit)
    {
        double followerSpeedSI = followerSpeed.getSI();
        double followerMaximumSpeedSI = followerMaximumSpeed.getSI();
        double leaderSpeedSI = leaderSpeed.getSI();
        double headwaySI = headway.getSI();
        double speedLimitSI = speedLimit.getSI();

        double leftComponent = 1 - Math.pow(followerSpeedSI / vDes(speedLimitSI, followerMaximumSpeedSI), 4);
        if (Double.isNaN(leftComponent))
        {
            leftComponent = 0;
        }
        double dV = followerSpeedSI - leaderSpeedSI;
        double sStar =
            (this.s0 + followerSpeedSI * this.tSafe) + (dV * followerSpeedSI / this.logWeightedAccelerationTimes2);
        if (sStar < 0)
        {
            // Negative value should be treated as 0? This is NOT in the LMRS paper
            // Without this "fix" a higher speed of the leader may cause a lower acceleration (which is crazy)
            sStar = 0.0;
        }

        double rightComponent = 1 - Math.pow(sStar / headwaySI, 2);
        double newAcceleration = this.a * Math.min(leftComponent, rightComponent);
        if (newAcceleration * this.stepSize + followerSpeedSI < 0)
        {
            newAcceleration = -followerSpeedSI / this.stepSize;
        }
        return new Acceleration.Abs(newAcceleration, AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Rel getStepSize()
    {
        return new Time.Rel(this.stepSize, TimeUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration.Abs maximumSafeDeceleration()
    {
        return new Acceleration.Abs(this.b, AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "IDM+ (SI)";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(), this.a,
            this.b, this.s0, this.tSafe, this.delta);
    }

}
