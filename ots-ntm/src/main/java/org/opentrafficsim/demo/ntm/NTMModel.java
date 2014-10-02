package org.opentrafficsim.demo.ntm;

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
    private Map<String, AreaNTM> areas;

    /** nodes from shape file. */
    private Map<String, ShpNode> shpNodes;

    /** connectors from shape file. */
    private Map<String, ShpLink> shpConnectors;

    /** links from shape file. */
    private Map<String, ShpLink> shpLinks;

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
            
            // read the shape files
            // public static Map<Long, ShpNode> ReadNodes(final String shapeFileName, final String numberType, boolean
            // returnCentroid, boolean allCentroids)
            // if returnCentroid: true: return centroids;
            // false: return nodes
            // if allCentroids: true: we are reading a file with only centroids
            // false: mixed centroids (number starts with "C") and nodes

            this.centroids = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", true, false);
            this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            this.shpNodes = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", false, false);
            /*
             * this.centroids = ShapeFileReader.ReadNodes("/gis/centroids.shp", "CENTROIDNR", true, true); this.areas =
             * ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids); this.shpNodes =
             * ShapeFileReader.ReadNodes("/gis/nodes.shp", "NODENR", false, false);
             */

            this.shpLinks = new HashMap<>();
            this.shpConnectors = new HashMap<>();
            ShapeFileReader.ReadLinks("/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.shpConnectors,
                    this.shpNodes, this.centroids);
            // ShapeFileReader.ReadLinks("/gis/links.shp", this.shpLinks, this.shpConnectors, this.shpNodes,
            // this.centroids);
            this.areas = createHighwayAreas(this.shpLinks, this.areas);
            // read the time profile curves: these will be attached to the demands
            this.setDepartureTimeProfiles(CsvFileReader.readDepartureTimeProfiles("/gis/profiles.txt", ";", "\\s+"));

            // read TrafficDemand /src/main/resources
            // including information on the time period this demand covers!
            this.setTripDemand(CsvFileReader.readOmnitransExportDemand("/gis/cordonmatrix_pa_os.txt", ";", "\\s+|-",
                    this.centroids, this.shpLinks, this.shpConnectors, this.settingsNTM,
                    this.getDepartureTimeProfiles()));

            // connect time profiles to the trips:

            // build the higher level map and the graph
            buildGraph();

            // in case we run on an animator and not on a simulator, we create the animation
            if (_simulator instanceof OTSAnimatorInterface)
            {
                createAnimation();
            }

            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "initiateSimulationNTM", null);

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
                            this.tripDemand.getTripDemand_Origin_Destination(origin.getId(), endNode.getId());
                    if (tripInfo != null)
                    {
                        tripInfo.setNeighbour(endNode);
                    }
                }
            }
        }

        long now = System.currentTimeMillis() - initial;
        System.out.println("Floyd: time duration in millis = " + now);

        // if we want to repeat:
        try
        {
            this.simulator.scheduleEventRel(this.settingsNTM.getTimeStepDuration(), this, this, "startSimulate", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * 
     */
    protected final void startSimulate()
    {
        double accumulatedCars = 0;
        //long timeStep = 0;
        
        // Initiate trips from OD to first Area (Origin)
        Map<String, Map<String, TripInfoTimeDynamic>> trips = this.tripDemand.getTripInfo();
        for (AreaNTM areaFrom : this.areas.values())
        {
            // at every iteration: initiate with value 0
            areaFrom.getCellBehaviourNTM().setDemandToEnter(0);

            accumulatedCars = areaFrom.getAccumulatedCars();
            if (trips.containsKey(areaFrom))
            {
                Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(areaFrom);
                for (AreaNTM areaTo : this.areas.values())
                {
                    if (tripsFrom.containsKey(areaTo))
                    {
                        // adjust next formulae wit time dependant variable
                        double startingTrips = tripsFrom.get(areaTo).getNumberOfTrips();
                        tripsFrom.get(areaTo).addToPassingTrips(startingTrips);
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
        for (AreaNTM areaFrom : this.areas.values())
        {
            accumulatedCars = areaFrom.getAccumulatedCars();
            if (trips.containsKey(areaFrom))
            {
                Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(areaFrom);
                for (AreaNTM areaTo : this.areas.values())
                {
                    if (tripsFrom.containsKey(areaTo))
                    {
                        // retrieve the number of cars that want to leave to a neighbouring cell
                        AreaNTM neighbour = findArea(tripsFrom.get(areaTo).getNeighbour().getPoint());
                        double share = tripsFrom.get(areaTo).getPassingTrips() / areaFrom.getAccumulatedCars();
                        double flowFromDemand = share * areaFrom.getCellBehaviourNTM().getProductionDemand();
                        tripsFrom.get(neighbour).setFlow(flowFromDemand);     
                        neighbour.getCellBehaviourNTM().addDemandToEnter(flowFromDemand);
                    }
                }
            }
        }

        // compute the flows if no restrictions on the supply side
        // these will be corrected if supply poses restrictions!
        for (AreaNTM areaFrom : this.areas.values())
        {
            accumulatedCars = areaFrom.getAccumulatedCars();
            if (trips.containsKey(areaFrom))
            {
                Map<String, TripInfoTimeDynamic> tripsFrom = trips.get(areaFrom);
                for (AreaNTM areaTo : this.areas.values())
                {
                    if (tripsFrom.containsKey(areaTo))
                    {
                        // retrieve the number of cars that want to leave to a neighbouring cell
                        AreaNTM neighbour = findArea(tripsFrom.get(areaTo).getNeighbour().getPoint());
                        double share = tripsFrom.get(areaTo).getFlow() / neighbour.getCellBehaviourNTM().getDemandToEnter();
                        double flowFromDemand = share * neighbour.getCellBehaviourNTM().getProductionSupply();
                        tripsFrom.get(neighbour).setFlow(flowFromDemand);
                    }
                }
            }
        }
        
        
        
        // Evaluate the accumulation per area and determine the maximum production

        // potential transfer from a source area to all neighbours (demand)
        // evaluate the production

        // evaluate all potential transfers

    }

    /**
     * Build the graph using roads between touching areas.
     */
    private void buildGraph()
    {
        // iterate over the areas and find boundary areas
        for (Area area1 : this.areas.values())
        {
            Geometry g1 = area1.getGeometry();
            Envelope e1 = g1.getEnvelopeInternal();
            for (Area area2 : this.areas.values())
            {
                try
                {
                    // if (area != area2 && (area.getGeometry().touches(area2.getGeometry())
                    // || area.getGeometry().intersects(area2.getGeometry())))
                    // first see if envelopes overlap
                    if (area1 != area2 && e1.intersects(area2.getGeometry().getEnvelopeInternal()))
                    {
                        // 1 meter distance
                        // if (area1.getGeometry().isWithinDistance(area2.getGeometry(), 1.0d))
                        if (area1.getGeometry().touches(area2.getGeometry())
                                || area1.getGeometry().intersects(area2.getGeometry()))
                        {
                            area1.getTouchingAreas().add(area2);
                        }
                    }
                }
                catch (TopologyException te)
                {
                    System.out.println("TopologyException " + te.getMessage() + " when checking border of " + area1
                            + " and " + area2);
                }
            }
        }

        // temporary storage for nodes and edges mapped from the number to the node
        Map<String, AreaNode> nodeMap = new HashMap<>();
        Map<String, LinkEdge<Link>> linkMap = new HashMap<>();

        // make a directed graph of the entire network
        for (ShpLink shpLink : this.shpLinks.values())
        {
            // area node: copies a node from a link and connects the area
            // the nodeMap connects the shpNodes to these new AreaNode
            AreaNode n1 = nodeMap.get(shpLink.getNodeA().getId());
            if (n1 == null)
            {
                Area areaA = findArea(shpLink.getNodeA().getPoint());
                if (areaA == null)
                {
                    System.err.println("Could not find area for NodeA of shapeLink " + shpLink);
                }
                else
                {
                    n1 = new AreaNode(shpLink.getNodeA().getPoint(), areaA);
                    nodeMap.put(shpLink.getNodeA().getName(), n1);
                    this.linkGraph.addVertex(n1);
                }
            }

            AreaNode n2 = nodeMap.get(shpLink.getNodeB().getId());
            if (n2 == null)
            {
                Area areaB = findArea(shpLink.getNodeB().getPoint());
                if (areaB == null)
                {
                    System.err.println("Could not find area for NodeB of shapeLink " + shpLink);
                }
                else
                {
                    n2 = new AreaNode(shpLink.getNodeB().getPoint(), areaB);
                    nodeMap.put(shpLink.getNodeB().getName(), n2);
                    this.linkGraph.addVertex(n2);
                }
            }

            // TODO: direction of a road?
            // TODO: is the length in ShapeFiles in meters or in kilometers? I believe in km.
            if (n1 != null && n2 != null)
            {
                DoubleScalar<LengthUnit> length =
                        new DoubleScalar.Abs<LengthUnit>(shpLink.getLength(), LengthUnit.KILOMETER);
                Link link = new Link(shpLink.getNr(), n1, n2, length, shpLink.getName());
                LinkEdge<Link> linkEdge = new LinkEdge<>(link);
                this.linkGraph.addEdge(n1, n2, linkEdge);
                this.linkGraph.setEdgeWeight(linkEdge, length.doubleValue());
                linkMap.put(shpLink.getNr(), linkEdge);
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
        Map<Area, AreaNode> areaNodeCentroidMap = new HashMap<>();
        // add all the cordon points that don't have an area
        for (Area area : this.areas.values())
        {
            AreaNode centroid = new AreaNode(area.getCentroid(), area);
            AreaNode nc = new AreaNode(centroid.getPoint(), area);
            this.areaGraph.addVertex(nc);
            areaNodeCentroidMap.put(area, nc);
        }

        // iterate over the roads and map them on the area centroids
        // long uniqueNr = 0;
        for (LinkEdge<Link> le : linkMap.values())
        {
            Area aA = le.getLink().getStartNode().getArea();
            Area aB = le.getLink().getEndNode().getArea();
            // if the nodes are in adjacent areas, create a link between their centroids
            // otherwise, discard the link (either in same area, or in non-adjacent areas)
            if (aA != null && aB != null && aA.getTouchingAreas().contains(aB))
            {
                AreaNode cA = areaNodeCentroidMap.get(aA);
                AreaNode cB = areaNodeCentroidMap.get(aB);
                if (this.areaGraph.containsEdge(cA, cB))
                {
                    // TODO: if the link between these areas already exists, add the capacity to the link
                }
                else
                {
                    // TODO: is the distance between two points in Amersfoort Rijksdriehoeksmeting Nieuw in m or in km?
                    DoubleScalar<LengthUnit> length =
                            new DoubleScalar.Abs<LengthUnit>(cA.getPoint().distance(cB.getPoint()), LengthUnit.METER);
                    Link link =
                            new Link(le.getLink().getId(), cA, cB, length, aA.getCentroidNr() + " - "
                                    + aB.getCentroidNr());
                    LinkEdge<Link> linkEdge = new LinkEdge<>(link);
                    this.areaGraph.addEdge(cA, cB, linkEdge);
                    // TODO: average length? straight distance? straight distance + 20%?
                    this.areaGraph.setEdgeWeight(linkEdge, length.doubleValue());
                }
            }
        }

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

    /**
     * @param p the point to search.
     * @return the area that contains point p, or null if not found.
     */
    private AreaNTM findArea(final Point p)
    {
        AreaNTM area = null;
        for (AreaNTM a : this.areas.values())
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
     * Make the animation for each of the components that we want to see on the screen.
     */
    private void createAnimation()

    {
        try
        {
            // let's make several layers with the different types of information
            for (Area area : this.areas.values())
            {
                new AreaAnimation(area, this.simulator, 2.5f);
            }
            for (ShpLink shpLink : this.shpLinks.values())
            {
                new ShpLinkAnimation(shpLink, this.simulator);
            }
            for (ShpNode shpNode : this.shpNodes.values())
            {
                new ShpNodeAnimation(shpNode, this.simulator);
            }
            for (LinkEdge<Link> linkEdge : this.linkGraph.edgeSet())
            {
                // new LinkAnimation(linkEdge.getEdge(), this.simulator, 0.5f);
            }
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

    // Create new Areas around highways, that show different behaviour
    /**
     * @param shpLinks the links of this model
     * @param areas the intial areas
     * @return the additional areas
     */
    public static Map<String, AreaNTM> createHighwayAreas(final Map<String, ShpLink> shpLinks,
            final Map<String, AreaNTM> areas)
    {
        for (ShpLink shpLink : shpLinks.values())
        {
            if (shpLink.getSpeed() > 70 && shpLink.getCapacity() > 3400)
            {
                Geometry buffer = shpLink.getGeometry().buffer(10);
                Point centroid = buffer.getCentroid();
                String nr = shpLink.getNr();
                String name = shpLink.getName();
                String gemeente = shpLink.getName();
                String gebied = shpLink.getName();
                String regio = shpLink.getName();
                double dhb = 0.0;
                AreaNTM area = new AreaNTM(buffer, nr, name, gemeente, gebied, regio, dhb, centroid);
                areas.put(nr, area);
            }
        }
        return areas;
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

}
