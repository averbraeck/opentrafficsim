package org.opentrafficsim.road.gtu.strategical.route;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.NestedCache;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * Supplies a route by determining one.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface RouteSupplier
{
    /** No route route supplier. */
    public static RouteSupplier NULL = new RouteSupplier()
    {
        @Override
        public Route getRoute(final Node origin, final Node destination, final GTUType gtuType)
        {
            return null;
        }
    };

    /** Shortest route route supplier. */
    public static RouteSupplier SHORTEST = new RouteSupplier()
    {
        /** Shortest route cache. */
        private NestedCache<Route> shortestRouteCache = new NestedCache<>(GTUType.class, Node.class, Node.class);

        @Override
        public Route getRoute(final Node origin, final Node destination, final GTUType gtuType)
        {
            return this.shortestRouteCache.getValue(
                    () -> Try.assign(() -> origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination),
                            "Could not determine the shortest route from %s to %s.", origin, destination),
                    gtuType, origin, destination);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ShortestRouteGTUCharacteristicsGeneratorOD [shortestRouteCache=" + this.shortestRouteCache + "]";
        }
    };

    /**
     * Returns a route.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param gtuType GTUType; gtu type
     * @return Route; route
     */
    Route getRoute(Node origin, Node destination, GTUType gtuType);
}
