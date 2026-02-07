package org.opentrafficsim.road.gtu.strategical;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Connector;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkWeight;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generates a route by determining one. This class is different from {@code Generator<Route>} in that it has the origin,
 * destination and GTU type as input.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface RouteGenerator
{
    /** No route route generator. */
    RouteGenerator NULL = new RouteGenerator()
    {
        @Override
        public Route getRoute(final Node origin, final Node destination, final GtuType gtuType)
        {
            return null;
        }
    };

    /** Cache of default route generators per stream and link-weight. */
    MultiKeyMap<RouteGenerator> DEFAULT_SUPPLIERS = new MultiKeyMap<>(StreamInterface.class, LinkWeight.class);

    /**
     * Returns a default route generator for shortest routes based on the given stream.
     * @param stream random number stream
     * @param linkWeight link weight.
     * @return default route generator for shortest routes based on the given stream
     */
    static RouteGenerator getDefaultRouteSupplier(final StreamInterface stream, final LinkWeight linkWeight)
    {
        RouteGenerator def = DEFAULT_SUPPLIERS.get(stream, linkWeight);
        if (def == null)
        {
            def = new DefaultRouteGenerator(stream, linkWeight);
            DEFAULT_SUPPLIERS.put(def, stream, linkWeight);
        }
        return def;
    }

    /** Shortest route route generator. */
    class DefaultRouteGenerator implements RouteGenerator
    {
        /** Shortest route cache. */
        private final MultiKeyMap<Route> shortestRouteCache =
                new MultiKeyMap<>(GtuType.class, Node.class, Node.class, List.class);

        /** Link weight. */
        private final LinkWeight linkWeight;

        /** Stream of random numbers. */
        private final StreamInterface stream;

        /**
         * Constructor.
         * @param stream stream of random numbers
         * @param linkWeight link weight.
         */
        public DefaultRouteGenerator(final StreamInterface stream, final LinkWeight linkWeight)
        {
            Throw.whenNull(stream, "Stream may not be null.");
            Throw.whenNull(linkWeight, "Link weight may not be null.");
            this.stream = stream;
            this.linkWeight = linkWeight;
        }

        @Override
        public Route getRoute(final Node origin, final Node destination, final GtuType gtuType)
        {
            List<Node> viaNodes = new ArrayList<>();
            double cumulWeight = 0.0;
            Map<Link, Double> links = new LinkedHashMap<>();
            boolean directLinkExists = false;
            for (Link link : destination.getLinks())
            {
                if (link.isConnector() && link instanceof Connector && ((Connector) link).getDemandWeight() > 0.0)
                {
                    // Verify there is a route from origin to this link
                    List<Node> testViaNode = new ArrayList<>();
                    Node linkEntryNode = link.getStartNode();
                    testViaNode.add(linkEntryNode);
                    try
                    {
                        if (origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination, viaNodes,
                                this.linkWeight) != null)
                        {
                            Double weight = ((Connector) link).getDemandWeight();
                            links.put(link, weight);
                            cumulWeight += weight;
                        }
                    }
                    catch (NetworkException e)
                    {
                        // ignore this link
                    }
                }
                if (link.getStartNode().equals(origin) || link.getEndNode().equals(origin))
                {
                    directLinkExists = true;
                }
            }
            if (cumulWeight > 0.0 && links.size() > 1 && (!directLinkExists))
            {
                Link via = Draw.drawWeighted(links, this.stream);
                if (via.getEndNode().equals(destination))
                {
                    viaNodes.add(via.getStartNode());
                }
                else if (via.getStartNode().equals(destination))
                {
                    viaNodes.add(via.getEndNode());
                }
                else
                {
                    viaNodes.add(via.getEndNode());
                }
            }
            if (!this.linkWeight.isStatic())
            {
                return Try.assign(
                        () -> origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination, viaNodes,
                                this.linkWeight),
                        "Could not determine the shortest route from %s to %s via %s.", origin, destination, viaNodes);
            }
            return this.shortestRouteCache.get(() -> Try.assign(
                    () -> origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination, viaNodes, this.linkWeight),
                    "Could not determine the shortest route from %s to %s via %s.", origin, destination, viaNodes), gtuType,
                    origin, destination, viaNodes);
        }

        @Override
        public String toString()
        {
            return "DefaultRouteGenerator [linkWeight=" + this.linkWeight + "shortestRouteCache=" + this.shortestRouteCache
                    + "]";
        }
    };

    /**
     * Returns a route.
     * @param origin origin
     * @param destination destination
     * @param gtuType gtu type
     * @return route
     */
    Route getRoute(Node origin, Node destination, GtuType gtuType);
}
