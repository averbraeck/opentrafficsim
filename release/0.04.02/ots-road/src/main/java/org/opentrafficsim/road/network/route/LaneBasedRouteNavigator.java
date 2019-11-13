package org.opentrafficsim.road.network.route;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.RouteNavigator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * The LaneBasedRouteNavigator interface defines helper methods are available to see if the GTU needs to change lanes to reach
 * the next link on the route.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedRouteNavigator extends RouteNavigator
{
    /**
     * Determine the suitability of being at a particular longitudinal position in a particular Lane for following this Route.
     * @param lane Lane; the lane to consider
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position in the lane
     * @param gtu GTU; the GTU (used to check lane compatibility of lanes, and current lane the GTU is on)
     * @param timeHorizon DoubleScalar.Rel&lt;TimeUnit&gt;; the maximum time that a driver may want to look ahead
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; a value that indicates within what distance the GTU should try to vacate this
     *         lane.
     * @throws NetworkException on network inconsistency, or when the continuation Link at a branch cannot be determined
     */
    Length.Rel suitability(final Lane lane, final Length.Rel longitudinalPosition, final LaneBasedGTU gtu,
        final Time.Rel timeHorizon) throws NetworkException;
}