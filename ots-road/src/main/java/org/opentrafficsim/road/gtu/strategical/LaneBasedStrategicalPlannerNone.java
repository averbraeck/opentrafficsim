package org.opentrafficsim.road.gtu.strategical;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedStrategicalPlannerNone extends AbstractLaneBasedStrategicalPlanner implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150724L;

    /**
     * Constructor.
     * @param gtu LaneBasedGtu; GTU
     */
    public LaneBasedStrategicalPlannerNone(final LaneBasedGtu gtu)
    {
        super(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedTacticalPlanner getTacticalPlanner()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedTacticalPlanner getTacticalPlanner(final Time time)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Link link, final GtuType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Link nextLink(final Link link, final GtuType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Node node, final Link previousLink, final GtuType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Link nextLink(final Node node, final Link previousLink, final GtuType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Route getRoute()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node getOrigin()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node getDestination()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedStrategicalPlannerNone []";
    }

}
