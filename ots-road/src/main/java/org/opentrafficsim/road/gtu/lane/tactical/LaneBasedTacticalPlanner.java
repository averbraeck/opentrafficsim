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
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param from LaneDirection; lane we come from
     * @param lanes Set&lt;LaneDirection&gt;; set of lane directions possible
     * @return LaneDirection; preferred lane direction
     * @throws ParameterException in case of a missing parameter
     */
    default LaneDirection chooseLaneAtSplit(final LaneDirection from, final Set<LaneDirection> lanes) throws ParameterException
    {
        if (getGtu().getOperationalPlan() instanceof LaneBasedOperationalPlan
                && ((LaneBasedOperationalPlan) getGtu().getOperationalPlan()).isDeviative())
        {
            // take the lane adjacent to lane we are registered on, if any
            LateralDirectionality forceSide = LateralDirectionality.NONE;
            try
            {
                Set<Lane> leftLanes = from.getLane().accessibleAdjacentLanesPhysical(LateralDirectionality.LEFT,
                        getGtu().getGtuType(), from.getDirection());
                if (!Collections.disjoint(getGtu().positions(getGtu().getReference()).keySet(), leftLanes))
                {
                    forceSide = LateralDirectionality.LEFT;
                }
                else
                {
                    Set<Lane> rightLanes = from.getLane().accessibleAdjacentLanesPhysical(LateralDirectionality.RIGHT,
                            getGtu().getGtuType(), from.getDirection());
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
                    Iterator<LaneDirection> iter = lanes.iterator();
                    LaneDirection next = iter.next();
                    while (iter.hasNext())
                    {
                        LaneDirection candidate = iter.next();
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
            LaneDirection rightMost = null;
            for (LaneDirection lane : lanes)
            {
                rightMost = rightMost == null ? lane : mostOnSide(rightMost, lane, LateralDirectionality.RIGHT);
            }
            return rightMost;
        }
        Length maxDistance = Length.NEGATIVE_INFINITY;
        LaneDirection best = null;
        for (LaneDirection lane : lanes)
        {
            LaneDirection next = getGtu().getNextLaneForRoute(lane);
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
     * @param lane LaneDirection; lane and direction
     * @param distance Length; distance so far
     * @param route Route; route
     * @param maxDistance Length; max search distance
     * @return Length; distance from this lane onwards where the route can be followed
     */
    // TODO private when we use java 9
    default Length okDistance(final LaneDirection lane, final Length distance, final Route route, final Length maxDistance)
    {
        if (distance.gt(maxDistance))
        {
            return maxDistance;
        }
        LaneDirection next = getGtu().getNextLaneForRoute(lane);
        if (next == null)
        {
            Node endNode = lane.getDirection().isPlus() ? lane.getLane().getParentLink().getEndNode()
                    : lane.getLane().getParentLink().getStartNode();
            Set<Link> links = endNode.getLinks().toSet();
            links.remove(lane.getLane().getParentLink());
            if (route.contains(endNode) && (links.isEmpty() || links.iterator().next().getLinkType().isConnector()))
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
     * @param lane1 LaneDirection; lane 1
     * @param lane2 LaneDirection; lane 2
     * @param lat LateralDirectionality; lateral side
     * @return LaneDirection; right-most of two lanes
     */
    static LaneDirection mostOnSide(final LaneDirection lane1, final LaneDirection lane2, final LateralDirectionality lat)
    {
        Length offset1 = lane1.getLane().getDesignLineOffsetAtBegin().plus(lane1.getLane().getDesignLineOffsetAtEnd());
        offset1 = lane1.getDirection().isPlus() ? offset1 : offset1.neg();
        Length offset2 = lane2.getLane().getDesignLineOffsetAtBegin().plus(lane2.getLane().getDesignLineOffsetAtEnd());
        offset2 = lane2.getDirection().isPlus() ? offset2 : offset2.neg();
        if (lat.isLeft())
        {
            return offset1.gt(offset2) ? lane1 : lane2;
        }
        return offset1.gt(offset2) ? lane2 : lane1;
    }

}
