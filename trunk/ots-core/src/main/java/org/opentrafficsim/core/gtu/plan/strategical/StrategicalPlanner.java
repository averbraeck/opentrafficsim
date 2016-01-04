package org.opentrafficsim.core.gtu.plan.strategical;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * A strategicalPlanner is the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs to
 * go. It operates by instantiating tactical planners to do the actual work, which is generating operational plans (paths over
 * time) to follow to reach the destination that the strategical plan is aware of.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 14, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface StrategicalPlanner
{
    /**
     * Generate a new tactical planner for the GTU.
     * @param gtu the gtu to generate the plan for
     * @return a new tactical planner
     */
    TacticalPlanner generateTacticalPlanner(GTU gtu);

    /**
     * Determine the next node in a network based on a current Link we are on.
     * @param link the link we are on
     * @param direction the direction the GTU is driving on the link
     * @return the next node in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    Node nextNode(final Link link, final GTUDirectionality direction) throws NetworkException;

    /**
     * Determine the next link and driving direction (with or against the design line) in a network based on a current Link we
     * are on.
     * @param link the link we are on
     * @param direction the direction the GTU is driving on the link
     * @return the next link and GTU direction in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    LinkDirection nextLinkDirection(final Link link, final GTUDirectionality direction) throws NetworkException;

    /**
     * Determine the next node in a network based on a given node.
     * @param node the node for which we want to find the successor
     * @return the next node in the route AFTER the current node
     * @throws NetworkException when no route planner is present or the node cannot be found in the route of the GTU
     */
    Node nextNode(final Node node) throws NetworkException;

    /**
     * Determine the next link and driving direction (with or against the design line) in a network based on a node and a
     * driving direction of the GTU.
     * @param node the node for which we want to find the successor in the driving direction of the GTU
     * @return the next link and GTU direction in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    LinkDirection nextLinkDirection(final Node node) throws NetworkException;
}
