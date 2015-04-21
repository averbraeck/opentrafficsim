package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * A Route consists of a list of Nodes. The last visited node is kept. Code can ask what the next node is, and can
 * indicate the next node to visit. Routes can be expanded (e.g. for node expansion), collapsed (e.g. to use a macro
 * model for a part of the route) or changed (e.g. to avoid congestion). Changing is done by adding and/or removing
 * nodes of the node list. When the last visited node of the route is deleted, however, it is impossible to follow the
 * route any further, which will result in a <code>NetworkException</code>.<br>
 * The Node type in the list has generic parameters. This requires callers of getNode(), visitNextNode(), etc. to cast
 * the result as needed. and permits inclusion mixed types of Nodes in one Route.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 1, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Route implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** The nodes of the route. */
    private final List<Node<?, ?>> nodes;

    /** last visited node on the route. */
    private int lastNode = -1;

    /**
     * Create an empty route.
     */
    public Route()
    {
        this.nodes = new ArrayList<Node<?, ?>>();
    }

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param nodes the initial list of nodes.
     */
    public Route(final List<Node<?, ?>> nodes)
    {
        // Make a defensive copy so constructors of GTUs can alter and re-use their List to construct other GTUs
        this.nodes = new ArrayList<Node<?, ?>>(nodes);
    }

    /**
     * Add a node to the end of the node list.
     * @param node the node to add.
     * @return whether the add was successful.
     * @throws NetworkException when node could not be added.
     */
    public final boolean addNode(final Node<?, ?> node) throws NetworkException
    {
        try
        {
            return this.nodes.add(node);
        }
        catch (RuntimeException e)
        {
            throw new NetworkException("Route.addNode(Node) could not be executed", e);
        }
    }

    /**
     * Add a node at a specific location.
     * @param i the location to put the node (0-based).
     * @param node the node to add.
     * @throws NetworkException when i&lt;0 or i&gt;=nodes.size(). Also thrown when another error occurs.
     */
    public final void addNode(final int i, final Node<?, ?> node) throws NetworkException
    {
        try
        {
            this.nodes.add(i, node);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new NetworkException("Route.addNode(i, Node) was called where i<0 or i>=nodes.size()");
        }
        catch (RuntimeException e)
        {
            throw new NetworkException("Route.addNode(i, Node) could not be executed", e);
        }
        // Do this AFTER executing the insert operation (otherwise we could/would increment lastNode and then fail to
        // insert the node).
        if (i <= this.lastNode)
        {
            // quite useless, as we have already done that part of the route, but we have to keep consistency!
            this.lastNode++;
        }
    }

    /**
     * Remove a node from a specific location.
     * @param i the location to remove the node from (0-based).
     * @return the removed node.
     * @throws NetworkException when i is equal to the last visited node because the next link on the route cannot be
     *             computed anymore. Also thrown when another error occurs.
     */
    public final Node<?, ?> removeNode(final int i) throws NetworkException
    {
        Node<?, ?> result = null;
        if (i == this.lastNode)
        {
            throw new NetworkException("Route.removeNode(i) was called where i was equal to the last visited node");
        }
        try
        {
            result = this.nodes.remove(i);
        }
        catch (RuntimeException e)
        {
            throw new NetworkException("Route.removeNode(i, Node) could not be executed", e);
        }
        // Do this AFTER the removal operation has succeeded.
        if (i < this.lastNode)
        {
            // quite useless, as we have already done that part of the route, but we have to keep consistency!
            this.lastNode--;
        }
        return result;
    }

    /**
     * Return a node at a specific location.
     * @param i the location to get the node from (0-based).
     * @return the retrieved node.
     * @throws NetworkException when i&lt;0 or i&gt;=nodes.size().
     */
    public final Node<?, ?> getNode(final int i) throws NetworkException
    {
        try
        {
            return this.nodes.get(i);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new NetworkException("Route.getNode(i) was called where i < 0 or i>= nodes.size()");
        }
    }

    /**
     * @return the first node of the route.
     * @throws NetworkException when the list has no nodes.
     */
    public final Node<?, ?> originNode() throws NetworkException
    {
        if (this.nodes.size() == 0)
        {
            throw new NetworkException("Route.getOrigin() called, but node list has no nodes");
        }
        return getNode(0);
    }

    /**
     * @return the number of nodes in the list. If the list contains more than Integer.MAX_VALUE elements, returns
     *         Integer.MAX_VALUE.
     */
    public final int size()
    {
        return this.nodes.size();
    }

    /**
     * @return the last node of the route.
     * @throws NetworkException when the list has no nodes.
     */
    public final Node<?, ?> destinationNode() throws NetworkException
    {
        if (this.nodes.size() == 0)
        {
            throw new NetworkException("Route.getDestination() called, but node list has no nodes");
        }
        return getNode(size() - 1);
    }

    /**
     * @return the last visited node of the route, and null when no nodes have been visited yet.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    public final Node<?, ?> lastVisitedNode() throws NetworkException
    {
        if (this.lastNode == -1)
        {
            return null;
        }
        return getNode(this.lastNode);
    }

    /**
     * This method does <b>not</b> advance the route pointer.
     * @return the next node of the route to visit, and null when we already reached the destination.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    public final Node<?, ?> nextNodeToVisit() throws NetworkException
    {
        if (this.lastNode >= size() - 1)
        {
            return null;
        }
        return getNode(this.lastNode + 1);
    }

    /**
     * This method <b>does</b> advance the route pointer (if possible).
     * @return the next node of the route to visit, and null when we already reached the destination.
     * @throws NetworkException when the index is out of bounds (should never happen).
     */
    public final Node<?, ?> visitNextNode() throws NetworkException
    {
        if (this.lastNode >= size() - 1)
        {
            return null;
        }
        this.lastNode++;
        return getNode(this.lastNode);
    }

    /**
     * Determine if this Route contains the specified Link.
     * @param link the link to check in the route.
     * @return whether the link is part of the route or not.
     */
    public final boolean containsLink(final CrossSectionLink<?, ?> link)
    {
        Node<?, ?> sn = link.getStartNode();
        Node<?, ?> en = link.getEndNode();
        for (int index = 1; index < this.nodes.size(); index++)
        {
            if (this.nodes.get(index) == en && this.nodes.get(index - 1) == sn)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the index of a Node in this Route, or -1 if this Route does not contain the specified Node. <br>
     * If this route contains the Node more than once, the index of the first is returned.
     * @param node Node&lt;?, ?&gt;; the Node to find
     * @return int;
     */
    public final int indexOf(final Node<?, ?> node)
    {
        return this.nodes.indexOf(node);
    }

    /** Return value of suitability when no lane change is required withing the time horizon. */
    public static final DoubleScalar.Rel<LengthUnit> NOLANECHANGENEEDED = new DoubleScalar.Rel<LengthUnit>(
            Double.MAX_VALUE, LengthUnit.SI);

    /** Return value of suitability when a lane change is required <i>right now</i>. */
    public static final DoubleScalar.Rel<LengthUnit> GETOFFTHISLANENOW = new DoubleScalar.Rel<LengthUnit>(0,
            LengthUnit.SI);

    /**
     * Determine the suitability of being at a particular longitudinal position in a particular Lane for following this
     * Route.
     * @param lane Lane; the lane to consider
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position in the lane
     * @param gtuType GTUType&lt;?&gt;; the type of the GTU (used to check lane compatibility of lanes)
     * @param timeHorizon DoubleScalar.Rel&lt;TimeUnit&gt;; the maximum time that a driver may want to look ahead
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; a value that indicates within what distance the GTU should try to
     *         vacate this lane.
     * @throws NetworkException on network inconsistency, or when the continuation Link at a branch cannot be determined
     */
    public final DoubleScalar.Rel<LengthUnit> suitability(final Lane lane,
            final DoubleScalar.Rel<LengthUnit> longitudinalPosition, GTUType<?> gtuType,
            DoubleScalar.Rel<TimeUnit> timeHorizon) throws NetworkException
    {
        double remainingDistance = lane.getLength().getSI() - longitudinalPosition.getSI();
        double spareTime = timeHorizon.getSI() - remainingDistance / lane.getSpeedLimit().getSI();
        // Find the first upcoming Node where there is a branch
        Node<?, ?> nextNode = lane.getParentLink().getEndNode();
        Node<?, ?> nextSplitNode = null;
        Lane currentLane = lane;
        CrossSectionLink<?, ?> linkBeforeBranch = lane.getParentLink();
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
                Link<?, ?> nextLink = nextNode.getLinksOut().iterator().next(); // cannot be null
                if (nextLink instanceof CrossSectionLink)
                {
                    nextNode = nextLink.getEndNode();
                    remainingDistance += linkBeforeBranch.getLength().getSI();
                    linkBeforeBranch = (CrossSectionLink<?, ?>) nextLink;
                    // Figure out the new currentLane
                    if (currentLane.nextLanes().size() == 0)
                    {
                        // Lane drop; our lane disappears. This is a compulsory lane change; which is not controlled
                        // by the Route. Perform the forced lane change.
                        Set<Lane> adjacentLanes =
                                currentLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType);
                        if (adjacentLanes.size() > 0)
                        {
                            for (Lane adjacentLane : adjacentLanes)
                            {
                                if (adjacentLane.nextLanes().size() > 0)
                                {
                                    currentLane = adjacentLane;
                                    break;
                                }
                                // If there are several adjacent lanes that have non empty nextLanes, we simple take the
                                // first in the set
                            }
                        }
                        adjacentLanes = currentLane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType);
                        for (Lane adjacentLane : adjacentLanes)
                        {
                            if (adjacentLane.nextLanes().size() > 0)
                            {
                                currentLane = adjacentLane;
                                break;
                            }
                            // If there are several adjacent lanes that have non empty nextLanes, we simple take the
                            // first in the set
                        }
                        if (currentLane.nextLanes().size() == 0)
                        {
                            throw new NetworkException("Lane ends and there is not compatible adjacent lane that does "
                                    + "not end");
                        }
                    }
                    // Any compulsory lane change(s) have been performed and there is guaranteed a compatible next lane.
                    for (Lane nextLane : currentLane.nextLanes())
                    {
                        if (nextLane.getLaneType().isCompatible(gtuType))
                        {
                            currentLane = currentLane.nextLanes().iterator().next();
                            break;
                        }
                    }
                    spareTime -= currentLane.getLength().getSI() / currentLane.getSpeedLimit().getSI();
                }
                else
                {
                    // There is a non-CrossSectionLink on the path to the next branch. These do
                    // A non-CrossSectionLink does not have identifiable Lanes, therefore we can't aim for a particular
                    // Lane
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
        Map<Lane, DoubleScalar.Rel<LengthUnit>> suitabilityOfLanesBeforeBranch =
                new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        Link<?, ?> linkAfterBranch = null;
        for (Link<?, ?> link : nextSplitNode.getLinksOut())
        {
            Node<?, ?> nextNodeOnLink = link.getEndNode();
            for (int i = this.lastNode + 1; i < this.nodes.size(); i++)
            {
                Node<?, ?> n = getNode(i);
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
                    for (Lane connectingLane : l.nextLanes())
                    {
                        if (connectingLane.getParentLink() == linkAfterBranch
                                && connectingLane.getLaneType().isCompatible(gtuType))
                        {
                            DoubleScalar.Rel<LengthUnit> currentValue = suitabilityOfLanesBeforeBranch.get(l);
                            // Use recursion to find out HOW suitable this continuation lane is, but don't revert back
                            // to the maximum time horizon (or we could end up in infinite recursion when there are
                            // loops in the network).
                            DoubleScalar.Rel<LengthUnit> value =
                                    suitability(connectingLane, new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.SI),
                                            gtuType, new DoubleScalar.Rel<TimeUnit>(spareTime, TimeUnit.SI));
                            // Use the minimum of the value computed for the first split junction (if there is one)
                            // and the value computed for the second split junction.
                            suitabilityOfLanesBeforeBranch.put(l, null == currentValue || value.le(currentValue)
                                    ? value : currentValue);
                        }
                    }
                }
            }
        }
        if (suitabilityOfLanesBeforeBranch.size() == 0)
        {
            throw new NetworkException("No lanes available on Link " + linkBeforeBranch);
        }
        DoubleScalar.Rel<LengthUnit> currentLaneSuitability = suitabilityOfLanesBeforeBranch.get(currentLane);
        if (null != currentLaneSuitability)
        {
            return currentLaneSuitability; // Following the current lane will keep us on the Route
        }
        // Performing one or more lane changes (left or right) is required.
        int totalLanes = countCompatibleLanes(currentLane.getParentLink(), gtuType);
        DoubleScalar.Rel<LengthUnit> leftSuitability =
                computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch,
                        totalLanes, LateralDirectionality.LEFT, gtuType);
        DoubleScalar.Rel<LengthUnit> rightSuitability =
                computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch,
                        totalLanes, LateralDirectionality.RIGHT, gtuType);
        if (leftSuitability.ge(rightSuitability))
        {
            return leftSuitability;
        }
        else if (rightSuitability.ge(leftSuitability))
        {
            return rightSuitability;
        }
        if (leftSuitability.getSI() <= GETOFFTHISLANENOW.getSI())
        {
            throw new NetworkException("Changing lanes in any direction does not get the GTU on a suitable lane");
        }
        return leftSuitability; // left equals right; this is odd but topologically possible
    }

    /**
     * Compute the suitability of a lane from which lane changes are required to get to the next point on the Route.<br>
     * This method weighs the suitability of the nearest suitable lane by (m - n) / m where n is the number of lane
     * changes required and m is the total number of lanes in the CrossSectionLink.
     * @param startLane Lane; the current lane of the GTU
     * @param remainingDistance double; distance in m of GTU to first branch
     * @param suitabilities Map&lt;Lane, Double&gt;; the set of suitable lanes and their suitability
     * @param totalLanes integer; total number of lanes compatible with the GTU type
     * @param direction LateralDirectionality; the direction of the lane changes to attempt
     * @param gtuType GTUType&lt;?&gt;; the type of the GTU
     * @return double; the suitability of the <cite>startLane</site> for following the Route
     */
    private DoubleScalar.Rel<LengthUnit> computeSuitabilityWithLaneChanges(final Lane startLane,
            double remainingDistance, final Map<Lane, DoubleScalar.Rel<LengthUnit>> suitabilities, int totalLanes,
            final LateralDirectionality direction, GTUType<?> gtuType)
    {
        /*-
         * The time per required lane change seems more relevant than distance per required lane change.
         * Total time required does not grow linearly with the number of required lane changes. Logarithmic, arc tangent 
         * is more like it.
         * Rijkswaterstaat appears to use a fixed time for ANY number of lane changes (about 60s). 
         * TomTom navigation systems give more time (about 90s).
         * In this method the returned suitability decreases linearly with the number of required lane changes.
         */
        int laneChangesUsed = 0;
        Lane currentLane = startLane;
        DoubleScalar.Rel<LengthUnit> currentSuitability = null;
        while (null == currentSuitability)
        {
            laneChangesUsed++;
            Set<Lane> adjacentLanes = currentLane.accessibleAdjacentLanes(direction, gtuType);
            if (adjacentLanes.size() == 0)
            {
                return GETOFFTHISLANENOW;
            }
            currentLane = adjacentLanes.iterator().next();
            currentSuitability = suitabilities.get(currentLane);
        }
        double fraction = currentSuitability == NOLANECHANGENEEDED ? 0 : 0.5;
        int notSuitableLaneCount = totalLanes - suitabilities.size();
        return new DoubleScalar.Rel<LengthUnit>(remainingDistance
                * (notSuitableLaneCount - laneChangesUsed + 1 + fraction) / (notSuitableLaneCount + fraction),
                LengthUnit.SI);
    }

    /**
     * Determine how many lanes on a CrossSectionLink are compatible with a particular GTU type.<br>
     * TODO: this method should probably be moved into the CrossSectionLink class
     * @param link CrossSectionLink&lt;?, ?&gt;; the link
     * @param gtuType GTUType; the GTU type
     * @return integer; the number of lanes on the link that are compatible with the GTU type
     */
    private int countCompatibleLanes(CrossSectionLink<?, ?> link, GTUType<?> gtuType)
    {
        int result = 0;
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().isCompatible(gtuType))
                {
                    result++;
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        final String currentLocationMark = "<>";
        result.append("Route: [");
        String separator = "";
        if (this.lastNode < 0)
        {
            result.append(currentLocationMark);
        }
        for (int index = 0; index < this.nodes.size(); index++)
        {
            Node<?, ?> node = this.nodes.get(index);
            result.append(separator + node);
            if (index == this.lastNode)
            {
                result.append(" " + currentLocationMark); // Indicate current position in the route
            }
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}
