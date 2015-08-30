package org.opentrafficsim.core.network.route;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;

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
     * @param gtuType GTUType; the type of the GTU (used to check lane compatibility of lanes)
     * @param timeHorizon DoubleScalar.Rel&lt;TimeUnit&gt;; the maximum time that a driver may want to look ahead
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; a value that indicates within what distance the GTU should try to vacate this
     *         lane.
     * @throws NetworkException on network inconsistency, or when the continuation Link at a branch cannot be determined
     */
    DoubleScalar.Rel<LengthUnit> suitability(final Lane lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition,
        final GTUType gtuType, final DoubleScalar.Rel<TimeUnit> timeHorizon) throws NetworkException;
}
