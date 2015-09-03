package org.opentrafficsim.core.gtu.lane.changing;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Dummy lane change model with totally predictable results (used for testing).
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedLaneChangeModel implements LaneChangeModel
{
    /** Lane change that will always be returned by this FixedLaneChangeModel. */
    private final LateralDirectionality laneChange;

    /**
     * Construct a FixedLaneChangeModel.
     * @param laneChange LateralDirectionality; the lane change that (always) be returned by this FixedLaneChangeModel.
     */
    public FixedLaneChangeModel(final LateralDirectionality laneChange)
    {
        this.laneChange = laneChange;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final LaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
        final Collection<HeadwayGTU> sameLaneTraffic, final Collection<HeadwayGTU> rightLaneTraffic,
        final Collection<HeadwayGTU> leftLaneTraffic, final Speed.Abs speedLimit,
        final Acceleration.Rel preferredLaneRouteIncentive,
        final Acceleration.Rel laneChangeThreshold,
        final Acceleration.Rel nonPreferredLaneRouteIncentive) throws RemoteException
    {
        try
        {
            if (null == this.laneChange)
            {
                return new LaneMovementStep(gtu.getGTUFollowingModel().computeAcceleration(gtu, sameLaneTraffic, speedLimit)
                    .getLeaderAccelerationStep(), null);
            }
            else if (LateralDirectionality.LEFT == this.laneChange)
            {
                return new LaneMovementStep(gtu.getGTUFollowingModel().computeAcceleration(gtu, leftLaneTraffic, speedLimit)
                    .getLeaderAccelerationStep(), this.laneChange);
            }
            else if (LateralDirectionality.RIGHT == this.laneChange)
            {
                return new LaneMovementStep(gtu.getGTUFollowingModel()
                    .computeAcceleration(gtu, rightLaneTraffic, speedLimit).getLeaderAccelerationStep(), this.laneChange);
            }
            throw new Error("Program Error - unhandled LateralDirectionality");
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            throw new Error(
                "Cannot happen: caught NetworkException in FixedLaneChangeModel.computerLaneChangeAndAcceleration");
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Fixed lane change model";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Fixed lane change model. This model returns a lane change decision that is independent of the actual "
            + "traffic. It is used mostly for testing.";
    }

}
