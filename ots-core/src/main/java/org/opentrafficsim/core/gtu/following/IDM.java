package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * The Intelligent Driver Model by Treiber, Hennecke and Helbing.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 19 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDM implements GTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final DoubleScalar.Rel<LengthUnit> s0;

    /** Maximum longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> a;

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final DoubleScalar.Abs<AccelerationUnit> b;

    /** Safe time headway. */
    private final DoubleScalar.Rel<TimeUnit> tSafe;

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the
     * speed limit, etc.).
     */
    private final double delta;

    /**
     * Construct a new IDM car following model with reasonable values (reasonable for passenger cars).
     */
    public IDM()
    {
        this.a = new DoubleScalar.Abs<AccelerationUnit>(1.56, AccelerationUnit.METER_PER_SECOND_2);
        this.b = new DoubleScalar.Abs<AccelerationUnit>(2.09, AccelerationUnit.METER_PER_SECOND_2);
        this.s0 = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);
        this.tSafe = new DoubleScalar.Rel<TimeUnit>(1.2, TimeUnit.SECOND);
        this.delta = 1d;
    }
    
    /**
     * Construct a new IDM car following model.
     * @param a DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum acceleration of a stationary vehicle (normal value
     *            is 1 m/s/s)
     * @param b DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum deemed-safe deceleration (this is a positive
     *            value). Normal value is 1.5 m/s/s.
     * @param s0 DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum stationary headway (normal value is 2 m)
     * @param tSafe DoubleScalar.Rel&lt;TimeUnit&gt;; the minimum time-headway (normal value is 1s)
     * @param delta double; the speed limit adherence (1.0; mean free speed equals the speed limit; 1.1: mean free speed
     *            equals 110% of the speed limit; etc.)
     */
    public IDM(DoubleScalar.Abs<AccelerationUnit> a, DoubleScalar.Abs<AccelerationUnit> b,
            DoubleScalar.Rel<LengthUnit> s0, DoubleScalar.Rel<TimeUnit> tSafe, double delta)
    {
        this.a = a;
        this.b = b;
        this.s0 = s0;
        this.tSafe = tSafe;
        this.delta = delta;
    }

    /**
     * Time slot size used by IDM (not defined in the paper, but 0.5s is a reasonable trade-off between computational
     * speed and accuracy).
     */
    private final DoubleScalar.Rel<TimeUnit> stepSize = new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND);

    /** {@inheritDoc} */
    @Override
    public GTUFollowingModelResult computeAcceleration(LaneBasedGTU<?> follower,
            Collection<? extends LaneBasedGTU<?>> leaders, DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        // System.out.println("evaluation time is " + thisEvaluationTime);
        // System.out.println("vDes is " + vDes);
        DoubleScalar.Rel<LengthUnit> myFrontPosition =
                follower.positionOfFront(thisEvaluationTime).getLongitudinalPosition();
        // System.out.println("myFrontPosition is " + myFrontPosition);
        DoubleScalar.Rel<LengthUnit> shortestHeadway =
                new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        LaneBasedGTU<?> closestLeader = null;
        for (LaneBasedGTU<?> leader : leaders)
        {
            DoubleScalar.Rel<LengthUnit> s =
                    DoubleScalar.minus(leader.positionOfRear(thisEvaluationTime).getLongitudinalPosition(),
                            myFrontPosition).immutable();
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
    public GTUFollowingModelResult computeAcceleration(LaneBasedGTU<?> follower, LaneBasedGTU<?> leader,
            DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        DoubleScalar.Abs<SpeedUnit> leaderSpeed =
                null == leader ? follower.getLongitudinalVelocity(thisEvaluationTime) : leader
                        .getLongitudinalVelocity(thisEvaluationTime);
        DoubleScalar.Rel<LengthUnit> headway =
                null == leader ? new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER) : DoubleScalar
                        .minus(leader.positionOfRear(thisEvaluationTime).getLongitudinalPosition(),
                                follower.positionOfFront(thisEvaluationTime).getLongitudinalPosition()).immutable();

        return computeAcceleration(follower, leaderSpeed, headway, speedLimit);
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
    public GTUFollowingModelResult computeAcceleration(LaneBasedGTU<?> follower,
            DoubleScalar.Abs<SpeedUnit> leaderSpeed, DoubleScalar.Rel<LengthUnit> headway,
            DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        // System.out.println("Applying IDM for " + follower + " headway is " + headway);
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        DoubleScalar.Abs<SpeedUnit> followerCurrentSpeed = follower.getLongitudinalVelocity(thisEvaluationTime);
        if (follower.getId().equals(35))
        {
            // System.out.println("Let op");
        }
        // dV is the approach speed
        DoubleScalar.Rel<SpeedUnit> dV =
                DoubleScalar.minus(follower.getLongitudinalVelocity(thisEvaluationTime), leaderSpeed).immutable();
        DoubleScalar.Abs<AccelerationUnit> aFree =
                new DoubleScalar.Abs<AccelerationUnit>(this.a.getSI()
                        * (1 - Math.pow(followerCurrentSpeed.getSI() / vDes(follower, speedLimit).getSI(), 4)),
                        AccelerationUnit.METER_PER_SECOND_2);
        MutableDoubleScalar.Rel<AccelerationUnit> logWeightedAccelerationTimes2 =
                new MutableDoubleScalar.Rel<AccelerationUnit>(Math.sqrt(this.a.getSI() * this.b.getSI()),
                        AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAccelerationTimes2.multiply(2); // don't forget the times 2
        // TODO compute logWeightedAccelerationTimes2 only once per run
        /*
         * DoubleScalar.Rel<LengthUnit> sStar = DoubleScalar.plus( DoubleScalar.plus(this.s0,
         * Calc.speedTimesTime(follower.getLongitudinalVelocity(thisEvaluationTime), this.tSafe)) .immutable(),
         * Calc.speedTimesTime( dV, Calc.speedDividedByAcceleration(followerCurrentSpeed,
         * logWeightedAccelerationTimes2.immutable()))).immutable();
         */
        DoubleScalar.Rel<LengthUnit> right =
                DoubleScalar.plus(
                        Calc.speedTimesTime(follower.getLongitudinalVelocity(thisEvaluationTime), this.tSafe),
                        Calc.speedTimesTime(
                                dV,
                                Calc.speedDividedByAcceleration(followerCurrentSpeed,
                                        logWeightedAccelerationTimes2.immutable()))).immutable();
        if (right.getSI() < 0)
        {
            System.out.println("Fixing negative right");
            right = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        DoubleScalar.Rel<LengthUnit> sStar = DoubleScalar.plus(this.s0, right).immutable();
        if (sStar.getSI() < 0) // Negative value should be treated as 0
        {
            System.out.println("sStar is negative");
            sStar = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        // System.out.println("s* is " + sStar);
        DoubleScalar.Rel<AccelerationUnit> aInteraction =
                new DoubleScalar.Rel<AccelerationUnit>(-Math.pow(this.a.getSI() * sStar.getSI() / headway.getSI(), 2),
                        AccelerationUnit.METER_PER_SECOND_2);
        DoubleScalar.Abs<AccelerationUnit> newAcceleration = DoubleScalar.plus(aFree, aInteraction).immutable();
        if (newAcceleration.getSI() * this.stepSize.getSI() + follower.getLongitudinalVelocity().getSI() < 0)
        {
            System.out.println("Limiting deceleration to prevent moving backwards");
            newAcceleration =
                    new DoubleScalar.Abs<AccelerationUnit>(-follower.getLongitudinalVelocity().getSI()
                            / this.stepSize.getSI(), AccelerationUnit.METER_PER_SECOND_2);
        }
        // System.out.println("newAcceleration is " + newAcceleration);
        MutableDoubleScalar.Abs<TimeUnit> nextEvaluationTime = thisEvaluationTime.mutable();
        nextEvaluationTime.incrementBy(this.stepSize);
        return new GTUFollowingModelResult(newAcceleration, nextEvaluationTime.immutable());

    }

    /** {@inheritDoc} */
    @Override
    public Abs<AccelerationUnit> maximumSafeDeceleration()
    {
        return this.b;
    }

    /** {@inheritDoc} */
    @Override
    public String getName()
    {
        return "IDM";
    }

    /** {@inheritDoc} */
    @Override
    public String getLongName()
    {
        return String.format("%s (a=%.1fm/s\u00b2, b=%.1fm/s\u00b2, s0=%.1fm, tSafe=%.1fs, delta=%.2f)", getName(),
                this.a.getSI(), this.b.getSI(), this.s0.getSI(), this.tSafe.getSI(), this.delta);
    }

}
