package org.opentrafficsim.road.gtu.strategical.route;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.NestedCache;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.Throw;

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
    RouteSupplier NULL = new RouteSupplier()
    {
        @Override
        public Route getRoute(final Node origin, final Node destination, final GTUType gtuType)
        {
            return null;
        }
    };

    /** Cache of default route suppliers per stream. */
    Map<StreamInterface, RouteSupplier> DEFAULT_MAP = new LinkedHashMap<>();

    /**
     * Returns a default route supplier for shortest routes based on the given stream.
     * @param stream StreamInterface; random number stream
     * @return RouteSupplier; default route supplier for shortest routes based on the given stream
     */
    static RouteSupplier getDefaultRouteSupplier(final StreamInterface stream)
    {
        RouteSupplier def = DEFAULT_MAP.get(stream);
        if (def == null)
        {
            def = new DefaultRouteSupplier(stream);
            DEFAULT_MAP.put(stream, def);
        }
        return def;
    }

    /** Shortest route route supplier. */
    class DefaultRouteSupplier implements RouteSupplier
    {
        /** Shortest route cache. */
        private NestedCache<Route> shortestRouteCache = new NestedCache<>(GTUType.class, Node.class, Node.class, List.class);

        /** Stream of random numbers. */
        private final StreamInterface stream;

        /**
         * Constructor.
         * @param stream StreamInterface; stream of random numbers
         */
        public DefaultRouteSupplier(final StreamInterface stream)
        {
            Throw.whenNull(stream, "Stream may not be null.");
            this.stream = stream;
        }

        @Override
        public Route getRoute(final Node origin, final Node destination, final GTUType gtuType)
        {
            List<Node> viaNodes = new ArrayList<>();
            double cumulWeight = 0.0;
            List<Double> weights = new ArrayList<>();
            Map<Link, Double> links = new LinkedHashMap<>();
            for (Link link : destination.getLinks())
            {
                GTUDirectionality direction =
                        link.getEndNode().equals(destination) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
                if (link.getLinkType().isConnector() && link.getDirectionality(gtuType).permits(direction)
                        && link instanceof CrossSectionLink && ((CrossSectionLink) link).getDemandWeight() != null)
                {
                    Double weight = ((CrossSectionLink) link).getDemandWeight();
                    weights.add(weight);
                    links.put(link, weight);
                    cumulWeight += weight;
                }
            }
            if (cumulWeight > 0.0)
            {
                Link via = Draw.drawWeighted(links, this.stream);
                viaNodes.add(via.getStartNode().equals(destination) ? via.getEndNode() : via.getStartNode());
            }
            return this.shortestRouteCache.getValue(
                    () -> Try.assign(() -> origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination, viaNodes),
                            "Could not determine the shortest route from %s to %s via %s.", origin, destination, viaNodes),
                    gtuType, origin, destination, viaNodes);
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
