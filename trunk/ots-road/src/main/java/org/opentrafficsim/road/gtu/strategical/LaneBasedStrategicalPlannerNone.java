package org.opentrafficsim.road.gtu.strategical;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param gtu GTU
     */
    public LaneBasedStrategicalPlannerNone(final LaneBasedGTU gtu)
    {
        super(new BehavioralCharacteristics(), gtu);
    }

    /** {@inheritDoc} */
    @Override
    public TacticalPlanner generateTacticalPlanner()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node nextNode(Link link, GTUDirectionality direction, GTUType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public LinkDirection nextLinkDirection(Link link, GTUDirectionality direction, GTUType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node nextNode(Node node, Link previousLink, GTUType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public LinkDirection nextLinkDirection(Node node, Link previousLink, GTUType gtuType) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Route getRoute()
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
