package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * The Intelligent Driver Model by Treiber, Hennecke and Helbing.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class IdmOld extends AbstractGtuFollowingModelMobil implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141119L;

    /** Preferred net longitudinal distance when stopped [m]. */
    private final Length s0;

    /** Maximum longitudinal acceleration [m/s^2]. */
    private Acceleration a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final Acceleration b;

    /** Safe time headway. */
    private Duration tSafe;

    /**
     * Default step size used by IDM (not defined in the paper, but 0.5s is a reasonable trade-off between computational speed
     * and accuracy).
     */
    private static final Duration DEFAULT_STEP_SIZE = new Duration(0.5, DurationUnit.SECOND);

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the speed
     * limit, etc.).
     */
    private double delta;

    /**
     * Construct a new IDM car following model with reasonable values (reasonable for passenger cars).
     */
    public IdmOld()
    {
        this.a = new Acceleration(1.56, AccelerationUnit.METER_PER_SECOND_2);
        this.b = new Acceleration(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new Length(3, LengthUnit.METER);
        this.tSafe = new Duration(1.2, DurationUnit.SECOND);
        this.delta = 1.0;
    }

    /**
     * Construct a new IDM car following model.
     * @param a Acceleration; the maximum acceleration of a stationary vehicle (normal value is 1.56 m/s/s)
     * @param b Acceleration; the maximum deemed-safe deceleration (this is a positive value). Normal value is 2.09 m/s/s.
     * @param s0 Length; the minimum stationary headway (normal value is 3 m)
     * @param tSafe Duration; the minimum time-headway (normal value is 1.2 s)
     * @param delta double; the speed limit adherence (1.0; mean free speed equals the speed limit; 1.1: mean free speed equals
     *            110% of the speed limit; etc.)
     */
    public IdmOld(final Acceleration a, final Acceleration b, final Length s0, final Duration tSafe, final double delta)
    {
        this.a = a;
        this.b = b;
        this.s0 = s0;
        this.tSafe = tSafe;
        this.delta = delta;
    }

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param speedLimit Speed; the speed limit
     * @param followerMaximumSpeed Speed; the maximum speed that the follower can drive
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private Speed vDes(final Speed speedLimit, final Speed followerMaximumSpeed)
    {
        return new Speed(Math.min(this.delta * speedLimit.getSI(), followerMaximumSpeed.getSI()), SpeedUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit)
    {
        return computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit, DEFAULT_STEP_SIZE);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit, final Duration stepSize)
    {
        // TODO maxDistance
        // dV is the approach speed
        Speed dV = followerSpeed.minus(leaderSpeed);
        double sStar = this.s0.si + followerSpeed.si * this.tSafe.si
                + dV.si * followerSpeed.si / (2.0 * Math.sqrt(this.a.si * this.b.si));
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
        return DEFAULT_STEP_SIZE;
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
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(),
                this.a.getSI(), this.b.getSI(), this.s0.getSI(), this.tSafe.getSI(), this.delta);
    }

    /** {@inheritDoc} */
    @Override
    public final void setA(final Acceleration a)
    {
        this.a = a;
    }

    /** {@inheritDoc} */
    @Override
    public final void setT(final Duration t)
    {
        this.tSafe = t;
    }

    /** {@inheritDoc} */
    @Override
    public final void setFspeed(final double fSpeed)
    {
        this.delta = fSpeed;
    }

    // The following is inherited from CarFollowingModel

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        throw new UnsupportedOperationException("Old car-following model does not support desired speed.");
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
    {
        throw new UnsupportedOperationException("Old car-following model does not support desired headway.");
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final Parameters parameters, final Speed speed,
            final SpeedLimitInfo speedInfo, final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        Length headway;
        Speed leaderSpeed;
        if (leaders.isEmpty())
        {
            headway = new Length(Double.MAX_VALUE, LengthUnit.SI);
            leaderSpeed = speed;
        }
        else
        {
            Headway leader = leaders.first();
            headway = leader.getDistance();
            leaderSpeed = leader.getSpeed();
        }
        return this.computeAcceleration(speed, speedInfo.getSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED), leaderSpeed, headway,
                speedInfo.getSpeedInfo(SpeedLimitTypes.FIXED_SIGN));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDMOld [s0=" + this.s0 + ", a=" + this.a + ", b=" + this.b + ", tSafe=" + this.tSafe + ", stepSize="
                + DEFAULT_STEP_SIZE + ", delta=" + this.delta + "]";
    }

}
