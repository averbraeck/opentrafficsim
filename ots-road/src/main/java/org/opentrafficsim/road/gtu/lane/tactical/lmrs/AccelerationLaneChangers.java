package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Accelerate to follow lane-changing leaders from adjacent lanes towards the current lane. This is only relevant when
 * {@code LaneBookkeeping.EDGE} is used.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AccelerationLaneChangers implements AccelerationIncentive, Stateless<AccelerationLaneChangers>
{

    /** Singleton instance. */
    public static final AccelerationLaneChangers SINGLETON = new AccelerationLaneChangers();

    /**
     * Constructor.
     */
    private AccelerationLaneChangers()
    {
        //
    }

    @Override
    public Acceleration accelerate(final RelativeLane lane, final Length mergeDistance, final LaneBasedGtu gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final Parameters params, final SpeedLimitInfo speedLimitInfo) throws ParameterException, GtuException
    {
        for (LateralDirectionality lat : LateralDirectionality.LEFT_AND_RIGHT)
        {
            LateralDirectionality flip = lat.flip();
            for (PerceivedGtu leader : perception.getPerceptionCategory(NeighborsPerception.class)
                    .getLeaders(new RelativeLane(lat, 1)))
            {
                if (leader.getManeuver().isChangingLane(flip))
                {
                    return CarFollowingUtil.followSingleLeader(carFollowingModel, params, speed, speedLimitInfo, leader);
                }
            }
        }
        return NO_REASON;
    }

    @Override
    public AccelerationLaneChangers get()
    {
        return SINGLETON;
    }

}
