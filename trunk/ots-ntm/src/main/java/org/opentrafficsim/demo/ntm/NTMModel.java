package org.opentrafficsim.demo.ntm;

import java.awt.Color;
import java.rmi.RemoteException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.demo.ntm.GeoObject.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.LinkAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

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
    private Map<String, ShpNode> shpNodes;

    /** connectors from shape file. */
    private Map<String, ShpLink> shpConnectors;

    /** links from shape file. */
    private Map<String, ShpLink> shpLinks;

    /** subset of links from shape file used as flow links. */
    private Map<String, ShpLink> flowLinks;

    /** the centroids. */
    private Map<String, ShpNode> centroids;

    /** the demand of trips by Origin and Destination. */
    private TripDemand<TripInfoTimeDynamic> tripDemand;

    /** The simulation settings. */
    private NTMSettings settingsNTM;

    /** profiles with fractions of total demand. */
    private ArrayList<DepartureTimeProfile> departureTimeProfiles;

    /** graph containing the original network. */
    private SimpleWeightedGraph<AreaNode, LinkEdge<Link>> linkGraph;

    /** graph containing the simplified network. */
    private SimpleWeightedGraph<AreaNode, LinkEdge<Link>> areaGraph;

    /** debug information?. */
    private static final boolean DEBUG = true;

    /**
     * Constructor to make the graphs with the right type.
     */
    @SuppressWarnings("unchecked")
    public NTMModel()
    {
        LinkEdge<Link> l = new LinkEdge<Link>(null);
        this.linkGraph =
                new SimpleWeightedGraph<AreaNode, LinkEdge<Link>>((Class<? extends LinkEdge<Link>>) l.getClass());
        this.areaGraph =
                new SimpleWeightedGraph<AreaNode, LinkEdge<Link>>((Class<? extends LinkEdge<Link>>) l.getClass());
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
            this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            this.shpNodes = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", false, false);

            // this.centroids = ShapeFileReader.ReadNodes("/gis/centroids.shp", "CENTROIDNR", true, true);
            // this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            // this.shpNodes = ShapeFileReader.ReadNodes("/gis/nodes.shp", "NODENR", false, false);

            this.shpLinks = new HashMap<>();
            this.shpConnectors = new HashMap<>();
            ShapeFileReader.ReadLinks("/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.shpConnectors,
                    this.shpNodes, this.centroids);
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
        // they encounter (a neighbour) on their shortest path towards that destination.

        // Initiate the simulation by creating the paths
        boolean floyd = true;
        @SuppressWarnings("unchecked")
        Collection<GraphPath<AreaNode, LinkEdge<Link>>> sp1 = null;

        long initial = System.currentTimeMillis();

        if (floyd)
        {
            sp1 = new FloydWarshallShortestPaths(this.areaGraph).getShortestPaths();
            for (GraphPath<AreaNode, LinkEdge<Link>> path : sp1)
            {
                AreaNode origin = path.getStartVertex();
                AreaNode endNode = path.getEdgeList().get(0).getLink().getEndNode();
                AreaNode startNode = path.getEdgeList().get(0).getLink().getStartNode();

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
    protected final void ntmFlowTimestep()
    {
        double accumulatedCars = 0;
        // long timeStep = 0;

        // Initiate trips from OD to first Area (Origin)
        Map<String, Map<String, TripInfoTimeDynamic>> trips = this.tripDemand.getTripInfo();
        for (Area areaFrom : this.areas.values())
        {
            // at every iteration: initiate with value 0
            if (areaFrom.getCellBehaviourNTM() == null)
            {
                System.out.println(" area null");
            }

            areaFrom.getCellBehaviourNTM().setDemandToEnter(0);

            accumulatedCars = areaFrom.getAccumulatedCars();
            if (trips.containsKey(areaFrom.getCentroidNr()))
            {
                Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(areaFrom.getCentroidNr());
                for (Area areaTo : this.areas.values())
                {
                    if (tripsFrom.containsKey(areaTo.getCentroidNr()))
                    {
                        // adjust next formulae wit time dependant variable
                        double startingTrips = tripsFrom.get(areaTo.getCentroidNr()).getNumberOfTrips();
                        tripsFrom.get(areaTo.getCentroidNr()).addToPassingTrips(startingTrips);
                        accumulatedCars += startingTrips;
                    }
                }
            }
            // put these trips in the stock of the Area (added the new Trips)
            areaFrom.setAccumulatedCars(accumulatedCars);

            // compute the total production from an Area to all other Destinations (based on the accumulation NFD)
            areaFrom.getCellBehaviourNTM().computeProductionDemand(accumulatedCars);

            // compute the total supply (maximum) from neighbours to an Area (based on the accumulation NFD)
            areaFrom.getCellBehaviourNTM().computeProductionSupply(accumulatedCars);

        }

        // compute the flows if no restrictions on the supply side
        // these will be corrected if supply poses restrictions!
        for (Area areaFrom : this.areas.values())
        {
            accumulatedCars = areaFrom.getAccumulatedCars();
            if (trips.containsKey(areaFrom.getCentroidNr()))
            {
                Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(areaFrom.getCentroidNr());
                for (Area areaTo : this.areas.values())
                {
                    if (tripsFrom.containsKey(areaTo.getCentroidNr()))
                    {
                        // retrieve the number of cars that want to leave to a neighbouring cell
                        Area neighbour = findArea(tripsFrom.get(areaTo.getCentroidNr()).getNeighbour().getPoint());
                        double share =
                                tripsFrom.get(areaTo.getCentroidNr()).getPassingTrips() / areaFrom.getAccumulatedCars();
                        double flowFromDemand = share * areaFrom.getCellBehaviourNTM().getProductionDemand();
                        tripsFrom.get(neighbour.getCentroidNr()).setFlow(flowFromDemand);
                        neighbour.getCellBehaviourNTM().addDemandToEnter(flowFromDemand);
                    }
                }
            }
        }

        // compute the flows if no restrictions on the supply side
        // these will be corrected if supply poses restrictions!
        for (Area areaFrom : this.areas.values())
        {
            accumulatedCars = areaFrom.getAccumulatedCars();
            if (trips.containsKey(areaFrom.getCentroidNr()))
            {
                Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(areaFrom.getCentroidNr());
                for (Area areaTo : this.areas.values())
                {
                    if (tripsFrom.containsKey(areaTo.getCentroidNr()))
                    {
                        // retrieve the number of cars that want to leave to a neighbouring cell
                        Area neighbour = findArea(tripsFrom.get(areaTo.getCentroidNr()).getNeighbour().getPoint());
                        double share =
                                tripsFrom.get(areaTo.getCentroidNr()).getFlow()
                                        / neighbour.getCellBehaviourNTM().getDemandToEnter();
                        double flowFromDemand = share * neighbour.getCellBehaviourNTM().getProductionSupply();
                        tripsFrom.get(neighbour.getCentroidNr()).setFlow(flowFromDemand);
                    }
                }
            }
        }

        // Evaluate the accumulation per area and determine the maximum production

        // potential transfer from a source area to all neighbours (demand)
        // evaluate the production

        // evaluate all potential transfers
        try
        {
            this.simulator
                    .scheduleEventRel(this.settingsNTM.getTimeStepDuration(), this, this, "ntmFlowTimestep", null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Build the graph using roads between touching areas (real ones), flowLinks and Cordon areas.
     */
    private void buildGraph()
    {
        // iterate over the GIS objects and find boundary areas
        // these can be "real" areas, flowLinks or the Cordon-areas

        // First, add all GIS-like objects in an array
        ArrayList<GeoObject> gisObjects = new ArrayList<GeoObject>();
        gisObjects.addAll(this.areas.values());
        gisObjects.addAll(this.flowLinks.values());
        // then find out if they touch
        for (GeoObject gis1 : gisObjects)
        {
            Geometry geom1 = gis1.getGeometry();
            for (GeoObject gis2 : gisObjects)
            {
                Geometry geom2 = gis2.getGeometry();
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
                System.out.println("no touching area for this one");
            }
        }

        for (Area area : this.areas.values())
        {
            if (area.getTouchingAreas() == null)
            {
                System.out.println("no touching area for number " + area.getCentroidNr() + ", Area type: "
                        + area.getAreaType());
                // find the nearest areas and connect them (HERE? of further down this Class...)
                // TODO make the code
            }
        }

        // temporary storage for nodes and edges mapped from the number to the node
        Map<String, AreaNode> nodeMap = new HashMap<>();
        Map<Area, AreaNode> areaNodeCentroidMap = new HashMap<>();
        Map<String, LinkEdge<Link>> linkMap = new HashMap<>();
        ArrayList<ShpLink> allLinks = new ArrayList<ShpLink>();

        allLinks.addAll(this.shpLinks.values());
        allLinks.addAll(this.flowLinks.values());
        allLinks.addAll(this.shpConnectors.values());

        // make a directed graph of the entire network
        for (ShpLink shpLink : allLinks)
        {
            // area node: copies a node from a link and connects the area
            // the nodeMap connects the shpNodes to these new AreaNode
            AreaNode n1 = nodeMap.get(shpLink.getNodeA().getNr());
            if (n1 == null)
            {
                Area areaA = findArea(shpLink.getNodeA().getPoint());
                if (areaA == null)
                {
                    System.err.println("Could not find area for NodeA of shapeLink " + shpLink);
                }

                n1 = new AreaNode(shpLink.getNodeA().getNr(), shpLink.getNodeA().getPoint(), areaA);
                nodeMap.put(shpLink.getNodeA().getNr(), n1);
                this.linkGraph.addVertex(n1);
            }
            if (shpLink.getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                if (!this.areaGraph.containsVertex(n1))
                {
                    this.areaGraph.addVertex(n1);
                }
                // areaNodeCentroidMap.put(areaA, n1);
            }

            AreaNode n2 = nodeMap.get(shpLink.getNodeB().getNr());
            if (n2 == null)
            {
                Area areaB = findArea(shpLink.getNodeB().getPoint());
                if (areaB == null)
                {
                    System.err.println("Could not find area for NodeB of shapeLink " + shpLink);
                }
                n2 = new AreaNode(shpLink.getNodeB().getNr(), shpLink.getNodeB().getPoint(), areaB);
                nodeMap.put(shpLink.getNodeB().getNr(), n2);
                this.linkGraph.addVertex(n2);
            }
            if (shpLink.getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                if (!this.areaGraph.containsVertex(n2))
                {
                    this.areaGraph.addVertex(n2);
                }
                // areaNodeCentroidMap.put(areaA, n1);
            }

            // TODO: direction of a road?
            // TODO: is the length in ShapeFiles in meters or in kilometers? I believe in km.
            if (n1 != null && n2 != null)
            {
                DoubleScalar<LengthUnit> length =
                        new DoubleScalar.Abs<LengthUnit>(shpLink.getLength(), LengthUnit.KILOMETER);
                Link link = new Link(shpLink.getNr(), n1, n2, length, shpLink.getName(), shpLink.getBehaviourType());
                LinkEdge<Link> linkEdge = new LinkEdge<>(link);
                this.linkGraph.addEdge(n1, n2, linkEdge);
                this.linkGraph.setEdgeWeight(linkEdge, length.doubleValue());
                linkMap.put(shpLink.getNr(), linkEdge);
                if (shpLink.getBehaviourType() == TrafficBehaviourType.FLOW)
                {
                    this.areaGraph.addEdge(n1, n2, linkEdge);
                    this.areaGraph.setEdgeWeight(linkEdge, length.doubleValue());
                }
            }
        }

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            AreaNode nSch = nodeMap.get("314071");
            AreaNode nVb = nodeMap.get("78816");
            DijkstraShortestPath<AreaNode, LinkEdge<Link>> sp = new DijkstraShortestPath<>(this.linkGraph, nSch, nVb);
            System.out.println("\nScheveningen -> Voorburg");
            System.out.println("Length=" + sp.getPathLength());
            List<LinkEdge<Link>> spList = sp.getPathEdgeList();
            if (spList != null)
            {
                for (LinkEdge<Link> le : spList)
                {
                    System.out.println(le.getLink().getName());
                }
            }
        }

        // put all centroids in the Graph as nodes

        // add all the cordon points that don't have an area
        for (Area area : this.areas.values())
        {
            AreaNode nc = new AreaNode(area.getCentroidNr(), area.getCentroid(), area);
            this.areaGraph.addVertex(nc);
            areaNodeCentroidMap.put(area, nc);
        }

        // iterate over the roads and map them on the area centroids
        // long uniqueNr = 0;
        for (LinkEdge<Link> le : linkMap.values())
        {
            AreaNode cA = null;
            AreaNode cB = null;
            Area aA = le.getLink().getStartNode().getArea();
            Area aB = le.getLink().getEndNode().getArea();
            // if the nodes are in adjacent areas, create a link between their centroids
            // otherwise, discard the link (either in same area, or in non-adjacent areas)

            // inspect if these flow links connect to urban roads (in/out going)
            if (le.getLink().getBehaviourType() == TrafficBehaviourType.FLOW)
            {
                AreaNode flowNodeA = le.getLink().getStartNode();
                cA = areaNodeCentroidMap.get(flowNodeA.getArea());
                String centroidNrA = flowNodeA.getId();

                AreaNode flowNodeB = le.getLink().getEndNode();
                cB = areaNodeCentroidMap.get(flowNodeB);
                String centroidNrB = flowNodeA.getId();

                addLinkEdge(flowNodeA, flowNodeB, centroidNrA, centroidNrB, le, TrafficBehaviourType.FLOW);

                for (LinkEdge<Link> urbanLink : linkMap.values())
                {
                    if (urbanLink.getLink().getBehaviourType() == TrafficBehaviourType.ROAD)
                    {
                        if (urbanLink.getLink().getEndNode().equals(flowNodeA))
                        {
                            // from urban (Area) to Highway (flow)
                            aA = urbanLink.getLink().getEndNode().getArea();
                            cA = areaNodeCentroidMap.get(aA);
                            centroidNrB = flowNodeA.getId();
                            if (aA != null)
                            {
                                centroidNrA = aA.getCentroidNr();
                                addLinkEdge(cA, flowNodeA, centroidNrA, centroidNrB, le, TrafficBehaviourType.NTM);
                            }
                            else
                            {
                                centroidNrA = "unknown";
                            }
                            
                        }
                        if (urbanLink.getLink().getStartNode().equals(flowNodeB))
                        {
                            // from Highway (flow) to urban (Area)
                            aB = urbanLink.getLink().getStartNode().getArea();
                            cB = areaNodeCentroidMap.get(aB);
                            centroidNrA = flowNodeB.getId();
                            if (aB != null)
                            {
                                centroidNrB = aB.getCentroidNr();
                                addLinkEdge(flowNodeB, cB, centroidNrA, centroidNrB, le, TrafficBehaviourType.NTM);
                            }
                            else
                            {
                                centroidNrB = "unknown";
                            }
                            
                        }
                    }
                }
            }

            else if (aA != null && aB != null && aA.getTouchingAreas().contains(aB))
            {
                cA = areaNodeCentroidMap.get(aA);
                cB = areaNodeCentroidMap.get(aB);
                if (this.areaGraph.containsEdge(cA, cB))
                {
                    // TODO: if the link between these areas already exists, add the capacity to the link
                }
                else
                {
                    addLinkEdge(cA, cB, aA.getCentroidNr(), aB.getCentroidNr(), le, TrafficBehaviourType.NTM);
                    // TODO: is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in km?
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
            // Point pSch = nodeMap.get("C1").getPoint();
            // Point pVb = nodeMap.get("C71").getPoint();
            Area aSch = findArea(pSch);
            Area aVb = findArea(pVb);
            if (aSch == null || aVb == null)
            {
                System.out.println("Could not find areas");
            }
            else
            {
                AreaNode cSch = areaNodeCentroidMap.get(aSch);
                AreaNode cVb = areaNodeCentroidMap.get(aVb);
                DijkstraShortestPath<AreaNode, LinkEdge<Link>> sp =
                        new DijkstraShortestPath<>(this.areaGraph, cSch, cVb);
                System.out.println("Length=" + sp.getPathLength());
                List<LinkEdge<Link>> spList = sp.getPathEdgeList();
                if (spList != null)
                {
                    for (LinkEdge<Link> le : spList)
                    {
                        System.out.println(le.getLink().getName());
                    }
                }
                System.out.println("Length = " + System.currentTimeMillis());

            }
        }

    }

    private void addLinkEdge(AreaNode cA, AreaNode cB, String centroidA, String centroidB, LinkEdge<Link> le,
            TrafficBehaviourType type)
    {
        // TODO: is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in km?
        DoubleScalar<LengthUnit> length =
                new DoubleScalar.Abs<LengthUnit>(cA.getPoint().distance(cB.getPoint()), LengthUnit.METER);
        Link link = new Link(le.getLink().getId(), cA, cB, length, centroidA + " - " + centroidA, type);
        LinkEdge<Link> linkEdge = new LinkEdge<>(link);
        this.areaGraph.addEdge(cA, cB, linkEdge);
        // TODO: average length? straight distance? straight distance + 20%?
        this.areaGraph.setEdgeWeight(linkEdge, length.doubleValue());
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
     * Links that show typical highway or mainroad behaviour are specified explicitly as roads
     * @param shpLinks the links of this model
     * @return the flowLinks
     */
    public static Map<String, ShpLink> createFlowLinks(final Map<String, ShpLink> shpLinks)
    {
        Map<String, ShpLink> flowLinks = new HashMap<String, ShpLink>();
        for (ShpLink shpLink : shpLinks.values())
        {
            if (shpLink.getSpeed() >= 65 && shpLink.getCapacity() > 3000)
            {
                ShpLink flowLink = new ShpLink(shpLink);
                flowLink.setBehaviourType(TrafficBehaviourType.FLOW);
                flowLinks.put(flowLink.getNr(), flowLink);
            }
        }

        for (ShpLink flowLink : flowLinks.values())
        {
            if (flowLink.getSpeed() >= 65 && flowLink.getCapacity() > 3000)
            {
                shpLinks.remove(flowLink.getNr());
            }
        }

        return flowLinks;
    }

    // Create new Areas where they are lacking
    /**
     * @param centroid
     * @return the additional areas
     */
    public static Area createMissingArea(final ShpNode centroid)
    {
        Geometry buffer = centroid.getPoint().getGeometryN(0).buffer(30);
        Point centroid1 = buffer.getCentroid();
        String nr = String.valueOf(centroid.getId());
        String name = centroid.getNr();
        String gemeente = "Area is missing for: " + centroid.getNr();
        String gebied = "Area is missing for: " + centroid.getNr();
        String regio = "Missing";
        double dhb = 0.0;
        Area area = new Area(buffer, nr, name, gemeente, gebied, regio, dhb, centroid1, TrafficBehaviourType.NTM);
        return area;
    }

    /**
     * Make the animation for each of the components that we want to see on the screen.
     */
    private void createAnimation()

    {
        try
        {
            // let's make several layers with the different types of information
            boolean showLinks = true;
            boolean showConnectors = true;
            boolean showNodes = false;
            for (Area area : this.areas.values())
            {
                new AreaAnimation(area, this.simulator, 5f);
            }
            if (showLinks)
            {
                for (ShpLink shpLink : this.shpLinks.values())
                {
                    new ShpLinkAnimation(shpLink, this.simulator, 2.0F, Color.GRAY);
                }
            }
            if (showConnectors)
            {
                for (ShpLink shpConnector : this.shpConnectors.values())
                {
                    new ShpLinkAnimation(shpConnector, this.simulator, 5.0F, Color.BLUE);
                }
            }
            for (ShpLink flowLink : this.flowLinks.values())
            {
                new ShpLinkAnimation(flowLink, this.simulator, 5.0F, Color.RED);
            }
            if (showNodes)
            {
                for (ShpNode shpNode : this.shpNodes.values())
                {
                    new ShpNodeAnimation(shpNode, this.simulator);
                }
            }
            /*
             * for (LinkEdge<Link> linkEdge : this.linkGraph.edgeSet()) { new LinkAnimation(linkEdge.getEdge(),
             * this.simulator, 0.5f); }
             */
            for (LinkEdge<Link> linkEdge : this.areaGraph.edgeSet())
            {
                new LinkAnimation(linkEdge.getLink(), this.simulator, 2.5f);
            }
            for (AreaNode node : this.areaGraph.vertexSet())
            {
                new NodeAnimation(node, this.simulator);
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
    public final Map<String, ShpLink> getFlowLinks()
    {
        return this.flowLinks;
    }

    /**
     * @param flowLinks set flowLinks.
     */
    public final void setFlowLinks(final Map<String, ShpLink> flowLinks)
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
