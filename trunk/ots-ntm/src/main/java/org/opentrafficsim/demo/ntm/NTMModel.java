package org.opentrafficsim.demo.ntm;

import java.awt.Color;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;

import java.util.Map;

import javax.naming.NamingException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.IO.WriteToShp;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;

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
            DoubleScalar.Rel<TimeUnit> timeStepNTM = new DoubleScalar.Rel<TimeUnit>(10, TimeUnit.SECOND);
            DoubleScalar.Rel<TimeUnit> timeStepCellTransmissionModel =
                    new DoubleScalar.Rel<TimeUnit>(2, TimeUnit.SECOND);
            Rel<TimeUnit> durationOfSimulation = new DoubleScalar.Rel<TimeUnit>(7200, TimeUnit.SECOND);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
            Calendar startTime = new GregorianCalendar(2014, 1, 28, 7, 0, 0);
            this.settingsNTM =
                    new NTMSettings(startTime, durationOfSimulation, " NTM The Hague ", timeStepNTM,
                            timeStepCellTransmissionModel);

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

            // read the time profile curves: these will be attached to the demands afterwards
            this.setDepartureTimeProfiles(CsvFileReader.readDepartureTimeProfiles("/gis/profiles.txt", ";", "\\s+"));

            // read TrafficDemand from /src/main/resources
            // including information on the time period this demand covers!
            // within "readOmnitransExportDemand" the cordon zones are determined and areas are created around them
            this.setTripDemand(CsvFileReader.readOmnitransExportDemand("/gis/cordonmatrix_pa_os.txt", ";", "\\s+|-",
                    this.centroids, this.shpLinks, this.shpConnectors, this.settingsNTM,
                    this.getDepartureTimeProfiles(), this.areas));

            this.flowLinks = createFlowLinks(this.shpLinks);

            // merge link segments between junctions:
            Link.findSequentialLinks(this.flowLinks, this.nodes);
            Link.findSequentialLinks(this.shpLinks, this.nodes);

            // save the selected and created areas to a shape file 
            WriteToShp.createShape(this.areas);
            
            // build the higher level map and the graph
            BuildGraph.buildGraph(this);

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
        // These paths are used to determine the first area (neighbour) on the path to destination!!!
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
                BoundedNode destination = path.getEndVertex();

                // determine the start and endnode of the first edge that starts from the origin
                // the endNode of this edge is the "Neighbour" area
                Node node = path.getEdgeList().get(0).getLink().getStartNode();
                BoundedNode startNode = new BoundedNode(node.getPoint(), node.getId(), null, node.getBehaviourType());
                node = path.getEdgeList().get(0).getLink().getEndNode();
                BoundedNode endNode = new BoundedNode(node.getPoint(), node.getId(), null, node.getBehaviourType());

                // the order of endNode and startNode of the edge seems to be not consistent!!!!!!
                if (origin.equals(endNode))
                {
                    endNode = startNode;
                }

                TripInfoTimeDynamic tripInfo =
                        this.tripDemand.getTripDemandOriginToDestination(origin.getId(), destination.getId());
                // for all OD-pairs with trips, the TripInfo is already initiated
                // if this relationship is not yet initiated, we do it here

                if (tripInfo == null)
                {
                    tripInfo = new TripInfoTimeDynamic(0, null);
                    tripInfo.setNeighbour(endNode);
                    this.tripDemand.setTripDemandOriginToDestination(origin.getId(), destination.getId(), tripInfo,
                            this.tripDemand.getTripInfo());
                }
                else
                {
                    tripInfo.setNeighbour(endNode);
                }
                if (origin.getId().equals("481332"))
                {
                    if (tripInfo.getNeighbour() == null)
                    {
                        System.out.println("neighbour???");
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
        NTMsimulation.simulate(this);
        // in case we run on an animator and not on a simulator, we create the animation
        if (this.simulator instanceof OTSAnimatorInterface)
        {
            createDynamicAreaAnimation();
        }
        try
        {
            // start this method again
            this.simulator.scheduleEventRel(this.settingsNTM.getTimeStepDurationNTM(), this, this, "ntmFlowTimestep",
                    null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
            boolean showLinks = true;
            boolean showFlowLinks = true;
            boolean showConnectors = true;
            boolean showNodes = true;
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
                    new ShpLinkAnimation(flowLink, this.simulator, 2.0F, Color.RED);
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
                    new ShpLinkAnimation(linkEdge.getLink(), this.simulator, 5.5f, Color.BLACK);
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

    /**
     * Make the animation for each of the components that we want to see on the screen.
     */
    private void createDynamicAreaAnimation()

    {
        try
        {
            // let's make several layers with the different types of information
            boolean showLinks = false;
            boolean showFlowLinks = false;
            boolean showConnectors = false;
            boolean showNodes = false;
            boolean showEdges = false;
            boolean showAreaNode = false;
            boolean showArea = true;

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
                    new ShpLinkAnimation(flowLink, this.simulator, 2.0F, Color.RED);
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
                    new ShpLinkAnimation(linkEdge.getLink(), this.simulator, 5.5f, Color.BLACK);
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

    /**
     * @return areaGraph.
     */
    public final SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> getAreaGraph()
    {
        return this.areaGraph;
    }

    /**
     * @return shpConnectors.
     */
    public final Map<String, Link> getShpConnectors()
    {
        return this.shpConnectors;
    }

    /**
     * @param shpConnectors set shpConnectors.
     */
    public final void setShpConnectors(final Map<String, Link> shpConnectors)
    {
        this.shpConnectors = shpConnectors;
    }

    /**
     * @return areas.
     */
    public final Map<String, Area> getAreas()
    {
        return this.areas;
    }

    /**
     * @return nodes.
     */
    public final Map<String, Node> getNodes()
    {
        return this.nodes;
    }

    /**
     * @return shpLinks.
     */
    public final Map<String, Link> getShpLinks()
    {
        return this.shpLinks;
    }

    /**
     * @return centroids.
     */
    public final Map<String, Node> getCentroids()
    {
        return this.centroids;
    }

    /**
     * @return linkGraph.
     */
    public final SimpleWeightedGraph<BoundedNode, LinkEdge<Link>> getLinkGraph()
    {
        return this.linkGraph;
    }

    /**
     * Links that show typical highway or mainroad behaviour are specified explicitly as roads.
     * @param shpLinks the links of this model
     * @return the flowLinks
     */
    public static Map<String, Link> createFlowLinks(final Map<String, Link> shpLinks)
    {
        Map<String, Link> flowLinks = new HashMap<String, Link>();
        DoubleScalar<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(65, SpeedUnit.KM_PER_HOUR);
        DoubleScalar<FrequencyUnit> maxCapacity = new DoubleScalar.Abs<FrequencyUnit>(3000, FrequencyUnit.PER_HOUR);
        for (Link shpLink : shpLinks.values())
        {

            if (shpLink.getSpeed().doubleValue() >= maxSpeed.doubleValue()
                    && shpLink.getCapacity().doubleValue() > maxCapacity.doubleValue())
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

            if (flowLink.getSpeed().doubleValue() >= maxSpeed.doubleValue()
                    && flowLink.getCapacity().doubleValue() > maxCapacity.doubleValue())
            {
                shpLinks.remove(flowLink.getId());
            }
        }

        return flowLinks;
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
