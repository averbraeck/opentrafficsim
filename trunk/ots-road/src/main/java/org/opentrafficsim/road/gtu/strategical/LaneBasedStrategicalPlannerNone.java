package org.opentrafficsim.road.gtu.strategical;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 31, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedStrategicalPlannerNone extends AbstractLaneBasedStrategicalPlanner
{

    /**
     * 
     */
    public LaneBasedStrategicalPlannerNone()
    {
        super(new BehavioralCharacteristics());
    }

    /** {@inheritDoc} */
    @Override
    public TacticalPlanner generateTacticalPlanner(GTU gtu)
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
    public LinkDirection nextLinkDirection(Link link, GTUDirectionality direction, GTUType gtuType)
        throws NetworkException
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

}

