package org.opentrafficsim.core.gtu.plan.strategical;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * A strategicalPlanner is the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs to
 * go. It operates by instantiating tactical planners to do the actual work, which is generating operational plans (paths over
 * time) to follow to reach the destination that the strategical plan is aware of.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
    Gtu getGtu();

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
     * @param gtuType GtuType; the GtuType to determine the next node for
     * @return Node; the next node in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    default Node nextNode(final Link link, final GtuType gtuType) throws NetworkException
    {
        return nextLink(link, gtuType).getEndNode();
    }

    /**
     * Determine the next link in a network based on a current Link we are on.
     * @param previousLink Link; the link before the node to avoid U-turn
     * @param gtuType GtuType; the GtuType to determine the next node for
     * @return Link; the next link in the route AFTER the current link
     * @throws NetworkException when no route planner is present or the final node in the current link cannot be found in the
     *             route
     */
    Link nextLink(Link previousLink, GtuType gtuType) throws NetworkException;

}
