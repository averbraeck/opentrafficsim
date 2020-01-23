package org.opentrafficsim.road.gtu.strategical.route;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.NestedCache;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generates a route by determining one. This class is different from {@code RouteGenerator} in that it has the origin,
 * destination and GTU type as input.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface RouteGeneratorOD
{
    /** No route route generator. */
    RouteGeneratorOD NULL = new RouteGeneratorOD()
    {
        @Override
        public Route getRoute(final Node origin, final Node destination, final GTUType gtuType)
        {
            return null;
        }
    };

    /** Cache of default route generators per stream. */
    Map<StreamInterface, RouteGeneratorOD> DEFAULT_MAP = new LinkedHashMap<>();

    /**
     * Returns a default route generator for shortest routes based on the given stream.
     * @param stream StreamInterface; random number stream
     * @return RouteSupplier; default route generator for shortest routes based on the given stream
     */
    static RouteGeneratorOD getDefaultRouteSupplier(final StreamInterface stream)
    {
        RouteGeneratorOD def = DEFAULT_MAP.get(stream);
        if (def == null)
        {
            def = new DefaultRouteGenerator(stream);
            DEFAULT_MAP.put(stream, def);
        }
        return def;
    }

    /** Shortest route route generator. */
    class DefaultRouteGenerator implements RouteGeneratorOD
    {
        /** Shortest route cache. */
        private NestedCache<Route> shortestRouteCache = new NestedCache<>(GTUType.class, Node.class, Node.class, List.class);

        /** Stream of random numbers. */
        private final StreamInterface stream;

        /**
         * Constructor.
         * @param stream StreamInterface; stream of random numbers
         */
        public DefaultRouteGenerator(final StreamInterface stream)
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
            // XXX make silent, as the higher level method should draw another destination if the route does not exist 
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
