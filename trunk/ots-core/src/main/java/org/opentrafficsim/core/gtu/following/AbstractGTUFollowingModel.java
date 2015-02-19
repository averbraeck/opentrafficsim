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
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

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
    private final static AccelerationStep PROHIBITIVEACCELERATIONSTEP = new AccelerationStep(
            new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.SI),
            new DoubleScalar.Abs<TimeUnit>(Double.NaN, TimeUnit.SI));

    /** Return value if lane change causes immediate collision. */
    public final static AccelerationStep[] TOODANGEROUS = new AccelerationStep[]{PROHIBITIVEACCELERATIONSTEP,
            PROHIBITIVEACCELERATIONSTEP};

    /** {@inheritDoc} */
    @Override
    public AccelerationStep[] computeAcceleration(LaneBasedGTU<?> referenceGTU, Collection<HeadwayGTU> otherGTUs,
            Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException
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
                    //if (as.getAcceleration().getSI() < -gfm.maximumSafeDeceleration().getSI())
                    //{
                    //    return TOODANGEROUS;
                    //}
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
                        || (as.getAcceleration().getSI() < referenceGTUAccelerationStep.getAcceleration().getSI()))
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
    public AccelerationStep computeAccelerationWithNoLeader(LaneBasedGTU<?> gtu, Abs<SpeedUnit> speedLimit)
            throws RemoteException, NetworkException
    {
        return computeAcceleration(gtu, gtu.getLongitudinalVelocity(),
                Calc.speedSquaredDividedByDoubleAcceleration(gtu.getMaximumVelocity(), maximumSafeDeceleration()),
                speedLimit);
    }

}
