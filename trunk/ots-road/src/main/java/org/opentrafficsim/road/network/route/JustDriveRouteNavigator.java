package org.opentrafficsim.road.network.route;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A RouteNavigator helps to navigate on a route. In addition, helper methods are available to see if the GTU needs to change
 * lanes to reach the next link on the route.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class JustDriveRouteNavigator extends AbstractLaneBasedRouteNavigator
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /**
     * Create a navigator.
     */
    public JustDriveRouteNavigator()
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public final Node lastVisitedNode() throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNodeToVisit() throws NetworkException
    {
        return null;
    }

    /**
     * @return nnnode
     */
    public final Node nextNextNodeToVisit()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Node visitNextNode() throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel suitability(final Lane lane, final Length.Rel longitudinalPosition, final LaneBasedGTU gtu,
        final Time.Rel timeHorizon) throws NetworkException
    {
        return new Length.Rel(50.0, LengthUnit.METER);
    }

}
