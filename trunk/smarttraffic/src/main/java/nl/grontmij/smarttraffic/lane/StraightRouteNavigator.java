package nl.grontmij.smarttraffic.lane;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.AbstractLaneBasedRouteNavigator;
import org.opentrafficsim.core.network.route.CompleteRoute;

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
    protected final CompleteRoute straightRoute;

    /** last visited node on the route. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected int lastVisitedNodeIndex = -1;

    /** follow the route - no lane change. */
    private static final DoubleScalar.Rel<LengthUnit> FOLLOW_ROUTE =
        new DoubleScalar.Rel<LengthUnit>(1000, LengthUnit.METER);

    /** leave the route - lane change. */
    private static final DoubleScalar.Rel<LengthUnit> LEAVE_ROUTE = new DoubleScalar.Rel<LengthUnit>(1000, LengthUnit.METER);

    /**
     * @param straightRoute
     */
    public StraightRouteNavigator(CompleteRoute straightRoute, Link currentLink)
    {
        super();
        this.straightRoute = straightRoute;
    }

    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Rel<LengthUnit> suitability(Lane lane, DoubleScalar.Rel<LengthUnit> longitudinalPosition,
        GTUType gtuType, DoubleScalar.Rel<TimeUnit> timeHorizon) throws NetworkException
    {
        // if the lane connects to the main route: good, otherwise: bad
        if (this.straightRoute.contains(lane.getParentLink().getEndNode()))
        {
            if (lane.nextLanes(gtuType).size() == 0) // no choice
                return FOLLOW_ROUTE;
            Lane nextLane = lane.nextLanes(gtuType).iterator().next();
            if (nextLane != null && this.straightRoute.contains(nextLane.getParentLink().getEndNode()))
                return FOLLOW_ROUTE;
            else
                return LEAVE_ROUTE;
        }
        else
            // Math.max(0.0, lane.getLength().getSI() - longitudinalPosition.getSI())
            return LEAVE_ROUTE;
    }

    /** {@inheritDoc} */
    @Override
    public final Node lastVisitedNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex == -1)
        {
            return null;
        }
        return this.straightRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNodeToVisit() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.straightRoute.size() - 1)
        {
            return null;
        }
        return this.straightRoute.getNodes().get(this.lastVisitedNodeIndex + 1);
    }

    /** {@inheritDoc} */
    @Override
    public final Node visitNextNode() throws NetworkException
    {
        if (this.lastVisitedNodeIndex >= this.straightRoute.size() - 1)
        {
            return null;
        }
        this.lastVisitedNodeIndex++;
        return this.straightRoute.getNodes().get(this.lastVisitedNodeIndex);
    }

}
