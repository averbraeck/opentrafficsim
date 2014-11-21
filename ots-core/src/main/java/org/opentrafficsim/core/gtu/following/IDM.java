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
    private final DoubleScalar.Rel<LengthUnit> s0 = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);

    /** Maximum longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> a = new DoubleScalar.Abs<AccelerationUnit>(1,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final DoubleScalar.Abs<AccelerationUnit> b = new DoubleScalar.Abs<AccelerationUnit>(3,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Safe time headway. */
    private final DoubleScalar.Rel<TimeUnit> tSafe = new DoubleScalar.Rel<TimeUnit>(1.6, TimeUnit.SECOND);

    /**
     * Mean speed limit adherence (1.0: mean free speed equals the speed limit; 1.1: mean speed limit equals 110% of the
     * speed limit, etc.).
     */
    private final double delta = 1.0;

    /**
     * Time slot size used by IDMPlus (not defined in the paper, but 0.5s is a reasonable trade-off between
     * computational speed and accuracy).
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
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        DoubleScalar.Abs<SpeedUnit> followerCurrentSpeed = follower.getLongitudinalVelocity(thisEvaluationTime);
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
        DoubleScalar.Rel<LengthUnit> sStar =
                DoubleScalar.plus(
                        DoubleScalar.plus(this.s0,
                                Calc.speedTimesTime(follower.getLongitudinalVelocity(thisEvaluationTime), this.tSafe))
                                .immutable(),
                        Calc.speedTimesTime(
                                dV,
                                Calc.speedDividedByAcceleration(followerCurrentSpeed,
                                        logWeightedAccelerationTimes2.immutable()))).immutable();
        if (sStar.getSI() < 0) // Negative value should be treated as 0
        {
            System.out.println("sStar is negative");
            sStar = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        // System.out.println("s* is " + sStar);
        DoubleScalar.Rel<AccelerationUnit> aInteraction =
                new DoubleScalar.Rel<AccelerationUnit>(-this.a.getSI() * sStar.getSI() / headway.getSI(),
                        AccelerationUnit.METER_PER_SECOND_2);
        /*-
        System.out
                .println(String
                        .format("headway %6.1fm, leaderV %6.1fkm/h followerV %6.1fkm/h, dV %6.1fkm/h aFree %6.2fm/s/s aInteraction %6.1fm/s/s",
                                headway.getSI() > 9999 ? 9999d : headway.getSI(), leaderSpeed.getInUnit(), follower
                                        .getLongitudinalVelocity(thisEvaluationTime).getInUnit(), dV.getInUnit(), aFree
                                        .getSI(), aInteraction.getSI()));
         */
        DoubleScalar.Abs<AccelerationUnit> newAcceleration = DoubleScalar.plus(aFree, aInteraction).immutable();

        // System.out.println("distanceIncentive is " + distanceIncentive);
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

}
