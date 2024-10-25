package org.opentrafficsim.core.network;

import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;

/**
 * Interface to determine a link weight.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LinkWeight
{

    /** Default link weight using link length. */
    LinkWeight LENGTH = new LinkWeight()
    {
        @Override
        public double getWeight(final Link link)
        {
            return link.getLength().si;
        }

        @Override
        public String toString()
        {
            return "LENGTH";
        }
    };

    /** Link weight with very high penalty for Connectors. */
    LinkWeight LENGTH_NO_CONNECTORS = new LinkWeight()
    {
        /**
         * Length that should encourage Dijkstra to not include links that have this length. On the other hand, this value (when
         * on an unavoidable link) should not cause underflow problems.
         */
        static final double PROHIBITIVE_CONNECTOR_LENGTH = 1000000;

        @Override
        public double getWeight(final Link link)
        {
            if (link.isConnector())
            {
                return PROHIBITIVE_CONNECTOR_LENGTH;
            }
            return link.getLength().si;
        }

        @Override
        public String toString()
        {
            return "LENGTH_NO_CONNECTORS";
        }
    };

    /** Link weight with very high penalty for Connectors. */
    LinkWeight ASTAR_LENGTH_NO_CONNECTORS = new LinkWeight()
    {
        @Override
        public double getWeight(final Link link)
        {
            return LENGTH_NO_CONNECTORS.getWeight(link);
        }

        @Override
        public AStarAdmissibleHeuristic<Node> getAStarHeuristic()
        {
            return EUCLIDEAN_DISTANCE;
        }

        @Override
        public String toString()
        {
            return "ASTAR_LENGTH_NO_CONNECTORS";
        }
    };

    /**
     * Heuristic for the A* algorithm that uses Euclidean distance.
     */
    AStarAdmissibleHeuristic<Node> EUCLIDEAN_DISTANCE = new AStarAdmissibleHeuristic<>()
    {
        @Override
        public double getCostEstimate(final Node sourceVertex, final Node targetVertex)
        {
            return sourceVertex.getPoint().distance(targetVertex.getPoint());
        }
    };

    /**
     * Returns the link weight.
     * @param link link
     * @return link weight
     */
    double getWeight(Link link);

    /**
     * Return a heuristic for the A* algorithm. The default value is {@code null} in which case {@code Network} will use a
     * regular Dijkstra shortest path algorithm.
     * @return heuristic for the A* algorithm, default is {@code null}.
     */
    default AStarAdmissibleHeuristic<Node> getAStarHeuristic()
    {
        return null;
    }

    /**
     * Returns whether the link weights are static. In that case caching may be done on shortest routes.
     * @return whether the link weights are static.
     */
    default boolean isStatic()
    {
        return true;
    }
}
