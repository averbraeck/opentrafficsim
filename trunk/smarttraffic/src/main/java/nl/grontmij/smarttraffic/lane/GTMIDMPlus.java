package nl.grontmij.smarttraffic.lane;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.conversions.Calc;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.MutableDoubleScalar;
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
public class GTMIDMPlus extends AbstractGTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final DoubleScalar.Rel<LengthUnit> s0;

    /** Longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final DoubleScalar.Abs<AccelerationUnit> b;

    /** Safe time headway. */
    private final DoubleScalar.Rel<TimeUnit> tSafe;

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean free speed equals 110% of the speed
     * limit, etc.).
     */
    private final double delta;

    /**
     * Time slot size used by IDMPlus by (not defined in the paper, but 0.5s is a reasonable trade-off between computational
     * speed and accuracy).
     */
    private final DoubleScalar.Rel<TimeUnit> stepSize = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND);

    /**
     * Construct a new IDM+ car following model with reasonable values (reasonable for passenger cars). <br>
     * These values are from <b>Integrated Lane Change Model with Relaxation and Synchronization</b> by Wouter J. Schakel,
     * Victor L. Knoop, and Bart van Arem, published in Transportation Research Record: Journal of the Transportation Research
     * Board, No. 2316, Transportation Research Board of the National Academies, Washington, D.C., 2012, pp. 47–57.
     */
    public GTMIDMPlus()
    {
        this.a = new DoubleScalar.Abs<AccelerationUnit>(2.09, AccelerationUnit.METER_PER_SECOND_2); // 1.56
        this.b = new DoubleScalar.Abs<AccelerationUnit>(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new DoubleScalar.Rel<LengthUnit>(1.5, LengthUnit.METER); // 3
        this.tSafe = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND); // 1.2
        this.delta = 1d;
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
    public GTMIDMPlus(final DoubleScalar.Abs<AccelerationUnit> a, final DoubleScalar.Abs<AccelerationUnit> b,
        final DoubleScalar.Rel<LengthUnit> s0, final DoubleScalar.Rel<TimeUnit> tSafe, final double delta)
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
    private DoubleScalar.Rel<SpeedUnit> vDes(final DoubleScalar.Abs<SpeedUnit> speedLimit,
        final DoubleScalar.Abs<SpeedUnit> followerMaximumSpeed)
    {
        return new DoubleScalar.Rel<SpeedUnit>(Math.min(this.delta * speedLimit.getSI(), followerMaximumSpeed.getSI()),
            SpeedUnit.METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    public final DoubleScalar.Abs<AccelerationUnit> computeAcceleration(final DoubleScalar.Abs<SpeedUnit> followerSpeed,
        final DoubleScalar.Abs<SpeedUnit> followerMaximumSpeed, final DoubleScalar.Abs<SpeedUnit> leaderSpeed,
        final DoubleScalar.Rel<LengthUnit> headway, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        double leftComponent = 1 - Math.pow(followerSpeed.getSI() / vDes(speedLimit, followerMaximumSpeed).getSI(), 4);
        if (Double.isNaN(leftComponent))
        {
            leftComponent = 0;
        }
        // if (leftComponent < 0)
        // {
        // System.out.println("leftComponent is " + leftComponent);
        // }
        MutableDoubleScalar.Rel<AccelerationUnit> logWeightedAccelerationTimes2 =
            new MutableDoubleScalar.Rel<AccelerationUnit>(Math.sqrt(this.a.getSI() * this.b.getSI()),
                AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAccelerationTimes2.multiplyBy(2); // don't forget the times 2

        DoubleScalar.Rel<SpeedUnit> dV = DoubleScalar.minus(followerSpeed, leaderSpeed).immutable();
        // System.out.println("dV is " + dV);
        // System.out.println(" v is " + gtu.speed(thisEvaluationTime));
        // System.out.println("s0 is " + this.s0);
        DoubleScalar.Rel<LengthUnit> sStar =
            DoubleScalar.plus(
                DoubleScalar.plus(this.s0, Calc.speedTimesTime(followerSpeed, this.tSafe)).immutable(),
                Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(followerSpeed, logWeightedAccelerationTimes2
                    .immutable()))).immutable();
        if (sStar.getSI() < 0)
        {
            // Negative value should be treated as 0? This is NOT in the LMRS paper
            // Without this "fix" a higher speed of the leader may cause a lower acceleration (which is crazy)
            sStar = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        // System.out.println("s* is " + sStar);

        double rightComponent = 1 - Math.pow(sStar.getSI() / headway.getSI(), 2);
        // if (rightComponent < 0)
        // {
        // System.out.println("rightComponent is " + rightComponent);
        // }
        MutableDoubleScalar.Abs<AccelerationUnit> newAcceleration = new MutableDoubleScalar.Abs<AccelerationUnit>(this.a);
        newAcceleration.multiplyBy(Math.min(leftComponent, rightComponent));
        // System.out.println("newAcceleration is " + newAcceleration);
        if (newAcceleration.getSI() * this.stepSize.getSI() + followerSpeed.getSI() < 0)
        {
            // System.out.println("Preventing follower from driving backwards " + follower);
            newAcceleration =
                new MutableDoubleScalar.Abs<AccelerationUnit>(-followerSpeed.getSI() / this.stepSize.getSI(),
                    AccelerationUnit.METER_PER_SECOND_2);
        }
        // System.out.println("newAcceleration is " + newAcceleration);
        return newAcceleration.immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<TimeUnit> getStepSize()
    {
        return new DoubleScalar.Rel<TimeUnit>(this.stepSize);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> maximumSafeDeceleration()
    {
        return this.b;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "IDM+";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(), this.a
            .getSI(), this.b.getSI(), this.s0.getSI(), this.tSafe.getSI(), this.delta);
    }

}
