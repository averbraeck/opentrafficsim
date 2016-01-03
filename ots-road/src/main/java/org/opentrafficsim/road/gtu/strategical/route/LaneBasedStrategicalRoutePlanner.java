package org.opentrafficsim.road.gtu.strategical.route;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.AbstractLaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;

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

    /**
     * @param drivingCharacteristics the personal driving characteristics, which contain settings for the tactical planner
     */
    public LaneBasedStrategicalRoutePlanner(LaneBasedDrivingCharacteristics drivingCharacteristics)
    {
        this(drivingCharacteristics, null);
    }

    /**
     * @param drivingCharacteristics the personal driving characteristics, which contain settings for the tactical planner
     * @param route the route to drive
     */
    public LaneBasedStrategicalRoutePlanner(LaneBasedDrivingCharacteristics drivingCharacteristics, final Route route)
    {
        super(drivingCharacteristics);
        this.route = route;
    }

    /** {@inheritDoc} */
    @Override
    public TacticalPlanner generateTacticalPlanner(final GTU gtu)
    {
        return new LaneBasedCFLCTacticalPlanner(this);
    }

    /** {@inheritDoc} */
    @Override
    public Node nextNode(final Link link, final GTUDirectionality direction) throws NetworkException
    {
        LinkDirection linkDirection = nextLinkDirection(link, direction);
        return linkDirection.getDirection().equals(GTUDirectionality.DIR_PLUS) ? linkDirection.getLink().getEndNode()
            : linkDirection.getLink().getStartNode();
    }

    /** {@inheritDoc} */
    @Override
    public LinkDirection nextLinkDirection(final Link link, final GTUDirectionality direction) throws NetworkException
    {
        if (this.route == null)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner does not have a route");
        }
        Node lastNode = direction.equals(GTUDirectionality.DIR_PLUS) ? link.getEndNode() : link.getStartNode();
        int i = this.route.getNodes().indexOf(lastNode);
        if (i == -1)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link, but node "
                + lastNode + " not in route " + this.route);
        }
        if (i == this.route.getNodes().size() - 1)
        {
            throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link, "
                + "but the GTU reached the last node for route " + this.route);
        }
        Node nextNode = this.route.getNode(i + 1);
        for (Link l : lastNode.getLinks())
        {
            if (l.getStartNode().equals(nextNode))
            {
                return new LinkDirection(l, GTUDirectionality.DIR_MINUS);
            }
            if (l.getEndNode().equals(nextNode))
            {
                return new LinkDirection(l, GTUDirectionality.DIR_PLUS);
            }
        }
        throw new NetworkException("LaneBasedStrategicalRoutePlanner is asked for a next link, "
            + "but no link could be found connecting node " + lastNode + " and node " + nextNode + " for route "
            + this.route);
    }

    /**
     * @return the route
     */
    public final Route getRoute()
    {
        return this.route;
    }

}
