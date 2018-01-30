package org.opentrafficsim.road.gtu.strategical.route;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.language.Throw;

/**
 * Strategical planner, route-based, with personal driving characteristics, which contain settings for the tactical planner. The
 * tactical planner will only consult the route when the GTU has multiple possibilities on a node, so the route does not have to
 * be complete. As long as all 'splitting' nodes are part of the route and have a valid successor node (connected by a Link),
 * the strategical planner is able to make a plan.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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

    /**
     * Constructor for a strategical planner without route. This can only be used if the network does not have splits, or split
     * fractions are used.
     * @param fixedTacticalPlanner the tactical planner to use for the GTU
     * @param gtu GTU
     * @throws GTUException if fixed tactical planner == null
     */
    public LaneBasedStrategicalRoutePlanner(final LaneBasedTacticalPlanner fixedTacticalPlanner, final LaneBasedGTU gtu)
            throws GTUException
    {
        this(fixedTacticalPlanner, null, gtu, null, null);
    }

    /**
     * Constructor for a strategical planner with route. If the route is {@code null}, a shortest path to the destination is
     * derived.
     * @param fixedTacticalPlanner the tactical planner to use for the GTU
     * @param route the route to drive
     * @param gtu GTU
     * @param origin origin node
     * @param destination destination node
     * @throws GTUException if fixed tactical planner == null
     */
    public LaneBasedStrategicalRoutePlanner(final LaneBasedTacticalPlanner fixedTacticalPlanner, final Route route,
            final LaneBasedGTU gtu, final Node origin, final Node destination) throws GTUException
    {
        super(gtu);
        this.route = route;
        this.origin = origin;
        this.destination = destination;
        this.fixedTacticalPlanner = fixedTacticalPlanner;
        Throw.when(fixedTacticalPlanner == null, GTUException.class,
                "Fixed Tactical Planner for a Strategical planner is null");
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedTacticalPlanner getTacticalPlanner()
    {
        return this.fixedTacticalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedTacticalPlanner getTacticalPlanner(Time time)
    {
        return this.fixedTacticalPlanner; // fixed anyway
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Link link, final GTUDirectionality direction, final GTUType gtuType)
            throws NetworkException
    {
        LinkDirection linkDirection = nextLinkDirection(link, direction, gtuType);
        return linkDirection.getNodeTo();
    }

    /** {@inheritDoc} */
    @Override
    public final LinkDirection nextLinkDirection(final Link link, final GTUDirectionality direction, final GTUType gtuType)
            throws NetworkException
    {
        Node nextNode = direction.equals(GTUDirectionality.DIR_PLUS) ? link.getEndNode() : link.getStartNode();
        return nextLinkDirection(nextNode, link, gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Node node, final Link previousLink, final GTUType gtuType) throws NetworkException
    {
        LinkDirection linkDirection = nextLinkDirection(node, previousLink, gtuType);
        return linkDirection.getNodeTo();
    }

    /** {@inheritDoc} */
    @Override
    public final LinkDirection nextLinkDirection(final Node node, final Link previousLink, final GTUType gtuType)
            throws NetworkException
    {

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
            Link link = node.getLinks().iterator().next();
            return link.getStartNode().equals(node) ? new LinkDirection(link, GTUDirectionality.DIR_PLUS)
                    : new LinkDirection(link, GTUDirectionality.DIR_MINUS);
        }
        if (node.getLinks().size() == 2)
        {
            for (Link link : node.getLinks())
            {
                if (!link.equals(previousLink))
                {
                    return link.getStartNode().equals(node) ? new LinkDirection(link, GTUDirectionality.DIR_PLUS)
                            : new LinkDirection(link, GTUDirectionality.DIR_MINUS);
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
                if ((link.getStartNode().equals(node) && !link.getDirectionality(gtuType).isForwardOrBoth())
                        || (link.getEndNode().equals(node) && !link.getDirectionality(gtuType).isBackwardOrBoth()))
                {
                    linkIterator.remove();
                }
                else
                {
                    // are there no lanes from the node into this link in the outgoing direction?
                    boolean out = false;
                    CrossSectionLink csLink = (CrossSectionLink) link;
                    for (CrossSectionElement cse : csLink.getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            Lane lane = (Lane) cse;
                            if ((link.getStartNode().equals(node)
                                    && lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_PLUS))
                                    || (link.getEndNode().equals(node)
                                            && lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_MINUS)))
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
            Link link = links.iterator().next();
            return link.getStartNode().equals(node) ? new LinkDirection(link, GTUDirectionality.DIR_PLUS)
                    : new LinkDirection(link, GTUDirectionality.DIR_MINUS);
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

        for (Link link : links)
        {
            if (link.getStartNode().equals(nextNode) && link.getEndNode().equals(node))
            {
                return new LinkDirection(link, GTUDirectionality.DIR_MINUS);
            }
            if (link.getEndNode().equals(nextNode) && link.getStartNode().equals(node))
            {
                return new LinkDirection(link, GTUDirectionality.DIR_PLUS);
            }
        }
        throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link coming from "
                + previousLink.getId() + ", but no link could be found connecting node " + node + " and node " + nextNode
                + " for route " + this.route);
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute()
    {
        if (this.route == null && this.destination != null)
        {
            try
            {
                DirectedLanePosition pos = getGtu().getReferencePosition();
                CrossSectionLink link = pos.getLane().getParentLink();
                Node from = pos.getGtuDirection().isPlus() ? link.getStartNode() : link.getEndNode();
                this.route = link.getNetwork().getShortestRouteBetween(getGtu().getGTUType(), from, this.destination);
            }
            catch (GTUException | NetworkException exception)
            {
                throw new RuntimeException("Route could not be determined.", exception);
            }
        }
        return this.route;
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
