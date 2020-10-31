package org.opentrafficsim.demo.ntm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 29 Oct 2014 <br>
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
    static void buildGraph(NTMModel model, Map<String, Area> areasToUse, Map<String, NTMNode> centroidsToUse,
            Map<String, NTMLink> shpConnectorsToUse)
    {
        /** Debug information?. */
        final boolean DEBUG = false;
        // temporary storage for nodes and edges mapped from the number to the node
        Map<String, NTMNode> nodeMap = new LinkedHashMap<>();
        // Map<String, Node> nodeAreaGraphMap = new LinkedHashMap<>();

        Map<Area, NTMNode> areaNodeCentroidMap = new LinkedHashMap<>();
        Map<String, LinkEdge<NTMLink>> linkMap = new LinkedHashMap<>();
        ArrayList<NTMLink> allLinks = new ArrayList<NTMLink>();

        allLinks.addAll(model.getShpLinks().values());
        allLinks.addAll(model.getFlowLinks().values());
        allLinks.addAll(shpConnectorsToUse.values());

        /*
         * //generate the incoming and outgoing links from nodes for (Link link: allLinks) {
         * link.getStartNode().getIncomingLinks().add(link); link.getEndNode().getIncomingLinks().add(link); }
         */// make a directed graph of the entire network
           // FIRST CREATE the LinkGraph
        for (NTMLink shpLink : allLinks)
        {
            // area node: copies a node from a link and connects the area
            // the nodeMap connects the shpNodes to these new AreaNode
            BoundedNode nodeA = (BoundedNode) nodeMap.get(shpLink.getStartNode().getId());
            if (nodeA == null)
            {
                nodeA = addNodeToLinkGraph(shpLink, (NTMNode) shpLink.getStartNode(), nodeMap, areasToUse.values(), model);
            }
            BoundedNode nodeB = (BoundedNode) nodeMap.get(shpLink.getEndNode().getId());
            if (nodeB == null)
            {
                nodeB = addNodeToLinkGraph(shpLink, (NTMNode) shpLink.getEndNode(), nodeMap, areasToUse.values(), model);
            }

            try
            {
                LinkEdge<NTMLink> linkEdge = new LinkEdge<>(shpLink);
                model.getLinkGraph().addEdge(nodeA, nodeB, linkEdge);
                double speed = shpLink.getFreeSpeed().getInUnit(SpeedUnit.METER_PER_SECOND);
                double length = shpLink.getLength().getInUnit(LengthUnit.METER);
                double travelTime = length / speed;
                model.getLinkGraph().setEdgeWeight(linkEdge, travelTime);
                linkMap.put(shpLink.getId(), linkEdge);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // The flow links are included in the AREA graph: here the vertices are already added
            if (shpLink.getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                nodeA = (BoundedNode) model.getNodeAreaGraphMap().get(shpLink.getStartNode().getId());
                if (nodeA == null)
                {
                    nodeA = addNodeToAreaGraph(shpLink, (NTMNode) shpLink.getStartNode(), model.getNodeAreaGraphMap(),
                            areaNodeCentroidMap, areasToUse.values(), model);
                }

                nodeB = (BoundedNode) model.getNodeAreaGraphMap().get(shpLink.getEndNode().getId());
                if (nodeB == null)
                {
                    nodeB = addNodeToAreaGraph(shpLink, (NTMNode) shpLink.getEndNode(), model.getNodeAreaGraphMap(),
                            areaNodeCentroidMap, areasToUse.values(), model);
                }
            }
        }

        // and finally put all centroids in the Graph as vertices
        for (Area area : areasToUse.values())
        {
            // find the new centroids in the big areas
            BoundedNode node = findCentroidInArea(model, area, centroidsToUse.values());

            // BoundedNode node = nodeMap.get(area.getCentroidNr());
            if (node != null)
            {
                // put centroid back in links
                if (node.getArea().getRoadLength().doubleValue() == Double.POSITIVE_INFINITY)
                {
                    node.setBehaviourType(TrafficBehaviourType.CORDON);
                }
                areaNodeCentroidMap.put(area, node);
                model.getNodeAreaGraphMap().put(node.getId(), node);
                model.getAreaGraph().addVertex(node);
                // model.getLinkGraph().addVertex(node);
            }
            else
            {
                System.out.println("Build graph line 133: look out, no area connected!!!");
            }

        }

        // ///////////////////////////////////////
        // SECOND part: the AREA graph creation:
        // The next section creates the EDGES

        // First, add all GIS-like objects in an array
        ArrayList<GeoObject> gisObjects = new ArrayList<GeoObject>();
        gisObjects.addAll(areasToUse.values());

        // gisObjects.addAll(this.flowLinks.values());
        findTouching(gisObjects);
        // Secondly, find the Areas that do not touch any other area and connect them with the nearest areas!!
        connectIsolatedAreas(model, model.getNodeAreaGraphMap(), areaNodeCentroidMap, areasToUse.values());

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            NTMNode nSch = nodeMap.get("314071");
            NTMNode nVb = nodeMap.get("78816");

            DijkstraShortestPath<NTMNode, LinkEdge<NTMLink>> dijkstra = new DijkstraShortestPath<>(model.getLinkGraph());
            GraphPath<NTMNode, LinkEdge<NTMLink>> sp = dijkstra.getPath(nSch, nVb);
            System.out.println("\nScheveningen -> Voorburg");
            System.out.println("Length=" + sp.getLength());
            List<LinkEdge<NTMLink>> spList = sp.getEdgeList();
            if (spList != null)
            {
                for (LinkEdge<NTMLink> le : spList)
                {
                    System.out.println(le.getLink().getLinkData().getName());
                }
            }

        }

        // iterate over the roads and create the areaGraph
        // this connects the areas and highways
        // map them on the area centroids
        // The LinkEdges already have a travel time (allLinks not)
        for (LinkEdge<NTMLink> le : linkMap.values())
        {
            Area aA;
            Area aB;

            /*
             * if (le.getLink().getStartNode().getId().equals("65800") && le.getLink().getEndNode().getId().equals("78816")) {
             * System.out.println("no"); }
             */
            aA = findArea(le.getLink().getStartNode().getPoint().getCoordinate(), areasToUse.values());
            aB = findArea(le.getLink().getEndNode().getPoint().getCoordinate(), areasToUse.values());

            // When this is a flow link, inspect if they connect to urban roads
            // if so, create a GraphEdge that connects flow roads with urban roads / areas (in/out going)
            if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                // make FLOW connectors (in the areaGraph!!)
                // create cellTransmissionLinks for the edges of the real FLOW connectors
                // every CTM link receives a set of FlowCells that will be simulated as a nested process within the
                // Network Transmission Model
                createFlowConnectors(aA, aB, le, linkMap, areaNodeCentroidMap, model.getNodeAreaGraphMap(), model, areasToUse);
            }
            // for all other links, inspect if they connect areas (startNode in areaA and endNode in area B (or vice
            // versa))
            else if (aA != null && aB != null && aA.getTouchingAreas().contains(aB))
            {
                BoundedNode cA = null;
                BoundedNode cB = null;
                cA = (BoundedNode) areaNodeCentroidMap.get(aA);
                cB = (BoundedNode) areaNodeCentroidMap.get(aB);
                // first, test if these links connect two different areas (not within one area)

                if (cA.getId() != cB.getId())
                {
                    if (model.getAreaGraph().containsEdge(cA, cB))
                    {
                        if (le.getLink().getCapacity() != null && model.getAreaGraph().getEdge(cA, cB).getLink() != null)
                        {
                            if (model.getAreaGraph().getEdge(cA, cB).getLink().getCorridorCapacity() != null)
                            {
                                model.getAreaGraph().getEdge(cA, cB).getLink().addCorridorCapacity(le.getLink().getCapacity());
                            }
                            else
                            {
                                System.out.println("no corridorCapacity computed for this link/edge: node " + cA + " , " + cB);
                            }

                        }
                        else
                        {
                            System.out.println("no capacity computed for this link/edge: node " + cA + " , " + cB);
                        }
                    }
                    else
                    {
                        // Create the edge that connects two areas and find the time to travel between centroids
                        if (cA != null && cB != null)
                        {
                            // Node cAVertex = model.getLinkGraph().vertexSet().contains(cA)?cA:null;
                            NTMNode cAVertex = null;
                            NTMNode cBVertex = null;
                            for (NTMNode node : model.getLinkGraph().vertexSet())
                            {
                                if (node.getId().equals(cA.getId()))
                                {
                                    cAVertex = node;
                                }
                                if (node.getId().equals(cB.getId()))
                                {
                                    cBVertex = node;
                                }
                            }

                            if (cAVertex == null || cBVertex == null)
                            {
                                throw new RuntimeException("cAVertex == null || cBVertex == null");
                            }

                            Speed speedA = null;
                            Speed speedB = null;

                            // TODO: checken
                            if (cA.getBehaviourType() == TrafficBehaviourType.NTM)
                            {
                                CellBehaviourNTM cellBehaviourNTMA = (CellBehaviourNTM) cA.getCellBehaviour();
                                speedA = cellBehaviourNTMA.getParametersNTM().getFreeSpeed();
                            }
                            else if (cA.getBehaviourType() == TrafficBehaviourType.CORDON)
                            {
                                speedA = new Speed(70, SpeedUnit.KM_PER_HOUR);
                            }
                            if (cB.getBehaviourType() == TrafficBehaviourType.NTM)
                            {
                                CellBehaviourNTM cellBehaviourNTMB = (CellBehaviourNTM) cB.getCellBehaviour();
                                speedB = cellBehaviourNTMB.getParametersNTM().getFreeSpeed();
                            }
                            else if (cB.getBehaviourType() == TrafficBehaviourType.CORDON)
                            {
                                speedB = new Speed(70, SpeedUnit.KM_PER_HOUR);
                            }

                            addGraphConnector(model, cAVertex, cBVertex, speedA, speedB, le, TrafficBehaviourType.NTM);

                        }
                        // TODO is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in
                        // km?
                        else if (cA == null || cB == null)
                        {
                            System.out.println("cA == null || cB == null");
                        }

                    }
                }
            }
            // else
            // {
            // System.out.println("test: cA == cB??");
            // }
        }

        // add the flowLinks and their A and B nodes as special types of areaNodes and edges

        // add the unconnected cordon feeders

        // create the connections between the cordon connectors and their nearest areas or roads

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            System.out.println("\nScheveningen -> Voorburg via centroids");
            Coordinate pSch = nodeMap.get("314071").getPoint().getCoordinate();
            Coordinate pVb = nodeMap.get("78816").getPoint().getCoordinate();
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
                BoundedNode cSch = (BoundedNode) areaNodeCentroidMap.get(aSch);
                BoundedNode cVb = (BoundedNode) areaNodeCentroidMap.get(aVb);
                DijkstraShortestPath<NTMNode, LinkEdge<NTMLink>> dijkstra = new DijkstraShortestPath<>(model.getAreaGraph());
                GraphPath<NTMNode, LinkEdge<NTMLink>> sp = dijkstra.getPath(cSch, cVb);
                System.out.println("Length=" + sp.getLength());
                List<LinkEdge<NTMLink>> spList = sp.getEdgeList();
                if (spList != null)
                {
                    for (LinkEdge<NTMLink> le : spList)
                    {
                        System.out.println(le.getLink().getId());
                    }
                }
                System.out.println("Length = " + System.currentTimeMillis());

            }
        }

    }

    /**
     * @param model NTMModel;
     * @param cAVertex NTMNode;
     * @param cBVertex NTMNode;
     */
    private static void addGraphConnector(NTMModel model, NTMNode cAVertex, NTMNode cBVertex, Speed speedA, Speed speedB,
            LinkEdge le, TrafficBehaviourType trafficBehaviourType)
    {
        DijkstraShortestPath<NTMNode, LinkEdge<NTMLink>> sp = new DijkstraShortestPath<>(model.getLinkGraph());
        // if (model.getLinkGraph().containsVertex(cAVertex) && model.getLinkGraph().containsVertex(cBVertex))
        // {
        // sp = new DijkstraShortestPath<NTMNode, LinkEdge<NTMLink>>(model.getLinkGraph(), cAVertex, cBVertex);
        // }
        /*
         * else { System.out.println("no grapph for this  node " + cAVertex + " or , " + cBVertex); }
         */

        Duration time = null;
        if (sp != null)
        {
            if (sp.getPath(cAVertex, cBVertex) != null)
            {

                time = new Duration(sp.getPath(cAVertex, cBVertex).getWeight(), DurationUnit.HOUR);
                double xA = cAVertex.getPoint().getCoordinate().x;
                double yA = cAVertex.getPoint().getCoordinate().y;
                double xB = cBVertex.getPoint().getCoordinate().x;
                double yB = cBVertex.getPoint().getCoordinate().y;
                // TODO check distance by coordinates!!!!
                double distance = 1.3 * Math.sqrt(Math.pow(xB - xA, 2) + Math.pow(yB - yA, 2));
                double timeDouble = 0.5 * distance / speedA.getSI() + 0.5 * distance / speedB.getSI();
                time = new Duration(timeDouble, DurationUnit.SECOND);
                double speedDouble =
                        0.5 * speedA.getInUnit(SpeedUnit.KM_PER_HOUR) + 0.5 * speedB.getInUnit(SpeedUnit.KM_PER_HOUR);
                Speed speed = new Speed(speedDouble, SpeedUnit.KM_PER_HOUR);
                NTMLink newLink = NTMLink.createLink(model.getNetwork(), model.getSimulator(), cAVertex, cBVertex, null, speed,
                        time, trafficBehaviourType);
                if (((NTMLink) le.getLink()).getCapacity() != null)
                {
                    newLink.setCorridorCapacity(((NTMLink) le.getLink()).getCapacity());
                }
                else
                {
                    System.out.println("no capacity computed for this link/edge: node " + cAVertex + " , " + cBVertex);
                }
                LinkEdge<NTMLink> newLinkEdge = new LinkEdge<>(newLink);

                addLinkEdge(cAVertex, cBVertex, newLinkEdge, trafficBehaviourType, model.getAreaGraph());
            }
            else
            {
                System.out.println("No path between these nodes, while trying to connect areas" + cAVertex + ", " + cBVertex);
            }
        }
    }

    /**
     * @param flowNodeA NTMNode;
     * @param flowNodeB NTMNode;
     * @param centroidA
     * @param centroidB
     * @param le
     * @param type TrafficBehaviourType;
     */
    private static void addLinkEdge(NTMNode flowNodeA, NTMNode flowNodeB, LinkEdge<NTMLink> linkEdge, TrafficBehaviourType type,
            SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>> graph)
    {

        if (!graph.containsEdge(flowNodeA, flowNodeB))
        {
            try
            {
                if (graph.containsVertex(flowNodeA) && graph.containsVertex(flowNodeB))
                {
                    if (flowNodeA != flowNodeB)
                    {
                        graph.addEdge(flowNodeA, flowNodeB, linkEdge);
                        if (linkEdge.getLink().getDuration() != null)
                        {
                            graph.setEdgeWeight(linkEdge, linkEdge.getLink().getDuration().getInUnit(DurationUnit.HOUR));
                        }
                        else
                        {
                            java.lang.Double timeDouble = linkEdge.getLink().getLength().getInUnit(LengthUnit.KILOMETER)
                                    / linkEdge.getLink().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                            Duration time = new Duration(timeDouble, DurationUnit.HOUR);
                            linkEdge.getLink().setDuration(time);
                            graph.setEdgeWeight(linkEdge, linkEdge.getLink().getDuration().getInUnit(DurationUnit.HOUR));
                        }
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
            catch (Exception exception1)
            {
                exception1.printStackTrace();
            }

        }
        /*
         * else { System.out.println("Already found"); }
         */
        // TODO average length? straight distance? straight distance + 20%?
    }

    /**
     * @param shpLink NTMLink; link
     * @param node node
     * @param map Map&lt;String,NTMNode&gt;; receives node
     */
    private static BoundedNode addNodeToLinkGraph(NTMLink shpLink, NTMNode shpLinkNode, Map<String, NTMNode> map,
            Collection<Area> areas, NTMModel model)
    {
        Area area = findArea(shpLinkNode.getPoint().getCoordinate(), areas);
        /*
         * if (area == null) { System.out.println("Could not find area for NodeA of shapeLink " + shpLinkNode); }
         */
        // BoundedNode node1 = (BoundedNode) shpLinkNode;
        // node1.setArea(area);
        BoundedNode node;
        try
        {
            node = new BoundedNode(model.getNetwork(), shpLinkNode.getPoint().getCoordinate(), shpLinkNode.getId(), area,
                    shpLink.getBehaviourType());
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            node = null;
        }
        map.put(shpLinkNode.getId(), node);
        if (!model.getLinkGraph().containsVertex(node))
        {
            model.getLinkGraph().addVertex(node);
        }
        else
        {
            throw new RuntimeException("Node added to network: already existed " + node.getId());
        }
        return node;
    }

    /**
     * @param shpLink NTMLink; link
     * @param node node
     * @param nodeGraphMap Map&lt;String,NTMNode&gt;; receives node
     */
    private static BoundedNode addNodeToAreaGraph(NTMLink shpLink, NTMNode shpLinkNode, Map<String, NTMNode> nodeGraphMap,
            Map<Area, NTMNode> areaNodeCentroidMap, Collection<Area> areas, NTMModel model)
    {
        Area area = findArea(shpLinkNode.getPoint().getCoordinate(), areas);
        /*
         * if (area == null) { System.err.println("Could not find area for NodeA of shapeLink " + shpLinkNode); }
         */
        // BoundedNode node1 = (BoundedNode) shpLinkNode;
        // node1.setArea(area);
        BoundedNode node;
        try
        {
            node = new BoundedNode(model.getNetwork(), shpLinkNode.getPoint().getCoordinate(), shpLinkNode.getId(), area,
                    shpLink.getBehaviourType());
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            node = null;
        }
        nodeGraphMap.put(shpLinkNode.getId(), node);
        areaNodeCentroidMap.put(area, node);
        shpLinkNode = node;
        if (!model.getAreaGraph().containsVertex(node))
        {
            model.getAreaGraph().addVertex(node);
        }
        return node;
    }

    /**
     * @param area Area; the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private static BoundedNode findCentroidInArea(NTMModel model, final Area area, Collection<NTMNode> collection)
    {
        BoundedNode centroid = null;
        for (NTMNode node : collection)
        {
            Geometry g = new GeometryFactory().createPoint(node.getPoint().getCoordinate());
            if (area.getGeometry().contains(g))
            {
                // TODO later: why multiple areas
                /*
                 * if (area != null) { System.out.println("findArea: point " + p.toText() + " is in multiple areas: " +
                 * a.getCentroidNr() + " and " + area.getCentroidNr()); }
                 */
                try
                {
                    if (node.getId().equals(area.getName()))
                    {
                        centroid = new BoundedNode(model.getNetwork(), node.getPoint().getCoordinate(), node.getId(), area,
                                node.getBehaviourType());
                    }
                    else if (centroid == null)
                    {
                        centroid = new BoundedNode(model.getNetwork(), node.getPoint().getCoordinate(), node.getId(), area,
                                node.getBehaviourType());
                    }
                }
                catch (NetworkException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        return centroid;
    }

    /**
     * @param c Coordinate; the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private static Area findArea(final Coordinate c, Collection<Area> areas)
    {
        Geometry g = new GeometryFactory().createPoint(c);
        Area area = null;
        for (Area a : areas)
        {
            if (a.getGeometry().contains(g))
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
     * @param geom1 Geometry;
     * @param geom2 Geometry;
     * @return
     */
    private static boolean findBoundaryAreas(Geometry geom1, Geometry geom2)
    {
        boolean touch = false;
        Envelope e1 = geom1.getEnvelopeInternal();
        try
        {
            // if (area != area2 && (area.getDesignLine().touches(area2.getDesignLine())
            // || area.getDesignLine().intersects(area2.getDesignLine())))
            // first see if envelopes overlap
            if (geom1 != geom2 && e1.intersects(geom2.getEnvelopeInternal()))
            {
                // 1 meter distance
                // if (area1.getDesignLine().isWithinDistance(area2.getDesignLine(), 1.0d))
                if (geom1.touches(geom2) || geom1.intersects(geom2))
                {
                    touch = true;
                }
                else if (geom1.isWithinDistance(geom2, 50.0d))
                {
                    touch = true;
                }
            }
        }
        catch (TopologyException te)
        {
            System.out.println("TopologyException " + te.getMessage() + " when checking border of " + geom1 + " and " + geom2);
        }
        return touch;
    }

    /**
     * finds the Areas that do not touch any other area and connects them with the nearest areas!!
     * @param areaNodeCentroidMap Map&lt;Area,NTMNode&gt;;
     * @param areaGraph2
     * @param linkGraph2
     * @param areas2
     */
    private static void connectIsolatedAreas(final NTMModel model, final Map<String, NTMNode> nodeGraphMap,
            final Map<Area, NTMNode> areaNodeCentroidMap, final Collection<Area> areas)
    {
        final SpatialIndex index = new STRtree();
        for (Area areaIndex : areas)
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
        final double MAX_SEARCH_DISTANCE = 8000.0; // meters?
        final int NUMBER_OF_AREAS = 6;
        for (Area isolatedArea : areas)
        {
            if (isolatedArea.getTouchingAreas().size() == 0)
            {
                System.out.println("no touching area for number " + isolatedArea.getCentroidNr() + ", Area type: "
                        + isolatedArea.getTrafficBehaviourType());
                if (isolatedArea.getCentroidNr().equals("3794"))
                {
                    System.out.println("no ");
                }
                // Get point and create search envelope
                Geometry geom = isolatedArea.getGeometry();
                Envelope search = geom.getEnvelopeInternal();
                double searchDistance = MAX_SEARCH_DISTANCE;
                search.expandBy(searchDistance);
                /*
                 * Query the spatial index for objects within the search envelope. Note that this just compares the point
                 * envelope to the line envelopes so it is possible that the point is actually more distant than
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
                NTMNode nodeIsolated = areaNodeCentroidMap.get(isolatedArea);
                for (Area nearArea : nearestAreas)
                {
                    NTMNode nodeNear = areaNodeCentroidMap.get(nearArea);
                    try
                    {
                        if (!model.getLinkGraph().containsVertex(nodeNear))
                        {
                            System.out.println("No nodeNear");
                        }
                        else if (!model.getLinkGraph().containsVertex(nodeIsolated))
                        {
                            System.out.println("No nodeNear");
                        }
                        else
                        {
                            DijkstraShortestPath<NTMNode, LinkEdge<NTMLink>> dijkstra =
                                    new DijkstraShortestPath<>(model.getLinkGraph());
                            GraphPath<NTMNode, LinkEdge<NTMLink>> sp = dijkstra.getPath(nodeIsolated, nodeNear);
                            List<LinkEdge<NTMLink>> spList = sp.getEdgeList();
                            if (spList != null)
                            {
                                double cumulativeTime = 0;
                                double cumulativeLength = 0;
                                for (LinkEdge<NTMLink> le : spList)
                                {
                                    double speed = le.getLink().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                                    double length = le.getLink().getLength().getInUnit(LengthUnit.KILOMETER);
                                    cumulativeTime += length / speed;
                                    cumulativeLength += length;
                                    Area enteredArea = findArea(le.getLink().getEndNode().getPoint().getCoordinate(), areas);
                                    if (enteredArea != null && enteredArea != isolatedArea
                                            && le.getLink().getBehaviourType() != TrafficBehaviourType.FLOW)
                                    {
                                        isolatedArea.getTouchingAreas().add(enteredArea);
                                        NTMNode centroidEntered = areaNodeCentroidMap.get(enteredArea);
                                        if (centroidEntered == null)
                                        {
                                            System.out.println("No node in this area");
                                        }
                                        Speed speedA = new Speed(cumulativeLength / cumulativeTime, SpeedUnit.KM_PER_HOUR);
                                        addGraphConnector(model, nodeIsolated, centroidEntered, speedA, speedA, le,
                                                nodeIsolated.getBehaviourType());
                                        break;
                                    }
                                    else if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
                                    {
                                        NTMNode bN = nodeGraphMap.get(le.getLink().getStartNode().getId());
                                        Speed speedA = new Speed(cumulativeLength / cumulativeTime, SpeedUnit.KM_PER_HOUR);
                                        addGraphConnector(model, nodeIsolated, bN, speedA, speedA, le,
                                                nodeIsolated.getBehaviourType());

                                        break;
                                    }
                                }
                            }

                            dijkstra = new DijkstraShortestPath<>(model.getLinkGraph());
                            sp = dijkstra.getPath(nodeNear, nodeIsolated);
                            spList = sp.getEdgeList();
                            if (spList != null)
                            {
                                double cumulativeTime = 0;
                                double cumulativeLength = 0;
                                for (LinkEdge<NTMLink> le : spList)
                                {
                                    double speed = le.getLink().getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
                                    double length = le.getLink().getLength().getInUnit(LengthUnit.KILOMETER);
                                    cumulativeTime += length / speed;
                                    cumulativeLength += length;
                                    Area enteredArea = findArea(le.getLink().getEndNode().getPoint().getCoordinate(), areas);
                                    if (enteredArea != null && enteredArea != isolatedArea
                                            && le.getLink().getBehaviourType() != TrafficBehaviourType.FLOW)
                                    {
                                        isolatedArea.getTouchingAreas().add(enteredArea);
                                        NTMNode centroidEntered = areaNodeCentroidMap.get(enteredArea);
                                        if (centroidEntered == null)
                                        {
                                            System.out.println("No node in this area");
                                        }
                                        Speed speedA = new Speed(cumulativeLength / cumulativeTime, SpeedUnit.KM_PER_HOUR);
                                        addGraphConnector(model, centroidEntered, nodeIsolated, speedA, speedA, le,
                                                nodeIsolated.getBehaviourType());
                                        break;
                                    }
                                    else if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
                                    {
                                        NTMNode bN = nodeGraphMap.get(le.getLink().getStartNode().getId());
                                        Speed speedA = new Speed(cumulativeLength / cumulativeTime, SpeedUnit.KM_PER_HOUR);
                                        addGraphConnector(model, bN, nodeIsolated, speedA, speedA, le,
                                                nodeIsolated.getBehaviourType());

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
     * @param areaStart Area;
     * @param areaEnd Area;
     * @param le LinkEdge&lt;NTMLink&gt;;
     * @param linkMap Map&lt;String,LinkEdge&lt;NTMLink&gt;&gt;;
     * @param areaNodeCentroidMap Map&lt;Area,NTMNode&gt;;
     */
    private static void createFlowConnectors(final Area areaStart, final Area areaEnd, final LinkEdge<NTMLink> le,
            final Map<String, LinkEdge<NTMLink>> linkMap, final Map<Area, NTMNode> areaNodeCentroidMap,
            final Map<String, NTMNode> nodeGraphMap, NTMModel model, Map<String, Area> areasToUse)
    {
        NTMNode node = (NTMNode) le.getLink().getStartNode();
        BoundedNode flowNodeStart = (BoundedNode) nodeGraphMap.get(node.getId());
        if (flowNodeStart.getArea() == null)
        {
            flowNodeStart.setArea(areaStart);
        }
        // BoundedNode flowNodeStart = new BoundedNode(node.getPoint().getCoordinate(), node.getId(), areaStart,
        // node.getBehaviourType());
        node = (NTMNode) le.getLink().getEndNode();
        BoundedNode flowNodeEnd = (BoundedNode) nodeGraphMap.get(node.getId());
        if (flowNodeEnd.getArea() == null)
        {
            flowNodeEnd.setArea(areaStart);
        }

        NTMLink link = le.getLink();
        ArrayList<FlowCell> cells =
                LinkCellTransmission.createCells(link, model.getSettingsNTM().getTimeStepDurationCellTransmissionModel());
        LinkCellTransmission linkCTM;
        try
        {
            linkCTM = new LinkCellTransmission(link, flowNodeStart, flowNodeEnd, cells);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            linkCTM = null;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        LinkEdge leNew = new LinkEdge(linkCTM);
        addLinkEdge(flowNodeStart, flowNodeEnd, leNew, TrafficBehaviourType.FLOW, model.getAreaGraph());
        // loop through the other links to find the links that connect
        BoundedNode cA = null;
        BoundedNode cB = null;
        // cA = areaNodeCentroidMap.get(areaStart);
        // cB = areaNodeCentroidMap.get(areaEnd);
        for (LinkEdge<NTMLink> urbanLink : linkMap.values())
        {
            if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.ROAD
                    || urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.NTM)
            {
                if (urbanLink.getLink().getEndNode().getId().equals(flowNodeStart.getId()))
                {
                    // from urban (Area) to Highway (flow)
                    Area aStart = findArea(urbanLink.getLink().getStartNode().getPoint().getCoordinate(), areasToUse.values());
                    cA = (BoundedNode) areaNodeCentroidMap.get(aStart);
                    if (aStart != null)
                    {
                        if (cA == null || flowNodeStart == null)
                        {
                            System.out.println("No connection of flow Link to Area for this one...");
                        }
                        Speed speed = new Speed(70, SpeedUnit.KM_PER_HOUR);
                        Frequency capacity =

                                new Frequency(4000.0, FrequencyUnit.PER_HOUR);
                        NTMLink newLink = NTMLink.createLink(model.getNetwork(), model.getSimulator(), cA, flowNodeStart,
                                capacity, speed, null, TrafficBehaviourType.NTM);
                        LinkEdge<NTMLink> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(cA, flowNodeStart, newLinkEdge, TrafficBehaviourType.NTM, model.getAreaGraph());

                    }
                    else
                    {
                        System.out.println("BuildGraph line 785: this Node is outside any area: "
                                + urbanLink.getLink().getStartNode().getId());
                    }

                }
                if (urbanLink.getLink().getStartNode().getId().equals(flowNodeEnd.getId()))
                {
                    // from Highway (flow) to urban (Area)
                    Area aEnd = findArea(urbanLink.getLink().getEndNode().getPoint().getCoordinate(), areasToUse.values());
                    cB = (BoundedNode) areaNodeCentroidMap.get(aEnd);
                    if (aEnd != null)
                    {
                        if (cB == null || flowNodeStart == null)
                        {
                            System.out.println("No connection of flow Link to Area for this one...");
                        }

                        Speed speed = new Speed(70, SpeedUnit.KM_PER_HOUR);
                        Frequency capacity = new Frequency(4000.0, FrequencyUnit.PER_HOUR);
                        NTMLink newLink = NTMLink.createLink(model.getNetwork(), model.getSimulator(), flowNodeEnd, cB,
                                capacity, speed, null, TrafficBehaviourType.NTM);
                        LinkEdge<NTMLink> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(flowNodeEnd, cB, newLinkEdge, TrafficBehaviourType.NTM, model.getAreaGraph());
                    }
                    else
                    {
                        System.out.println("BuildGraph line 812 this Node is outside any area: "
                                + urbanLink.getLink().getEndNode().getId());
                    }

                }
            }

            else if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.CORDON)
            {
                if (urbanLink.getLink().getEndNode().getId().equals(flowNodeStart.getId()))
                {
                    // from urban (Area) to Highway (flow)
                    node = (NTMNode) urbanLink.getLink().getStartNode();
                    cA = (BoundedNode) nodeGraphMap.get(node.getId());

                    // cA = new BoundedNode(node.getPoint().getCoordinate(), node.getId(), areaStart, node.getBehaviourType());
                    // cA = (BoundedNode) urbanLink.getLink().getStartNode();
                    if (cA != null)
                    {
                        if (flowNodeStart.getArea() == null)
                        {
                            cA.setArea(areaStart);
                        }
                        Speed speed = new Speed(70, SpeedUnit.KM_PER_HOUR);
                        Frequency capacity = new Frequency(4000.0, FrequencyUnit.PER_HOUR);
                        NTMLink newLink = NTMLink.createLink(model.getNetwork(), model.getSimulator(), cA, flowNodeStart,
                                capacity, speed, null, TrafficBehaviourType.CORDON);
                        LinkEdge<NTMLink> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(cA, flowNodeStart, newLinkEdge, TrafficBehaviourType.CORDON, model.getAreaGraph());
                    }

                }
                else if (urbanLink.getLink().getStartNode().getId().equals(flowNodeEnd.getId()))
                {
                    // from Highway (flow) to urban (Area)
                    node = (NTMNode) urbanLink.getLink().getEndNode();
                    cB = (BoundedNode) nodeGraphMap.get(node.getId());

                    // cB = new BoundedNode(node.getPoint().getCoordinate(), node.getId(), areaEnd, node.getBehaviourType());
                    // cB = (BoundedNode) urbanLink.getLink().getStartNode();
                    if (cB != null)
                    {
                        if (flowNodeStart.getArea() == null)
                        {
                            cB.setArea(areaStart);
                        }
                        Speed speed = new Speed(70, SpeedUnit.KM_PER_HOUR);
                        Frequency capacity = new Frequency(4000.0, FrequencyUnit.PER_HOUR);
                        NTMLink newLink = NTMLink.createLink(model.getNetwork(), model.getSimulator(), flowNodeEnd, cB,
                                capacity, speed, null, TrafficBehaviourType.CORDON);
                        LinkEdge<NTMLink> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(flowNodeEnd, cB, newLinkEdge, TrafficBehaviourType.CORDON, model.getAreaGraph());
                    }

                }
            }

        }
    }

    // Create new Areas where they are lacking
    /**
     * @param centroid NTMNode;
     * @return the additional areas
     */
    public static Area createMissingArea(final NTMNode centroid)
    {
        Geometry cg = new GeometryFactory().createPoint(centroid.getPoint().getCoordinate());
        Geometry buffer = cg.buffer(30);
        String nr = centroid.getId();
        String name = centroid.getId();
        String gemeente = "Area is missing for: " + centroid.getId();
        String gebied = "Area is missing for: " + centroid.getId();
        String regio = "Missing";
        double dhb = 0.0;
        Double increaseDemandByFactor = 1.0;
        ParametersNTM parametersNTM = new ParametersNTM();
        Area area = new Area(buffer, nr, name, gemeente, gebied, regio, dhb, centroid.getPoint().getCoordinate(),
                TrafficBehaviourType.NTM, new Length(0, LengthUnit.METER), new Speed(0, SpeedUnit.KM_PER_HOUR),
                increaseDemandByFactor, parametersNTM);
        return area;
    }

    /*
     * // Create new Areas where they are lacking
     *//**
        * @param centroid
        * @return the additional areas
        */
    public SimpleDirectedWeightedGraph copySimpleDirectedWeightedGraph(final SimpleDirectedWeightedGraph graph)
    {
        SimpleDirectedWeightedGraph copyOfGraph = null;
        return copyOfGraph;
    }

    /**
     * For every area, find the touching areas
     * @param gisObjects ArrayList&lt;GeoObject&gt;;
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
