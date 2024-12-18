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
 * IDMPlus implements the <i>Integrated Lane Change Model with Relaxation and Synchronization</i> as published by Wouter J.
 * Schakel, Bart van Arem, Member, IEEE, and Bart D. Netten. 2012. <br>
 * There are two nasty type setting errors in equation 7 in this published version of the paper. Both times an equals sign
 * (<cite>=</cite>) after <cite>a<sub>gain</sub></cite> should <b>not</b> be there.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class IdmPlusOld extends AbstractGtuFollowingModelMobil implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140704L;

    /** Preferred net longitudinal distance when stopped [m]. */
    private final Length s0;

    /** Longitudinal acceleration [m/s^2]. */
    private Acceleration a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final Acceleration b;

    /** Safe time headway. */
    private Duration tSafe;

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean free speed equals 110% of the speed
     * limit, etc.).
     */
    private double delta;

    /**
     * Default step size used by IDMPlus (not defined in the paper, but 0.5s is a reasonable trade-off between computational
     * speed and accuracy).
     */
    private static final Duration DEFAULT_STEP_SIZE = new Duration(0.5, DurationUnit.SECOND);

    /**
     * Construct a new IDM+ car following model with reasonable values (reasonable for passenger cars). <br>
     * These values are from <b>Integrated Lane Change Model with Relaxation and Synchronization</b> by Wouter J. Schakel,
     * Victor L. Knoop, and Bart van Arem, published in Transportation Research Record: Journal of the Transportation Research
     * Board, No. 2316, Transportation Research Board of the National Academies, Washington, D.C., 2012, pp. 47–57.
     */
    public IdmPlusOld()
    {
        this.a = new Acceleration(1.56, AccelerationUnit.METER_PER_SECOND_2);
        this.b = new Acceleration(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new Length(3, LengthUnit.METER);
        this.tSafe = new Duration(1.2, DurationUnit.SECOND);
        this.delta = 1d;
    }

    /**
     * Construct a new IDMPlus car following model.
     * @param a the maximum acceleration of a stationary vehicle (normal value is 1 m/s/s)
     * @param b the maximum deemed-safe deceleration (this is a positive value)
     * @param s0 the minimum stationary headway
     * @param tSafe the minimum time-headway
     * @param delta the speed limit adherence (1.0; mean free speed equals the speed limit; 1.1: mean free speed equals 110% of
     *            the speed limit; etc.)
     */
    public IdmPlusOld(final Acceleration a, final Acceleration b, final Length s0, final Duration tSafe, final double delta)
    {
        this.a = a;
        this.b = b;
        this.s0 = s0;
        this.tSafe = tSafe;
        this.delta = delta;
    }

    /**
     * Desired speed (taking into account the urge to drive a little faster or slower than the posted speed limit).
     * @param speedLimit the speed limit
     * @param followerMaximumSpeed the maximum speed that the follower can drive
     * @return the desired speed
     */
    private Speed vDes(final Speed speedLimit, final Speed followerMaximumSpeed)
    {
        return new Speed(Math.min(this.delta * speedLimit.getSI(), followerMaximumSpeed.getSI()), SpeedUnit.SI);
    }

    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit)
    {
        return computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit, DEFAULT_STEP_SIZE);
    }

    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit, final Duration stepSize)
    {
        // TODO maxDistance
        double leftComponent = 1 - Math.pow(followerSpeed.getSI() / vDes(speedLimit, followerMaximumSpeed).getSI(), 4);
        if (Double.isNaN(leftComponent))
        {
            leftComponent = 0;
        }
        // limit deceleration for free term (= leftComponent)
        if (leftComponent * this.a.si < -0.5)
        {
            leftComponent = -0.5 / this.a.si;
        }
        Acceleration logWeightedAccelerationTimes2 =
                new Acceleration(Math.sqrt(this.a.getSI() * this.b.getSI()), AccelerationUnit.SI).times(2);
        // don't forget the times 2

        Speed dV = followerSpeed.minus(leaderSpeed);
        Length sStar = this.s0.plus(followerSpeed.times(this.tSafe))
                .plus(dV.times(followerSpeed.divide(logWeightedAccelerationTimes2)));

        /*-
        this.s0.plus(Calc.speedTimesTime(followerSpeed, this.tSafe)).plus(
        Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(followerSpeed, logWeightedAccelerationTimes2)));
         */
        if (sStar.getSI() < 0)
        {
            // Negative value should be treated as 0? This is NOT in the LMRS paper
            // Without this "fix" a higher speed of the leader may cause a lower acceleration (which is crazy)
            sStar = new Length(0, LengthUnit.SI);
        }

        double rightComponent = 1 - Math.pow(sStar.getSI() / headway.getSI(), 2);
        Acceleration newAcceleration = new Acceleration(this.a).times(Math.min(leftComponent, rightComponent));
        if (newAcceleration.getSI() * stepSize.getSI() + followerSpeed.getSI() < 0)
        {
            newAcceleration = new Acceleration(-followerSpeed.getSI() / stepSize.getSI(), AccelerationUnit.SI);
        }
        return newAcceleration;
    }

    @Override
    public final Duration getStepSize()
    {
        return DEFAULT_STEP_SIZE;
    }

    @Override
    public final Acceleration getMaximumSafeDeceleration()
    {
        return this.b;
    }

    @Override
    public final String getName()
    {
        return "IDM+";
    }

    @Override
    public final String getLongName()
    {
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(),
                this.a.getSI(), this.b.getSI(), this.s0.getSI(), this.tSafe.getSI(), this.delta);
    }

    @Override
    public final void setA(final Acceleration a)
    {
        this.a = a;
    }

    @Override
    public final void setT(final Duration t)
    {
        this.tSafe = t;
    }

    @Override
    public final void setFspeed(final double fSpeed)
    {
        this.delta = fSpeed;
    }

    // The following is inherited from CarFollowingModel

    @Override
    public final Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        throw new UnsupportedOperationException("Old car-following model does not support desired speed.");
    }

    @Override
    public final Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
    {
        throw new UnsupportedOperationException("Old car-following model does not support desired headway.");
    }

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

    @Override
    public final String toString()
    {
        return "IDMPlusOld [s0=" + this.s0 + ", a=" + this.a + ", b=" + this.b + ", tSafe=" + this.tSafe + ", delta="
                + this.delta + ", stepSize=" + DEFAULT_STEP_SIZE + "]";
    }

}
