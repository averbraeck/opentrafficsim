package nl.grontmij.smarttraffic.lane;

import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.LaneBasedRouteNavigator;

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
public class StraightRouteNavigator extends LaneBasedRouteNavigator
{

    /**
     * @param completeRoute
     */
    public StraightRouteNavigator(CompleteRoute<?, ?> completeRoute)
    {
        super(completeRoute);
    }

}

