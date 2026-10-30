package org.opentrafficsim.road.network;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.djutils.immutablecollections.ImmutableTreeSet;
import org.djutils.multikeymap.MultiKeyMap;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;

/**
 * RoadNetwork adds the ability to retrieve lane change information.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class RoadNetwork extends Network
{
    /** Cached lane graph for legal connections, per GTU type. */
    private Map<GtuType, RouteWeightedGraph> legalLaneGraph = new LinkedHashMap<>();

    /** Cached lane graph for physical connections. */
    private RouteWeightedGraph physicalLaneGraph = null;

    /** Cached legal lane change info, over complete length of route. */
    private MultiKeyMap<NavigableSet<LaneChangeInfo>> legalLaneChangeInfoCache =
            new MultiKeyMap<>(GtuType.class, Route.class, LaneChangeTiming.class, Lane.class);

    /** Cached physical lane change info, over complete length of route. */
    private MultiKeyMap<NavigableSet<LaneChangeInfo>> physicalLaneChangeInfoCache =
            new MultiKeyMap<>(Route.class, LaneChangeTiming.class, Lane.class);

    /**
     * Construction of an empty network.
     * @param id the network id.
     * @param simulator the DSOL simulator engine
     */
    public RoadNetwork(final String id, final OtsSimulatorInterface simulator)
    {
        super(id, simulator);
    }

    /**
     * Returns lane change info from the given lane. Distances are given from the start of the lane and will never exceed the
     * given range. This method returns {@code null} if no valid path exists. If there are no reasons to change lane within
     * range, an empty set is returned.
     * @param lane from lane
     * @param route route
     * @param timing lane change timing
     * @param gtuType GTU Type
     * @param range maximum range of info to consider, from the start of the given lane
     * @param laneAccessLaw lane access law
     * @return lane change info from the given lane, or empty if no path exists
     */
    public ImmutableSortedSet<LaneChangeInfo> getLaneChangeInfo(final Lane lane, final Route route,
            final LaneChangeTiming timing, final GtuType gtuType, final Length range, final LaneAccessLaw laneAccessLaw)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        Throw.whenNull(route, "Route may not be null.");
        Throw.whenNull(gtuType, "GTU type may not be null.");
        Throw.whenNull(range, "Range may not be null.");
        Throw.whenNull(laneAccessLaw, "Lane access law may not be null.");
        Throw.when(range.le0(), IllegalArgumentException.class, "Range should be a positive value.");

        // get the complete info
        NavigableSet<LaneChangeInfo> info = getCompleteLaneChangeInfo(lane, route, timing, gtuType, laneAccessLaw);
        if (info == null)
        {
            return new ImmutableTreeSet<>(Collections.emptySet());
        }
        return new ImmutableTreeSet<>(
                info.headSet(new LaneChangeInfo(Integer.MAX_VALUE, range, false, LateralDirectionality.NONE), true),
                Immutable.WRAP);
    }

    /**
     * Returns the complete (i.e. without range) lane change info from the given lane. It is either taken from cache, or
     * created.
     * @param lane from lane
     * @param route route
     * @param timing lane change timing
     * @param gtuType GTU Type
     * @param laneAccessLaw lane access law
     * @return complete (i.e. without range) lane change info from the given lane, or {@code null} if no path exists
     */
    private NavigableSet<LaneChangeInfo> getCompleteLaneChangeInfo(final Lane lane, final Route route,
            final LaneChangeTiming timing, final GtuType gtuType, final LaneAccessLaw laneAccessLaw)
    {
        return switch (laneAccessLaw)
        {
            case LEGAL -> determineLaneChangeInfo(lane, getLegalGraph(gtuType), gtuType, route, timing,
                    (l) -> this.legalLaneChangeInfoCache.get(gtuType, route, timing, l),
                    (l, info) -> this.legalLaneChangeInfoCache.put(info, gtuType, route, timing, l));
            case PHYSICAL -> determineLaneChangeInfo(lane, getPhysicalGraph(), gtuType, route, timing,
                    (l) -> this.physicalLaneChangeInfoCache.get(route, timing, l),
                    (l, info) -> this.physicalLaneChangeInfoCache.put(info, route, timing, l));
        };
    }

    /**
     * Returns legal graph representation of lane network.
     * @param gtuType GTU type
     * @return legal graph representation of lane network
     */
    private RouteWeightedGraph getLegalGraph(final GtuType gtuType)
    {
        RouteWeightedGraph graph = this.legalLaneGraph.get(gtuType);
        if (graph == null)
        {
            graph = new RouteWeightedGraph();
            this.legalLaneGraph.put(gtuType, graph);
            buildGraph(graph, gtuType, LaneAccessLaw.LEGAL);
        }
        return graph;
    }

    /**
     * Returns physical graph representation of lane network.
     * @return physical graph representation of lane network
     */
    private RouteWeightedGraph getPhysicalGraph()
    {
        if (this.physicalLaneGraph == null)
        {
            this.physicalLaneGraph = new RouteWeightedGraph();
            buildGraph(this.physicalLaneGraph, null, LaneAccessLaw.PHYSICAL);
        }
        return this.physicalLaneGraph;
    }

    /**
     * Determines the lane change information from the given lane following the given route. This methods uses caching.
     * @param lane lane
     * @param graph graph representation of lane network
     * @param gtuType GTU type, for physical only relevant to find shortest path when route is {@code null}
     * @param route route, may be {@code null}
     * @param timing lane change timing
     * @param cacheGet get from cache (may return {@code null} if not in cache)
     * @param cachePut put in cache
     * @return lane change information from the given lane following the given route
     */
    private NavigableSet<LaneChangeInfo> determineLaneChangeInfo(final Lane lane, final RouteWeightedGraph graph,
            final GtuType gtuType, final Route route, final LaneChangeTiming timing,
            final Function<Lane, NavigableSet<LaneChangeInfo>> cacheGet,
            final BiConsumer<Lane, NavigableSet<LaneChangeInfo>> cachePut)
    {
        NavigableSet<LaneChangeInfo> outputLaneChangeInfo = cacheGet.apply(lane);
        if (outputLaneChangeInfo == null)
        {
            List<LaneChangeInfoEdge> path = findPath(lane, graph, gtuType, route, timing);
            if (path != null)
            {
                boolean originalPath = true;
                while (!path.isEmpty())
                {
                    NavigableSet<LaneChangeInfo> laneChangeInfo = extractLaneChangeInfo(path, timing);
                    if (originalPath)
                    {
                        outputLaneChangeInfo = laneChangeInfo;
                        originalPath = false;
                    }
                    cachePut.accept(path.get(0).fromLane(), laneChangeInfo);
                    path.remove(0); // build laneChangeInfo and cache for next lane
                }
            }
        }
        return outputLaneChangeInfo;
    }

    /**
     * Builds the graph.
     * @param graph empty graph to build
     * @param gtuType GTU type
     * @param laneChangeLaw lane change law, legal or physical
     */
    private void buildGraph(final RouteWeightedGraph graph, final GtuType gtuType, final LaneAccessLaw laneChangeLaw)
    {
        // add vertices
        boolean legal = laneChangeLaw.equals(LaneAccessLaw.LEGAL);
        for (Link link : this.getLinkMap().values())
        {
            for (Lane lane : legal ? ((CrossSectionLink) link).getLanes() : ((CrossSectionLink) link).getLanesAndShoulders())
            {
                graph.addVertex(lane);
            }
            // each end node may be a destination for the shortest path search
            graph.addVertex(link.getEndNode());
        }

        // add edges
        for (Link link : this.getLinkMap().values())
        {
            if (link instanceof CrossSectionLink cLink)
            {
                for (Lane lane : legal ? cLink.getLanes() : cLink.getLanesAndShoulders())
                {
                    // adjacent lanes
                    for (LateralDirectionality lat : List.of(LateralDirectionality.LEFT, LateralDirectionality.RIGHT))
                    {
                        Set<Lane> adjacentLanes;
                        if (legal)
                        {
                            adjacentLanes = lane.accessibleAdjacentLanesLegal(lat, gtuType);
                        }
                        else
                        {
                            adjacentLanes = lane.accessibleAdjacentLanesPhysical(lat, gtuType);
                        }
                        for (Lane adjacentLane : adjacentLanes)
                        {
                            LaneChangeInfoEdgeType type = lat.equals(LateralDirectionality.LEFT) ? LaneChangeInfoEdgeType.LEFT
                                    : LaneChangeInfoEdgeType.RIGHT;
                            // downstream link may be null for lateral edges
                            LaneChangeInfoEdge edge = new LaneChangeInfoEdge(lane, type, null);
                            graph.addEdge(lane, adjacentLane, edge);
                        }
                    }
                    // next lanes
                    Set<Lane> nextLanes = lane.nextLanes(legal ? gtuType : null);
                    for (Lane nextLane : nextLanes)
                    {
                        LaneChangeInfoEdge edge =
                                new LaneChangeInfoEdge(lane, LaneChangeInfoEdgeType.DOWNSTREAM, nextLane.getLink());
                        graph.addEdge(lane, nextLane, edge);
                    }
                    // add edge towards end node so that it can be used as a destination in the shortest path search
                    LaneChangeInfoEdge edge = new LaneChangeInfoEdge(lane, LaneChangeInfoEdgeType.DOWNSTREAM, null);
                    graph.addEdge(lane, lane.getLink().getEndNode(), edge);
                }
            }
        }
    }

    /**
     * Returns a set of lane change info, extracted from the graph.
     * @param lane from lane
     * @param graph graph
     * @param gtuType GTU Type
     * @param route route
     * @param timing lane change timing
     * @return path derived from the graph, or {@code null} if there is no path.
     */
    private List<LaneChangeInfoEdge> findPath(final Lane lane, final RouteWeightedGraph graph, final GtuType gtuType,
            final Route route, final LaneChangeTiming timing)
    {
        // if there is no route, find the destination node by moving down the links (no splits allowed)
        Node destination = null;
        Route routeForWeights = route;
        if (route == null)
        {
            destination = graph.getNoRouteDestinationNode();
            try
            {
                routeForWeights = getShortestRouteBetween(gtuType, lane.getLink().getStartNode(), destination);
            }
            catch (NetworkException exception)
            {
                // this should not happen, as we obtained the destination by moving downstream towards the end of the network
                throw new OtsRuntimeException("Could not find route to destination.", exception);
            }
        }
        else
        {
            // otherwise, get destination node from route, which is the last node on a link with lanes (i.e. no connector)
            List<Node> nodes = route.getNodes();
            for (int i = nodes.size() - 1; i > 0; i--)
            {
                Link link = getLink(nodes.get(i - 1), nodes.get(i))
                        .orElseThrow(() -> new OtsRuntimeException("Unable to find link for two consecutive nodes in route."));
                if (link instanceof CrossSectionLink cLink && !cLink.getLanes().isEmpty())
                {
                    destination = nodes.get(i);
                    break; // found most downstream link with lanes, who's end node is the destination for lane changes
                }
            }
            Throw.whenNull(destination, "Route has no links with lanes, "
                    + "unable to find a suitable destination node regarding lane change information.");
        }

        // set the route on the path for route-dependent edge weights
        graph.setWeightInformation(routeForWeights, timing);

        // find the shortest path
        GraphPath<Identifiable, LaneChangeInfoEdge> path = DijkstraShortestPath.findPathBetween(graph, lane, destination);
        return path == null ? null : path.getEdgeList();
    }

    /**
     * Extracts lane change info from a path.
     * @param path path
     * @param timing lane change timing
     * @return lane change info
     */
    private NavigableSet<LaneChangeInfo> extractLaneChangeInfo(final List<LaneChangeInfoEdge> path,
            final LaneChangeTiming timing)
    {
        NavigableSet<LaneChangeInfo> info = new TreeSet<>();
        Length x = Length.ZERO; // cumulative longitudinal distance
        int n = 0; // number of applied lane changes
        boolean inLateralState = false; // consecutive lateral moves in the path create 1 LaneChangeInfo
        boolean lateralMoveIsDeadEnd = false;
        for (LaneChangeInfoEdge edge : path)
        {
            LaneChangeInfoEdgeType lcType = edge.laneChangeInfoEdgeType();
            int lat = lcType.equals(LaneChangeInfoEdgeType.LEFT) ? -1 : (lcType.equals(LaneChangeInfoEdgeType.RIGHT) ? 1 : 0);

            // check opposite lateral direction
            if (n * lat < 0)
            {
                /*
                 * The required direction is opposite a former required direction, in which case all further lane change
                 * information is not yet of concern. For example, we first need to make 1 right lane change for a lane drop,
                 * and then later 2 lane changes to the left for a split. The latter information is pointless before the lane
                 * drop; we are not going to stay on the lane longer as it won't affect the ease of the left lane changes later.
                 */
                break;
            }

            // increase n, x, and trigger (consecutive) lateral move start or stop
            if (lat == 0)
            {
                // lateral move stop
                if (inLateralState)
                {
                    info.add(new LaneChangeInfo(Math.abs(n), x, lateralMoveIsDeadEnd,
                            n < 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT));
                    inLateralState = false;
                    if (LaneChangeTiming.EARLIEST_FEASIBLE.equals(timing))
                    {
                        // need to add length of last lane of all lateral moves for next info
                        x = x.plus(edge.fromLane().getLength());
                    }
                    // don't add the length for LATEST_FEASIBLE, that was already done for the first lane of all lateral moves
                }
                else
                {
                    // longitudinal move, we need to add distance to x
                    x = x.plus(edge.fromLane().getLength());
                }
            }
            else
            {
                // lateral move start
                if (!inLateralState)
                {
                    lateralMoveIsDeadEnd = edge.fromLane().nextLanes(null).isEmpty();
                    if (LaneChangeTiming.LATEST_FEASIBLE.equals(timing))
                    {
                        x = x.plus(edge.fromLane().getLength()); // need to add length of first lane of all lateral moves
                    }
                    inLateralState = true;
                }
                // increase lane change count (negative for left)
                n += lat;
            }
        }
        return info;
    }

    /**
     * Clears all lane change info graphs and cached sets. This method should be invoked on every network change that affects
     * lane changes and the distances within which they need to be performed.
     */
    public void clearLaneChangeInfoCache()
    {
        this.legalLaneGraph.clear();
        this.physicalLaneGraph = null;
        this.legalLaneChangeInfoCache = new MultiKeyMap<>(GtuType.class, Route.class, LaneChangeTiming.class, Lane.class);
        this.physicalLaneChangeInfoCache = new MultiKeyMap<>(Route.class, LaneChangeTiming.class, Lane.class);
    }

    /**
     * Timing of lane changes regarding lane change information.
     */
    public enum LaneChangeTiming
    {
        /** As soon as possible. */
        EARLIEST_FEASIBLE,

        /** As late as possible. */
        LATEST_FEASIBLE;
    }

    /**
     * A {@link SimpleDirectedWeightedGraph} to search over the lanes, where the weight of an edge (movement between lanes) is
     * tailored to providing lane change information. The vertex type is {@link Identifiable} such that both {@link Lane}'s and
     * {@link Node}'s can be used. The latter is required to find paths towards a destination node.
     */
    private class RouteWeightedGraph extends SimpleDirectedWeightedGraph<Identifiable, LaneChangeInfoEdge>
    {

        /** Serialization version UID. */
        private static final long serialVersionUID = 20220923L;

        /** Route. */
        private Route route;

        /** Timing for the lane change information. */
        private LaneChangeTiming timing;

        /** Node in the network that is the destination if no route is used. */
        private Node noRouteDestination = null;

        /**
         * Constructor.
         */
        RouteWeightedGraph()
        {
            super(LaneChangeInfoEdge.class);
        }

        /**
         * Set the route.
         * @param route route
         * @param timing lane change timing
         */
        @SuppressWarnings("hiddenfield")
        public void setWeightInformation(final Route route, final LaneChangeTiming timing)
        {
            this.route = route;
            this.timing = timing;
        }

        /**
         * Returns the weight of moving from one lane to the next. In order to find the earliest/latest possible location at
         * which lane changes may be performed, the longitudinal weights are 1.0 while the lateral weights are 1.0 - 1/(X+1) for
         * earliest and 1.0 + 1/(X+1) for latest lane changes, where X is the number (index) of the link within the route. This
         * favors earlier/later lane changes for the shortest path algorithm, as we are interested in the distances within which
         * the lane changes can or have to be performed. In the case an edge is towards a link that is not in a given route, a
         * positive infinite weight is returned. Finally, when the edge is towards a node, which may be the destination in a
         * route, 0.0 is returned.
         */
        @Override
        public double getEdgeWeight(final LaneChangeInfoEdge e)
        {
            if (e.laneChangeInfoEdgeType().equals(LaneChangeInfoEdgeType.LEFT)
                    || e.laneChangeInfoEdgeType().equals(LaneChangeInfoEdgeType.RIGHT))
            {
                int indexEndNode = this.route.indexOf(e.fromLane().getLink().getEndNode());
                if (indexEndNode < 0)
                {
                    return Double.POSITIVE_INFINITY;
                }
                double sign = LaneChangeTiming.EARLIEST_FEASIBLE.equals(this.timing) ? -1.0 : 1.0;
                return 1.0 + sign / (indexEndNode + 1); // earliest -> reduce weight for nearer lane changes
            }
            Link toLink = e.toLink();
            if (toLink == null)
            {
                return 0.0; // edge towards Node, which may be the destination in a Route
            }
            if (this.route.contains(toLink.getEndNode())
                    && this.route.indexOf(toLink.getEndNode()) == this.route.indexOf(toLink.getStartNode()) + 1)
            {
                return 1.0; // downstream, always 1.0 if the next lane is on the route
            }
            return Double.POSITIVE_INFINITY; // next lane not on the route, this is a dead-end branch for the route
        }

        /**
         * Returns the destination node to use when no route is available. This will be the last node found moving downstream.
         * @return destination node to use when no route is available.
         */
        public Node getNoRouteDestinationNode()
        {
            if (this.noRouteDestination == null)
            {
                // get any lane from the network
                Lane lane = null;
                Iterator<Identifiable> iterator = this.vertexSet().iterator();
                while (lane == null && iterator.hasNext())
                {
                    Identifiable next = iterator.next();
                    if (next instanceof Lane)
                    {
                        lane = (Lane) next;
                    }
                }
                Throw.when(lane == null, OtsRuntimeException.class, "Requesting destination node on network without lanes.");
                // move to downstream link for as long as there is 1 downstream link
                this.noRouteDestination = getEndNode(lane.getLink());
            }
            return this.noRouteDestination;
        }

        /**
         * Finds the next link along a corridor. The end-node of the input link should have only two links connected.
         * @param link link
         * @return the next link along a corridor
         */
        private Node getEndNode(final Link link)
        {
            Set<Link> set = link.getEndNode().getLinks().stream().filter((l) -> l.getStartNode().equals(link.getEndNode())
                    && l instanceof CrossSectionLink cLink && !cLink.getLanes().isEmpty()).collect(Collectors.toSet());
            Throw.when(set.size() > 1, OtsRuntimeException.class,
                    "Using null route on network with split. Unable to find a destination to find lane change info towards.");
            return set.isEmpty() ? link.getEndNode() : getEndNode(set.iterator().next());
        }

    }

    /**
     * Edge between two lanes, or between a lane and a node (to provide the shortest path algorithm with a suitable
     * destination). From a list of these from a path, the lane change information along the path (distances and number of lane
     * changes) can be derived.
     * @param fromLane from lane, to allow construction of distances from a path
     * @param laneChangeInfoEdgeType the type of lane to lane movement performed along this edge
     * @param toLink to link (of the lane this edge moves to), or {@code null} for an edge to a node
     */
    private record LaneChangeInfoEdge(Lane fromLane, LaneChangeInfoEdgeType laneChangeInfoEdgeType, Link toLink)
    {
    }

    /**
     * Enum to provide information on the lane to lane movement in a path.
     */
    private enum LaneChangeInfoEdgeType
    {
        /** Left lane change. */
        LEFT,

        /** Right lane change. */
        RIGHT,

        /** Downstream movement, either towards a lane, or towards a node (which may be the destination in a route). */
        DOWNSTREAM;
    }

}
