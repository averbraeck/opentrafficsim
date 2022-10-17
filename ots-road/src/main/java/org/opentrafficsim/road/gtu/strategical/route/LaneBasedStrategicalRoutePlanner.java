package org.opentrafficsim.road.gtu.strategical.route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * Strategical planner, route-based, with personal driving characteristics, which contain settings for the tactical planner. The
 * tactical planner will only consult the route when the GTU has multiple possibilities on a node, so the route does not have to
 * be complete. As long as all 'splitting' nodes are part of the route and have a valid successor node (connected by a Link),
 * the strategical planner is able to make a plan.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedStrategicalRoutePlanner extends AbstractLaneBasedStrategicalPlanner
        implements LaneBasedStrategicalPlanner, Serializable
{
    /** */
    private static final long serialVersionUID = 20150724L;

    /** The route to drive. */
    private Route route;

    /** Origin node. */
    private final Node origin;

    /** Destination node. */
    private final Node destination;

    /** The fixed tactical planner to use for the GTU. */
    private final LaneBasedTacticalPlanner fixedTacticalPlanner;

    /** Route supplier. */
    private final RouteGeneratorOd routeGenerator;

    /**
     * Constructor for a strategical planner without route. This can only be used if the network does not have splits, or split
     * fractions are used.
     * @param fixedTacticalPlanner LaneBasedTacticalPlanner; the tactical planner to use for the GTU
     * @param gtu LaneBasedGtu; GTU
     * @throws GtuException if fixed tactical planner == null
     */
    public LaneBasedStrategicalRoutePlanner(final LaneBasedTacticalPlanner fixedTacticalPlanner, final LaneBasedGtu gtu)
            throws GtuException
    {
        this(fixedTacticalPlanner, null, gtu, null, null, RouteGeneratorOd.NULL);
    }

    /**
     * Constructor for a strategical planner with route.
     * @param fixedTacticalPlanner LaneBasedTacticalPlanner; the tactical planner to use for the GTU
     * @param route Route; the route to drive
     * @param gtu LaneBasedGtu; GTU
     * @param origin Node; origin node
     * @param destination Node; destination node
     * @throws GtuException if fixed tactical planner == null
     */
    public LaneBasedStrategicalRoutePlanner(final LaneBasedTacticalPlanner fixedTacticalPlanner, final Route route,
            final LaneBasedGtu gtu, final Node origin, final Node destination) throws GtuException
    {
        this(fixedTacticalPlanner, route, gtu, origin, destination, RouteGeneratorOd.NULL);
    }

    /**
     * Constructor for a strategical planner with route generator.
     * @param fixedTacticalPlanner LaneBasedTacticalPlanner; the tactical planner to use for the GTU
     * @param gtu LaneBasedGtu; GTU
     * @param origin Node; origin node
     * @param destination Node; destination node
     * @param routeGenerator RouteGeneratorOD; route generator
     * @throws GtuException if fixed tactical planner == null
     */
    public LaneBasedStrategicalRoutePlanner(final LaneBasedTacticalPlanner fixedTacticalPlanner, final LaneBasedGtu gtu,
            final Node origin, final Node destination, final RouteGeneratorOd routeGenerator) throws GtuException
    {
        this(fixedTacticalPlanner, null, gtu, origin, destination, routeGenerator);
    }

    /**
     * Constructor for a strategical planner with route. If the route is {@code null}, a shortest path to the destination is
     * derived.
     * @param fixedTacticalPlanner LaneBasedTacticalPlanner; the tactical planner to use for the GTU
     * @param route Route; the route to drive
     * @param gtu LaneBasedGtu; GTU
     * @param origin Node; origin node
     * @param destination Node; destination node
     * @param routeGenerator RouteGeneratorOD; route generator
     * @throws GtuException if fixed tactical planner == null
     */
    public LaneBasedStrategicalRoutePlanner(final LaneBasedTacticalPlanner fixedTacticalPlanner, final Route route,
            final LaneBasedGtu gtu, final Node origin, final Node destination, final RouteGeneratorOd routeGenerator)
            throws GtuException
    {
        super(gtu);
        this.route = route;
        this.origin = origin;
        this.destination = destination;
        this.fixedTacticalPlanner = fixedTacticalPlanner;
        Throw.when(fixedTacticalPlanner == null, GtuException.class,
                "Fixed Tactical Planner for a Strategical planner is null");
        this.routeGenerator = routeGenerator;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedTacticalPlanner getTacticalPlanner()
    {
        return this.fixedTacticalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedTacticalPlanner getTacticalPlanner(final Time time)
    {
        return this.fixedTacticalPlanner; // fixed anyway
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Link link, final GtuType gtuType) throws NetworkException
    {
        assureRoute(gtuType);
        Link nextLink = nextLink(link, gtuType);
        return nextLink.getEndNode();
    }

    /** {@inheritDoc} */
    @Override
    public final Link nextLink(final Link link, final GtuType gtuType) throws NetworkException
    {
        assureRoute(gtuType);
        Node nextNode = link.getEndNode();
        if ((null != this.route) && (!this.route.contains(nextNode)))
        {
            link.getSimulator().getLogger().always().warn("nextNode {} is not in route {}", nextNode, this.route);
            Node prevNode = link.getStartNode();
            link.getSimulator().getLogger().always().warn("   other node of link is {}", prevNode);
            int index = 0;
            for (Node node : this.route.getNodes())
            {
                link.getSimulator().getLogger().always().warn("{} {}{}", index, node.equals(prevNode) ? "--->" : "    ", node);
                index++;
            }
        }
        return nextLink(nextNode, link, gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Node node, final Link previousLink, final GtuType gtuType) throws NetworkException
    {
        assureRoute(gtuType);
        Link nextLink = nextLink(node, previousLink, gtuType);
        return nextLink.getEndNode();
    }

    /** {@inheritDoc} */
    @Override
    public final Link nextLink(final Node node, final Link previousLink, final GtuType gtuType) throws NetworkException
    {
        assureRoute(gtuType);

        // if there is no split, don't ask the route
        if (node.getLinks().size() == 1 && previousLink != null)
        {
            // end node
            throw new NetworkException(
                    "LaneBasedStrategicalRoutePlanner is asked for a next link, but node " + node + " has no successors");
        }
        if (node.getLinks().size() == 1 && previousLink == null)
        {
            // start node
            return node.getLinks().iterator().next();
        }
        if (node.getLinks().size() == 2)
        {
            for (Link link : node.getLinks())
            {
                if (!link.equals(previousLink))
                {
                    return link;
                }
            }
        }

        // if we only have one way to go, don't bother about the route yet
        Set<Link> links = node.getLinks().toSet();
        for (Iterator<Link> linkIterator = links.iterator(); linkIterator.hasNext();)
        {
            Link link = linkIterator.next();
            if (link.equals(previousLink))
            {
                // No u-turn...
                linkIterator.remove();
            }
            else
            {
                // does the directionality of the link forbid us to go in?
                if (link.getEndNode().equals(node))
                {
                    linkIterator.remove();
                }
                else
                {
                    // are there no lanes from the node into this link in the outgoing direction?
                    boolean out = false;
                    CrossSectionLink csLink = (CrossSectionLink) link;
                    // TODO: Is there a reason not to iterate over csLink.getLanes()?
                    for (CrossSectionElement cse : csLink.getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            Lane lane = (Lane) cse;
                            if ((link.getStartNode().equals(node) && lane.getLaneType().isCompatible(gtuType)))
                            {
                                out = true;
                            }
                        }
                    }
                    if (!out)
                    {
                        linkIterator.remove();
                    }
                }
            }
        }

        if (links.size() == 1)
        {
            return links.iterator().next();
        }

        // more than 2 links... We have to check the route!
        if (getRoute() == null)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner does not have a route");
        }
        int i = this.route.getNodes().indexOf(node);
        if (i == -1)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link coming from " + previousLink
                    + ", but node " + node + " not in route " + this.route);
        }
        if (i == this.route.getNodes().size() - 1)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link coming from " + previousLink
                    + ", but the GTU reached the last node for route " + this.route);
        }
        Node nextNode = this.route.getNode(i + 1);
        Link result = null;
        for (Link link : links)
        {
            // TODO this takes the first in the set of links that connects the correct nodes; does not handle parallel links
            // consistently
            Link l = null;
            if (link.getStartNode().equals(nextNode) && link.getEndNode().equals(node))
            {
                l = link; // FIXME: Probably this test can be removed since we only go "forward"
            }
            if (link.getEndNode().equals(nextNode) && link.getStartNode().equals(node))
            {
                l = link;
            }
            if (null != result && null != l)
            {
                throw new NetworkException("Cannot choose among multiple links from " + node + " to " + nextNode);
            }
            else if (null == result)
            {
                result = l;
            }
        }
        if (null == result)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link coming from "
                    + previousLink.getId() + ", but no link could be found connecting node " + node + " and node " + nextNode
                    + " for route " + this.route);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute()
    {
        assureRoute(getGtu().getType());
        if (this.route == null && this.destination != null)
        {
            try
            {
                LanePosition pos = getGtu().getReferencePosition();
                CrossSectionLink link = pos.getLane().getParentLink();
                Node from = link.getStartNode();
                if (this.routeGenerator != null)
                {
                    this.route = this.routeGenerator.getRoute(from, this.destination, getGtu().getType());
                }
                if (this.route == null)
                {
                    this.route = link.getNetwork().getShortestRouteBetween(getGtu().getType(), from, this.destination);
                }
            }
            catch (GtuException | NetworkException exception)
            {
                throw new RuntimeException("Route could not be determined.", exception);
            }
        }
        return this.route;
    }

    /**
     * Assures a route is available if a route is already present, or a destination and route supplier are provided.
     * @param gtuType GtuType; the type of the GTU for which a route must be assured
     */
    private void assureRoute(final GtuType gtuType)
    {
        if (this.route == null && this.destination != null && !this.routeGenerator.equals(RouteGeneratorOd.NULL))
        {
            LanePosition ref = Try.assign(() -> getGtu().getReferencePosition(), "Could not retrieve GTU reference position.");
            List<Node> nodes = new ArrayList<>();
            if (this.origin != null)
            {
                nodes.addAll(this.routeGenerator.getRoute(this.origin, ref.getLane().getParentLink().getStartNode(), gtuType)
                        .getNodes());
            }
            else
            {
                nodes.add(ref.getLane().getParentLink().getStartNode());
            }
            Route newRoute =
                    this.routeGenerator.getRoute(ref.getLane().getParentLink().getEndNode(), this.destination, gtuType);
            if (null == newRoute)
            {
                System.err.println("this.routeGenerator.getRoute() returned null");
                throw new RuntimeException("getRoute failed");
            }
            List<Node> newNodes = newRoute.getNodes();
            if (newNodes == null)
            {
                System.err.println("Route.getNodes() returned null");
                newRoute.getNodes();
            }
            nodes.addAll(newNodes);
            this.route =
                    Try.assign(
                            () -> new CompleteRoute("Route for " + gtuType + " from " + this.origin + "to " + this.destination
                                    + " via " + ref.getLane().getParentLink(), gtuType, nodes),
                            "No route possible over nodes %s", nodes);
            // System.out.println("RouteSupplier route for GTU " + getGtu().getId() + ": " + this.route);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Node getOrigin()
    {
        return this.origin;
    }

    /** {@inheritDoc} */
    @Override
    public final Node getDestination()
    {
        return this.destination;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedStrategicalRoutePlanner [route=" + this.route + ", fixedTacticalPlanner=" + this.fixedTacticalPlanner
                + "]";
    }

}
