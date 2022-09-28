package org.opentrafficsim.road.gtu.strategical.route;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generates a route by determining one. This class is different from {@code RouteGenerator} in that it has the origin,
 * destination and GTU type as input.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
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
        private MultiKeyMap<Route> shortestRouteCache = new MultiKeyMap<>(GTUType.class, Node.class, Node.class, List.class);

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
            boolean directLinkExists = false;
            for (Link link : destination.getLinks())
            {
                GTUDirectionality direction =
                        link.getEndNode().equals(destination) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
                if (link.getLinkType().isConnector() && link.getDirectionality(gtuType).permits(direction)
                        && link instanceof CrossSectionLink && ((CrossSectionLink) link).getDemandWeight() != null)
                {
                    // Verify there is a route from origin to this link
                    List<Node> testViaNode = new ArrayList<>();
                    Node linkEntryNode = direction.isPlus() ? link.getStartNode() : link.getEndNode();
                    testViaNode.add(linkEntryNode);
                    try
                    {
                        if (origin.getNetwork().getShortestRouteBetween(gtuType, origin, destination, viaNodes) != null)
                        {
                            Double weight = ((CrossSectionLink) link).getDemandWeight();
                            weights.add(weight);
                            links.put(link, weight);
                            cumulWeight += weight;
                        }
                        else
                        {
                            System.out.println("No route from origin to link; NOT including link " + link);
                        }
                    }
                    catch (NetworkException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (link.getDirectionality(gtuType).permits(direction)
                        && (link.getStartNode().equals(origin) || link.getEndNode().equals(origin)))
                {
                    directLinkExists = true;
                }
            }
            if (cumulWeight > 0.0 && links.size() > 1 && (!directLinkExists))
            {
                System.out.println("Need to select access point to destination from " + links.size() + " options:");
                for (Link link : links.keySet())
                {
                    System.out.println(" " + link);
                }
                Link via = Draw.drawWeighted(links, this.stream);
                // System.out.println("selected via " + via);
                if (via.getEndNode().equals(destination))
                {
                    // System.out
                    // .println("using start node to force use of randomly selected access point to destination centroid");
                    viaNodes.add(via.getStartNode());
                }
                else if (via.getStartNode().equals(destination))
                {
                    // System.out.println("using end node to force use of randomly selected access point to destination
                    // centroid");
                    viaNodes.add(via.getEndNode());
                }
                else
                {
                    // System.out.println("using end node (could also have used start node) to force use of randomly "
                    // + "selected access point to destination centroid");
                    viaNodes.add(via.getEndNode());
                }
                if (viaNodes.size() > 0 && viaNodes.get(0).getId().startsWith("Centroid "))
                {
                    System.out.println("oops:   via node is a centroid");
                }
                System.out.println("Selected via node(s) " + viaNodes);
            }
            // XXX make silent, as the higher level method should draw another destination if the route does not exist
            return this.shortestRouteCache.get(
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
