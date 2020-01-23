package org.opentrafficsim.road.gtu.strategical;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 31, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedStrategicalPlannerNone extends AbstractLaneBasedStrategicalPlanner implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150724L;

    /**
     * Constructor.
     * @param gtu LaneBasedGTU; GTU
     */
    public LaneBasedStrategicalPlannerNone(final LaneBasedGTU gtu)
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
    public final Node nextNode(final Link link, final GTUDirectionality direction, final GTUType gtuType)
            throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LinkDirection nextLinkDirection(final Link link, final GTUDirectionality direction, final GTUType gtuType)
            throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNode(final Node node, final Link previousLink, final GTUType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LinkDirection nextLinkDirection(final Node node, final Link previousLink, final GTUType gtuType)
            throws NetworkException
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
