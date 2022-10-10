package org.opentrafficsim.road.network;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.Throw;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.djutils.immutablecollections.ImmutableTreeSet;
import org.djutils.multikeymap.MultiKeyMap;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;

/**
 * OTSRoadNetwork adds a number of methods to the Network class that are specific for roads, such as the LaneTypes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class OtsRoadNetwork extends OtsNetwork implements RoadNetwork
{
    /** */
    private static final long serialVersionUID = 1L;

    /** LaneTypes registered for this network. */
    private Map<String, LaneType> laneTypeMap = new LinkedHashMap<>();

    /** Cached lane graph for legal connections, per GTU type. */
    private Map<GtuType, RouteWeightedGraph> legalLaneGraph = new LinkedHashMap<>();

    /** Cached lane graph for physical connections. */
    private RouteWeightedGraph physicalLaneGraph = null;

    /** Cached legal lane change info, over complete length of route. */
    private MultiKeyMap<SortedSet<LaneChangeInfo>> legalLaneChangeInfoCache =
            new MultiKeyMap<>(GtuType.class, Route.class, Lane.class);

    /** Cached physical lane change info, over complete length of route. */
    private MultiKeyMap<SortedSet<LaneChangeInfo>> physicalLaneChangeInfoCache = new MultiKeyMap<>(Route.class, Lane.class);

    /**
     * Construction of an empty network.
     * @param id String; the network id.
     * @param addDefaultTypes add the default GtuTypes, LinkTypesand LaneTypes, or not
     * @param simulator OTSSimulatorInterface; the DSOL simulator engine
     */
    public OtsRoadNetwork(final String id, final boolean addDefaultTypes, final OtsSimulatorInterface simulator)
    {
        super(id, addDefaultTypes, simulator);
        if (addDefaultTypes)
        {
            addDefaultLaneTypes();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addDefaultLaneTypes()
    {
        new LaneType("NONE", this);

        LaneType road = new LaneType("TWO_WAY_LANE", this);
        new LaneType("RURAL_ROAD", road, this);
        new LaneType("URBAN_ROAD", road, this);
        new LaneType("RESIDENTIAL_ROAD", road, this);
        road.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.ROAD_USER));

        LaneType oneWayLane = new LaneType("ONE_WAY_LANE", road, this);
        oneWayLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.ROAD_USER));
        oneWayLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));

        LaneType freeway = new LaneType("FREEWAY", oneWayLane, this);
        freeway.addIncompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));
        LaneType highway = new LaneType("HIGHWAY", oneWayLane, this);
        highway.addIncompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));

        LaneType busLane = new LaneType("BUS_LANE", this);
        busLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.BUS));

        LaneType mopedAndBicycleLane = new LaneType("MOPED_PATH", this);
        mopedAndBicycleLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.BICYCLE));
        mopedAndBicycleLane.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.MOPED));

        LaneType bicycleOnly = new LaneType("BICYCLE_PATH", mopedAndBicycleLane, this);
        bicycleOnly.addIncompatibleGtuType(getGtuType(GtuType.DEFAULTS.MOPED));

        LaneType pedestriansOnly = new LaneType("FOOTPATH", this);
        pedestriansOnly.addCompatibleGtuType(getGtuType(GtuType.DEFAULTS.PEDESTRIAN));
    }

    /** {@inheritDoc} */
    @Override
    public void addLaneType(final LaneType laneType)
    {
        this.laneTypeMap.put(laneType.getId(), laneType);
    }

    /** {@inheritDoc} */
    @Override
    public LaneType getLaneType(final String laneTypeId)
    {
        return this.laneTypeMap.get(laneTypeId);
    }

    /** {@inheritDoc} */
    @Override
    public LaneType getLaneType(final LaneType.DEFAULTS laneTypeEnum)
    {
        return this.laneTypeMap.get(laneTypeEnum.getId());
    }

    /** {@inheritDoc} */
    @Override
    public ImmutableMap<String, LaneType> getLaneTypes()
    {
        return new ImmutableHashMap<>(this.laneTypeMap, Immutable.WRAP);
    }

    /**
     * Returns lane change info from the given lane. Distances are given from the start of the lane and will never exceed the
     * given range. This method returns {@code null} if no valid path exists. If there are no reasons to change lane within
     * range, an empty set is returned.
     * @param lane Lane; from lane.
     * @param route Route; route.
     * @param gtuType GtuType; GTU Type.
     * @param range Length; maximum range of info to consider, from the start of the given lane.
     * @param laneAccessLaw LaneAccessLaw; lane access law.
     * @return ImmutableSortedSet&lt;LaneChangeInfo&gt;; lane change info from the given lane, or {@code null} if no path
     *         exists.
     */
    public ImmutableSortedSet<LaneChangeInfo> getLaneChangeInfo(final Lane lane, final Route route, final GtuType gtuType,
            final Length range, final LaneAccessLaw laneAccessLaw)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        Throw.whenNull(route, "Route may not be null.");
        Throw.whenNull(gtuType, "GTU type may not be null.");
        Throw.whenNull(range, "Range may not be null.");
        Throw.whenNull(laneAccessLaw, "Lane access law may not be null.");
        Throw.when(range.le0(), IllegalArgumentException.class, "Range should be a positive value.");

        // get the complete info
        SortedSet<LaneChangeInfo> info = getCompleteLaneChangeInfo(lane, route, gtuType, laneAccessLaw);
        if (info == null)
        {
            return null;
        }

        // find first LaneChangeInfo beyond range, if any
        LaneChangeInfo lcInfoBeyondHorizon = null;
        Iterator<LaneChangeInfo> iterator = info.iterator();
        while (lcInfoBeyondHorizon == null && iterator.hasNext())
        {
            LaneChangeInfo lcInfo = iterator.next();
            if (lcInfo.getRemainingDistance().gt(range))
            {
                lcInfoBeyondHorizon = lcInfo;
            }
        }

        // return subset in range
        if (lcInfoBeyondHorizon != null)
        {
            return new ImmutableTreeSet<>(info.headSet(lcInfoBeyondHorizon));
        }
        return new ImmutableTreeSet<>(info); // empty, or all in range
    }

    /**
     * Returns the complete (i.e. without range) lane change info from the given lane. It is either taken from cache, or
     * created.
     * @param lane Lane; from lane.
     * @param route Route; route.
     * @param gtuType GtuType; GTU Type.
     * @param laneAccessLaw LaneAccessLaw; lane access law.
     * @return SortedSet&lt;LaneChangeInfo&gt;; complete (i.e. without range) lane change info from the given lane, or
     *         {@code null} if no path exists.
     */
    private SortedSet<LaneChangeInfo> getCompleteLaneChangeInfo(final Lane lane, final Route route, final GtuType gtuType,
            final LaneAccessLaw laneAccessLaw)
    {
        // try to get info from the right cache
        SortedSet<LaneChangeInfo> outputLaneChangeInfo;
        if (laneAccessLaw.equals(LaneAccessLaw.LEGAL))
        {
            outputLaneChangeInfo = this.legalLaneChangeInfoCache.get(gtuType, route, lane);
            // build info if required
            if (outputLaneChangeInfo == null)
            {
                // get the right lane graph for the GTU type, or build it
                RouteWeightedGraph graph = this.legalLaneGraph.get(gtuType);
                if (graph == null)
                {
                    graph = new RouteWeightedGraph();
                    this.legalLaneGraph.put(gtuType, graph);
                    buildGraph(graph, gtuType, laneAccessLaw);
                }
                List<LaneChangeInfoEdge> path = findPath(lane, graph, gtuType, route);

                if (path != null)
                {
                    // derive lane change info from every lane along the path and cache it
                    boolean originalPath = true;
                    while (!path.isEmpty())
                    {
                        SortedSet<LaneChangeInfo> laneChangeInfo = extractLaneChangeInfo(path);
                        if (originalPath)
                        {
                            outputLaneChangeInfo = laneChangeInfo;
                            originalPath = false;
                        }
                        this.legalLaneChangeInfoCache.put(laneChangeInfo, gtuType, route, path.get(0).getFromLane());
                        path.remove(0); // next lane
                    }
                }
            }
        }
        else if (laneAccessLaw.equals(LaneAccessLaw.PHYSICAL))
        {
            outputLaneChangeInfo = this.physicalLaneChangeInfoCache.get(route, lane);
            // build info if required
            if (outputLaneChangeInfo == null)
            {
                // build the lane graph if required
                if (this.physicalLaneGraph == null)
                {
                    this.physicalLaneGraph = new RouteWeightedGraph();
                    // TODO: Is the GTU type actually relevant for physical? It is used still to find adjacent lanes.
                    buildGraph(this.physicalLaneGraph, gtuType, laneAccessLaw);
                }
                List<LaneChangeInfoEdge> path = findPath(lane, this.physicalLaneGraph, gtuType, route);

                if (path != null)
                {
                    // derive lane change info from every lane along the path and cache it
                    boolean originalPath = true;
                    while (!path.isEmpty())
                    {
                        SortedSet<LaneChangeInfo> laneChangeInfo = extractLaneChangeInfo(path);
                        if (originalPath)
                        {
                            outputLaneChangeInfo = laneChangeInfo;
                            originalPath = false;
                        }
                        this.physicalLaneChangeInfoCache.put(laneChangeInfo, route, path.get(0).getFromLane());
                        path.remove(0); // next lane
                    }
                }
            }
        }
        else
        {
            // in case it is inadvertently extended in the future
            throw new RuntimeException(String.format("Unknown LaneChangeLaw %s", laneAccessLaw));
        }
        return outputLaneChangeInfo;
    }

    /**
     * Builds the graph.
     * @param graph RouteWeightedGraph; empty graph to build.
     * @param gtuType GtuType; GTU type.
     * @param laneChangeLaw LaneChangeLaw; lane change law, legal or physical.
     */
    private void buildGraph(final RouteWeightedGraph graph, final GtuType gtuType, final LaneAccessLaw laneChangeLaw)
    {
        // add vertices
        for (Link link : this.getLinkMap().values())
        {
            for (Lane lane : ((CrossSectionLink) link).getLanes())
            {
                graph.addVertex(lane);
            }
            // each end node may be a destination for the shortest path search
            graph.addVertex(link.getEndNode());
        }

        // add edges
        for (Link link : this.getLinkMap().values())
        {
            for (Lane lane : ((CrossSectionLink) link).getLanes())
            {
                // adjacent lanes
                for (LateralDirectionality lat : List.of(LateralDirectionality.LEFT, LateralDirectionality.RIGHT))
                {
                    Set<Lane> adjacentLanes;
                    if (laneChangeLaw.equals(LaneAccessLaw.LEGAL))
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
                // TODO: nextLanes adheres to GTU type, which makes the legal/physical question invalid
                Set<Lane> nextLanes = lane.nextLanes(gtuType);
                for (Lane nextLane : nextLanes)
                {
                    LaneChangeInfoEdge edge =
                            new LaneChangeInfoEdge(lane, LaneChangeInfoEdgeType.DOWNSTREAM, nextLane.getParentLink());
                    graph.addEdge(lane, nextLane, edge);
                }
                // add edge towards end node so that it can be used as a destination in the shortest path search
                LaneChangeInfoEdge edge = new LaneChangeInfoEdge(lane, LaneChangeInfoEdgeType.DOWNSTREAM, null);
                graph.addEdge(lane, lane.getParentLink().getEndNode(), edge);
            }
        }
    }

    /**
     * Returns a set of lane change info, extracted from the graph.
     * @param lane Lane; from lane.
     * @param graph RouteWeightedGraph; graph.
     * @param gtuType GtuType; GTU Type.
     * @param route Route; route.
     * @return List&lt;LaneChangeInfoEdge&gt;; path derived from the graph, or {@code null} if there is no path.
     */
    private List<LaneChangeInfoEdge> findPath(final Lane lane, final RouteWeightedGraph graph, final GtuType gtuType,
            final Route route)
    {
        // if there is no route, find the destination node by moving down the links
        Node destination = null;
        Route routeForWeights = route;
        if (route == null)
        {
            destination = graph.getNoRouteDestinationNode(gtuType);
            try
            {
                routeForWeights = getShortestRouteBetween(gtuType, lane.getParentLink().getStartNode(), destination);
            }
            catch (NetworkException exception)
            {
                // this should not happen, as we obtained the destination by moving downstream towards the end of the network
                throw new RuntimeException("Could not find route to destination.", exception);
            }
        }
        else
        {
            // otherwise, get destination node from route, which is the last node on a link with lanes (i.e. no connector)
            List<Node> nodes = route.getNodes();
            for (int i = nodes.size() - 1; i > 0; i--)
            {
                Link link = getLink(nodes.get(i - 1), nodes.get(i));
                if (link instanceof CrossSectionLink && !((CrossSectionLink) link).getLanes().isEmpty())
                {
                    destination = nodes.get(i);
                    break; // found most downstream link with lanes, who's end node is the destination for lane changes
                }
            }
            Throw.whenNull(destination, "Route has no links with lanes, "
                    + "unable to find a suitable destination node regarding lane change information.");
        }

        // set the route on the path for route-dependent edge weights
        graph.setRoute(routeForWeights);

        // find the shortest path
        GraphPath<Identifiable, LaneChangeInfoEdge> path = DijkstraShortestPath.findPathBetween(graph, lane, destination);
        return path == null ? null : path.getEdgeList();
    }

    /**
     * Extracts lane change info from a path.
     * @param path List&lt;LaneChangeInfoEdge&gt;; path.
     * @return SortedSet&lt;LaneChangeInfo&gt;; lane change info.
     */
    private SortedSet<LaneChangeInfo> extractLaneChangeInfo(final List<LaneChangeInfoEdge> path)
    {
        SortedSet<LaneChangeInfo> info = new TreeSet<>();
        Length x = Length.ZERO; // cumulative longitudinal distance
        int n = 0; // number of applied lane changes
        boolean inLateralState = false; // consecutive lateral moves in the path create 1 LaneChangeInfo
        for (LaneChangeInfoEdge edge : path)
        {
            LaneChangeInfoEdgeType lcType = edge.getLaneChangeInfoEdgeType();
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
                    // TODO: isDeadEnd should be removed from LaneChangeInfo, behavior should consider legal vs. physical
                    boolean isDeadEnd = false;
                    info.add(new LaneChangeInfo(Math.abs(n), x, isDeadEnd,
                            n < 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT));
                    inLateralState = false;
                    // don't add the length of the previous lane, that was already done for the first lane of all lateral moves
                }
                else
                {
                    // longitudinal move, we need to add distance to x
                    x = x.plus(edge.getFromLane().getLength());
                }
            }
            else
            {
                // lateral move start
                if (!inLateralState)
                {
                    x = x.plus(edge.getFromLane().getLength()); // need to add length of first lane of all lateral moves
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
        this.legalLaneChangeInfoCache = new MultiKeyMap<>(GtuType.class, Route.class, Lane.class);
        this.physicalLaneChangeInfoCache = new MultiKeyMap<>(Route.class, Lane.class);
    }

    /**
     * A {@code SimpleDirectedWeightedGraph} to search over the lanes, where the weight of an edge (movement between lanes) is
     * tailored to providing lane change information. The vertex type is {@code Identifiable} such that both {@Lane}'s and
     * {@Node}'s can be used. The latter is required to find paths towards a destination node.<br>
     * <br>
     * Copyright (c) 2022-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project
     * is distributed under a three-clause BSD-style license, which can be found at
     * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class RouteWeightedGraph extends SimpleDirectedWeightedGraph<Identifiable, LaneChangeInfoEdge>
    {

        /** */
        private static final long serialVersionUID = 20220923L;

        /** Route. */
        private Route route;

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
         * @param route Route; route.
         */
        public void setRoute(final Route route)
        {
            Throw.whenNull(route, "Route may not be null for lane change information.");
            this.route = route;
        }

        /**
         * Returns the weight of moving from one lane to the next. In order to find the latest possible location at which lane
         * changes may still be performed, the longitudinal weights are 1.0 while the lateral weights are 1.0 + 1/X, where X is
         * the number (index) of the link within the route. This favors later lane changes for the shortest path algorithm, as
         * we are interested in the distances within which the lane change have to be performed. In the case an edge is towards
         * a link that is not in a given route, a positive infinite weight is returned. Finally, when the edge is towards a
         * node, which may be the destination in a route, 0.0 is returned.
         */
        @Override
        public double getEdgeWeight(final LaneChangeInfoEdge e)
        {
            if (e.getLaneChangeInfoEdgeType().equals(LaneChangeInfoEdgeType.LEFT)
                    || e.getLaneChangeInfoEdgeType().equals(LaneChangeInfoEdgeType.RIGHT))
            {
                int indexEndNode = this.route.indexOf(e.getFromLane().getParentLink().getEndNode());
                return 1.0 + 1.0 / indexEndNode; // lateral, reduce weight for further lane changes
            }
            Link toLink = e.getToLink();
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
         * @param gtuType GtuType; GTU type.
         * @return Node; destination node to use when no route is available.
         */
        public Node getNoRouteDestinationNode(final GtuType gtuType)
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
                Throw.when(lane == null, RuntimeException.class, "Requesting destination node on network without lanes.");
                // move to downstream link for as long as there is 1 downstream link
                try
                {
                    Link link = lane.getParentLink();
                    Set<Link> downstreamLinks = link.getEndNode().nextLinks(gtuType, link);
                    while (downstreamLinks.size() == 1)
                    {
                        link = downstreamLinks.iterator().next();
                        downstreamLinks = link.getEndNode().nextLinks(gtuType, link);
                    }
                    Throw.when(downstreamLinks.size() > 1, RuntimeException.class, "Using null route on network with split. "
                            + "Unable to find a destination to find lane change info towards.");
                    this.noRouteDestination = link.getEndNode();
                }
                catch (NetworkException ne)
                {
                    throw new RuntimeException("Requesting lane change info from link that does not allow the GTU type.", ne);
                }
            }
            return this.noRouteDestination;
        }
    }

    /**
     * Edge between two lanes, or between a lane and a node (to provide the shortest path algorithm with a suitable
     * destination). From a list of these from a path, the lane change information along the path (distances and number of lane
     * changes) can be derived.<br>
     * <br>
     * Copyright (c) 2022-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project
     * is distributed under a three-clause BSD-style license, which can be found at
     * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class LaneChangeInfoEdge
    {
        /** From lane, to allow construction of distances from a path. */
        private final Lane fromLane;

        /** The type of lane to lane movement performed along this edge. */
        private final LaneChangeInfoEdgeType laneChangeInfoEdgeType;

        /** To link (of the lane this edge moves to). */
        private final Link toLink;

        /**
         * Constructor.
         * @param fromLane Lane; lane this edge is from.
         * @param laneChangeInfoEdgeType LaneChangeInfoEdgeType; type of lane to lane movement performed along this edge.
         * @param toLink Link; to link of target lane (if any, may be {@code null}).
         */
        LaneChangeInfoEdge(final Lane fromLane, final LaneChangeInfoEdgeType laneChangeInfoEdgeType, final Link toLink)
        {
            this.fromLane = fromLane;
            this.laneChangeInfoEdgeType = laneChangeInfoEdgeType;
            this.toLink = toLink;
        }

        /**
         * Returns the from lane to allow construction of distances from a path.
         * @return Lane; from lane.
         */
        public Lane getFromLane()
        {
            return this.fromLane;
        }

        /**
         * Returns the type of lane to lane movement performed along this edge.
         * @return LaneChangeInfoEdgeType; type of lane to lane movement performed along this edge.
         */
        public LaneChangeInfoEdgeType getLaneChangeInfoEdgeType()
        {
            return this.laneChangeInfoEdgeType;
        }

        /**
         * Returns the to link.
         * @return Link; to link of target lane (if any, may be {@code null})
         */
        public Link getToLink()
        {
            return this.toLink;
        }
    }

    /**
     * Enum to provide information on the lane to lane movement in a path.<br>
     * <br>
     * Copyright (c) 2022-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project
     * is distributed under a three-clause BSD-style license, which can be found at
     * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
