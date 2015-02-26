package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 19 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public abstract class AbstractGTUFollowingModel implements GTUFollowingModel
{
    /** Prohibitive deceleration used to construct the TOODANGEROUS result below. */
    private static final AccelerationStep PROHIBITIVEACCELERATIONSTEP = new AccelerationStep(
            new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.SI),
            new DoubleScalar.Abs<TimeUnit>(Double.NaN, TimeUnit.SI));

    /** Return value if lane change causes immediate collision. */
    public static final AccelerationStep[] TOODANGEROUS = new AccelerationStep[]{PROHIBITIVEACCELERATIONSTEP,
            PROHIBITIVEACCELERATIONSTEP};

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep[] computeAcceleration(final LaneBasedGTU<?> referenceGTU,
            final Collection<HeadwayGTU> otherGTUs, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = referenceGTU.getSimulator().getSimulatorTime().get();
        // Find out if there is an immediate collision
        for (HeadwayGTU headwayGTU : otherGTUs)
        {
            if (null == headwayGTU.getDistance())
            {
                return TOODANGEROUS;
            }
        }
        AccelerationStep followerAccelerationStep = null;
        AccelerationStep referenceGTUAccelerationStep = null;
        GTUFollowingModel gfm = referenceGTU.getGTUFollowingModel();
        // Find the leader and the follower that cause/experience the least positive (most negative) acceleration.
        for (HeadwayGTU headwayGTU : otherGTUs)
        {
            if (null == headwayGTU.getOtherGTU())
            {
                System.out.println("FollowAcceleration.acceleration: Cannot happen");
            }
            if (headwayGTU.getOtherGTU() == referenceGTU)
            {
                continue;
            }
            if (headwayGTU.getDistanceSI() < 0)
            {
                // This one is behind
                AccelerationStep as =
                        gfm.computeAcceleration(headwayGTU.getOtherGTU(), referenceGTU.getLongitudinalVelocity(when),
                                new DoubleScalar.Rel<LengthUnit>(-headwayGTU.getDistanceSI(), LengthUnit.SI),
                                speedLimit);
                if (null == followerAccelerationStep
                        || as.getAcceleration().getSI() < followerAccelerationStep.getAcceleration().getSI())
                {
                    // if (as.getAcceleration().getSI() < -gfm.maximumSafeDeceleration().getSI())
                    // {
                    // return TOODANGEROUS;
                    // }
                    followerAccelerationStep = as;
                }
            }
            else
            {
                // This one is ahead
                AccelerationStep as =
                        gfm.computeAcceleration(referenceGTU, headwayGTU.getOtherGTU().getLongitudinalVelocity(when),
                                headwayGTU.getDistance(), speedLimit);
                if (null == referenceGTUAccelerationStep
                        || as.getAcceleration().getSI() < referenceGTUAccelerationStep.getAcceleration().getSI())
                {
                    referenceGTUAccelerationStep = as;
                }
            }
        }
        if (null == followerAccelerationStep)
        {
            followerAccelerationStep =
                    new AccelerationStep(new DoubleScalar.Abs<AccelerationUnit>(0, AccelerationUnit.SI), DoubleScalar
                            .plus(referenceGTU.getSimulator().getSimulatorTime().get(), gfm.getStepSize()).immutable());
        }
        if (null == referenceGTUAccelerationStep)
        {
            referenceGTUAccelerationStep = gfm.computeAccelerationWithNoLeader(referenceGTU, speedLimit);
        }
        return new AccelerationStep[]{referenceGTUAccelerationStep, followerAccelerationStep};
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAcceleration(final LaneBasedGTU<?> follower,
            final DoubleScalar.Abs<SpeedUnit> leaderSpeed, final DoubleScalar.Rel<LengthUnit> headway,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        final DoubleScalar.Abs<TimeUnit> currentTime = follower.getNextEvaluationTime();
        final DoubleScalar.Abs<SpeedUnit> followerSpeed = follower.getLongitudinalVelocity(currentTime);
        final DoubleScalar.Abs<SpeedUnit> followerMaximumSpeed = follower.getMaximumVelocity();
        DoubleScalar.Abs<AccelerationUnit> newAcceleration =
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit);
        MutableDoubleScalar.Abs<TimeUnit> nextEvaluationTime = currentTime.mutable();
        nextEvaluationTime.incrementBy(getStepSize());
        return new AccelerationStep(newAcceleration, nextEvaluationTime.immutable());

    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationWithNoLeader(final LaneBasedGTU<?> gtu,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException
    {
        return computeAcceleration(gtu, gtu.getLongitudinalVelocity(),
                Calc.speedSquaredDividedByDoubleAcceleration(gtu.getMaximumVelocity(), maximumSafeDeceleration()),
                speedLimit);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> minimumHeadway(final DoubleScalar.Abs<SpeedUnit> followerSpeed,
            final DoubleScalar.Abs<SpeedUnit> leaderSpeed) throws RemoteException
    {
        // Make a usable assumption for the speed limit (use max of followerSpeed an leaderSpeed)
        DoubleScalar.Abs<SpeedUnit> speedLimit =
                new DoubleScalar.Abs<SpeedUnit>(Math.max(followerSpeed.getSI(), leaderSpeed.getSI()), SpeedUnit.SI);
        double maximumDeceleration = -maximumSafeDeceleration().getSI();
        // Find a decent interval to bisect
        double minimumSI = 0;
        double minimumSIDeceleration =
                computeAcceleration(followerSpeed, followerSpeed, leaderSpeed,
                        new DoubleScalar.Rel<LengthUnit>(minimumSI, LengthUnit.SI), speedLimit).getSI();
        if (minimumSIDeceleration >= maximumDeceleration)
        {
            // Weird... The GTU following model allows zero headway
            return new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.SI);
        }
        double maximumSI = 1; // this is - deliberately - way too small
        double maximumSIDeceleration = Double.NaN;
        // Double the value of maximumSI until the resulting deceleration is less severe than the maximum
        for (int step = 0; step < 20; step++)
        {
            maximumSIDeceleration =
                    computeAcceleration(followerSpeed, followerSpeed, leaderSpeed,
                            new DoubleScalar.Rel<LengthUnit>(maximumSI, LengthUnit.SI), speedLimit).getSI();
            if (maximumSIDeceleration <= maximumDeceleration)
            {
                break;
            }
            maximumSI *= 2;
        }
        if (maximumSIDeceleration < maximumDeceleration)
        {
            throw new Error("Cannot find headway that results in an acceptable deceleration");
        }
        final double maximumError = 0.1; // [m]
        // Now bisect until the error is less than maximumError
        final int maximumStep = (int) Math.ceil(Math.log((maximumSI - minimumSI) / maximumError) / Math.log(2));
        for (int step = 0; step < maximumStep; step++)
        {
            double midSI = (minimumSI + maximumSI) / 2;
            double midSIAcceleration =
                    computeAcceleration(followerSpeed, followerSpeed, leaderSpeed,
                            new DoubleScalar.Rel<LengthUnit>(midSI, LengthUnit.SI), speedLimit).getSI();
            if (midSIAcceleration < maximumDeceleration)
            {
                minimumSI = midSI;
            }
            else
            {
                maximumSI = midSI;
            }
        }
        return new DoubleScalar.Rel<LengthUnit>((minimumSI + maximumSI) / 2, LengthUnit.SI);
    }
}
