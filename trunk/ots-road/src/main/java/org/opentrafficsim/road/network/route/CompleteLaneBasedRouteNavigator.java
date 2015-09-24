package org.opentrafficsim.road.network.route;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A RouteNavigator helps to navigate on a route. In addition, helper methods are available to see if the GTU needs to change
 * lanes to reach the next link on the route.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CompleteLaneBasedRouteNavigator extends AbstractLaneBasedRouteNavigator
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The complete route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CompleteRoute completeRoute;

    /** last visited node on the route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected int lastVisitedNodeIndex = -1;

    /**
     * Create a navigator.
     * @param completeRoute the route to follow
     */
    public CompleteLaneBasedRouteNavigator(final CompleteRoute completeRoute)
    {
        this.completeRoute = completeRoute;
    }

    /** {@inheritDoc} */
    @Override
    public final Node lastVisitedNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex == -1)
        {
            return null;
        }
        return this.completeRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNodeToVisit() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.completeRoute.size() - 1)
        {
            return null;
        }
        return this.completeRoute.getNodes().get(this.lastVisitedNodeIndex + 1);
    }

    /** {@inheritDoc} */
    @Override
    public final Node visitNextNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.completeRoute.size() - 1)
        {
            return null;
        }
        this.lastVisitedNodeIndex++;
        return this.completeRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel suitability(final Lane lane, final Length.Rel longitudinalPosition, final GTUType gtuType,
        final Time.Rel timeHorizon) throws NetworkException
    {
        double remainingDistance = lane.getLength().getSI() - longitudinalPosition.getSI();
        double spareTime = timeHorizon.getSI() - remainingDistance / lane.getSpeedLimit(gtuType).getSI();
        // Find the first upcoming Node where there is a branch
        Node nextNode = lane.getParentLink().getEndNode();
        Node nextSplitNode = null;
        Lane currentLane = lane;
        CrossSectionLink linkBeforeBranch = lane.getParentLink();
        while (null != nextNode)
        {
            if (spareTime <= 0)
            {
                return NOLANECHANGENEEDED; // It is not yet time to worry; this lane will do as well as any other
            }
            int laneCount = countCompatibleLanes(linkBeforeBranch, gtuType);
            if (0 == laneCount)
            {
                throw new NetworkException("No compatible Lanes on Link " + linkBeforeBranch);
            }
            if (1 == laneCount)
            {
                return NOLANECHANGENEEDED; // Only one compatible lane available; we'll get there "automatically";
                // i.e. without influence from the Route
            }
            int branching = nextNode.getLinksOut().size();
            if (branching > 1)
            { // Found a split
                nextSplitNode = nextNode;
                break;
            }
            else if (0 == branching)
            {
                return NOLANECHANGENEEDED; // dead end; no more choices to make
            }
            else
            { // Look beyond this nextNode
                Link nextLink = nextNode.getLinksOut().iterator().next(); // cannot be null
                if (nextLink instanceof CrossSectionLink)
                {
                    nextNode = nextLink.getEndNode();
                    // Oops: wrong code added the length of linkBeforeBranch in stead of length of nextLink
                    remainingDistance += nextLink.getLength().getSI();
                    linkBeforeBranch = (CrossSectionLink) nextLink;
                    // Figure out the new currentLane
                    if (currentLane.nextLanes(gtuType).size() == 0)
                    {
                        // Lane drop; our lane disappears. This is a compulsory lane change; which is not controlled
                        // by the Route. Perform the forced lane change.
                        if (currentLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType).size() > 0)
                        {
                            for (Lane adjacentLane : currentLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT,
                                gtuType))
                            {
                                if (adjacentLane.nextLanes(gtuType).size() > 0)
                                {
                                    currentLane = adjacentLane;
                                    break;
                                }
                                // If there are several adjacent lanes that have non empty nextLanes, we simple take the
                                // first in the set
                            }
                        }
                        for (Lane adjacentLane : currentLane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType))
                        {
                            if (adjacentLane.nextLanes(gtuType).size() > 0)
                            {
                                currentLane = adjacentLane;
                                break;
                            }
                            // If there are several adjacent lanes that have non empty nextLanes, we simple take the
                            // first in the set
                        }
                        if (currentLane.nextLanes(gtuType).size() == 0)
                        {
                            throw new NetworkException("Lane ends and there is not a compatible adjacent lane that does "
                                + "not end");
                        }
                    }
                    // Any compulsory lane change(s) have been performed and there is guaranteed a compatible next lane.
                    for (Lane nextLane : currentLane.nextLanes(gtuType))
                    {
                        if (nextLane.getLaneType().isCompatible(gtuType))
                        {
                            currentLane = currentLane.nextLanes(gtuType).iterator().next();
                            break;
                        }
                    }
                    spareTime -= currentLane.getLength().getSI() / currentLane.getSpeedLimit(gtuType).getSI();
                }
                else
                {
                    // There is a non-CrossSectionLink on the path to the next branch. A non-CrossSectionLink does not
                    // have identifiable Lanes, therefore we can't aim for a particular Lane
                    return NOLANECHANGENEEDED; // Any Lane will do equally well
                }
            }
        }
        if (null == nextNode)
        {
            throw new NetworkException("Cannot find the next branch or sink node");
        }
        // We have now found the first upcoming branching Node
        // Which continuing link is the one we need?
        Map<Lane, Length.Rel> suitabilityOfLanesBeforeBranch = new HashMap<Lane, Length.Rel>();
        Link linkAfterBranch = null;
        for (Link link : nextSplitNode.getLinksOut())
        {
            Node nextNodeOnLink = link.getEndNode();
            for (int i = this.lastVisitedNodeIndex + 1; i < this.completeRoute.size(); i++)
            {
                Node n = this.completeRoute.getNodes().get(i);
                if (nextNodeOnLink == n)
                {
                    if (null != linkAfterBranch)
                    {
                        throw new NetworkException("Parallel Links at " + nextSplitNode + " go to " + nextNodeOnLink);
                        // FIXME If all but one of these have no Lane compatible with gtuType, dying here is a bit
                        // premature
                    }
                    linkAfterBranch = link;
                    break;
                }
            }
        }
        if (null == linkAfterBranch)
        {
            throw new NetworkException("Cannot identify the link to follow after " + nextSplitNode + " in " + this);
        }
        for (CrossSectionElement cse : linkBeforeBranch.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().isCompatible(gtuType))
                {
                    for (Lane connectingLane : l.nextLanes(gtuType))
                    {
                        if (connectingLane.getParentLink() == linkAfterBranch
                            && connectingLane.getLaneType().isCompatible(gtuType))
                        {
                            Length.Rel currentValue = suitabilityOfLanesBeforeBranch.get(l);
                            // Use recursion to find out HOW suitable this continuation lane is, but don't revert back
                            // to the maximum time horizon (or we could end up in infinite recursion when there are
                            // loops in the network).
                            Length.Rel value =
                                suitability(connectingLane, new Length.Rel(0, LengthUnit.SI), gtuType, new Time.Rel(
                                    spareTime, TimeUnit.SI));
                            // Use the minimum of the value computed for the first split junction (if there is one)
                            // and the value computed for the second split junction.
                            suitabilityOfLanesBeforeBranch.put(l, null == currentValue || value.le(currentValue) ? value
                                : currentValue);
                        }
                    }
                }
            }
        }
        if (suitabilityOfLanesBeforeBranch.size() == 0)
        {
            throw new NetworkException("No lanes available on Link " + linkBeforeBranch);
        }
        Length.Rel currentLaneSuitability = suitabilityOfLanesBeforeBranch.get(currentLane);
        if (null != currentLaneSuitability)
        {
            return currentLaneSuitability; // Following the current lane will keep us on the Route
        }
        // Performing one or more lane changes (left or right) is required.
        int totalLanes = countCompatibleLanes(currentLane.getParentLink(), gtuType);
        Length.Rel leftSuitability =
            computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch, totalLanes,
                LateralDirectionality.LEFT, gtuType);
        Length.Rel rightSuitability =
            computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch, totalLanes,
                LateralDirectionality.RIGHT, gtuType);
        if (leftSuitability.ge(rightSuitability))
        {
            return leftSuitability;
        }
        else if (rightSuitability.ge(leftSuitability))
        {
            return rightSuitability;
        }
        if (leftSuitability.le(GETOFFTHISLANENOW))
        {
            throw new NetworkException("Changing lanes in any direction does not get the GTU on a suitable lane");
        }
        return leftSuitability; // left equals right; this is odd but topologically possible
    }

    /**
     * @return the (complete) route.
     */
    public final CompleteRoute getRoute()
    {
        return this.completeRoute;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String currentLocationMark = "<>";
        result.append("Route: [");
        String separator = "";
        if (this.lastVisitedNodeIndex < 0)
        {
            result.append(currentLocationMark);
        }
        for (int index = 0; index < this.completeRoute.size(); index++)
        {
            Node node = this.completeRoute.getNodes().get(index);
            result.append(separator + node);
            if (index == this.lastVisitedNodeIndex)
            {
                result.append(" " + currentLocationMark); // Indicate current position in the route
            }
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}
