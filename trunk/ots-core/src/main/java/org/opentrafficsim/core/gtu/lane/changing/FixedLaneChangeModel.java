package org.opentrafficsim.core.gtu.lane.changing;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.following.FollowAcceleration;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * Dummy lane change model with totally predictable results (used for testing).
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedLaneChangeModel implements LaneChangeModel
{
    /** Lane change that will always be returned by this FixedLaneChangeModel. */
    final LateralDirectionality laneChange;

    /**
     * Construct a FixedLaneChangeModel.
     * @param laneChange LateralDirectionality; the lane change that (always) be returned by this FixedLaneChangeModel.
     */
    public FixedLaneChangeModel(final LateralDirectionality laneChange)
    {
        this.laneChange = laneChange;
    }

    /** {@inheritDoc} */
    @Override
    public LaneMovementStep computeLaneChangeAndAcceleration(LaneBasedGTU<?> gtu,
            Collection<HeadwayGTU> sameLaneTraffic, Collection<HeadwayGTU> rightLaneTraffic,
            Collection<HeadwayGTU> leftLaneTraffic, Abs<SpeedUnit> speedLimit,
            Rel<AccelerationUnit> preferredLaneRouteIncentive, Rel<AccelerationUnit> laneChangeThreshold,
            Rel<AccelerationUnit> nonPreferredLaneRouteIncentive) throws RemoteException
    {
        try
        {
            if (null == this.laneChange)
            {
                return new LaneMovementStep(FollowAcceleration.acceleration(gtu, sameLaneTraffic, speedLimit)[0], null);
            }
            else if (LateralDirectionality.LEFT == this.laneChange)
            {
                return new LaneMovementStep(FollowAcceleration.acceleration(gtu, leftLaneTraffic, speedLimit)[0],
                        this.laneChange);
            }
            else if (LateralDirectionality.RIGHT == this.laneChange)
            {
                return new LaneMovementStep(FollowAcceleration.acceleration(gtu, rightLaneTraffic, speedLimit)[0],
                        this.laneChange);
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

}
