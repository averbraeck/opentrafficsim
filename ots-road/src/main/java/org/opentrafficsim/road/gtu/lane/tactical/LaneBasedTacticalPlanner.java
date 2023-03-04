package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface LaneBasedTacticalPlanner extends TacticalPlanner<LaneBasedGtu, LanePerception>
{
    /**
     * Returns the car-following model.
     * @return car following model
     */
    CarFollowingModel getCarFollowingModel();

    /**
     * Selects a lane from a possible set. This set contains all viable lanes in to which a lanes splits.
     * @param from Lane; lane we come from
     * @param lanes Set&lt;Lane&gt;; set of lanes possible
     * @return Lane; preferred lane
     * @throws ParameterException in case of a missing parameter
     */
    default Lane chooseLaneAtSplit(final Lane from, final Set<Lane> lanes) throws ParameterException
    {
        if (getGtu().getOperationalPlan() instanceof LaneBasedOperationalPlan
                && ((LaneBasedOperationalPlan) getGtu().getOperationalPlan()).isDeviative())
        {
            // take the lane adjacent to lane we are registered on, if any
            LateralDirectionality forceSide = LateralDirectionality.NONE;
            try
            {
                Set<Lane> leftLanes = from.accessibleAdjacentLanesPhysical(LateralDirectionality.LEFT, getGtu().getType());
                if (!Collections.disjoint(getGtu().positions(getGtu().getReference()).keySet(), leftLanes))
                {
                    forceSide = LateralDirectionality.LEFT;
                }
                else
                {
                    Set<Lane> rightLanes =
                            from.accessibleAdjacentLanesPhysical(LateralDirectionality.RIGHT, getGtu().getType());
                    if (!Collections.disjoint(getGtu().positions(getGtu().getReference()).keySet(), rightLanes))
                    {
                        forceSide = LateralDirectionality.RIGHT;
                    }
                }
            }
            catch (GtuException exception)
            {
                throw new RuntimeException("Exception obtaining reference position.", exception);
            }
            if (!forceSide.isNone())
            {
                if (lanes.isEmpty())
                {
                    // A sink should delete the GTU, or a lane change should end, before reaching the end of the lane
                    return null;
                }
                else
                {
                    Iterator<Lane> iter = lanes.iterator();
                    Lane next = iter.next();
                    while (iter.hasNext())
                    {
                        Lane candidate = iter.next();
                        next = LaneBasedTacticalPlanner.mostOnSide(next, candidate, forceSide);
                    }
                    return next;
                }
            }
        }
        Route route = getGtu().getStrategicalPlanner().getRoute();
        if (route == null)
        {
            // select right-most lane
            Lane rightMost = null;
            for (Lane lane : lanes)
            {
                rightMost = rightMost == null ? lane : mostOnSide(rightMost, lane, LateralDirectionality.RIGHT);
            }
            return rightMost;
        }
        Length maxDistance = Length.NEGATIVE_INFINITY;
        Lane best = null;
        for (Lane lane : lanes)
        {
            Lane next = getGtu().getNextLaneForRoute(lane);
            if (next != null)
            {
                Length okDistance = okDistance(next, lane.getLength(), route,
                        getGtu().getParameters().getParameter(ParameterTypes.PERCEPTION));
                if (maxDistance.eq(okDistance))
                {
                    best = mostOnSide(best, lane, LateralDirectionality.RIGHT);
                }
                else if (okDistance.gt(maxDistance))
                {
                    maxDistance = okDistance;
                    best = lane;
                }
            }
        }
        return best;
    }

    /**
     * Helper method for default chooseLaneAtSplit implementation that returns the distance from this lane onwards where the
     * route can be followed.
     * @param lane Lane; lane and direction
     * @param distance Length; distance so far
     * @param route Route; route
     * @param maxDistance Length; max search distance
     * @return Length; distance from this lane onwards where the route can be followed
     */
    // TODO private when we use java 9
    default Length okDistance(final Lane lane, final Length distance, final Route route, final Length maxDistance)
    {
        if (distance.gt(maxDistance))
        {
            return maxDistance;
        }
        Lane next = getGtu().getNextLaneForRoute(lane);
        if (next == null)
        {
            Node endNode = lane.getParentLink().getEndNode();
            Set<Link> links = endNode.getLinks().toSet();
            links.remove(lane.getParentLink());
            if (route.contains(endNode) && (links.isEmpty() || links.iterator().next().isConnector()))
            {
                // dead-end link, must be destination
                return maxDistance;
            }
            // there is no next lane on the route, return the distance to the end of this lane
            return distance.plus(lane.getLength());
        }
        return okDistance(next, distance.plus(lane.getLength()), route, maxDistance);
    }

    /**
     * Returns the right-most of two lanes.
     * @param lane1 Lane; lane 1
     * @param lane2 Lane; lane 2
     * @param lat LateralDirectionality; lateral side
     * @return Lane; right-most of two lanes
     */
    static Lane mostOnSide(final Lane lane1, final Lane lane2, final LateralDirectionality lat)
    {
        Length offset1 = lane1.getDesignLineOffsetAtBegin().plus(lane1.getDesignLineOffsetAtEnd());
        Length offset2 = lane2.getDesignLineOffsetAtBegin().plus(lane2.getDesignLineOffsetAtEnd());
        if (lat.isLeft())
        {
            return offset1.gt(offset2) ? lane1 : lane2;
        }
        return offset1.gt(offset2) ? lane2 : lane1;
    }

}
