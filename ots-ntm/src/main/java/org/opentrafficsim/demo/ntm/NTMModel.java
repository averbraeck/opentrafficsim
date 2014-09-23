package org.opentrafficsim.demo.ntm;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.LinkAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
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
    private Map<java.lang.Long, Area> areas;

    /** nodes from shape file. */
    private Map<java.lang.Long, ShpNode> shpNodes;

    /** connectors from shape file. */
    private Map<java.lang.Long, ShpLink> shpConnectors;

    /** links from shape file. */
    private Map<java.lang.Long, ShpLink> shpLinks;

    /** the centroids. */
    private Map<java.lang.Long, ShpNode> centroids;

    /** the demand of trips by Origin and Destination. */
    private TripDemand tripDemand;

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
            // read TrafficDemand /src/main/resources
            // including information on the time period this demand covers!
            this.setTripDemand(CsvFileReader.ReadOmnitransExportDemand("/gis/cordonmatrix_pa_os.txt", ";","\\s+|-"));
            // read the time profile curves: these will be attached to the demands
            this.departureTimeProfiles = CsvFileReader.ReadDepartureTimeProfiles("/gis/profiles.txt", ";", "\\s+"); 
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

            // build the higher level map and the graph
            buildGraph();

            // in case we run on an animator and not on a simulator, we create the animation
            if (_simulator instanceof OTSAnimatorInterface)
            {
                createAnimation();
            }
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
        }
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
        Map<java.lang.Long, AreaNode> nodeMap = new HashMap<>();
        Map<java.lang.Long, LinkEdge<Link>> linkMap = new HashMap<>();

        // make a directed graph of the entire network
        for (ShpLink shpLink : this.shpLinks.values())
        {
            AreaNode n1 = nodeMap.get(shpLink.getNodeA().getNr());
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
                    nodeMap.put(shpLink.getNodeA().getNr(), n1);
                    this.linkGraph.addVertex(n1);
                }
            }

            AreaNode n2 = nodeMap.get(shpLink.getNodeB().getNr());
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
                    nodeMap.put(shpLink.getNodeB().getNr(), n2);
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
            AreaNode nSch = nodeMap.get(314071L);
            AreaNode nVb = nodeMap.get(78816L);
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
        for (Area area : this.areas.values())
        {
            AreaNode centroid = new AreaNode(area.getCentroid(), area);
            AreaNode nc = new AreaNode(centroid.getPoint(), area);
            this.areaGraph.addVertex(nc);
            areaNodeCentroidMap.put(area, nc);
        }

        // iterate over the roads and map them on the area centroids
        long uniqueNr = 0;
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
                    Link link = new Link(uniqueNr++, cA, cB, length, aA.getNr() + " - " + aB.getNr());
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
            Point pSch = nodeMap.get(314071L).getPoint();
            Point pVb = nodeMap.get(78816L).getPoint();
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
            }
        }

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
                    System.out.println("findArea: point " + p.toText() + " is in multiple areas: " + a.getNr()
                            + " and " + area.getNr());
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
                new AreaAnimation(area, this.simulator);
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

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
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
     * @return tripDemand.
     */
    public final TripDemand getTripDemand()
    {
        return this.tripDemand;
    }

    /**
     * @param tripDemand set tripDemand.
     */
    public final void setTripDemand(final TripDemand tripDemand)
    {
        this.tripDemand = tripDemand;
    }

}
