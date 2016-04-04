package org.opentrafficsim.road.gtu.strategical.route;

import java.util.Iterator;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedBehavioralCharacteristics;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Strategical planner, route-based, with personal driving characteristics, which contain settings for the tactical planner. The
 * tactical planner will only consult the route when the GTU has multiple possibilities on a node, so the route does not have to
 * be complete. As long as all 'splitting' nodes are part of the route and have a valid successor node (connected by a Link),
 * the strategical planner is able to make a plan.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedStrategicalRoutePlanner extends AbstractLaneBasedStrategicalPlanner implements
    LaneBasedStrategicalPlanner
{
    /** the route to drive. */
    private final Route route;

    /** the fixed tactical planner to use for the GTU. */
    private final TacticalPlanner fixedTacticalPlanner;

    /**
     * @param drivingCharacteristics the personal driving characteristics, which contain settings for the tactical planner
     * @param fixedTacticalPlanner the tactical planner to use for the GTU
     */
    public LaneBasedStrategicalRoutePlanner(LaneBasedBehavioralCharacteristics drivingCharacteristics,
        final TacticalPlanner fixedTacticalPlanner)
    {
        this(drivingCharacteristics, fixedTacticalPlanner, null);
    }

    /**
     * @param drivingCharacteristics the personal driving characteristics, which contain settings for the tactical planner
     * @param fixedTacticalPlanner the tactical planner to use for the GTU
     * @param route the route to drive
     */
    public LaneBasedStrategicalRoutePlanner(LaneBasedBehavioralCharacteristics drivingCharacteristics,
        final TacticalPlanner fixedTacticalPlanner, final Route route)
    {
        super(drivingCharacteristics);
        this.route = route;
        this.fixedTacticalPlanner = fixedTacticalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public TacticalPlanner generateTacticalPlanner(final GTU gtu)
    {
        return this.fixedTacticalPlanner;
    }

    /** {@inheritDoc} */
    @Override
    public Node nextNode(final Link link, final GTUDirectionality direction, final GTUType gtuType)
        throws NetworkException
    {
        LinkDirection linkDirection = nextLinkDirection(link, direction, gtuType);
        return linkDirection.getNodeTo();
    }

    /** {@inheritDoc} */
    @Override
    public LinkDirection nextLinkDirection(final Link link, final GTUDirectionality direction, final GTUType gtuType)
        throws NetworkException
    {
        Node lastNode = direction.equals(GTUDirectionality.DIR_PLUS) ? link.getEndNode() : link.getStartNode();
        return nextLinkDirection(lastNode, link, gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public Node nextNode(final Node node, final Link previousLink, final GTUType gtuType) throws NetworkException
    {
        LinkDirection linkDirection = nextLinkDirection(node, previousLink, gtuType);
        return linkDirection.getNodeTo();
    }

    /** {@inheritDoc} */
    @Override
    public LinkDirection nextLinkDirection(final Node node, final Link previousLink, final GTUType gtuType)
        throws NetworkException
    {
        // if there is no split, don't ask the route
        if (node.getLinks().size() == 1 && previousLink != null)
        {
            // end node
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link, but node " + node
                + " has no successors");
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
        Set<Link> links = node.getLinks();
        for (Iterator<Link> linkIterator = links.iterator(); linkIterator.hasNext();)
        {
            Link link = linkIterator.next();
            if (link.equals(previousLink))
            {
                linkIterator.remove();
            }
            else
            {
                // does the directionality of the link forbid us to go in?
                if ((link.getStartNode().equals(node) && link.getDirectionality(gtuType).isBackward())
                    || (link.getEndNode().equals(node) && link.getDirectionality(gtuType).isForward()))
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
                            if ((link.getStartNode().equals(node) && lane.getDirectionality(gtuType).isForwardOrBoth())
                                || (link.getEndNode().equals(node) && lane.getDirectionality(gtuType).isBackwardOrBoth()))
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
        if (this.route == null)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner does not have a route");
        }
        int i = this.route.getNodes().indexOf(node);
        if (i == -1)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link coming from "
                + previousLink + ", but node " + node + " not in route " + this.route);
        }
        if (i == this.route.getNodes().size() - 1)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link coming from "
                + previousLink + ", but the GTU reached the last node for route " + this.route);
        }
        Node nextNode = this.route.getNode(i + 1);
        for (Link link : links)
        {
            if (link.getStartNode().equals(nextNode))
            {
                return new LinkDirection(link, GTUDirectionality.DIR_MINUS);
            }
            if (link.getEndNode().equals(nextNode))
            {
                return new LinkDirection(link, GTUDirectionality.DIR_PLUS);
            }
        }
        throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next linkcoming from "
            + previousLink + ", but no link could be found connecting node " + node + " and node " + nextNode
            + " for route " + this.route);
    }

    /**
     * @return the route
     */
    public final Route getRoute()
    {
        return this.route;
    }

}
