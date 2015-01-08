package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 29 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class BuildGraph
{

    /**
     * Build the graph using roads between touching areas, flowLinks and Cordon areas (artificially created).
     */
    static void buildGraph(NTMModel model, boolean COMPRESS_AREAS)
    {
        /** debug information?. */
        final boolean DEBUG = false;
        // temporary storage for nodes and edges mapped from the number to the node
        Map<String, BoundedNode> nodeMap = new HashMap<>();
        Map<String, BoundedNode> nodeGraphMap = new HashMap<>();
        Map<Area, BoundedNode> areaNodeCentroidMap = new HashMap<>();
        Map<String, LinkEdge<Link>> linkMap = new HashMap<>();
        ArrayList<Link> allLinks = new ArrayList<Link>();

        allLinks.addAll(model.getShpLinks().values());
        allLinks.addAll(model.getFlowLinks().values());

        Map<String, Area> areasToUse;
        Map<String, Node> centroidsToUse;
        // the connectors depend on the area size (detailed/original or compressed/larger ones)
        if (COMPRESS_AREAS)
        {
            allLinks.addAll(model.getShpBigConnectors().values());
            areasToUse = model.getBigAreas();
            centroidsToUse = model.getBigCentroids();
        }
        else
        {
            allLinks.addAll(model.getShpConnectors().values());
            areasToUse = model.getAreas();
            centroidsToUse = model.getCentroids();
        }
        // make a directed graph of the entire network

        // FIRST, add ALL VERTICES
        for (Link shpLink : allLinks)
        {
            // area node: copies a node from a link and connects the area
            // the nodeMap connects the shpNodes to these new AreaNode
            BoundedNode nodeA = nodeMap.get(shpLink.getStartNode().getId());
            if (nodeA == null)
            {
                nodeA = addNodeToLinkGraph(shpLink, shpLink.getStartNode(), nodeMap, areasToUse.values(), model);
            }
            BoundedNode nodeB = nodeMap.get(shpLink.getEndNode().getId());
            if (nodeB == null)
            {
                nodeB = addNodeToLinkGraph(shpLink, shpLink.getEndNode(), nodeMap, areasToUse.values(), model);
            }

            // TODO direction of a road?
            // TODO is the length in ShapeFiles in meters or in kilometers? I believe in km.
            if (nodeA != null && nodeB != null)
            {
                // DoubleScalar<LengthUnit> length =
                // new DoubleScalar.Abs<LengthUnit>(shpLink.getLength(), LengthUnit.KILOMETER);
                try
                {
                    LinkEdge<Link> linkEdge = new LinkEdge<>(shpLink);
                    model.getLinkGraph().addEdge(nodeA, nodeB, linkEdge);
                    model.getLinkGraph().setEdgeWeight(linkEdge, shpLink.getLength().doubleValue());
                    linkMap.put(shpLink.getId(), linkEdge);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else
            {
                System.out.println("look out!!! line 434");
            }
            //
            if (shpLink.getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                nodeA = nodeGraphMap.get(shpLink.getStartNode().getId());
                if (nodeA == null)
                {
                    nodeA =
                            addNodeToAreaGraph(shpLink, shpLink.getStartNode(), nodeGraphMap, areaNodeCentroidMap,
                                    areasToUse.values(), model);
                }

                nodeB = nodeGraphMap.get(shpLink.getEndNode().getId());
                if (nodeB == null)
                {
                    nodeB =
                            addNodeToAreaGraph(shpLink, shpLink.getEndNode(), nodeGraphMap, areaNodeCentroidMap,
                                    areasToUse.values(), model);
                }
            }

        }

        // and finally put all centroids in the Graph as vertices
        for (Area area : areasToUse.values())
        {
            // find the new centroids in the big areas
            BoundedNode node = findCentroidInArea(area, centroidsToUse.values());

            // BoundedNode node = nodeMap.get(area.getCentroidNr());
            if (node != null)
            {
                areaNodeCentroidMap.put(area, node);
                nodeGraphMap.put(node.getId(), node);
                model.getAreaGraph().addVertex(node);
                model.getLinkGraph().addVertex(node);
            }
            else
            {
                System.out.println("Build graph line 133: look out, no area connected!!!");
            }

        }

        // ///////////////////////////////////////
        // SECOND part of the graph creation:
        // The next section creates the EDGES

        // First, add all GIS-like objects in an array
        ArrayList<GeoObject> gisObjects = new ArrayList<GeoObject>();
        gisObjects.addAll(areasToUse.values());

        // gisObjects.addAll(this.flowLinks.values());

        findTouching(gisObjects);

        // Secondly, find the Areas that do not touch any other area and connect them with the nearest areas!!
        connectIsolatedAreas(areasToUse, model.getLinkGraph(), model.getAreaGraph(), nodeGraphMap, areaNodeCentroidMap,
                areasToUse.values());

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            BoundedNode nSch = nodeMap.get("314071");
            BoundedNode nVb = nodeMap.get("78816");

            DijkstraShortestPath<BoundedNode, LinkEdge<Link>> sp =
                    new DijkstraShortestPath<>(model.getLinkGraph(), nSch, nVb);
            System.out.println("\nScheveningen -> Voorburg");
            System.out.println("Length=" + sp.getPathLength());
            List<LinkEdge<Link>> spList = sp.getPathEdgeList();
            if (spList != null)
            {
                for (LinkEdge<Link> le : spList)
                {
                    System.out.println(le.getLink().getLinkData().getName());
                }
            }

        }

        // iterate over the roads and create the areaGraph
        // this connects the areas and highways
        // map them on the area centroids
        for (LinkEdge<Link> le : linkMap.values())
        {
            Area aA;
            Area aB;
            aA = findArea(le.getLink().getStartNode().getPoint(), areasToUse.values());
            aB = findArea(le.getLink().getEndNode().getPoint(), areasToUse.values());
            int hierarchy = le.getLink().getHierarchy();
            // When this is a flow link, inspect if they connect to urban roads
            // if so, create a GraphEdge that connects flow roads with urban roads / areas (in/out going)
            if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                // make FLOW connectors (in the areaGraph!!)
                // create cellTransmissionLinks for the edges of the real FLOW connectors
                // every CTM link receives a set of FlowCells that will be simulated as a nested process within the
                // Network Transmission Model
                createFlowConnectors(aA, aB, le, linkMap, areaNodeCentroidMap, nodeGraphMap, model, areasToUse);
            }
            // for all other links, inspect if they connect areas
            else if (aA != null && aB != null && aA.getTouchingAreas().contains(aB))
            {
                BoundedNode cA = null;
                BoundedNode cB = null;
                cA = areaNodeCentroidMap.get(aA);
                cB = areaNodeCentroidMap.get(aB);
                // first, test if these links connect two different areas (not within one area)
                if (cA != cB)
                {
                    if (model.getAreaGraph().containsEdge(cA, cB))
                    {
                        // TODO if the link between these areas already exists, add the capacity to the link
                    }
                    else
                    {
                        if (cA == null || cB == null)
                        {
                            System.out.println("test");
                        }
                        else
                        {
                            DoubleScalar.Abs<FrequencyUnit> capacity =
                                    new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                            DoubleScalar.Abs<SpeedUnit> speed =
                                    new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);

                            Link newLink =
                                    Link.createLink(cA, cB, capacity, speed, TrafficBehaviourType.NTM, hierarchy);
                            LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                            addLinkEdge(cA, cB, newLinkEdge, TrafficBehaviourType.NTM, model.getAreaGraph());
                        }
                        // TODO is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in
                        // km?
                    }
                }
            }
        }

        // add the flowLinks and their A and B nodes as special types of areaNodes and edges

        // add the unconnected cordon feeders

        // create the connections between the cordon connectors and their nearest areas or roads

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            System.out.println("\nScheveningen -> Voorburg via centroids");
            Point pSch = nodeMap.get("314071").getPoint();
            Point pVb = nodeMap.get("78816").getPoint();
            Area aSch;
            Area aVb;
            aSch = findArea(pSch, areasToUse.values());
            aVb = findArea(pVb, areasToUse.values());
            if (aSch == null || aVb == null)
            {
                System.out.println("Could not find areas");
            }
            else
            {
                BoundedNode cSch = areaNodeCentroidMap.get(aSch);
                BoundedNode cVb = areaNodeCentroidMap.get(aVb);
                DijkstraShortestPath<BoundedNode, LinkEdge<Link>> sp =
                        new DijkstraShortestPath<>(model.getAreaGraph(), cSch, cVb);
                System.out.println("Length=" + sp.getPathLength());
                List<LinkEdge<Link>> spList = sp.getPathEdgeList();
                if (spList != null)
                {
                    for (LinkEdge<Link> le : spList)
                    {
                        System.out.println(le.getLink().getId());
                    }
                }
                System.out.println("Length = " + System.currentTimeMillis());

            }
        }

    }

    /**
     * @param flowNodeA
     * @param flowNodeB
     * @param centroidA
     * @param centroidB
     * @param le
     * @param type
     */
    private static void addLinkEdge(BoundedNode flowNodeA, BoundedNode flowNodeB, LinkEdge<Link> linkEdge,
            TrafficBehaviourType type, SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> graph)
    {
        // TODO is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in km?

        if (!graph.containsEdge(flowNodeA, flowNodeB))
        {
            if (graph.containsVertex(flowNodeA) && graph.containsVertex(flowNodeB))
            {
                if (flowNodeA != flowNodeB)
                {
                    graph.addEdge(flowNodeA, flowNodeB, linkEdge);

                }
                else
                {
                    System.out.println("same nodes????");
                }

            }
            else
            {
                System.out.println("missing");
            }
        }
        // TODO average length? straight distance? straight distance + 20%?
        graph.setEdgeWeight(linkEdge, linkEdge.getLink().getLength().doubleValue());
    }

    /**
     * @param shpLink link
     * @param node node
     * @param map receives node
     */
    private static BoundedNode addNodeToLinkGraph(Link shpLink, Node shpLinkNode, Map<String, BoundedNode> map,
            Collection<Area> areas, NTMModel model)
    {
        Area area = findArea(shpLinkNode.getPoint(), areas);
        /*
         * if (area == null) { System.out.println("Could not find area for NodeA of shapeLink " + shpLinkNode); }
         */
        // BoundedNode node1 = (BoundedNode) shpLinkNode;
        // node1.setArea(area);
        BoundedNode node =
                new BoundedNode(shpLinkNode.getPoint(), shpLinkNode.getId(), area, shpLink.getBehaviourType());
        map.put(shpLinkNode.getId(), node);
        model.getLinkGraph().addVertex(node);
        return node;
    }

    /**
     * @param shpLink link
     * @param node node
     * @param nodeGraphMap receives node
     */
    private static BoundedNode addNodeToAreaGraph(Link shpLink, Node shpLinkNode,
            Map<String, BoundedNode> nodeGraphMap, Map<Area, BoundedNode> areaNodeCentroidMap, Collection<Area> areas,
            NTMModel model)
    {
        Area area = findArea(shpLinkNode.getPoint(), areas);
        /*
         * if (area == null) { System.err.println("Could not find area for NodeA of shapeLink " + shpLinkNode); }
         */
        // BoundedNode node1 = (BoundedNode) shpLinkNode;
        // node1.setArea(area);
        BoundedNode node =
                new BoundedNode(shpLinkNode.getPoint(), shpLinkNode.getId(), area, shpLink.getBehaviourType());
        nodeGraphMap.put(shpLinkNode.getId(), node);
        areaNodeCentroidMap.put(area, node);
        model.getAreaGraph().addVertex(node);
        return node;
    }

    /**
     * @param area the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private static BoundedNode findCentroidInArea(final Area area, Collection<Node> collection)
    {
        BoundedNode centroid = null;
        for (Node node : collection)
        {
            if (area.getGeometry().contains(node.getPoint()))
            {
                // TODO later: why multiple areas
                /*
                 * if (area != null) { System.out.println("findArea: point " + p.toText() + " is in multiple areas: " +
                 * a.getCentroidNr() + " and " + area.getCentroidNr()); }
                 */
                centroid = new BoundedNode(node.getPoint(), node.getId(), area, node.getBehaviourType());
            }
        }
        return centroid;
    }

    /**
     * @param p the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private static Area findArea(final Point p, Collection<Area> areas)
    {
        Area area = null;
        for (Area a : areas)
        {
            if (a.getGeometry().contains(p))
            {
                // TODO later: why multiple areas
                /*
                 * if (area != null) { System.out.println("findArea: point " + p.toText() + " is in multiple areas: " +
                 * a.getCentroidNr() + " and " + area.getCentroidNr()); }
                 */
                area = a;
            }
        }
        return area;
    }

    /**
     * @param geom1
     * @param geom2
     * @return
     */
    private static boolean findBoundaryAreas(Geometry geom1, Geometry geom2)
    {
        boolean touch = false;
        Envelope e1 = geom1.getEnvelopeInternal();
        try
        {
            // if (area != area2 && (area.getGeometry().touches(area2.getGeometry())
            // || area.getGeometry().intersects(area2.getGeometry())))
            // first see if envelopes overlap
            if (geom1 != geom2 && e1.intersects(geom2.getEnvelopeInternal()))
            {
                // 1 meter distance
                // if (area1.getGeometry().isWithinDistance(area2.getGeometry(), 1.0d))
                if (geom1.touches(geom2) || geom1.intersects(geom2))
                {
                    touch = true;
                }
            }
        }
        catch (TopologyException te)
        {
            System.out.println("TopologyException " + te.getMessage() + " when checking border of " + geom1 + " and "
                    + geom2);
        }
        return touch;
    }

    /**
     * finds the Areas that do not touch any other area and connects them with the nearest areas!!
     * @param areaNodeCentroidMap
     * @param areaGraph2
     * @param linkGraph2
     * @param areas2
     */
    private static void connectIsolatedAreas(final Map<String, Area> areasAll,
            final SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> linkGraphIn,
            final SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> areaGraphIn,
            final Map<String, BoundedNode> nodeGraphMap, final Map<Area, BoundedNode> areaNodeCentroidMap,
            Collection<Area> areas)
    {
        final SpatialIndex index = new STRtree();
        for (Area areaIndex : areasAll.values())
        {
            Geometry geom = areaIndex.getGeometry();
            if (geom != null)
            {
                Envelope env = geom.getEnvelopeInternal();
                if (!env.isNull())
                {
                    index.insert(env, areaIndex);
                }
            }
        }

        // try and find
        final double MAX_SEARCH_DISTANCE = 2000.0; // meters?
        final int NUMBER_OF_AREAS = 6;
        for (Area isolatedArea : areasAll.values())
        {
            if (isolatedArea.getTouchingAreas().size() == 0)
            {
                System.out.println("no touching area for number " + isolatedArea.getCentroidNr() + ", Area type: "
                        + isolatedArea.getTrafficBehaviourType());

                // Get point and create search envelope
                Geometry geom = isolatedArea.getGeometry();
                Envelope search = geom.getEnvelopeInternal();
                double searchDistance = MAX_SEARCH_DISTANCE;
                search.expandBy(searchDistance);
                /*
                 * Query the spatial index for objects within the search envelope. Note that this just compares the
                 * point envelope to the line envelopes so it is possible that the point is actually more distant than
                 * MAX_SEARCH_DISTANCE from a line.
                 */
                @SuppressWarnings("unchecked")
                List<Area> nearestAreas = index.query(search);
                while (nearestAreas.size() > NUMBER_OF_AREAS && searchDistance > 0.1)
                {
                    double decreaseBy = -0.2 * searchDistance;
                    searchDistance += decreaseBy;
                    search.expandBy(decreaseBy);
                    nearestAreas = index.query(search);
                }

                // now find the nearest Areas that are connected by a road
                // / TODO the next part contains errors!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                BoundedNode nodeIsolated = areaNodeCentroidMap.get(isolatedArea);
                for (Area nearArea : nearestAreas)
                {
                    BoundedNode nodeNear = areaNodeCentroidMap.get(nearArea);
                    try
                    {
                        if (!linkGraphIn.containsVertex(nodeNear))
                        {
                            System.out.println("No nodeNear");
                        }
                        else if (!linkGraphIn.containsVertex(nodeIsolated))
                        {
                            System.out.println("No nodeNear");
                        }
                        else
                        {
                            DijkstraShortestPath<BoundedNode, LinkEdge<Link>> sp =
                                    new DijkstraShortestPath<>(linkGraphIn, nodeIsolated, nodeNear);
                            List<LinkEdge<Link>> spList = sp.getPathEdgeList();
                            if (spList != null)
                            {
                                for (LinkEdge<Link> le : spList)
                                {
                                    int hierarchy = le.getLink().getHierarchy();
                                    Area enteredArea = findArea(le.getLink().getEndNode().getPoint(), areas);
                                    if (enteredArea != null && enteredArea != isolatedArea
                                            && le.getLink().getBehaviourType() != TrafficBehaviourType.FLOW)
                                    {
                                        isolatedArea.getTouchingAreas().add(enteredArea);
                                        BoundedNode centroidEntered = areaNodeCentroidMap.get(enteredArea);
                                        DoubleScalar.Abs<SpeedUnit> speed =
                                                new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                                        DoubleScalar.Abs<FrequencyUnit> capacity =
                                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                                        Link newLink =
                                                Link.createLink(nodeIsolated, centroidEntered, capacity, speed,
                                                        TrafficBehaviourType.NTM, hierarchy);
                                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                                        addLinkEdge(nodeIsolated, centroidEntered, newLinkEdge,
                                                TrafficBehaviourType.NTM, areaGraphIn);
                                        break;
                                    }
                                    else if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
                                    {
                                        // BoundedNode bN =
                                        // new BoundedNode(le.getLink().getStartNode().getPoint(), le.getLink()
                                        // .getStartNode().getId(), null, le.getLink().getStartNode()
                                        // .getBehaviourType());
                                        BoundedNode bN = nodeGraphMap.get(le.getLink().getStartNode().getId());
                                        /*
                                         * if (flowNodeStart.getArea()== null) { cA.setArea(areaStart); }
                                         */

                                        // BoundedNode bN = (BoundedNode) le.getLink().getStartNode();

                                        DoubleScalar.Abs<SpeedUnit> speed =
                                                new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                                        DoubleScalar.Abs<FrequencyUnit> capacity =
                                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                                        Link newLink =
                                                Link.createLink(nodeIsolated, bN, capacity, speed,
                                                        TrafficBehaviourType.NTM, hierarchy);
                                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                                        addLinkEdge(nodeIsolated, bN, newLinkEdge, TrafficBehaviourType.FLOW,
                                                areaGraphIn);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                // find the nearest areas and connect them (HERE? of further down this Class...)
                // TODO make the code
            }
        }
    }

    /**
     * @param areaStart
     * @param areaEnd
     * @param le
     * @param linkMap
     * @param areaNodeCentroidMap
     */
    private static void createFlowConnectors(final Area areaStart, final Area areaEnd, final LinkEdge<Link> le,
            final Map<String, LinkEdge<Link>> linkMap, final Map<Area, BoundedNode> areaNodeCentroidMap,
            final Map<String, BoundedNode> nodeGraphMap, NTMModel model, Map<String, Area> areasToUse)
    {
        Node node = le.getLink().getStartNode();
        BoundedNode flowNodeStart = nodeGraphMap.get(node.getId());
        if (flowNodeStart.getArea() == null)
        {
            flowNodeStart.setArea(areaStart);
        }
        // BoundedNode flowNodeStart = new BoundedNode(node.getPoint(), node.getId(), areaStart,
        // node.getBehaviourType());
        node = le.getLink().getEndNode();
        BoundedNode flowNodeEnd = nodeGraphMap.get(node.getId());
        if (flowNodeEnd.getArea() == null)
        {
            flowNodeEnd.setArea(areaStart);
        }
        // BoundedNode flowNodeEnd = new BoundedNode(node.getPoint(), node.getId(), areaEnd, node.getBehaviourType());
        // BoundedNode flowNodeA = (BoundedNode) le.getLink().getStartNode();
        // BoundedNode flowNodeB = (BoundedNode) le.getLink().getEndNode();
        Link link = le.getLink();
        ArrayList<FlowCell> cells =
                LinkCellTransmission.createCells(link, model.getSettingsNTM()
                        .getTimeStepDurationCellTransmissionModel());
        LinkCellTransmission linkCTM = new LinkCellTransmission(link, flowNodeStart, flowNodeEnd, cells);
        LinkEdge leNew = new LinkEdge(linkCTM);
        int hierarchy = le.getLink().getHierarchy();
        addLinkEdge(flowNodeStart, flowNodeEnd, leNew, TrafficBehaviourType.FLOW, model.getAreaGraph());
        // loop through the other links to find the links that connect
        BoundedNode cA = null;
        BoundedNode cB = null;
        // cA = areaNodeCentroidMap.get(areaStart);
        // cB = areaNodeCentroidMap.get(areaEnd);
        for (LinkEdge<Link> urbanLink : linkMap.values())
        {
            if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.ROAD)
            {
                if (urbanLink.getLink().getEndNode().getId().equals(flowNodeStart.getId()))
                {
                    // from urban (Area) to Highway (flow)
                    Area aStart = findArea(urbanLink.getLink().getStartNode().getPoint(), areasToUse.values());
                    cA = areaNodeCentroidMap.get(aStart);
                    if (aStart != null)
                    {
                        if (cA == null || flowNodeStart == null)
                        {
                            System.out.println("No connection of flow Link to Area for this one...");
                        }
                        DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar.Abs<FrequencyUnit> capacity =

                        new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink =
                                Link.createLink(cA, flowNodeStart, capacity, speed, TrafficBehaviourType.NTM, hierarchy);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(cA, flowNodeStart, newLinkEdge, TrafficBehaviourType.NTM, model.getAreaGraph());

                    }
                    else
                    {
                        System.out.println("BuildGraph linne 593: aA == Null................");
                    }

                }
                if (urbanLink.getLink().getStartNode().getId().equals(flowNodeEnd.getId()))
                {
                    // from Highway (flow) to urban (Area)
                    Area aEnd = findArea(urbanLink.getLink().getEndNode().getPoint(), areasToUse.values());
                    cB = areaNodeCentroidMap.get(aEnd);
                    if (aEnd != null)
                    {
                        if (cB == null || flowNodeStart == null)
                        {
                            System.out.println("No connection of flow Link to Area for this one...");
                        }

                        DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar.Abs<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink =
                                Link.createLink(flowNodeEnd, cB, capacity, speed, TrafficBehaviourType.NTM, hierarchy);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(flowNodeEnd, cB, newLinkEdge, TrafficBehaviourType.NTM, model.getAreaGraph());
                    }
                    else
                    {
                        System.out.println("BuildGraph line 614 aB == Null................");
                    }

                }
            }

            else if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.CORDON)
            {
                if (urbanLink.getLink().getEndNode().getId().equals(flowNodeStart.getId()))
                {
                    // from urban (Area) to Highway (flow)
                    node = urbanLink.getLink().getStartNode();
                    cA = nodeGraphMap.get(node.getId());
                    if (flowNodeStart.getArea() == null)
                    {
                        cA.setArea(areaStart);
                    }
                    // cA = new BoundedNode(node.getPoint(), node.getId(), areaStart, node.getBehaviourType());
                    // cA = (BoundedNode) urbanLink.getLink().getStartNode();
                    if (cA != null)
                    {

                        DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar.Abs<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink =
                                Link.createLink(cA, flowNodeStart, capacity, speed, TrafficBehaviourType.CORDON,
                                        hierarchy);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(cA, flowNodeStart, newLinkEdge, TrafficBehaviourType.CORDON, model.getAreaGraph());
                    }

                }
                else if (urbanLink.getLink().getStartNode().getId().equals(flowNodeEnd.getId()))
                {
                    // from Highway (flow) to urban (Area)
                    node = urbanLink.getLink().getEndNode();
                    cB = nodeGraphMap.get(node.getId());
                    if (flowNodeStart.getArea() == null)
                    {
                        cB.setArea(areaStart);
                    }
                    // cB = new BoundedNode(node.getPoint(), node.getId(), areaEnd, node.getBehaviourType());
                    // cB = (BoundedNode) urbanLink.getLink().getStartNode();
                    if (cB != null)
                    {

                        DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar.Abs<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink =
                                Link.createLink(flowNodeEnd, cB, capacity, speed, TrafficBehaviourType.CORDON,
                                        hierarchy);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(flowNodeEnd, cB, newLinkEdge, TrafficBehaviourType.CORDON, model.getAreaGraph());
                    }

                }
            }

        }
    }

    // Create new Areas where they are lacking
    /**
     * @param centroid
     * @return the additional areas
     */
    public static Area createMissingArea(final Node centroid)
    {
        Geometry buffer = centroid.getPoint().getGeometryN(0).buffer(30);
        Point centroid1 = buffer.getCentroid();
        String nr = centroid.getId();
        String name = centroid.getId();
        String gemeente = "Area is missing for: " + centroid.getId();
        String gebied = "Area is missing for: " + centroid.getId();
        String regio = "Missing";
        double dhb = 0.0;
        Area area = new Area(buffer, nr, name, gemeente, gebied, regio, dhb, centroid1, TrafficBehaviourType.NTM);
        return area;
    }

    /**
     * For every area, find the touching areas
     * @param gisObjects
     */
    private static void findTouching(ArrayList<GeoObject> gisObjects)
    {
        // then find out if they touch
        for (GeoObject gis1 : gisObjects)
        {
            Geometry geom1 = gis1.getGeometry();
            for (GeoObject gis2 : gisObjects)
            {
                Geometry geom2 = gis2.getGeometry();
                // if the areas geometrically touch or intersect:
                if (findBoundaryAreas(geom1, geom2))
                {
                    gis1.getTouchingAreas().add(gis2);
                }
            }
        }

        // inspect, if there are objects without neighbours
        for (GeoObject gis1 : gisObjects)
        {
            if (gis1.getTouchingAreas() == null)
            {
                Area noTouch = (Area) gis1;
                System.out.println("no touching area for this one" + noTouch.getCentroidNr());
            }
        }
    }

}
