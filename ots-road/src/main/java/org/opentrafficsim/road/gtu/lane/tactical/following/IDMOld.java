package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;
import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

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
public class IDMOld extends AbstractGTUFollowingModelMobil implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141119L;

    /** Preferred net longitudinal distance when stopped [m]. */
    private final Length s0;

    /** Maximum longitudinal acceleration [m/s^2]. */
    private final Acceleration a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final Acceleration b;

    /** Safe time headway. */
    private final Duration tSafe;

    /**
     * Time slot size used by IDM (not defined in the paper, but 0.5s is a reasonable trade-off between computational speed and
     * accuracy).
     */
    private final Duration stepSize = new Duration(0.5, TimeUnit.SECOND);

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the speed
     * limit, etc.).
     */
    private final double delta;

    /**
     * Construct a new IDM car following model with reasonable values (reasonable for passenger cars).
     */
    public IDMOld()
    {
        this.a = new Acceleration(1.56, AccelerationUnit.METER_PER_SECOND_2);
        this.b = new Acceleration(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new Length(3, LengthUnit.METER);
        this.tSafe = new Duration(1.2, TimeUnit.SECOND);
        this.delta = 1d;
    }

    /**
     * Construct a new IDM car following model.
     * @param a Acceleration; the maximum acceleration of a stationary vehicle (normal value is 1 m/s/s)
     * @param b Acceleration; the maximum deemed-safe deceleration (this is a positive value). Normal value is 1.5 m/s/s.
     * @param s0 Length; the minimum stationary headway (normal value is 2 m)
     * @param tSafe Duration; the minimum time-headway (normal value is 1s)
     * @param delta double; the speed limit adherence (1.0; mean free speed equals the speed limit; 1.1: mean free speed equals
     *            110% of the speed limit; etc.)
     */
    public IDMOld(final Acceleration a, final Acceleration b, final Length s0, final Duration tSafe, final double delta)
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
     * @param followerMaximumSpeed Speed; the maximum speed that the follower can drive
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private Speed vDes(final Speed speedLimit, final Speed followerMaximumSpeed)
    {
        return new Speed(Math.min(this.delta * speedLimit.getSI(), followerMaximumSpeed.getSI()), SpeedUnit.SI);
    }

    /** {@inheritDoc} */
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
        final Speed leaderSpeed, final Length headway, final Speed speedLimit)
    {
        return computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit, this.stepSize);
    }

    /** {@inheritDoc} */
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
        final Speed leaderSpeed, final Length headway, final Speed speedLimit, final Duration stepSize)
    {
        // TODO maxDistance
        // dV is the approach speed
        Speed dV = followerSpeed.minus(leaderSpeed);
        double sStar =
            this.s0.si + followerSpeed.si * this.tSafe.si + dV.si * followerSpeed.si
                / (2.0 * Math.sqrt(this.a.si * this.b.si));
        if (sStar < 0.0 && headway.si < 0.0)
        {
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        sStar = sStar >= 0.0 ? sStar : 0.0;
        double s = headway.si > 0.0 ? headway.si : 1E-99;
        Acceleration aInteraction = new Acceleration(this.a.si * (sStar / s) * (sStar / s), AccelerationUnit.SI);
        Acceleration aFree =
            new Acceleration(this.a.si * (1.0 - Math.pow(followerSpeed.si / vDes(speedLimit, followerMaximumSpeed).si, 4)),
                AccelerationUnit.SI);
        // limit deceleration for free term (= aFree)
        if (aFree.si < -0.5)
        {
            aFree = new Acceleration(-0.5, AccelerationUnit.SI);
        }
        Acceleration newAcceleration = aFree.minus(aInteraction);
        if (newAcceleration.si * stepSize.si + followerSpeed.si < 0)
        {
            newAcceleration = new Acceleration(-followerSpeed.si / stepSize.si, AccelerationUnit.SI);
        }
        return newAcceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Duration getStepSize()
    {
        return this.stepSize;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getMaximumSafeDeceleration()
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

    // The following is inherited from CarFollowingModel

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final SpeedInfo speedInfo)
        throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed)
        throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedInfo speedInfo, final SortedMap<Length, Speed> leaders) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDMOld [s0=" + this.s0 + ", a=" + this.a + ", b=" + this.b + ", tSafe=" + this.tSafe + ", stepSize="
            + this.stepSize + ", delta=" + this.delta + "]";
    }

}
