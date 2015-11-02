package org.opentrafficsim.road.network.route;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
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
public class RandomLaneBasedRouteNavigator extends AbstractLaneBasedRouteNavigator
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the gtu. */
    private LaneBasedGTU gtu;

    /** last visited node on the route (given -- I have been there). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Node lastVisitedNode = null;

    /** next node to visit on the route (given -- I am on this route). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Node nextNodeToVisit = null;

    /** node to take after the next node to visit on the route (look ahead to choose the right lane). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Node nextNextNodeToVisit = null;

    /** link to take after the next node to visit on the route (look ahead to choose the right lane). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected Link nextNextLinkToVisit = null;

    /** the distribution to use. */
    private DistContinuous dist = new DistUniform(new MersenneTwister(), 0.0, 1.0);

    /**
     * Create a navigator.
     * @param gtu the link to the gtu for this navigator
     * @param generationLink the link on which the GTU is generated
     */
    public RandomLaneBasedRouteNavigator(final LaneBasedGTU gtu, final Link generationLink)
    {
        this.gtu = gtu;
        this.lastVisitedNode = generationLink.getStartNode();
        this.nextNodeToVisit = generationLink.getEndNode();
        determineNextNextNode();
    }

    /** {@inheritDoc} */
    @Override
    public final Node lastVisitedNode() throws NetworkException
    {
        return this.lastVisitedNode;
    }

    /** {@inheritDoc} */
    @Override
    public final Node nextNodeToVisit() throws NetworkException
    {
        return this.nextNodeToVisit;
    }

    /**
     * @return nnnode
     */
    public final Node nextNextNodeToVisit()
    {
        return this.nextNextNodeToVisit;
    }

    /** {@inheritDoc} */
    @Override
    public final Node visitNextNode() throws NetworkException
    {
        this.lastVisitedNode = this.nextNodeToVisit;
        this.nextNodeToVisit = this.nextNextNodeToVisit;
        determineNextNextNode();
        return this.nextNodeToVisit;
    }

    /**
     * Choose a random link out. Equal probabilities.
     */
    protected void determineNextNextNode()
    {
        int branches = this.nextNodeToVisit.getLinksOut().size();
        if (branches == 1)
        {
            this.nextNextLinkToVisit = this.nextNodeToVisit.getLinksOut().iterator().next();
            this.nextNextNodeToVisit = this.nextNextLinkToVisit.getEndNode();
            return;
        }
        double probability = this.dist.draw();
        int i = 1;
        for (Link link : this.nextNodeToVisit.getLinksOut())
        {
            if (probability < (1.0 * i) / branches)
            {
                this.nextNextLinkToVisit = link;
                this.nextNextNodeToVisit = link.getEndNode();
                return;
            }
            i++;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel suitability(final Lane lane, final Length.Rel longitudinalPosition, final GTUType gtuType,
        final Time.Rel timeHorizon) throws NetworkException
    {
        double remainingDistance = lane.getLength().getSI() - longitudinalPosition.getSI();
        boolean laneConnectedToDestination = false;
        boolean laneIsMyLane = this.gtu.positions(this.gtu.getFront()).containsKey(lane);
        for (Lane nextLane : lane.nextLanes(gtuType))
        {
            if (nextLane.getParentLink().equals(this.nextNextLinkToVisit))
                laneConnectedToDestination = true;
        }
        if ((lane.nextLanes(gtuType) == null || lane.nextLanes(gtuType).size() == 0))
        {
            if (laneIsMyLane)
                return new Length.Rel(500.0, LengthUnit.METER);
            else
                return new Length.Rel(remainingDistance, LengthUnit.METER);
        }

        if (laneIsMyLane)
        {
            if (laneConnectedToDestination)
                return new Length.Rel(500.0, LengthUnit.METER);
            else
                return new Length.Rel(remainingDistance, LengthUnit.METER);
        }
        else // not my lane
        {
            if (!laneConnectedToDestination)
                return new Length.Rel(500.0, LengthUnit.METER);
            else
                return new Length.Rel(remainingDistance, LengthUnit.METER);
        }
    }

}
