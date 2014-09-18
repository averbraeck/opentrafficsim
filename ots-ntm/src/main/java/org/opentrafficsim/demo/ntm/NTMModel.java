package org.opentrafficsim.demo.ntm;

import java.io.IOException;
import java.rmi.RemoteException;
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
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.LinkAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class NTMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator */
    private OTSDEVSSimulatorInterface simulator;

    /** areas */
    private Map<java.lang.Long, Area> areas;

    /** nodes from shape file */
    private Map<java.lang.Long, ShpNode> shpNodes;

    /** conectors from shape file */
    private Map<java.lang.Long, ShpLink> shpConnectors;
    
    /** links from shape file */
    private Map<java.lang.Long, ShpLink> shpLinks;

    /** the centroids */
    private Map<java.lang.Long, ShpNode> centroids;
    
    private TripDemand tripDemand;

    /** graph containing the original network */
    SimpleWeightedGraph<Node, LinkEdge> linkGraph = new SimpleWeightedGraph<Node, LinkEdge>(LinkEdge.class);

    /** graph containing the simplified network */
    SimpleWeightedGraph<Node, LinkEdge> areaGraph = new SimpleWeightedGraph<Node, LinkEdge>(LinkEdge.class);

    /** debug information? */
    private static boolean DEBUG = true;

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#constructModel(nl.tudelft.simulation.dsol.simulators.SimulatorInterface)
     */
    @Override
    public void constructModel(
            SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> _simulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) _simulator;
        try
        {
            // read the shape files
        	// public static Map<Long, ShpNode> ReadNodes(final String shapeFileName, final String numberType, boolean returnCentroid, boolean allCentroids)
        	// if returnCentroid: true: return centroids; 
        	//                    false: return nodes
        	// if allCentroids:   true: we are reading a file with only centroids
        	//                    false: mixed centroids (number starts with "C") and nodes        	
        	this.centroids = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", true, false);
            this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            this.shpNodes = ShapeFileReader.ReadNodes("/gis/TESTcordonnodes.shp", "NODENR", false, false);
      /*    this.centroids = ShapeFileReader.ReadNodes("/gis/centroids.shp", "CENTROIDNR", true, true);
            this.areas = ShapeFileReader.ReadAreas("/gis/areas.shp", this.centroids);
            this.shpNodes = ShapeFileReader.ReadNodes("/gis/nodes.shp", "NODENR", false, false);
            */

            
/*            // make all the centroids also a node.
            for (java.lang.Long nr : this.centroids.keySet())
            {
                if (this.shpNodes.containsKey(nr))
                {
                    System.out.println("Centroid nr " + nr + " equals existing node number");
                }
                else
                {
                    Point p = this.centroids.get(nr).getPoint();
                    this.shpNodes.put(nr, new ShpNode(p, nr, p.getX(), p.getY()));
                }            
                ShapeFileReader.ReadLinks("/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.shpConnectors, this.shpNodes, this.centroids);
            }*/
            this.shpLinks = new  HashMap<>();
            this.shpConnectors = new  HashMap<>();
            ShapeFileReader.ReadLinks("/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.shpConnectors, this.shpNodes, this.centroids);
            //ShapeFileReader.ReadLinks("/gis/links.shp", this.shpLinks, this.shpConnectors, this.shpNodes, this.centroids);

            // build the higher level map and the graph
            buildGraph();

        	this.tripDemand = CsvFileReader.ReadTrafficDemand("/gis/cordonmatrix_pa_os.txt");
        	
            
            // in case we run on an animator and not on a simulator, we create the animation
            if (_simulator instanceof OTSAnimatorInterface)
            {
                createAnimation();
            }
        }
        catch (IOException exception)
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
        Map<java.lang.Long, Node> nodeMap = new HashMap<>();
        Map<java.lang.Long, LinkEdge> linkMap = new HashMap<>();

        // make a directed graph of the entire network
        for (ShpLink shpLink : this.shpLinks.values())
        {
            Node n1 = nodeMap.get(shpLink.getNodeA().getNr());
            if (n1 == null)
            {
                n1 = new Node(shpLink.getNodeA().getPoint(), findArea(shpLink.getNodeA().getPoint()));
                nodeMap.put(shpLink.getNodeA().getNr(), n1);
                this.linkGraph.addVertex(n1);
            }

            Node n2 = nodeMap.get(shpLink.getNodeB().getNr());
            if (n2 == null)
            {
                n2 = new Node(shpLink.getNodeB().getPoint(), findArea(shpLink.getNodeB().getPoint()));
                nodeMap.put(shpLink.getNodeB().getNr(), n2);
                this.linkGraph.addVertex(n2);
            }

            // TODO: direction of a road?
            Link link = new Link(n1, n2, shpLink.getName());
            LinkEdge linkEdge = new LinkEdge(link);
            this.linkGraph.addEdge(n1, n2, linkEdge);
            this.linkGraph.setEdgeWeight(linkEdge, shpLink.getLength()); // shpLink.getGeometry().getLength());
            linkMap.put(shpLink.getNr(), linkEdge);
        }

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            Node nSch = nodeMap.get(314071L);
            Node nVb = nodeMap.get(78816L);
            DijkstraShortestPath<Node, LinkEdge> sp = new DijkstraShortestPath<>(this.linkGraph, nSch, nVb);
            System.out.println("\nScheveningen -> Voorburg");
            System.out.println("Length=" + sp.getPathLength());
            List<LinkEdge> spList = sp.getPathEdgeList();
            if (spList != null)
            {
                for (LinkEdge le : spList)
                {
                    System.out.println(le.getEdge().getName());
                }
            }
        }

        // put all centroids in the Graph as nodes
        Map<Area, Node> areaNodeCentroidMap = new HashMap<>();
        for (Area area : this.areas.values())
        {
            Node centroid = new Node(area.getCentroid(), area);
            Node nc = new Node(centroid.getCentroid(), area);
            this.areaGraph.addVertex(nc);
            areaNodeCentroidMap.put(area, nc);
        }

        // iterate over the roads and map them on the area centroids
        for (LinkEdge le : linkMap.values())
        {
            Area aA = le.getEdge().getNodeA().getArea();
            Area aB = le.getEdge().getNodeB().getArea();
            // if the nodes are in adjacent areas, create a link between their centroids
            // otherwise, discard the link (either in same area, or in non-adjacent areas)
            if (aA != null && aB != null && aA.getTouchingAreas().contains(aB))
            {
                Node cA = areaNodeCentroidMap.get(aA);
                Node cB = areaNodeCentroidMap.get(aB);
                if (this.areaGraph.containsEdge(cA, cB))
                {
                    // TODO: if the link between these areas already exists, add the capacity to the link
                }
                else
                {
                    Link link = new Link(cA, cB, aA.getNr() + " - " + aB.getNr());
                    LinkEdge linkEdge = new LinkEdge(link);
                    this.areaGraph.addEdge(cA, cB, linkEdge);
                    // TODO: average length? straight distance? straight distance + 20%?
                    this.areaGraph.setEdgeWeight(linkEdge, cA.getCentroid().distance(cB.getCentroid()));
                }
            }
        }

        if (DEBUG)
        {
            // test: from node 314071 (Scheveningen) to node 78816 (Voorburg)
            System.out.println("\nScheveningen -> Voorburg via centroids");
            Point pSch = nodeMap.get(314071L).getCentroid();
            Point pVb = nodeMap.get(78816L).getCentroid();
            Area aSch = findArea(pSch);
            Area aVb = findArea(pVb);
            if (aSch == null || aVb == null)
            {
                System.out.println("Could not find areas");
            }
            else
            {
                Node cSch = areaNodeCentroidMap.get(aSch);
                Node cVb = areaNodeCentroidMap.get(aVb);
                DijkstraShortestPath<Node, LinkEdge> sp = new DijkstraShortestPath<>(this.areaGraph, cSch, cVb);
                System.out.println("Length=" + sp.getPathLength());
                List<LinkEdge> spList = sp.getPathEdgeList();
                if (spList != null)
                {
                    for (LinkEdge le : spList)
                    {
                        System.out.println(le.getEdge().getName());
                    }
                }
            }
        }

    }

    /**
     * @param p the point to search
     * @return the area that contains point p, or null if not found
     */
    private Area findArea(Point p)
    {
        Area area = null;
        for (Area a : this.areas.values())
        {
            if (a.getGeometry().contains(p))
            {
                if (area != null)
                    System.out.println("findArea: point " + p.toText() + " is in multiple areas: " + a.getNr()
                            + " and " + area.getNr());
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
            for (LinkEdge linkEdge : this.linkGraph.edgeSet())
            {
                // new LinkAnimation(linkEdge.getEdge(), this.simulator, 0.5f);
            }
            for (LinkEdge linkEdge : this.areaGraph.edgeSet())
            {
                new LinkAnimation(linkEdge.getEdge(), this.simulator, 2.5f);
            }
            for (Node node : this.areaGraph.vertexSet())
            {
                new NodeAnimation(node, this.simulator);
            }
        }
        catch (NamingException | RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @see nl.tudelft.simulation.dsol.ModelInterface#getSimulator()
     */
    @Override
    public SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

}
