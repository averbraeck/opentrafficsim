package org.opentrafficsim.demo.ntm;

import java.awt.geom.Path2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opentrafficsim.core.network.AbstractLink;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SIUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

/**
 * A link contains the following information:
 * 
 * <pre>
 * the_geom class com.vividsolutions.jts.geom.MultiLineString MULTILINESTRING ((232250.38755446894 ...
 * and a lot of data attributes such as speed and length
 * </pre>
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Link extends AbstractLink<String, Node> 
{
    /** SPEEDAB class java.lang.Double 120.0. */
    private DoubleScalar.Abs<SpeedUnit> speed;

    /** the lines for the animation, relative to the centroid. */
    private Set<Path2D> lines = null;

    /** */
    private LinkData linkData;

    /** traffic behaviour. */
    private TrafficBehaviourType behaviourType;

    private ArrayList<FlowCell> flowCells;

    /**
     * @param geometry
     * @param nr
     * @param name
     * @param direction
     * @param length
     * @param startNode
     * @param endNode
     * @param speed
     * @param capacity
     * @param behaviourType
     */

    public Link(final LinearGeometry geometry, final String nr, final DoubleScalar.Rel<LengthUnit> length, final Node startNode,
            final Node endNode, DoubleScalar.Abs<SpeedUnit> speed, final DoubleScalar.Abs<FrequencyUnit> capacity,
            final TrafficBehaviourType behaviourType, LinkData linkData, int hierarchy)
    {

        super(nr, startNode, endNode, length, capacity, hierarchy);
        setGeometry(geometry);
        this.speed = speed;
        this.behaviourType = behaviourType;
        this.linkData = linkData;
        this.setGeometry(geometry);
        if (geometry != null)
        {
            Coordinate[] cc = geometry.getLineString().getCoordinates();
            if (cc.length == 0)
            {
                System.out.println("cc.length = 0 for " + nr + " (" + nr + ")");
            }
            else
            {
                if (Math.abs(cc[0].x - startNode.getPoint().getX()) > 0.001
                        && Math.abs(cc[0].x - endNode.getPoint().getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - startNode.getPoint().getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - endNode.getPoint().getX()) > 0.001)
                {
                    System.out.println("x coordinate non-match for " + nr + " (" + nr + "); cc[0].x=" + cc[0].x
                            + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x=" + startNode.getPoint().getX()
                            + ", nodeB.x=" + endNode.getPoint().getX());
                }
            }
        }

    }

    /**
     * @param link
     */
    public Link(final Link link)
    {
        super(link.getId(), link.getStartNode(), link.getEndNode(), link.getLength(), link.getCapacity(), link.getHierarchy());
        setGeometry(link.getGeometry());
        this.speed = link.speed;
        if (this.getGeometry() != null)
        {
            Coordinate[] cc = this.getGeometry().getLineString().getCoordinates();
            if (cc.length == 0)
                System.out.println("cc.length = 0 for " + this.getId() + " (" + this.getId() + ")");
            else
            {
                if (Math.abs(cc[0].x - this.getStartNode().getPoint().getX()) > 0.001
                        && Math.abs(cc[0].x - this.getEndNode().getPoint().getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - this.getStartNode().getPoint().getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - this.getEndNode().getPoint().getX()) > 0.001)
                    System.out.println("x coordinate non-match for " + this.getId() + " (" + this.getId()
                            + "); cc[0].x=" + cc[0].x + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x="
                            + this.getStartNode().getPoint().getX() + ", nodeB.x="
                            + this.getEndNode().getPoint().getX());
            }
        }
    }

    /**
     * @param startNode
     * @param endNode
     * @param capacity
     * @param speed
     * @param trafficBehaviourType
     * @return
     */
    public static Link createLink(Node startNode, Node endNode, Abs<FrequencyUnit> capacity,
            DoubleScalar.Abs<SpeedUnit> speed, TrafficBehaviourType trafficBehaviourType,  int hierarchy)

    {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate coordStart = new Coordinate(startNode.getPoint().getX(), startNode.getPoint().getY());
        Coordinate coordEnd = new Coordinate(endNode.getPoint().getX(), endNode.getPoint().getY());
        Coordinate[] coords = new Coordinate[]{coordStart, coordEnd};
        LineString line = geometryFactory.createLineString(coords);
        DoubleScalar.Rel<LengthUnit> length =
                new DoubleScalar.Rel<LengthUnit>(startNode.getPoint().distance(endNode.getPoint()), LengthUnit.METER);

        String nr = startNode.getId() + " - " + endNode.getId();
        Link newLink = new Link(null, nr, length, startNode, endNode, speed, capacity, trafficBehaviourType, null, hierarchy);
        try
        {
            LinearGeometry geometry = new LinearGeometry(newLink, line, null);
            newLink.setGeometry(geometry);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        return newLink;
    }

    /**
     * @param links HashMap
     */
    public static void findSequentialLinks(final Map<String, Link> links, Map<String, Node> nodes)
    {
        // compare all links
        HashMap<Node, ArrayList<Link>> linksStartAtNode = new HashMap<Node, ArrayList<Link>>();
        HashMap<Node, ArrayList<Link>> linksEndAtNode = new HashMap<Node, ArrayList<Link>>();
        // HashMap<Node, Link> endNodeToLinkMap = new HashMap<Node, Link>();
        // HashMap<Node, Integer> numberOfLinksFromStartNodeMap = new HashMap<Node, Integer>();
        // find out how many links start from the endNode of a link
        for (Link link : links.values())
        {
            // we put the first link we find in the map
            // as we are only interested in a connection with one in- and one out, one is enough
            if (linksStartAtNode.get(link.getStartNode()) == null)
            {
                ArrayList<Link> localLinks = new ArrayList<Link>();
                localLinks.add(link);
                linksStartAtNode.put(link.getStartNode(), localLinks);
            }
            else
            {
                ArrayList<Link> localLinks = linksStartAtNode.get(link.getStartNode());
                localLinks.add(link);
                linksStartAtNode.put(link.getStartNode(), localLinks);
            }

            if (link.getEndNode().getId().equals("62673"))
            {
                System.out.println("test: ");
            }

            if (linksEndAtNode.get(link.getEndNode()) == null)
            {
                ArrayList<Link> localLinks = new ArrayList<Link>();
                localLinks.add(link);
                linksEndAtNode.put(link.getEndNode(), localLinks);
            }
            else
            {
                ArrayList<Link> localLinks = linksEndAtNode.get(link.getEndNode());
                localLinks.add(link);
                linksEndAtNode.put(link.getEndNode(), localLinks);
            }
            // meanwhile look if there is only one link that starts from this node.
            // if there are more links starting, we don't have to exclude this link

        }

        HashMap<Link, ArrayList<Link>> upLinks = new HashMap<Link, ArrayList<Link>>();
        HashMap<Link, ArrayList<Link>> downLinks = new HashMap<Link, ArrayList<Link>>();
        for (Link link : links.values())
        {
            if (link.getEndNode().getId().equals("1090639793"))
            {
                System.out.println("test: ");
            }

            ArrayList<Link> downStreamLinks = null;
            if (linksStartAtNode.get(link.getEndNode()) != null)
            {
                downStreamLinks = new ArrayList<Link>(linksStartAtNode.get(link.getEndNode()));
            }

            ArrayList<Link> upStreamLinks = null;
            if (linksEndAtNode.get(link.getStartNode()) != null)
            {
                upStreamLinks = new ArrayList<Link>(linksEndAtNode.get(link.getStartNode()));

            }
            // remove the BA link (U-turn)
            if (downStreamLinks != null)
            {
                for (Link down : downStreamLinks)
                {
                    if (down.getEndNode().equals(link.getStartNode()))
                    {
                        downStreamLinks.remove(down);
                        break;
                    }
                }
            }
            downLinks.put(link, downStreamLinks);
            if (upStreamLinks != null)
            {

                for (Link up : upStreamLinks)
                {
                    if (up.getStartNode().equals(link.getEndNode()))
                    {
                        upStreamLinks.remove(up);
                        break;
                    }
                }
            }
            upLinks.put(link, upStreamLinks);
        }

        boolean loopedAllLinks = false;
        boolean finished = false;
        while (!loopedAllLinks)
        {
            finished = true;
            boolean noMoreFound = true;

            for (Link link : links.values())
            {
                ArrayList<Link> downStreamLinks = downLinks.get(link);
                // join this "link" with the "down" link, if they have no junction

                if (downStreamLinks != null)
                {
                    if (downStreamLinks.size() == 1)
                    {
                        Link down = downStreamLinks.get(0);
                        if (upLinks.get(down).size() == 1)
                        {
                            if (link.getSpeed().equals(down.getSpeed())
                                    && link.getCapacity().equals(down.getCapacity())
                                    && link.getBehaviourType().equals(down.getBehaviourType()))
                            {
                                noMoreFound = false;
                                Link mergedLink = joinLink(link, down);
                                if (mergedLink != null)
                                {
                                    ArrayList<Link> downLink = downLinks.get(down);
                                    upLinks.put(mergedLink, downLink);
                                    ArrayList<Link> upLink = upLinks.get(link);
                                    upLinks.put(mergedLink, upLink);

                                    ArrayList<Link> downDownStreamLinks = downLinks.get(down);
                                    if (downDownStreamLinks != null)
                                    {
                                        for (Link downDown : downDownStreamLinks)
                                        {
                                            if (upLinks.get(downDown) != null)
                                            {
                                                for (Link up : upLinks.get(downDown))
                                                {
                                                    if (up == down)
                                                    {
                                                        upLinks.get(downDown).remove(down);
                                                        upLinks.get(downDown).add(mergedLink);
                                                        break;
                                                    }

                                                }
                                            }
                                        }
                                    }

                                    ArrayList<Link> upStreamLinks = upLinks.get(link);
                                    if (upStreamLinks != null)
                                    {
                                        for (Link up : upStreamLinks)
                                        {
                                            if (downLinks.get(up) != null)
                                            {
                                                for (Link down1 : downLinks.get(up))
                                                {
                                                    if (down1 == link)
                                                    {
                                                        downLinks.get(up).remove(link);
                                                        downLinks.get(up).add(mergedLink);
                                                        break;
                                                    }

                                                }
                                            }
                                        }
                                    }

                                    upLinks.remove(link);
                                    downLinks.remove(link);
                                    upLinks.remove(down);
                                    downLinks.remove(down);
                                    links.remove(link.getId());
                                    links.remove(down.getId());
                                    nodes.remove(link.getEndNode().getId());
                                    links.put(mergedLink.getId(), mergedLink);

                                    finished = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (finished || noMoreFound)
            {
                loopedAllLinks = true;
            }
        }
    }

    /*
     * String splitBy =";"; String[] idList = down.getId().split(splitBy); int size = idList.length; if
     * (IdToLinkMap.get(idList[size-1] + "_BA") != null) { outLinks--; } else { if (idList[size-1].contains("_BA")) {
     * String Id = idList[size-1].replace("_BA", ""); if (IdToLinkMap.get(Id) != null) { outLinks--; } } }
     */

    /**
     * @param up
     * @param down
     * @return
     */
    public static Link joinLink(Link up, Link down)
    {
        LinkData dataUp = up.getLinkData();
        LinkData dataDown = down.getLinkData();
        Link mergedLink = null;
        LineMerger lineMerger = new LineMerger();
        Collection<Geometry> lineStrings = new ArrayList<Geometry>();
        lineStrings.add(down.getGeometry().getLineString());
        lineStrings.add(up.getGeometry().getLineString());
        lineMerger.add(lineStrings);
        Collection<Geometry> mergedLineStrings = lineMerger.getMergedLineStrings();
        Geometry mergedGeometry = mergedLineStrings.iterator().next();
        String nr = down.getId() + ";" + up.getId();
        if (nr.equals("557336_BA_557337_557333"))
        {
            System.out.println("test: ");
        }
        // System.out.println("test: " + nr + " length A: " + up.getLength().doubleValue() + " length B: "
        // + down.getLength().doubleValue());

        DoubleScalar.Rel<LengthUnit> length =
                new DoubleScalar.Rel<LengthUnit>(up.getLength().getSI() + down.getLength().getSI(), LengthUnit.METER);

        mergedLink =
                new Link(null, nr, length, up.getStartNode(), down.getEndNode(), up.getSpeed(), up.getCapacity(),
                        up.getBehaviourType(), up.getLinkData(), up.getHierarchy());

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate[] coords = mergedGeometry.getCoordinates();
        LineString line = geometryFactory.createLineString(coords);
        LinearGeometry geometry;
        try
        {
            geometry = new LinearGeometry(mergedLink, line, null);
            mergedLink.setGeometry(geometry);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }

        return mergedLink;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        Point c = this.getGeometry().getLineString().getCentroid();
        return new DirectedPoint(new double[]{c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        DirectedPoint c = getLocation();
        Envelope envelope = this.getGeometry().getLineString().getEnvelopeInternal();
        return new BoundingBox(new Point3d(envelope.getMinX() - c.x, envelope.getMinY() - c.y, 0.0d), new Point3d(
                envelope.getMaxX() - c.x, envelope.getMaxY() - c.y, 0.0d));
    }

    /**
     * @return polygon
     * @throws RemoteException
     */
    public Set<Path2D> getLines() throws RemoteException
    {
        // create the polygon if it did not exist before
        if (this.lines == null)
        {
            double dx = this.getLocation().getX();
            double dy = this.getLocation().getY();
            // double dx = 0;
            // double dy = 0;
            this.lines = new HashSet<Path2D>();
            for (int i = 0; i < this.getGeometry().getLineString().getNumGeometries(); i++)
            {
                Path2D line = new Path2D.Double();
                Geometry g = this.getGeometry().getLineString().getGeometryN(i);
                boolean start = true;
                for (Coordinate c : g.getCoordinates())
                {
                    if (start)
                    {
                        line.moveTo(c.x - dx, dy - c.y);
                        start = false;
                    }
                    else
                    {
                        line.lineTo(c.x - dx, dy - c.y);
                    }
                }
                this.lines.add(line);
            }
        }
        return this.lines;
    }

    /**
     * @return speed
     */
    public DoubleScalar.Abs<SpeedUnit> getSpeed()
    {
        return this.speed;
    }

    /**
     * @return behaviourType.
     */
    public TrafficBehaviourType getBehaviourType()
    {
        return this.behaviourType;
    }

    /**
     * @param behaviourType set behaviourType.
     */
    public void setBehaviourType(TrafficBehaviourType behaviourType)
    {
        this.behaviourType = behaviourType;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ShpLink [nr=" + this.getId() + ", nodeA=" + this.getStartNode() + ", nodeB=" + this.getEndNode() + "]";
    }

    /**
     * @return linkData.
     */
    public LinkData getLinkData()
    {
        return linkData;
    }

    /**
     * @param linkData set linkData.
     */
    public void setLinkData(LinkData linkData)
    {
        this.linkData = linkData;
    }

}
