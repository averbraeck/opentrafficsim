package org.opentrafficsim.demo.ntm;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.LinkAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class NTMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** areas. */
    private Map<String, Area> areas;

    /** nodes from shape file. */
    private Map<String, Node> nodes;

    /** connectors from shape file. */
    private Map<String, Link> shpConnectors;

    /** links from shape file. */
    private Map<String, Link> shpLinks;

    /** subset of links from shape file used as flow links. */
    private Map<String, Link> flowLinks;

    /** the centroids. */
    private Map<String, Node> centroids;

    /** the demand of trips by Origin and Destination. */
    private TripDemand<TripInfoTimeDynamic> tripDemand;

    /** The simulation settings. */
    private NTMSettings settingsNTM;

    /** profiles with fractions of total demand. */
    private ArrayList<DepartureTimeProfile> departureTimeProfiles;

    /** graph containing the original network. */
    private SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> linkGraph;

    /** graph containing the simplified network. */
    private SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> areaGraph;

    /** debug information?. */
    private static final boolean DEBUG = false;

    /**
     * Constructor to make the graphs with the right type.
     */
    @SuppressWarnings("unchecked")
    public NTMModel()
    {
        LinkEdge<Link> l = new LinkEdge<Link>(null);
        this.linkGraph =
                new SimpleWeightedGraph<BoundedNode, LinkEdge<Link>>((Class<? extends LinkEdge<Link>>) l.getClass());
        this.areaGraph =
                new SimpleWeightedGraph<BoundedNode, LinkEdge<Link>>((Class<? extends LinkEdge<Link>>) l.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> _simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) _simulator;
        try
        {
            // set the time step value at ten seconds;
            DoubleScalar.Rel<TimeUnit> timeStep = new DoubleScalar.Rel<TimeUnit>(10, TimeUnit.SECOND);
            this.settingsNTM = new NTMSettings(timeStep);

            // Read the shape files with the function:
            // public static Map<Long, ShpNode> ReadNodes(final String shapeFileName, final String numberType, boolean
            // returnCentroid, boolean allCentroids)
            // if returnCentroid: true: return centroids;
            // false: return nodes
            // if allCentroids: true: we are reading a file with only centroids
            // false: mixed file with centroids (number starts with "C") and normal nodes

            this.centroids = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", true, false);
            this.areas = ShapeFileReader.readAreas("/gis/areas.shp", this.centroids);
            this.nodes = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", false, false);

            // this.centroids = ShapeFileReader.ReadNodes("/gis/centroids.shp", "CENTROIDNR", true, true);
            // this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            // this.shpNodes = ShapeFileReader.ReadNodes("/gis/nodes.shp", "NODENR", false, false);

            this.shpLinks = new HashMap<>();
            this.shpConnectors = new HashMap<>();
            ShapeFileReader.readLinks("/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.shpConnectors,
                    this.nodes, this.centroids);
            // ShapeFileReader.ReadLinks("/gis/links.shp", this.shpLinks, this.shpConnectors, this.shpNodes,
            // this.centroids);

            // read the time profile curves: these will be attached to the demands
            this.setDepartureTimeProfiles(CsvFileReader.readDepartureTimeProfiles("/gis/profiles.txt", ";", "\\s+"));

            // read TrafficDemand /src/main/resources
            // including information on the time period this demand covers!
            // within "readOmnitransExportDemand" the cordon zones are determined and areas are created around them
            this.setTripDemand(CsvFileReader.readOmnitransExportDemand("/gis/cordonmatrix_pa_os.txt", ";", "\\s+|-",
                    this.centroids, this.shpLinks, this.shpConnectors, this.settingsNTM,
                    this.getDepartureTimeProfiles(), this.areas));


            this.flowLinks = createFlowLinks(this.shpLinks);
            // connect time profiles to the trips:

            // build the higher level map and the graph
            buildGraph();

            // shortest paths creation
            initiateSimulationNTM();

            // in case we run on an animator and not on a simulator, we create the animation
            if (_simulator instanceof OTSAnimatorInterface)
            {
                createAnimation();
            }

            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "ntmFlowTimestep", null);
            // this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(1799.99, TimeUnit.SECOND), this, this,
            // "drawGraph", null);
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Generate demand at fixed intervals based on traffic demand (implemented by re-scheduling this method).
     */
    protected final void initiateSimulationNTM()
    {
        // At time zero, there are no cars in the network
        // we start the simulation by injecting traffic into the areas, based on the traffic demand input
        // The trips are put in the "stock" of the area, but keep a reference to the destination and to the first Area
        // they encounter (neighbour) on their shortest path towards that destination.

        // Initiate the simulation by creating the paths
        boolean floyd = true;
        @SuppressWarnings("unchecked")
        Collection<GraphPath<BoundedNode, LinkEdge<Link>>> sp1 = null;

        long initial = System.currentTimeMillis();

        if (floyd)
        {
            sp1 = new FloydWarshallShortestPaths(this.areaGraph).getShortestPaths();
            for (GraphPath<BoundedNode, LinkEdge<Link>> path : sp1)
            {
                BoundedNode origin = path.getStartVertex();

                Node node =path.getEdgeList().get(0).getLink().getStartNode();
                BoundedNode startNode = new BoundedNode(node.getPoint(), node.getId(), null, node.getBehaviourType());
                node =path.getEdgeList().get(0).getLink().getEndNode();
                BoundedNode endNode = new BoundedNode(node.getPoint(), node.getId(), null, node.getBehaviourType());

                //BoundedNode endNode = (BoundedNode) path.getEdgeList().get(0).getLink().getEndNode();
                //BoundedNode startNode = (BoundedNode) path.getEdgeList().get(0).getLink().getStartNode();

                // the order of endNode and startNode seems to be not consistent!!!!!!
                if (origin.equals(endNode))
                {
                    endNode = startNode;
                }
                // TODO: the centroids at the cordon have yet to be connected to the area (yet to be created)
                if (origin.getId().startsWith("C") && endNode.getId().startsWith("C"))
                {
                    TripInfoTimeDynamic tripInfo =
                            this.tripDemand.getTripDemandOriginToDestination(origin.getId(), endNode.getId());
                    if (tripInfo != null)
                    {
                        tripInfo.setNeighbour(endNode);
                    }
                }
            }
        }

        long now = System.currentTimeMillis() - initial;
        System.out.println("Floyd: time duration in millis = " + now);

    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected final void ntmFlowTimestep()
    {
        double accumulatedCars = 0;
        // long timeStep = 0;

        // Initiate trips from OD to first Area (Origin)
        Map<String, Map<String, TripInfoTimeDynamic>> trips = this.tripDemand.getTripInfo();
        // retrieve information from the Area Graph containing the NTM areas and the selected highways
        for (BoundedNode nodefromNTM : this.areaGraph.vertexSet())
        {
            try
            {
                if (nodefromNTM.getBehaviourType() == TrafficBehaviourType.NTM)
                {
                    // first loop through the NTM and Cordon Area "nodes" that generate traffic from the trip demand
                    // file
                    CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) nodefromNTM.getCellBehaviour();
                    // double cars = cellBehaviour.retrieveDemand(nodeFromNTM.getDemandToEnter(),
                    // nodeFromNTM.getMaxCapacity(), nodeFromNTM.getParametersNTM());
                    accumulatedCars = cellBehaviour.getAccumulatedCars();
                    if (trips.containsKey(nodefromNTM.getArea().getCentroidNr()))
                    {
                        Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(nodefromNTM.getArea().getCentroidNr());
                        for (BoundedNode nodeTo : this.areaGraph.vertexSet())
                        {
                            if (tripsFrom.containsKey(nodeTo.getId()))
                            {
                                // adjust next formulae wit time dependant variable
                                double startingTrips = tripsFrom.get(nodeTo.getId()).getNumberOfTrips();
                                tripsFrom.get(nodeTo.getId()).addToPassingTrips(startingTrips);
                                accumulatedCars += startingTrips;
                            }
                        }
                    }
                    // put these trips in the stock of the Area (added the new Trips)
                    cellBehaviour.setAccumulatedCars(accumulatedCars);
                    // compute the total production from an Area to all other Destinations (based on the accumulation
                    // NFD)
                    cellBehaviour.setDemand(cellBehaviour.retrieveDemand(accumulatedCars,
                            cellBehaviour.getMaxCapacity(), cellBehaviour.getParametersNTM()));
                    // compute the total supply (maximum) from neighbours to an Area (based on the accumulation NFD)
                    cellBehaviour.setSupply(cellBehaviour.retrieveSupply(accumulatedCars,
                            cellBehaviour.getMaxCapacity(), cellBehaviour.getParametersNTM()));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // compute the flows if no restrictions on the supply side // these will be corrected if supply poses
        // restrictions!
        for (BoundedNode nodefromNTM : this.areaGraph.vertexSet())
        {
            try
            {
                if (nodefromNTM.getBehaviourType() == TrafficBehaviourType.NTM)
                {
                    // first loop through the NTM and Cordon Area "nodes" that generate traffic from the trip demand
                    // file
                    CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) nodefromNTM.getCellBehaviour();
                    accumulatedCars = cellBehaviour.getAccumulatedCars();
                    if (trips.containsKey(nodefromNTM.getArea().getCentroidNr()))
                    {
                        Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(nodefromNTM.getArea().getCentroidNr());
                        for (BoundedNode nodeTo : this.areaGraph.vertexSet())
                        {
                            if (tripsFrom.containsKey(nodeTo.getId()))
                            {
                                // retrieve the number of cars that want to leave to a neighbouring cell
                                BoundedNode neighbour = (BoundedNode) tripsFrom.get(nodeTo.getId()).getNeighbour();
                                double share =
                                        tripsFrom.get(nodeTo.getId()).getPassingTrips()
                                                / cellBehaviour.getAccumulatedCars();
                                double flowFromDemand = share * cellBehaviour.getDemand();
                                tripsFrom.get(neighbour.getId()).setFlow(flowFromDemand);
                                ((CellBehaviourNTM) neighbour.getCellBehaviour()).addDemandToEnter(flowFromDemand);
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
        // compute the flows if no restrictions on the supply side
        // these will be corrected if supply poses restrictions!
        for (BoundedNode nodefromNTM : this.areaGraph.vertexSet())
        {
            try
            {
                if (nodefromNTM.getBehaviourType() == TrafficBehaviourType.NTM)
                {
                    CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) nodefromNTM.getCellBehaviour();
                    // double cars = cellBehaviour.retrieveDemand(nodeFromNTM.getDemandToEnter(),
                    // nodeFromNTM.getMaxCapacity(), nodeFromNTM.getParametersNTM());
                    accumulatedCars = cellBehaviour.getAccumulatedCars();
                    if (trips.containsKey(nodefromNTM.getArea().getCentroidNr()))
                    {
                        Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(nodefromNTM.getArea().getCentroidNr());
                        for (BoundedNode nodeTo : this.areaGraph.vertexSet())
                        {
                            if (tripsFrom.containsKey(nodeTo.getId()))
                            {
                                // retrieve the number of cars that want to leave to a neighbouring cell
                                BoundedNode neighbour = (BoundedNode) tripsFrom.get(nodeTo.getId()).getNeighbour();
                                CellBehaviourNTM cellBehaviourNeighbour =
                                        (CellBehaviourNTM) neighbour.getCellBehaviour();
                                double share =
                                        cellBehaviourNeighbour.getFlow() / cellBehaviourNeighbour.getDemandToEnter();
                                double flowFromDemand = share * cellBehaviourNeighbour.getSupply();
                                cellBehaviourNeighbour.setFlow(flowFromDemand);
                                tripsFrom.get(neighbour.getId()).setFlow(flowFromDemand);
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

        // Evaluate the accumulation per area and determine the maximum production

        // potential transfer from a source area to all neighbours (demand)
        // evaluate the production

        // evaluate all potential transfers
        try
        {
            this.simulator
                    .scheduleEventRel(this.settingsNTM.getTimeStepDurationNTM(), this, this, "ntmFlowTimestep", null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Build the graph using roads between touching areas, flowLinks and Cordon areas (artificially created).
     */
    private void buildGraph()
    {

        // temporary storage for nodes and edges mapped from the number to the node
        Map<String, BoundedNode> nodeMap = new HashMap<>();
        Map<String, BoundedNode> nodeGraphMap = new HashMap<>();
        Map<Area, BoundedNode> areaNodeCentroidMap = new HashMap<>();
        Map<String, LinkEdge<Link>> linkMap = new HashMap<>();
        ArrayList<Link> allLinks = new ArrayList<Link>();

        allLinks.addAll(this.shpLinks.values());
        allLinks.addAll(this.flowLinks.values());
        allLinks.addAll(this.shpConnectors.values());

        // make a directed graph of the entire network

        // FIRST, add ALL VERTICES
        for (Link shpLink : allLinks)
        {
            // area node: copies a node from a link and connects the area
            // the nodeMap connects the shpNodes to these new AreaNode
            BoundedNode nodeA = nodeMap.get(shpLink.getStartNode().getId());
            if (nodeA == null)
            {
                nodeA = addNodeToLinkGraph(shpLink, shpLink.getStartNode(), nodeA, nodeMap);
            }
            BoundedNode nodeB = nodeMap.get(shpLink.getEndNode().getId());
            if (nodeB == null)
            {
                nodeB = addNodeToLinkGraph(shpLink, shpLink.getEndNode(), nodeB, nodeMap);
            }

            // TODO: direction of a road?
            // TODO: is the length in ShapeFiles in meters or in kilometers? I believe in km.
            if (nodeA != null && nodeB != null)
            {
                // DoubleScalar<LengthUnit> length =
                // new DoubleScalar.Abs<LengthUnit>(shpLink.getLength(), LengthUnit.KILOMETER);
                LinkEdge<Link> linkEdge = new LinkEdge<>(shpLink);
                this.linkGraph.addEdge(nodeA, nodeB, linkEdge);
                this.linkGraph.setEdgeWeight(linkEdge, shpLink.getLength().doubleValue());
                linkMap.put(shpLink.getId(), linkEdge);
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
                            addNodeToAreaGraph(shpLink, shpLink.getStartNode(), nodeA, nodeGraphMap,
                                    areaNodeCentroidMap);
                }

                nodeB = nodeGraphMap.get(shpLink.getEndNode().getId());
                if (nodeB == null)
                {
                    nodeB = addNodeToAreaGraph(shpLink, shpLink.getEndNode(), nodeB, nodeGraphMap, areaNodeCentroidMap);
                }
            }

        }

        // and finally put all centroids in the Graph as vertices
        for (Area area : this.areas.values())
        {
            BoundedNode node = nodeMap.get(area.getCentroidNr());
            if (node != null)
            {
                areaNodeCentroidMap.put(area, node);
                nodeGraphMap.put(node.getId(), node);
                this.areaGraph.addVertex(node);
                this.linkGraph.addVertex(node);
            }
            else
            {
                System.out.println("look out!!! line 459");
            }

        }

        // ///////////////////////////////////////
        // SECOND part of the graph creation:
        // The next section creates the EDGES

        // First, add all GIS-like objects in an array
        ArrayList<GeoObject> gisObjects = new ArrayList<GeoObject>();
        gisObjects.addAll(this.areas.values());
        // gisObjects.addAll(this.flowLinks.values());

        findTouching(gisObjects);

        // Secondly, find the Areas that do not touch any other area and connect them with the nearest areas!!

        connectIsolatedAreas(this.areas, this.linkGraph, this.areaGraph, areaNodeCentroidMap);

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            BoundedNode nSch = nodeMap.get("314071");
            BoundedNode nVb = nodeMap.get("78816");

            DijkstraShortestPath<BoundedNode, LinkEdge<Link>> sp =
                    new DijkstraShortestPath<>(this.linkGraph, nSch, nVb);
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
            Area aA = findArea(le.getLink().getStartNode().getPoint());
            Area aB = findArea(le.getLink().getEndNode().getPoint());

            // When this is a flow link, inspect if they connect to urban roads
            // if so, create a GraphEdge that connects flow roads with urban roads / areas (in/out going)
            if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                // make connectors (in the areaGraph!!)
                createFlowConnectors(aA, aB, le, linkMap, areaNodeCentroidMap);
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
                    if (this.areaGraph.containsEdge(cA, cB))
                    {
                        // TODO: if the link between these areas already exists, add the capacity to the link
                    }
                    else
                    {
                        if (cA == null || cB == null)
                        {
                            System.out.println("test");
                        }
                        else
                        {
                            DoubleScalar<FrequencyUnit> capacity =
                                    new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                            DoubleScalar<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                            Link newLink = Link.createLink(cA, cB, capacity, speed);
                            LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                            addLinkEdge(cA, cB, newLinkEdge, TrafficBehaviourType.NTM, this.areaGraph);
                        }
                        // TODO: is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in
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
            Area aSch = findArea(pSch);
            Area aVb = findArea(pVb);
            if (aSch == null || aVb == null)
            {
                System.out.println("Could not find areas");
            }
            else
            {
                BoundedNode cSch = areaNodeCentroidMap.get(aSch);
                BoundedNode cVb = areaNodeCentroidMap.get(aVb);
                DijkstraShortestPath<BoundedNode, LinkEdge<Link>> sp =
                        new DijkstraShortestPath<>(this.areaGraph, cSch, cVb);
                System.out.println("Length=" + sp.getPathLength());
                List<LinkEdge<Link>> spList = sp.getPathEdgeList();
                if (spList != null)
                {
                    for (LinkEdge<Link> le : spList)
                    {
                        System.out.println(le.getLink().getLinkData().getName());
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
    private void addLinkEdge(BoundedNode flowNodeA, BoundedNode flowNodeB, LinkEdge<Link> linkEdge,
            TrafficBehaviourType type, SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> graph)
    {
        // TODO: is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in km?

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
        // TODO: average length? straight distance? straight distance + 20%?
        graph.setEdgeWeight(linkEdge, linkEdge.getLink().getLength().doubleValue());
    }

    /**
     * @param shpLink link
     * @param node node
     * @param map receives node
     */
    private BoundedNode addNodeToLinkGraph(Link shpLink, Node shpLinkNode, BoundedNode node,
            Map<String, BoundedNode> map)
    {
        Area area = findArea(shpLinkNode.getPoint());
        if (area == null)
        {
            System.err.println("Could not find area for NodeA of shapeLink " + shpLinkNode);
        }

        node = new BoundedNode(shpLinkNode.getPoint(), shpLinkNode.getId(), area, shpLink.getBehaviourType());
        map.put(shpLinkNode.getId(), node);
        this.linkGraph.addVertex(node);
        return node;
    }

    /**
     * @param shpLink link
     * @param node node
     * @param nodeGraphMap receives node
     */
    private BoundedNode addNodeToAreaGraph(Link shpLink, Node shpLinkNode, BoundedNode node,
            Map<String, BoundedNode> nodeGraphMap, Map<Area, BoundedNode> areaNodeCentroidMap)
    {
        Area area = findArea(shpLinkNode.getPoint());
        if (area == null)
        {
            System.err.println("Could not find area for NodeA of shapeLink " + shpLinkNode);
        }
        node = new BoundedNode(shpLinkNode.getPoint(), shpLinkNode.getId(), area, shpLink.getBehaviourType());
        nodeGraphMap.put(shpLinkNode.getId(), node);
        areaNodeCentroidMap.put(area, node);
        this.areaGraph.addVertex(node);
        return node;
    }

    /**
     * @param p the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private Area findArea(final Point p)
    {
        Area area = null;
        for (Area a : this.areas.values())
        {
            if (a.getGeometry().contains(p))
            {
                if (area != null)
                {
                    System.out.println("findArea: point " + p.toText() + " is in multiple areas: " + a.getCentroidNr()
                            + " and " + area.getCentroidNr());
                }
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
    private void connectIsolatedAreas(Map<String, Area> areasAll,
            SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> linkGraphIn,
            SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> areaGraphIn, Map<Area, BoundedNode> areaNodeCentroidMap)
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
                while (nearestAreas.size() > NUMBER_OF_AREAS)
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
                        if (!this.linkGraph.containsVertex(nodeNear))
                        {
                            System.out.println("No nodeNear");
                        }
                        else if (!this.linkGraph.containsVertex(nodeIsolated))
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
                                    Area enteredArea = findArea(le.getLink().getEndNode().getPoint());
                                    if (enteredArea != null && enteredArea != isolatedArea)
                                    {
                                        isolatedArea.getTouchingAreas().add(enteredArea);
                                        BoundedNode centroidEntered = areaNodeCentroidMap.get(enteredArea);
                                        DoubleScalar<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                                        DoubleScalar<FrequencyUnit> capacity =
                                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                                        Link newLink = Link.createLink(nodeIsolated, centroidEntered, capacity, speed);
                                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                                        addLinkEdge(nodeIsolated, centroidEntered, newLinkEdge,
                                                TrafficBehaviourType.NTM, areaGraphIn);
                                        break;
                                    }
                                    else if (le.getLink().getBehaviourType().equals(TrafficBehaviourType.FLOW))
                                    {
                                        // BoundedNode bN = (BoundedNode) le.getLink().getStartNode();
                                        // addLinkEdge(nodeIsolated, bN, isolatedArea.getCentroidNr(), bN.getId(), le,
                                        // TrafficBehaviourType.NTM, areaGraphIn);
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
     * @param aA
     * @param aB
     * @param le
     * @param linkMap
     * @param areaNodeCentroidMap
     */
    private void createFlowConnectors(Area aA, Area aB, LinkEdge<Link> le, Map<String, LinkEdge<Link>> linkMap,
            Map<Area, BoundedNode> areaNodeCentroidMap)
    {
        Node node = le.getLink().getStartNode();
        BoundedNode flowNodeA = new BoundedNode(node.getPoint(), node.getId(), aA, node.getBehaviourType());
        node = le.getLink().getEndNode();
        BoundedNode flowNodeB = new BoundedNode(node.getPoint(), node.getId(), aB, node.getBehaviourType());
        //BoundedNode flowNodeA = (BoundedNode) le.getLink().getStartNode();
        //BoundedNode flowNodeB = (BoundedNode) le.getLink().getEndNode();
        addLinkEdge(flowNodeA, flowNodeB, le, TrafficBehaviourType.FLOW, this.areaGraph);
        // loop through the other links to find the links that connect
        BoundedNode cA = null;
        BoundedNode cB = null;
        cA = areaNodeCentroidMap.get(aA);
        cB = areaNodeCentroidMap.get(aB);

        for (LinkEdge<Link> urbanLink : linkMap.values())
        {
            if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.ROAD)
            {
                if (urbanLink.getLink().getEndNode().getId().equals(flowNodeA.getId()))
                {
                    // from urban (Area) to Highway (flow)
                    aA = findArea(urbanLink.getLink().getStartNode().getPoint());
                    cA = areaNodeCentroidMap.get(aA);
                    if (aA != null)
                    {
                        if (cA == null || flowNodeA == null)
                        {
                            System.out.println("Stop");
                        }
                        DoubleScalar<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink = Link.createLink(cA, flowNodeA, capacity, speed);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(cA, flowNodeA, newLinkEdge, TrafficBehaviourType.NTM, this.areaGraph);

                    }
                    else
                    {
                        System.out.println("aA == Null................");
                    }

                }
                if (urbanLink.getLink().getStartNode().getId().equals(flowNodeB.getId()))
                {
                    // from Highway (flow) to urban (Area)
                    aB = findArea(urbanLink.getLink().getEndNode().getPoint());
                    cB = areaNodeCentroidMap.get(aB);
                    if (aB != null)
                    {
                        DoubleScalar<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink = Link.createLink(flowNodeB, cB, capacity, speed);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(flowNodeB, cB, newLinkEdge, TrafficBehaviourType.NTM, this.areaGraph);
                    }
                    else
                    {
                        System.out.println("aB == Null................");
                    }

                }
            }

            else if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.CORDON)
            {
                if (urbanLink.getLink().getEndNode().getId().equals(flowNodeA.getId()))
                {
                    // from urban (Area) to Highway (flow)
                    node = urbanLink.getLink().getStartNode();
                    cA = new BoundedNode(node.getPoint(), node.getId(), aA, node.getBehaviourType());
                    //cA = (BoundedNode) urbanLink.getLink().getStartNode();
                    if (cA != null)
                    {
                        DoubleScalar<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink = Link.createLink(cA, flowNodeA, capacity, speed);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(cA, flowNodeA, newLinkEdge, TrafficBehaviourType.CORDON, this.areaGraph);
                    }
                    else
                    {
                        System.out.println("cA == Null................");
                    }

                }
                else if (urbanLink.getLink().getStartNode().getId().equals(flowNodeB.getId()))
                {
                    // from Highway (flow) to urban (Area)
                    node = urbanLink.getLink().getEndNode();
                    cB = new BoundedNode(node.getPoint(), node.getId(), aB, node.getBehaviourType());
                    //cB = (BoundedNode) urbanLink.getLink().getStartNode();
                    if (cB != null)
                    {
                        DoubleScalar<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(70, SpeedUnit.KM_PER_HOUR);
                        DoubleScalar<FrequencyUnit> capacity =
                                new DoubleScalar.Abs<FrequencyUnit>(4000.0, FrequencyUnit.PER_HOUR);
                        Link newLink = Link.createLink(flowNodeB, cB, capacity, speed);
                        LinkEdge<Link> newLinkEdge = new LinkEdge<>(newLink);
                        addLinkEdge(flowNodeB, cB, newLinkEdge, TrafficBehaviourType.CORDON, this.areaGraph);
                    }
                    else
                    {
                        System.out.println("cB == Null................");
                    }

                }
            }

        }
    }

    /**
     * Links that show typical highway or mainroad behaviour are specified explicitly as roads
     * @param shpLinks the links of this model
     * @return the flowLinks
     */
    public static Map<String, Link> createFlowLinks(final Map<String, Link> shpLinks)
    {
        Map<String, Link> flowLinks = new HashMap<String, Link>();
        for (Link shpLink : shpLinks.values())
        {
            DoubleScalar<FrequencyUnit> capacity = new DoubleScalar.Abs<FrequencyUnit>(3000, FrequencyUnit.PER_HOUR);
            if (shpLink.getSpeed().doubleValue() >= 65 && shpLink.getCapacity().doubleValue() > capacity.doubleValue())
            {
                Link flowLink = new Link(shpLink);
                if (flowLink.getGeometry() == null)
                {
                    System.out.println("nnn");
                }
                flowLink.setBehaviourType(TrafficBehaviourType.FLOW);
                flowLinks.put(flowLink.getId(), flowLink);
            }
        }

        for (Link flowLink : flowLinks.values())
        {
            if (flowLink.getSpeed().doubleValue() >= 65 && flowLink.getCapacity().doubleValue() > 3000)
            {
                shpLinks.remove(flowLink.getId());
            }
        }

        return flowLinks;
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
    private void findTouching(ArrayList<GeoObject> gisObjects)
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

    /**
     * Make the animation for each of the components that we want to see on the screen.
     */
    private void createAnimation()

    {
        try
        {
            // let's make several layers with the different types of information
            boolean showLinks = false;
            boolean showFlowLinks = false;
            boolean showConnectors = false;
            boolean showNodes = false;
            boolean showEdges = true;
            boolean showAreaNode = true;
            boolean showArea = false;

            if (showArea)
            {
                for (Area area : this.areas.values())
                {
                    new AreaAnimation(area, this.simulator, 5f);
                }
            }
            if (showLinks)
            {
                for (Link shpLink : this.shpLinks.values())
                {
                    new ShpLinkAnimation(shpLink, this.simulator, 2.0F, Color.GRAY);
                }
            }
            if (showConnectors)
            {
                for (Link shpConnector : this.shpConnectors.values())
                {
                    new ShpLinkAnimation(shpConnector, this.simulator, 5.0F, Color.BLUE);
                }
            }

            if (showFlowLinks)
            {
                for (Link flowLink : this.flowLinks.values())
                {
                    new ShpLinkAnimation(flowLink, this.simulator, 5.0F, Color.RED);
                }
            }
            if (showNodes)
            {
                for (Node Node : this.nodes.values())
                {
                    new ShpNodeAnimation(Node, this.simulator);
                }
            }
            // for (LinkEdge<Link> linkEdge : this.linkGraph.edgeSet())
            // {
            // new LinkAnimation(linkEdge.getEdge(), this.simulator, 0.5f);
            // }
            if (showEdges)
            {
                for (LinkEdge<Link> linkEdge : this.areaGraph.edgeSet())
                {
                    new ShpLinkAnimation(linkEdge.getLink(), this.simulator, 2.5f, Color.BLACK);
                }
            }
            if (showAreaNode)
            {
                for (Node node : this.areaGraph.vertexSet())
                {
                    new NodeAnimation(node, this.simulator);
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

    /**
     * @return settingsNTM.
     */
    public final NTMSettings getSettingsNTM()
    {
        return this.settingsNTM;
    }

    /**
     * @param settingsNTM set settingsNTM.
     */
    public final void setSettingsNTM(final NTMSettings settingsNTM)
    {
        this.settingsNTM = settingsNTM;
    }

    /**
     * @return tripDemand.
     */
    public final TripDemand getTripDemand()
    {
        return this.tripDemand;
    }

    /**
     * @param tripDemand set tripDemand.
     */
    public final void setTripDemand(TripDemand tripDemand)
    {
        this.tripDemand = tripDemand;
    }

    /**
     * @return departureTimeProfiles.
     */
    public final ArrayList<DepartureTimeProfile> getDepartureTimeProfiles()
    {
        return this.departureTimeProfiles;
    }

    /**
     * @param departureTimeProfiles set departureTimeProfiles.
     */
    public final void setDepartureTimeProfiles(final ArrayList<DepartureTimeProfile> departureTimeProfiles)
    {
        this.departureTimeProfiles = departureTimeProfiles;
    }

    /**
     * @return flowLinks.
     */
    public final Map<String, Link> getFlowLinks()
    {
        return this.flowLinks;
    }

    /**
     * @param flowLinks set flowLinks.
     */
    public final void setFlowLinks(final Map<String, Link> flowLinks)
    {
        this.flowLinks = flowLinks;
    }
    /*
     * // Create new Areas around highways, that show different behaviour
     *//**
     * @param shpLinks the links of this model
     * @param areas the intial areas
     * @return the additional areas
     */
    /*
     * public static Map<String, AreaNTM> createCordonFeederAreas(final Map<String, ShpLink> shpLinks, final Map<String,
     * AreaNTM> areas) { for (ShpLink shpLink : shpLinks.values()) { if (shpLink.getSpeed() > 70 &&
     * shpLink.getCapacity() > 3400) { Geometry buffer = shpLink.getGeometry().buffer(10); Point centroid =
     * buffer.getCentroid(); String nr = shpLink.getNr(); String name = shpLink.getName(); String gemeente =
     * shpLink.getName(); String gebied = shpLink.getName(); String regio = shpLink.getName(); double dhb = 0.0; AreaNTM
     * area = new AreaNTM(buffer, nr, name, gemeente, gebied, regio, dhb, centroid); areas.put(nr, area); } } return
     * areas; }
     */

}
