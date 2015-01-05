package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * IDMPlus implements the <i>Integrated Lane Change Model with Relaxation and Synchronization</i> as published by Wouter J.
 * Schakel, Bart van Arem, Member, IEEE, and Bart D. Netten. 2012. <br>
 * There are two nasty type setting errors in equation 7 in this published version of the paper. Both times an equals sign
 * (<cite>=</cite>) after <cite>a<sub>gain</sub></cite> should <b>not</b> be there.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlus implements GTUFollowingModel
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
    public IDMPlus()
    {
        this.a = new DoubleScalar.Abs<AccelerationUnit>(1.56, AccelerationUnit.METER_PER_SECOND_2);
        this.b = new DoubleScalar.Abs<AccelerationUnit>(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);
        this.tSafe = new DoubleScalar.Rel<TimeUnit>(1.2, TimeUnit.SECOND);
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
    public IDMPlus(final DoubleScalar.Abs<AccelerationUnit> a, final DoubleScalar.Abs<AccelerationUnit> b,
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
     * @param follower GTU; the GTU whose desired speed must be returned
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the desired speed
     */
    private DoubleScalar.Rel<SpeedUnit> vDes(final GTU<?> follower, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        return new DoubleScalar.Rel<SpeedUnit>(Math.min(this.delta * speedLimit.getSI(), follower.getMaximumVelocity()
            .getSI()), SpeedUnit.METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower,
        final Collection<? extends LaneBasedGTU<?>> leaders, final DoubleScalar.Abs<SpeedUnit> speedLimit)
        throws RemoteException, NetworkException
    {
        DoubleScalar.Rel<LengthUnit> shortestHeadway = new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        LaneBasedGTU<?> closestLeader = null;
        for (LaneBasedGTU<?> leader : leaders)
        {
            if (follower == leader)
            {
                continue;
            }
            // TODO 100 m is arbitrary. What should this be based on?
            DoubleScalar.Rel<LengthUnit> s =
                follower.headway(leader, new DoubleScalar.Abs<LengthUnit>(100, LengthUnit.METER));
            // System.out.println("s is " + s);
            if (s.getSI() < 0)
            {
                continue; // Ignore gtus that are behind this gtu
            }
            if (s.getSI() < shortestHeadway.getSI())
            {
                shortestHeadway = s;
                closestLeader = leader;
            }
        }
        // System.out.println("shortestHeadway is " + shortestHeadway);
        return computeAcceleration(follower, closestLeader, speedLimit);
    }

    /** {@inheritDoc} */
    @Override
    public final GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower, final LaneBasedGTU<?> leader,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        DoubleScalar.Abs<SpeedUnit> leaderSpeed =
            null == leader ? follower.getLongitudinalVelocity(thisEvaluationTime) : leader
                .getLongitudinalVelocity(thisEvaluationTime);
        // TODO 100 m is arbitrary. What should this be based on?
        DoubleScalar.Rel<LengthUnit> headway =
            follower.headway(leader, new DoubleScalar.Abs<LengthUnit>(100, LengthUnit.METER));
        return computeAcceleration(follower, leaderSpeed, headway, speedLimit);
    }

    /** {@inheritDoc} */
    @Override
    public final GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower,
        final DoubleScalar.Abs<SpeedUnit> leaderSpeed, final DoubleScalar.Rel<LengthUnit> headway,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        DoubleScalar.Abs<SpeedUnit> followerCurrentSpeed = follower.getLongitudinalVelocity(thisEvaluationTime);
        double leftComponent = 1 - Math.pow(followerCurrentSpeed.getSI() / vDes(follower, speedLimit).getSI(), 4);
        // if (leftComponent < 0)
        // {
        // System.out.println("leftComponent is " + leftComponent);
        // }
        MutableDoubleScalar.Rel<AccelerationUnit> logWeightedAccelerationTimes2 =
            new MutableDoubleScalar.Rel<AccelerationUnit>(Math.sqrt(this.a.getSI() * this.b.getSI()),
                AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAccelerationTimes2.multiply(2); // don't forget the times 2

        DoubleScalar.Rel<SpeedUnit> dV =
            DoubleScalar.minus(follower.getLongitudinalVelocity(thisEvaluationTime), leaderSpeed).immutable();
        // System.out.println("dV is " + dV);
        // System.out.println(" v is " + gtu.speed(thisEvaluationTime));
        // System.out.println("s0 is " + this.s0);
        DoubleScalar.Rel<LengthUnit> sStar =
            DoubleScalar.plus(
                DoubleScalar.plus(this.s0,
                    Calc.speedTimesTime(follower.getLongitudinalVelocity(thisEvaluationTime), this.tSafe)).immutable(),
                Calc.speedTimesTime(dV, Calc.speedDividedByAcceleration(followerCurrentSpeed, logWeightedAccelerationTimes2
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
        newAcceleration.multiply(Math.min(leftComponent, rightComponent));
        // System.out.println("newAcceleration is " + newAcceleration);
        if (newAcceleration.getSI() * this.stepSize.getSI() + followerCurrentSpeed.getSI() < 0)
        {
            // System.out.println("Preventing follower from driving backwards " + follower);
            newAcceleration =
                new MutableDoubleScalar.Abs<AccelerationUnit>(-followerCurrentSpeed.getSI() / this.stepSize.getSI(),
                    AccelerationUnit.METER_PER_SECOND_2);
        }
        MutableDoubleScalar.Abs<TimeUnit> nextEvaluationTime = thisEvaluationTime.mutable();
        nextEvaluationTime.incrementBy(this.stepSize);
        return new GTUFollowingModelResult(newAcceleration.immutable(), nextEvaluationTime.immutable());
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
