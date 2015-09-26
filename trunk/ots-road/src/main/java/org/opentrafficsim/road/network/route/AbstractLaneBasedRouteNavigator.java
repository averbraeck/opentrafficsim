package org.opentrafficsim.road.network.route;

import java.io.Serializable;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
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
public abstract class AbstractLaneBasedRouteNavigator implements LaneBasedRouteNavigator, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** Return value of suitability when no lane change is required withing the time horizon. */
    public static final Length.Rel NOLANECHANGENEEDED = new Length.Rel(Double.MAX_VALUE, LengthUnit.SI);

    /** Return value of suitability when a lane change is required <i>right now</i>. */
    public static final Length.Rel GETOFFTHISLANENOW = new Length.Rel(0, LengthUnit.SI);

    /**
     * Compute the suitability of a lane from which lane changes are required to get to the next point on the Route.<br>
     * This method weighs the suitability of the nearest suitable lane by (m - n) / m where n is the number of lane changes
     * required and m is the total number of lanes in the CrossSectionLink.
     * @param startLane Lane; the current lane of the GTU
     * @param remainingDistance double; distance in m of GTU to first branch
     * @param suitabilities Map&lt;Lane, Double&gt;; the set of suitable lanes and their suitability
     * @param totalLanes integer; total number of lanes compatible with the GTU type
     * @param direction LateralDirectionality; the direction of the lane changes to attempt
     * @param gtuType GTUType; the type of the GTU
     * @return double; the suitability of the <cite>startLane</cite> for following the Route
     */
    protected final Length.Rel computeSuitabilityWithLaneChanges(final Lane startLane, final double remainingDistance,
        final Map<Lane, Length.Rel> suitabilities, final int totalLanes, final LateralDirectionality direction,
        final GTUType gtuType)
    {
        /*-
         * The time per required lane change seems more relevant than distance per required lane change.
         * Total time required does not grow linearly with the number of required lane changes. Logarithmic, arc tangent 
         * is more like it.
         * Rijkswaterstaat appears to use a fixed time for ANY number of lane changes (about 60s). 
         * TomTom navigation systems give more time (about 90s).
         * In this method the returned suitability decreases linearly with the number of required lane changes. This
         * ensures that there is a gradient that coaches the GTU towards the most suitable lane.
         */
        int laneChangesUsed = 0;
        Lane currentLane = startLane;
        Length.Rel currentSuitability = null;
        while (null == currentSuitability)
        {
            laneChangesUsed++;
            if (currentLane.accessibleAdjacentLanes(direction, gtuType).size() == 0)
            {
                return GETOFFTHISLANENOW;
            }
            currentLane = currentLane.accessibleAdjacentLanes(direction, gtuType).iterator().next();
            currentSuitability = suitabilities.get(currentLane);
        }
        double fraction = currentSuitability == NOLANECHANGENEEDED ? 0 : 0.5;
        int notSuitableLaneCount = totalLanes - suitabilities.size();
        return new Length.Rel(remainingDistance * (notSuitableLaneCount - laneChangesUsed + 1 + fraction)
            / (notSuitableLaneCount + fraction), LengthUnit.SI);
    }

    /**
     * Determine how many lanes on a CrossSectionLink are compatible with a particular GTU type.<br>
     * TODO: this method should probably be moved into the CrossSectionLink class
     * @param link CrossSectionLink; the link
     * @param gtuType GTUType; the GTU type
     * @return integer; the number of lanes on the link that are compatible with the GTU type
     */
    protected final int countCompatibleLanes(final CrossSectionLink link, final GTUType gtuType)
    {
        int result = 0;
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().isCompatible(gtuType))
                {
                    result++;
                }
            }
        }
        return result;
    }
}
