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

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.geotools.LinearGeometry;
import org.opentrafficsim.core.network.geotools.LinkGeotools;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;

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
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionSep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Link extends LinkGeotools<String, String>
{
    /** SPEEDAB class java.lang.Double 120.0. */
    private DoubleScalar.Abs<SpeedUnit> freeSpeed;

    /** SPEEDAB class java.lang.Double 120.0. */
    private DoubleScalar.Rel<TimeUnit> time;

    /** the lines for the animation, relative to the centroid. */
    private Set<Path2D> lines = null;

    /** */
    private LinkData linkData;

    /** */
    private Abs<FrequencyUnit> corridorCapacity;

    /** */
    private int numberOfLanes;

    /** traffic behaviour. */
    private TrafficBehaviourType behaviourType;

    /**
     * @param geometry
     * @param nr
     * @param length
     * @param startNode
     * @param endNode
     * @param freeSpeed
     * @param time
     * @param capacity
     * @param behaviourType
     * @param linkData
     * @param name
     * @param direction
     */

    public Link(final LinearGeometry geometry, final String nr, final DoubleScalar.Rel<LengthUnit> length,
            final Node startNode, final Node endNode, DoubleScalar.Abs<SpeedUnit> freeSpeed,
            DoubleScalar.Rel<TimeUnit> time, final DoubleScalar.Abs<FrequencyUnit> capacity,
            final TrafficBehaviourType behaviourType, LinkData linkData)
    {
        super(nr, startNode, endNode, length, capacity);
        if (null == behaviourType)
        {
            System.out.println("behaviourType is null!");
        }
        // LinkGeotools(final IDL id, final NodeGeotools<IDN> startNode, final NodeGeotools<IDN> endNode,
        // final DoubleScalar.Rel<LengthUnit> length, final DoubleScalar.Abs<FrequencyUnit> capacity)
        this.freeSpeed = freeSpeed;
        this.time = time;
        this.behaviourType = behaviourType;
        this.numberOfLanes = estimateLanes(capacity, freeSpeed);
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
                if (Math.abs(cc[0].x - startNode.getX()) > 0.001 && Math.abs(cc[0].x - endNode.getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - startNode.getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - endNode.getX()) > 0.001)
                {
                    System.out.println("x coordinate non-match for " + nr + " (" + nr + "); cc[0].x=" + cc[0].x
                            + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x=" + startNode.getX() + ", nodeB.x="
                            + endNode.getX());
                }
            }
        }

    }

    /**
     * @param capacity
     * @param speed2
     * @return
     */
    private int estimateLanes(Abs<FrequencyUnit> capacity, Abs<SpeedUnit> speed)
    {
        int lanes = 0;
        if (capacity != null)
        {
            if (speed.getInUnit(SpeedUnit.KM_PER_HOUR) >= 80)
            {
                if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 3000)
                {
                    lanes = 1;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 5000)
                {
                    lanes = 2;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 7000)
                {
                    lanes = 3;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 9000)
                {
                    lanes = 4;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 10500)
                {
                    lanes = 5;
                }
                else
                {
                    lanes = 6;
                }
            }
            else if (speed.getInUnit(SpeedUnit.KM_PER_HOUR) >= 40)
            {
                if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 2000)
                {
                    lanes = 1;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 3000)
                {
                    lanes = 2;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 4000)
                {
                    lanes = 3;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 5000)
                {
                    lanes = 4;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 6000)
                {
                    lanes = 5;
                }
                else
                {
                    lanes = 6;
                }
            }
            else
            {
                if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 1800)
                {
                    lanes = 1;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 3200)
                {
                    lanes = 2;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 4400)
                {
                    lanes = 3;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 5400)
                {
                    lanes = 4;
                }
                else if (capacity.getInUnit(FrequencyUnit.PER_HOUR) < 6400)
                {
                    lanes = 5;
                }
                else
                {
                    lanes = 6;
                }
            }
        }
        return lanes;
    }

    /**
     * @param link
     */
    public Link(final Link link)
    {
        super(link.getId(), link.getStartNode(), link.getEndNode(), link.getLength(), link.getCapacity());
        setGeometry(link.getGeometry());
        this.freeSpeed = link.freeSpeed;
        this.numberOfLanes = link.getNumberOfLanes();
        this.behaviourType = link.behaviourType;
        if (this.getGeometry() != null)
        {
            Coordinate[] cc = this.getGeometry().getLineString().getCoordinates();
            if (cc.length == 0)
                System.out.println("cc.length = 0 for " + this.getId() + " (" + this.getId() + ")");
            else
            {
                if (Math.abs(cc[0].x - this.getStartNode().getX()) > 0.001
                        && Math.abs(cc[0].x - this.getEndNode().getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - this.getStartNode().getX()) > 0.001
                        && Math.abs(cc[cc.length - 1].x - this.getEndNode().getX()) > 0.001)
                    System.out.println("x coordinate non-match for " + this.getId() + " (" + this.getId()
                            + "); cc[0].x=" + cc[0].x + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x="
                            + this.getStartNode().getX() + ", nodeB.x=" + this.getEndNode().getX());
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
            DoubleScalar.Abs<SpeedUnit> speed, DoubleScalar.Rel<TimeUnit> time,
            TrafficBehaviourType trafficBehaviourType)

    {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate coordStart = new Coordinate(startNode.getX(), startNode.getY());
        Coordinate coordEnd = new Coordinate(endNode.getX(), endNode.getY());
        Coordinate[] coords = new Coordinate[]{coordStart, coordEnd};
        LineString line = geometryFactory.createLineString(coords);
        DoubleScalar.Rel<LengthUnit> length =
                new DoubleScalar.Rel<LengthUnit>(startNode.getPoint().distance(endNode.getPoint()), LengthUnit.METER);
        String nr = startNode.getId() + " - " + endNode.getId();
        Link newLink =
                new Link(null, nr, length, startNode, endNode, speed, null, capacity, trafficBehaviourType, null);
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
        HashMap<NodeGeotools, ArrayList<Link>> linksStartAtNode = new HashMap<NodeGeotools, ArrayList<Link>>();
        HashMap<NodeGeotools, ArrayList<Link>> linksEndAtNode = new HashMap<NodeGeotools, ArrayList<Link>>();
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
                            if (link.getFreeSpeed().equals(down.getFreeSpeed())
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
                new Link(null, nr, length, (Node) up.getStartNode(), (Node) down.getEndNode(), up.getFreeSpeed(),
                        up.getTime(), up.getCapacity(), up.getBehaviourType(), up.getLinkData());
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
    public DoubleScalar.Abs<SpeedUnit> getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @return corridorCapacity.
     */
    public Abs<FrequencyUnit> getCorridorCapacity()
    {
        return this.corridorCapacity;
    }

    /**
     * @param corridorCapacity set corridorCapacity.
     */
    public void setCorridorCapacity(Abs<FrequencyUnit> corridorCapacity)
    {
        this.corridorCapacity = corridorCapacity;
    }

    /**
     * @param roadCapacity
     * @param linkData set linkData.
     */
    public void addCorridorCapacity(Abs<FrequencyUnit> roadCapacity)
    {
        double cap = roadCapacity.getInUnit(FrequencyUnit.PER_HOUR);
        Rel<FrequencyUnit> addCap = new Rel<FrequencyUnit>(cap, FrequencyUnit.PER_HOUR);
        this.corridorCapacity = DoubleScalar.plus(this.getCorridorCapacity(), addCap).immutable();
    }

    /**
     * @return time.
     */
    public Rel<TimeUnit> getTime()
    {
        return this.time;
    }

    /**
     * @param time set time.
     */
    public void setTime(Rel<TimeUnit> time)
    {
        this.time = time;
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
        if (null == behaviourType)
        {
            System.out.println("OOOPS");
        }
        this.behaviourType = behaviourType;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ShpLink [nr=" + this.getId() + ", nodeA=" + this.getStartNode() + ", nodeB=" + this.getEndNode() + "]";
    }

    /**
     * @return numberOfLanes.
     */
    public int getNumberOfLanes()
    {
        return numberOfLanes;
    }

    /**
     * @param numberOfLanes set numberOfLanes.
     */
    public void setNumberOfLanes(int numberOfLanes)
    {
        this.numberOfLanes = numberOfLanes;
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
