package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Determine acceleration (deceleration) for a GTU that follows another GTU.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class FollowAcceleration
{
    /** Prohibitive deceleration used to construct the TOODANGEROUS result below. */
    private final static AccelerationStep PROHIBITIVEACCELERATIONSTEP = new AccelerationStep(
            new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.SI),
            new DoubleScalar.Abs<TimeUnit>(Double.NaN, TimeUnit.SI));

    /** Return value if lane change causes immediate collision. */
    public final static AccelerationStep[] TOODANGEROUS = new AccelerationStep[]{PROHIBITIVEACCELERATIONSTEP,
            PROHIBITIVEACCELERATIONSTEP};

    /**
     * This class should never be instantiated.
     */
    private FollowAcceleration()
    {
        // This class should never be instantiated
    }

    /**
     * Compute the acceleration of this GTU and the (new) follower GTU in the current lane, or after a considered lane
     * change. Both values are returned; the acceleration for the reference car at index 0, the acceleration of the
     * (new) follower GTU at index 1. If changing lane is not possible because it would result in dangerous deceleration
     * or collision, the returned value in both fields is Double.NEGATIVE_INFINITY.
     * @param referenceGTU LaneBasedGTU&lt;?&gt;; the car that considers changing lane and whose
     *            gtuFollowingModel will be used for all estimations of the resulting accelerations and decelerations
     * @param otherGTUs Set&lt;HeadwayGTU&gt;; the GTUs in the target lane and their headways from the referenceGTU
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleVector.Abs&lt;AccelerationUnit&gt;; the lowest accelerations (highest decelerations) incurred if
     *         the GTU is inserted among otherGTUs
     * @throws RemoteException on communication failure
     * @throws NetworkException when the network is inconsistent
     */
    public static AccelerationStep[] acceleration(final LaneBasedGTU<?> referenceGTU,
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

}
