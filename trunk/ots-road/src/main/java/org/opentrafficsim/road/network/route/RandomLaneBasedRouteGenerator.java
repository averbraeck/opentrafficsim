package org.opentrafficsim.road.network.route;

import org.opentrafficsim.core.network.Link;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class RandomLaneBasedRouteGenerator implements LaneBasedRouteGenerator
{
    /** the generation link. */
    private final Link link;

    /**
     * @param link the link to use in the generation
     */
    public RandomLaneBasedRouteGenerator(final Link link)
    {
        this.link = link;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedRouteNavigator generateRouteNavigator()
    {
        return new RandomLaneBasedRouteNavigator(this.link);
    }

}
