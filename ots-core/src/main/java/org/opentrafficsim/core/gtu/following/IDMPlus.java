package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * IDMPlus implements the <i>Integrated Lane Change Model with Relaxation and Synchronization</i> as published by Wouter
 * J. Schakel, Victor L. Knoop and Bart van Arem in Transportation Research Record No 2316 pp 47-57, Washington D.C.,
 * 2012. <br>
 * There are two nasty type setting errors in equation 7 in this published version of the paper. Both times an equals
 * sign (<cite>=</cite>) after <cite>a<sub>gain</sub></cite> should <b>not</b> be there.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDMPlus implements GTUFollowingModel
{
    /** Preferred net longitudinal distance when stopped [m]. */
    private final DoubleScalar.Rel<LengthUnit> s0 = new DoubleScalar.Rel<LengthUnit>(3, LengthUnit.METER);

    /** Longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> a = new DoubleScalar.Abs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Regular longitudinal acceleration [m/s^2]. */
    private final DoubleScalar.Abs<AccelerationUnit> aMin = new DoubleScalar.Abs<AccelerationUnit>(1.25,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal deceleration [m/s^2]. (Should be a positive value even though it is a <b>de</b>celeration.) */
    private final DoubleScalar.Abs<AccelerationUnit> b = new DoubleScalar.Abs<AccelerationUnit>(2.09,
            AccelerationUnit.METER_PER_SECOND_2);

    /**
     * Maximum deceleration when actual speed is more than desired speed (v &gt; vdes). (Should be a positive value even
     * though it is a <b>de</b>celeration.) [m/s^2]
     */
    private final DoubleScalar.Abs<AccelerationUnit> b0 = new DoubleScalar.Abs<AccelerationUnit>(0.5,
            AccelerationUnit.METER_PER_SECOND_2);

    /** Longitudinal regular following headway [s]. */
    private final DoubleScalar.Rel<TimeUnit> tMax = new DoubleScalar.Rel<TimeUnit>(1.2, TimeUnit.SECOND);

    /** LMRS free lane change threshold. (Value between 0 and dSync) */
    private final double dFree = .365;

    /** LMRS synchronized lane change threshold. (Value between dFree and dCoop) */
    private final double dSync = .577;

    /** LMRS cooperative lane change threshold. (Value between dSync and 1.0) */
    private final double dCoop = .788;

    /** LMRS mandatory lane change time [s]. */
    private final DoubleScalar.Rel<TimeUnit> t0 = new DoubleScalar.Rel<TimeUnit>(43, TimeUnit.SECOND);

    /** LMRS mandatory lane change distance [m]. */
    private final DoubleScalar.Rel<LengthUnit> x0 = new DoubleScalar.Rel<LengthUnit>(295, LengthUnit.METER);

    /** LMRS speed gain [m/s] for full desire. (The paper specifies this value in [km/h]) */
    private final DoubleScalar.Abs<SpeedUnit> vGain = new DoubleScalar.Abs<SpeedUnit>(69.6, SpeedUnit.KM_PER_HOUR);

    /** LMRS critical speed [m/s] for a speed gain in the right lane. (The paper specifies this value in [km/h]) */
    private final DoubleScalar.Abs<SpeedUnit> vCong = new DoubleScalar.Abs<SpeedUnit>(60, SpeedUnit.KM_PER_HOUR);

    /** Safe time headway. */
    private final DoubleScalar.Rel<TimeUnit> tSafe = new DoubleScalar.Rel<TimeUnit>(1.6, TimeUnit.SECOND);

    /** the reference to the simulator for the GTUs to use. */
    private final OTSDEVSSimulatorInterface simulator;

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

    /**
     * @param simulator the simulator.
     */
    public IDMPlus(final OTSDEVSSimulatorInterface simulator)
    {
        super();
        this.simulator = simulator;
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
    public final GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> gtu,
            final Collection<? extends LaneBasedGTU<?>> leaders, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = gtu.getNextEvaluationTime();
        // System.out.println("evaluation time is " + thisEvaluationTime);
        // System.out.println("vDes is " + vDes);
        DoubleScalar.Rel<LengthUnit> myFrontPosition =
                gtu.positionOfFront(thisEvaluationTime).getLongitudinalPosition();
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
        return computeAcceleration(gtu, closestLeader, speedLimit);
    }

    /** {@inheritDoc} */
    @Override
    public final GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower,
            final LaneBasedGTU<?> leader, final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> thisEvaluationTime = follower.getNextEvaluationTime();
        DoubleScalar.Abs<SpeedUnit> myCurrentSpeed = follower.getLongitudinalVelocity(thisEvaluationTime);
        double speedIncentive = 1 - Math.pow(myCurrentSpeed.getSI() / vDes(follower, speedLimit).getSI(), 4);
        // System.out.println("speedIncentive is " + speedIncentive);
        MutableDoubleScalar.Rel<AccelerationUnit> logWeightedAverageSpeedTimes2 =
                new MutableDoubleScalar.Rel<AccelerationUnit>(Math.sqrt(this.a.getSI() * this.b.getSI()),
                        AccelerationUnit.METER_PER_SECOND_2);
        logWeightedAverageSpeedTimes2.multiply(2); // don't forget the times 2
        DoubleScalar.Rel<SpeedUnit> dV =
                (null == leader) ? new DoubleScalar.Rel<SpeedUnit>(0, SpeedUnit.METER_PER_SECOND) : DoubleScalar.minus(
                        follower.getLongitudinalVelocity(thisEvaluationTime),
                        leader.getLongitudinalVelocity(thisEvaluationTime)).immutable();
        // System.out.println("dV is " + dV);
        // System.out.println(" v is " + gtu.speed(thisEvaluationTime));
        // System.out.println("s0 is " + this.s0);
        DoubleScalar.Rel<LengthUnit> sStar =
                DoubleScalar.plus(
                        DoubleScalar.plus(this.s0,
                                Calc.speedTimesTime(follower.getLongitudinalVelocity(thisEvaluationTime), this.tSafe))
                                .immutable(),
                        Calc.speedTimesTime(
                                dV,
                                Calc.speedDividedByAcceleration(myCurrentSpeed,
                                        logWeightedAverageSpeedTimes2.immutable()))).immutable();
        if (sStar.getSI() < 0) // Negative value should be treated as 0
        {
            sStar = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        }
        // System.out.println("s* is " + sStar);

        DoubleScalar.Rel<LengthUnit> shortestHeadway =
                null == leader ? new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER) : DoubleScalar
                        .minus(leader.positionOfRear(thisEvaluationTime).getLongitudinalPosition(),
                                follower.positionOfFront(thisEvaluationTime).getLongitudinalPosition()).immutable();
        double distanceIncentive = 1 - Math.pow(sStar.getSI() / shortestHeadway.getSI(), 2);
        MutableDoubleScalar.Abs<AccelerationUnit> newAcceleration =
                new MutableDoubleScalar.Abs<AccelerationUnit>(this.a);
        newAcceleration.multiply(Math.min(speedIncentive, distanceIncentive));
        // System.out.println("distanceIncentive is " + distanceIncentive);
        // System.out.println("newAcceleration is " + newAcceleration);
        MutableDoubleScalar.Abs<TimeUnit> nextEvaluationTime = thisEvaluationTime.mutable();
        nextEvaluationTime.incrementBy(this.stepSize);
        return new GTUFollowingModelResult(newAcceleration.immutable(), nextEvaluationTime.immutable());
    }

    /** {@inheritDoc} */
    @Override
    public Abs<AccelerationUnit> maximumSafeDeceleration()
    {
        return this.b;
    }

}
