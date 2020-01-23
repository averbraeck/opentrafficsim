package org.opentrafficsim.core.gtu.plan.strategical;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * A strategicalPlanner is the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs to
 * go. It operates by instantiating tactical planners to do the actual work, which is generating operational plans (paths over
 * time) to follow to reach the destination that the strategical plan is aware of.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 14, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface StrategicalPlanner
{

    /** Parameter type for strategical planner. */
    ParameterTypeClass<StrategicalPlanner> STRATEGICAL_PLANNER = new ParameterTypeClass<>("strat.plan.", "Strategcial planner",
            ParameterTypeClass.getValueClass(StrategicalPlanner.class));

    /**
     * Returns the GTU.
     * @return GTU
     */
    GTU getGtu();

    /**
     * Returns the route.
     * @return route, may be null
     */
    Route getRoute();

    /**
     * Returns the origin.
     * @return origin, may be null
     */
    Node getOrigin();

    /**
     * Returns the destination.
     * @return destination, may be null
     */
    Node getDestination();

    /**
     * Get tactical planner for the GTU. The stratigical planner is free to dynamically change this.
     * @return tactical planner
     */
    TacticalPlanner<?, ?> getTacticalPlanner();

    /**
     * Get tactical planner for the GTU. The stratigical planner is free to dynamically change this.
     * @param time Time; time at which to obtain the tactical planner
     * @return tactical planner
     */
    TacticalPlanner<?, ?> getTacticalPlanner(Time time);

    /**
     * Determine the next node in a network based on a current Link we are on.
     * @param link Link; the link we are on
     * @param direction GTUDirectionality; the direction the GTU is driving on the link
     * @param gtuType GTUType; the GTUType to determine the next node for
     * @return Node; the next node in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    Node nextNode(Link link, GTUDirectionality direction, GTUType gtuType) throws NetworkException;

    /**
     * Determine the next link and driving direction (with or against the design line) in a network based on a current Link we
     * are on.
     * @param link Link; the link we are on
     * @param direction GTUDirectionality; the direction the GTU is driving on the link
     * @param gtuType GTUType; the GTUType to determine the next node for
     * @return LinkDirection; the next link and GTU direction in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    LinkDirection nextLinkDirection(Link link, GTUDirectionality direction, GTUType gtuType) throws NetworkException;

    /**
     * Determine the next node in a network based on a given node.
     * @param node Node; the node for which we want to find the successor
     * @param previousLink Link; the link before the node (needed to avoid making a U-turn)
     * @param gtuType GTUType; the GTUType to determine the next node for
     * @return Node; the next node in the route AFTER the current node
     * @throws NetworkException when no route planner is present or the node cannot be found in the route of the GTU
     */
    Node nextNode(Node node, Link previousLink, GTUType gtuType) throws NetworkException;

    /**
     * Determine the next link and driving direction (with or against the design line) in a network based on a node and a
     * driving direction of the GTU.
     * @param node Node; the node for which we want to find the successor in the driving direction of the GTU
     * @param previousLink Link; the link before the node to avoid U-turn
     * @param gtuType GTUType; the GTUType to determine the next node for
     * @return LinkDirection; the next link and GTU direction in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    LinkDirection nextLinkDirection(Node node, Link previousLink, GTUType gtuType) throws NetworkException;

}
