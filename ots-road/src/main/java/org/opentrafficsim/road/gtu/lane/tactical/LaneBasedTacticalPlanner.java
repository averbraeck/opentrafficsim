package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.LaneAccessLaw;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LaneBasedTacticalPlanner extends TacticalPlanner<LaneBasedGtu, LanePerception>
{
    /**
     * Returns the car-following model.
     * @return car following model
     */
    CarFollowingModel getCarFollowingModel();

    /**
     * Selects a lane from a possible set. This set contains all viable lanes in to which a lane splits.
     * @param from lane we come from
     * @param lanes set of lanes possible
     * @return preferred lane
     * @throws ParameterException in case of a missing parameter
     */
    default Lane chooseLaneAtSplit(final Lane from, final Set<Lane> lanes) throws ParameterException
    {
        Route route = getGtu().getStrategicalPlanner().getRoute();
        Length perception = getGtu().getParameters().getParameter(ParameterTypes.PERCEPTION);
        Set<Lane> bestRegardingRoute = new LinkedHashSet<>();
        LaneChangeInfo bestInfo = null;
        for (Lane lane : lanes)
        {
            ImmutableSortedSet<LaneChangeInfo> lcInfo =
                    lane.getNetwork().getLaneChangeInfo(lane, route, getGtu().getType(), perception, LaneAccessLaw.LEGAL);
            LaneChangeInfo info = lcInfo.isEmpty() ? null : lcInfo.first();
            int comp = bestInfo == null ? (info == null ? 0 : -info.compareTo(bestInfo)) : bestInfo.compareTo(info);
            if (comp >= 0)
            {
                if (comp > 0)
                {
                    bestRegardingRoute.clear();
                    bestInfo = info;
                }
                bestRegardingRoute.add(lane);
            }
        }
        if (bestRegardingRoute.size() > 1)
        {
            Lane rightMost = null;
            for (Lane lane : bestRegardingRoute)
            {
                rightMost = rightMost == null ? lane : mostOnSide(rightMost, lane, LateralDirectionality.RIGHT);
            }
            return rightMost;
        }
        return bestRegardingRoute.iterator().next();
    }

    /**
     * Returns the right-most of two lanes.
     * @param lane1 lane 1
     * @param lane2 lane 2
     * @param lat lateral side
     * @return right-most of two lanes
     */
    static Lane mostOnSide(final Lane lane1, final Lane lane2, final LateralDirectionality lat)
    {
        Length offset1 = lane1.getOffsetAtBegin().plus(lane1.getOffsetAtEnd());
        Length offset2 = lane2.getOffsetAtBegin().plus(lane2.getOffsetAtEnd());
        if (lat.isLeft())
        {
            return offset1.gt(offset2) ? lane1 : lane2;
        }
        return offset1.gt(offset2) ? lane2 : lane1;
    }

}
