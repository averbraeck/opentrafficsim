package nl.grontmij.smarttraffic.lane;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.AbstractLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 12, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StraightRouteNavigator extends AbstractLaneBasedRouteNavigator
{
    /** The complete route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final CompleteRoute<?, ?> straightRoute;

    /** last visited node on the route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected int lastVisitedNodeIndex = -1;
    
    /**
     * @param straightRoute
     */
    public StraightRouteNavigator(CompleteRoute<?, ?> straightRoute, Link currentLink)
    {
        super();
        this.straightRoute = straightRoute;
    }

    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Rel<LengthUnit> suitability(Lane<?, ?> lane, Rel<LengthUnit> longitudinalPosition, GTUType<?> gtuType,
        Rel<TimeUnit> timeHorizon) throws NetworkException
    {
        // if the lane connects to the main route: good, otherwise: bad
        return new DoubleScalar.Rel<LengthUnit>(500.0, LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public final Node<?> lastVisitedNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex == -1)
        {
            return null;
        }
        return this.straightRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Node<?> nextNodeToVisit() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.straightRoute.size() - 1)
        {
            return null;
        }
        return this.straightRoute.getNodes().get(this.lastVisitedNodeIndex + 1);
    }

    /** {@inheritDoc} */
    @Override
    public final Node<?> visitNextNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.straightRoute.size() - 1)
        {
            return null;
        }
        this.lastVisitedNodeIndex++;
        return this.straightRoute.getNodes().get(this.lastVisitedNodeIndex);
    }


}

