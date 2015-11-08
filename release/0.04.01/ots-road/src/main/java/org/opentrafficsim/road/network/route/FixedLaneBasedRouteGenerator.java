package org.opentrafficsim.road.network.route;

import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedLaneBasedRouteGenerator extends FixedRouteGenerator implements LaneBasedRouteGenerator
{

    /**
     * @param route the route to use in the generation
     */
    public FixedLaneBasedRouteGenerator(final CompleteRoute route)
    {
        super(route);
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedRouteNavigator generateRouteNavigator()
    {
        return new CompleteLaneBasedRouteNavigator(getRoute());
    }

}
