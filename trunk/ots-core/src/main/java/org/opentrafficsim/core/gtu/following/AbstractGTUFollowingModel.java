package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.units.calc.Calc;

/**
 * Code shared between various car following models.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 19 feb. 2015 <br>
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
    public static final DualAccelerationStep TOODANGEROUS = new DualAccelerationStep(PROHIBITIVEACCELERATIONSTEP,
        PROHIBITIVEACCELERATIONSTEP);

    /** {@inheritDoc} */
    @Override
    public final DualAccelerationStep computeAcceleration(final LaneBasedGTU referenceGTU,
        final Collection<HeadwayGTU> otherGTUs, final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException,
        NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = referenceGTU.getSimulator().getSimulatorTime().get();
        // Find out if there is an immediate collision
        for (HeadwayGTU headwayGTU : otherGTUs)
        {
            if (headwayGTU.getOtherGTU() != referenceGTU && null == headwayGTU.getDistance())
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
                        new DoubleScalar.Rel<LengthUnit>(-headwayGTU.getDistanceSI(), LengthUnit.SI), speedLimit);
                if (null == followerAccelerationStep || as.getAcceleration().lt(followerAccelerationStep.getAcceleration()))
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
                    gfm.computeAcceleration(referenceGTU, headwayGTU.getOtherGTU().getLongitudinalVelocity(when), headwayGTU
                        .getDistance(), speedLimit);
                if (null == referenceGTUAccelerationStep
                    || as.getAcceleration().lt(referenceGTUAccelerationStep.getAcceleration()))
                {
                    referenceGTUAccelerationStep = as;
                }
            }
        }
        if (null == followerAccelerationStep)
        {
            followerAccelerationStep = gfm.computeAccelerationWithNoLeader(referenceGTU, speedLimit);
        }
        if (null == referenceGTUAccelerationStep)
        {
            referenceGTUAccelerationStep = gfm.computeAccelerationWithNoLeader(referenceGTU, speedLimit);
        }
        return new DualAccelerationStep(referenceGTUAccelerationStep, followerAccelerationStep);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAcceleration(final LaneBasedGTU follower,
        final DoubleScalar.Abs<SpeedUnit> leaderSpeed, final DoubleScalar.Rel<LengthUnit> headway,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        final DoubleScalar.Abs<TimeUnit> currentTime = follower.getNextEvaluationTime();
        final DoubleScalar.Abs<SpeedUnit> followerSpeed = follower.getLongitudinalVelocity(currentTime);
        final DoubleScalar.Abs<SpeedUnit> followerMaximumSpeed = follower.getMaximumVelocity();
        DoubleScalar.Abs<AccelerationUnit> newAcceleration =
            computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed, headway, speedLimit);
        DoubleScalar.Abs<TimeUnit> nextEvaluationTime = currentTime;
        nextEvaluationTime = nextEvaluationTime.plus(getStepSize());
        return new AccelerationStep(newAcceleration, nextEvaluationTime);

    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAccelerationWithNoLeader(final LaneBasedGTU gtu,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException
    {
        return computeAcceleration(gtu, gtu.getLongitudinalVelocity(), Calc.speedSquaredDividedByDoubleAcceleration(gtu
            .getMaximumVelocity(), maximumSafeDeceleration()), speedLimit);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> minimumHeadway(final DoubleScalar.Abs<SpeedUnit> followerSpeed,
        final DoubleScalar.Abs<SpeedUnit> leaderSpeed, final DoubleScalar.Rel<LengthUnit> precision,
        final DoubleScalar.Abs<SpeedUnit> speedLimit, final DoubleScalar.Abs<SpeedUnit> followerMaximumSpeed)
        throws RemoteException
    {
        if (precision.getSI() <= 0)
        {
            throw new Error("Precision has bad value (must be > 0; got " + precision + ")");
        }
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
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
                    new DoubleScalar.Rel<LengthUnit>(maximumSI, LengthUnit.SI), speedLimit).getSI();
            if (maximumSIDeceleration > maximumDeceleration)
            {
                break;
            }
            maximumSI *= 2;
        }
        if (maximumSIDeceleration < maximumDeceleration)
        {
            throw new Error("Cannot find headway that results in an acceptable deceleration");
        }
        // Now bisect until the error is less than the requested precision
        final int maximumStep = (int) Math.ceil(Math.log((maximumSI - minimumSI) / precision.getSI()) / Math.log(2));
        for (int step = 0; step < maximumStep; step++)
        {
            double midSI = (minimumSI + maximumSI) / 2;
            double midSIAcceleration =
                computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
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
        DoubleScalar.Rel<LengthUnit> result = new DoubleScalar.Rel<LengthUnit>((minimumSI + maximumSI) / 2, LengthUnit.SI);
        computeAcceleration(followerSpeed, followerMaximumSpeed, leaderSpeed,
            new DoubleScalar.Rel<LengthUnit>(result.getSI(), LengthUnit.SI), speedLimit).getSI();
        return result;
    }
}
