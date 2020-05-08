package org.opentrafficsim.demo.ntm;

import java.awt.geom.Path2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.CapacityOTSLink;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;

/**
 * A link contains the following information:
 * 
 * <pre>
 * the_geom class com.vividsolutions.jts.geom.MultiLineString MULTILINESTRING ((232250.38755446894 ...
 * and a lot of data attributes such as speed and length
 * </pre>
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class NTMLink extends CapacityOTSLink
{
    /** SPEEDAB class java.lang.Double 120.0. */
    private Speed freeSpeed;

    /** SPEEDAB class java.lang.Double 120.0. */
    private Duration time;

    /** The lines for the animation, relative to the centroid. */
    private Set<Path2D> lines = null;

    /** */
    private LinkData linkData;

    /** */
    private Frequency corridorCapacity;

    /** */
    private int numberOfLanes;

    /** Traffic behaviour. */
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
     * @throws NetworkException
     */

    public NTMLink(final Network network, final OTSSimulatorInterface simulator, final OTSLine3D geometry, final String nr,
            final Length length, final NTMNode startNode, final NTMNode endNode, Speed freeSpeed, Duration time,
            final Frequency capacity, final TrafficBehaviourType behaviourType, LinkData linkData) throws NetworkException
    {
        super(network, nr, startNode, endNode, network.getLinkType(LinkType.DEFAULTS.ROAD), geometry, capacity);
        if (null == behaviourType)
        {
            System.out.println("behaviourType is null!");
        }
        // OTSLink(final IDL id, final OTSNode<IDN> startNode, final OTSNode<IDN> endNode,
        // final Length length, final DoubleScalar.Frequency capacity)
        this.freeSpeed = freeSpeed;
        this.time = time;
        this.behaviourType = behaviourType;
        this.numberOfLanes = estimateLanes(capacity, freeSpeed);
        this.linkData = linkData;
        if (geometry != null)
        {
            Coordinate[] cc = geometry.getLineString().getCoordinates();
            if (cc.length == 0)
            {
                System.out.println("cc.length = 0 for " + nr + " (" + nr + ")");
            }
            else
            {
                if (Math.abs(cc[0].x - startNode.getPoint().x) > 0.001 && Math.abs(cc[0].x - endNode.getPoint().x) > 0.001
                        && Math.abs(cc[cc.length - 1].x - startNode.getPoint().x) > 0.001
                        && Math.abs(cc[cc.length - 1].x - endNode.getPoint().x) > 0.001)
                {
                    System.out.println("x coordinate non-match for " + nr + " (" + nr + "); cc[0].x=" + cc[0].x + ", cc[L].x="
                            + cc[cc.length - 1].x + ", nodeA.x=" + startNode.getPoint().x + ", nodeB.x="
                            + endNode.getPoint().x);
                }
            }
        }

    }

    /**
     * @param capacity Frequency;
     * @param speed2
     * @return
     */
    private int estimateLanes(Frequency capacity, Speed speed)
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
     * @param link NTMLink;
     * @throws NetworkException
     */
    public NTMLink(final NTMLink link) throws NetworkException
    {
        super(link.getNetwork(), link.getId(), link.getStartNode(), link.getEndNode(),
                link.getNetwork().getLinkType(LinkType.DEFAULTS.ROAD), link.getDesignLine(), link.getCapacity());
        this.freeSpeed = link.freeSpeed;
        this.numberOfLanes = link.getNumberOfLanes();
        this.behaviourType = link.behaviourType;
        if (this.getDesignLine() != null)
        {
            Coordinate[] cc = this.getDesignLine().getLineString().getCoordinates();
            if (cc.length == 0)
                System.out.println("cc.length = 0 for " + this.getId() + " (" + this.getId() + ")");
            else
            {
                if (Math.abs(cc[0].x - this.getStartNode().getPoint().x) > 0.001
                        && Math.abs(cc[0].x - this.getEndNode().getPoint().x) > 0.001
                        && Math.abs(cc[cc.length - 1].x - this.getStartNode().getPoint().x) > 0.001
                        && Math.abs(cc[cc.length - 1].x - this.getEndNode().getPoint().x) > 0.001)
                    System.out.println("x coordinate non-match for " + this.getId() + " (" + this.getId() + "); cc[0].x="
                            + cc[0].x + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x=" + this.getStartNode().getPoint().x
                            + ", nodeB.x=" + this.getEndNode().getPoint().x);
            }
        }
    }

    /**
     * @param startNode NTMNode;
     * @param endNode NTMNode;
     * @param capacity Frequency;
     * @param speed Speed;
     * @param trafficBehaviourType TrafficBehaviourType;
     * @return
     */
    public static NTMLink createLink(Network network, OTSSimulatorInterface simulator, NTMNode startNode, NTMNode endNode,
            Frequency capacity, Speed speed, Duration time, TrafficBehaviourType trafficBehaviourType)

    {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate coordStart = new Coordinate(startNode.getPoint().x, startNode.getPoint().y);
        Coordinate coordEnd = new Coordinate(endNode.getPoint().x, endNode.getPoint().y);
        Coordinate[] coords = new Coordinate[] {coordStart, coordEnd};
        LineString line = geometryFactory.createLineString(coords);
        OTSLine3D geometry = null;
        try
        {
            geometry = new OTSLine3D(line);
        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
        Length length =
                new Length(startNode.getPoint().getCoordinate().distance(endNode.getPoint().getCoordinate()), LengthUnit.METER);
        String nr = startNode.getId() + " - " + endNode.getId();
        NTMLink newLink;
        try
        {
            newLink = new NTMLink(network, simulator, geometry, nr, length, startNode, endNode, speed, null, capacity,
                    trafficBehaviourType, null);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            newLink = null;
        }
        return newLink;
    }

    /**
     * @param links Map&lt;String,NTMLink&gt;; LinkedHashMap
     */
    public static void findSequentialLinks(final Map<String, NTMLink> links, Map<String, NTMNode> nodes)
    {
        // compare all links
        LinkedHashMap<org.opentrafficsim.core.network.Node, ArrayList<NTMLink>> linksStartAtNode = new LinkedHashMap<>();
        LinkedHashMap<org.opentrafficsim.core.network.Node, ArrayList<NTMLink>> linksEndAtNode = new LinkedHashMap<>();
        // LinkedHashMap<Node, Link> endNodeToLinkMap = new LinkedHashMap<Node, Link>();
        // LinkedHashMap<Node, Integer> numberOfLinksFromStartNodeMap = new LinkedHashMap<Node, Integer>();
        // find out how many links start from the endNode of a link
        for (NTMLink link : links.values())
        {
            // we put the first link we find in the map
            // as we are only interested in a connection with one in- and one out, one is enough
            if (linksStartAtNode.get(link.getStartNode()) == null)
            {
                ArrayList<NTMLink> localLinks = new ArrayList<NTMLink>();
                localLinks.add(link);
                linksStartAtNode.put(link.getStartNode(), localLinks);
            }
            else
            {
                ArrayList<NTMLink> localLinks = linksStartAtNode.get(link.getStartNode());
                localLinks.add(link);
                linksStartAtNode.put(link.getStartNode(), localLinks);
            }

            if (link.getEndNode().getId().equals("62673"))
            {
                System.out.println("test: ");
            }

            if (linksEndAtNode.get(link.getEndNode()) == null)
            {
                ArrayList<NTMLink> localLinks = new ArrayList<NTMLink>();
                localLinks.add(link);
                linksEndAtNode.put(link.getEndNode(), localLinks);
            }
            else
            {
                ArrayList<NTMLink> localLinks = linksEndAtNode.get(link.getEndNode());
                localLinks.add(link);
                linksEndAtNode.put(link.getEndNode(), localLinks);
            }
            // meanwhile look if there is only one link that starts from this node.
            // if there are more links starting, we don't have to exclude this link

        }

        LinkedHashMap<NTMLink, ArrayList<NTMLink>> upLinks = new LinkedHashMap<NTMLink, ArrayList<NTMLink>>();
        LinkedHashMap<NTMLink, ArrayList<NTMLink>> downLinks = new LinkedHashMap<NTMLink, ArrayList<NTMLink>>();
        for (NTMLink link : links.values())
        {
            if (link.getEndNode().getId().equals("1090639793"))
            {
                System.out.println("test: ");
            }

            ArrayList<NTMLink> downStreamLinks = null;
            if (linksStartAtNode.get(link.getEndNode()) != null)
            {
                downStreamLinks = new ArrayList<NTMLink>(linksStartAtNode.get(link.getEndNode()));
            }

            ArrayList<NTMLink> upStreamLinks = null;
            if (linksEndAtNode.get(link.getStartNode()) != null)
            {
                upStreamLinks = new ArrayList<NTMLink>(linksEndAtNode.get(link.getStartNode()));

            }
            // remove the BA link (U-turn)
            if (downStreamLinks != null)
            {
                for (NTMLink down : downStreamLinks)
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

                for (NTMLink up : upStreamLinks)
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

            for (NTMLink link : links.values())
            {
                ArrayList<NTMLink> downStreamLinks = downLinks.get(link);
                // join this "link" with the "down" link, if they have no junction

                if (downStreamLinks != null)
                {
                    if (downStreamLinks.size() == 1)
                    {
                        NTMLink down = downStreamLinks.get(0);
                        if (upLinks.get(down).size() == 1)
                        {
                            if (link.getFreeSpeed().equals(down.getFreeSpeed()) && link.getCapacity().equals(down.getCapacity())
                                    && link.getBehaviourType().equals(down.getBehaviourType()))
                            {
                                noMoreFound = false;
                                NTMLink mergedLink = joinLink(link, down);
                                if (mergedLink != null)
                                {
                                    ArrayList<NTMLink> downLink = downLinks.get(down);
                                    upLinks.put(mergedLink, downLink);
                                    ArrayList<NTMLink> upLink = upLinks.get(link);
                                    upLinks.put(mergedLink, upLink);

                                    ArrayList<NTMLink> downDownStreamLinks = downLinks.get(down);
                                    if (downDownStreamLinks != null)
                                    {
                                        for (NTMLink downDown : downDownStreamLinks)
                                        {
                                            if (upLinks.get(downDown) != null)
                                            {
                                                for (NTMLink up : upLinks.get(downDown))
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

                                    ArrayList<NTMLink> upStreamLinks = upLinks.get(link);
                                    if (upStreamLinks != null)
                                    {
                                        for (NTMLink up : upStreamLinks)
                                        {
                                            if (downLinks.get(up) != null)
                                            {
                                                for (NTMLink down1 : downLinks.get(up))
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
     * (IdToLinkMap.get(idList[size-1] + "_BA") != null) { outLinks--; } else { if (idList[size-1].contains("_BA")) { String Id
     * = idList[size-1].replace("_BA", ""); if (IdToLinkMap.get(Id) != null) { outLinks--; } } }
     */

    /**
     * @param up NTMLink;
     * @param down NTMLink;
     * @return
     */
    public static NTMLink joinLink(NTMLink up, NTMLink down)
    {
        LinkData dataUp = up.getLinkData();
        LinkData dataDown = down.getLinkData();
        NTMLink mergedLink = null;
        LineMerger lineMerger = new LineMerger();
        Collection<Geometry> lineStrings = new ArrayList<Geometry>();
        lineStrings.add(down.getDesignLine().getLineString());
        lineStrings.add(up.getDesignLine().getLineString());
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

        Length length = new Length(up.getLength().getSI() + down.getLength().getSI(), LengthUnit.METER);
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate[] coords = mergedGeometry.getCoordinates();
        LineString line = geometryFactory.createLineString(coords);
        OTSLine3D geometry = null;
        try
        {
            geometry = new OTSLine3D(line);
        }
        catch (OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
        try
        {
            mergedLink = new NTMLink(up.getNetwork(), up.getSimulator(), geometry, nr, length, (NTMNode) up.getStartNode(),
                    (NTMNode) down.getEndNode(), up.getFreeSpeed(), up.getDuration(), up.getCapacity(), up.getBehaviourType(),
                    up.getLinkData());
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            mergedLink = null;
        }
        return mergedLink;
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
            double dx = this.getLocation().x;
            double dy = this.getLocation().y;
            // double dx = 0;
            // double dy = 0;
            this.lines = new LinkedHashSet<Path2D>();
            for (int i = 0; i < this.getDesignLine().getLineString().getNumGeometries(); i++)
            {
                Path2D line = new Path2D.Double();
                Geometry g = this.getDesignLine().getLineString().getGeometryN(i);
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
    public Speed getFreeSpeed()
    {
        return this.freeSpeed;
    }

    /**
     * @return corridorCapacity.
     */
    public Frequency getCorridorCapacity()
    {
        return this.corridorCapacity;
    }

    /**
     * @param corridorCapacity Frequency; set corridorCapacity.
     */
    public void setCorridorCapacity(Frequency corridorCapacity)
    {
        this.corridorCapacity = corridorCapacity;
    }

    /**
     * @param roadCapacity Frequency;
     * @param linkData set linkData.
     */
    public void addCorridorCapacity(Frequency roadCapacity)
    {
        double cap = roadCapacity.getInUnit(FrequencyUnit.PER_HOUR);
        Frequency addCap = new Frequency(cap, FrequencyUnit.PER_HOUR);
        this.corridorCapacity = this.getCorridorCapacity().plus(addCap);
    }

    /**
     * @return time.
     */
    public Duration getDuration()
    {
        return this.time;
    }

    /**
     * @param time Duration; set time.
     */
    public void setDuration(Duration time)
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
     * @param behaviourType TrafficBehaviourType; set behaviourType.
     */
    public void setBehaviourType(TrafficBehaviourType behaviourType)
    {
        if (null == behaviourType)
        {
            System.out.println("OOOPS");
        }
        this.behaviourType = behaviourType;
    }

    // TODO: uncomment after update of OTS
    // /** {@inheritDoc} */
    // @Override
    // public String toString()
    // {
    // return "ShpLink [nr=" + this.getId() + ", nodeA=" + this.getStartNode() + ", nodeB=" + this.getEndNode() + "]";
    // }

    /**
     * @return numberOfLanes.
     */
    public int getNumberOfLanes()
    {
        return this.numberOfLanes;
    }

    /**
     * @param numberOfLanes int; set numberOfLanes.
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
        return this.linkData;
    }

    /**
     * @param linkData LinkData; set linkData.
     */
    public void setLinkData(LinkData linkData)
    {
        this.linkData = linkData;
    }

}
