package org.opentrafficsim.demo.ntm;

import java.awt.geom.Path2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opentrafficsim.core.network.AbstractLink;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
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

/**
 * A link contains the following information:
 * 
 * <pre>
 * the_geom class com.vividsolutions.jts.geom.MultiLineString MULTILINESTRING ((232250.38755446894 ...
 * LINKNR class java.lang.Long 1
 * NAME class java.lang.String 
 * DIRECTION class java.lang.Long 1
 * LENGTH class java.lang.Double 1.80327678
 * ANODE class java.lang.Long 684088
 * BNODE class java.lang.Long 1090577263
 * LINKTAG class java.lang.String 967536
 * WEGTYPEAB class java.lang.String mvt
 * TYPEWEGVAB class java.lang.String asw 2x2 (8600)
 * NOMO_1E_AB class java.lang.String TypeItem78
 * TYPEWEG_AB class java.lang.String 12 Autosnelweg 2x2
 * BRONMODEAB class java.lang.String Rotterdam
 * BRONMODEBA class java.lang.String Rotterdam
 * SPEEDAB class java.lang.Double 120.0
 * CAPACITYAB class java.lang.Double 8600.0
 * FREESPEEAB class java.lang.Double 0.0
 * SATFLOWAB class java.lang.Double 0.0
 * SPEEDATCAB class java.lang.Double 0.0
 * SPEEDAB_2 class java.lang.Double 120.0
 * CAPACIAB_2 class java.lang.Double 8600.0
 * FREESPAB_2 class java.lang.Double 0.0
 * SATFLOAB_2 class java.lang.Double 0.0
 * SPEEDAAB_2 class java.lang.Double 0.0
 * SPEEDAB_3 class java.lang.Double 120.0
 * CAPACIAB_3 class java.lang.Double 8600.0
 * FREESPAB_3 class java.lang.Double 0.0
 * SATFLOAB_3 class java.lang.Double 0.0
 * SPEEDAAB_3 class java.lang.Double 0.0
 * SPEEDAB_4 class java.lang.Double 90.0
 * CAPACIAB_4 class java.lang.Double 8600.0
 * FREESPAB_4 class java.lang.Double 0.0
 * SATFLOAB_4 class java.lang.Double 0.0
 * SPEEDAAB_4 class java.lang.Double 0.0
 * SPEEDAB_5 class java.lang.Double 90.0
 * CAPACIAB_5 class java.lang.Double 8600.0
 * FREESPAB_5 class java.lang.Double 0.0
 * SATFLOAB_5 class java.lang.Double 0.0
 * SPEEDAAB_5 class java.lang.Double 0.0
 * SPEEDAB_6 class java.lang.Double 90.0
 * CAPACIAB_6 class java.lang.Double 8600.0
 * FREESPAB_6 class java.lang.Double 0.0
 * SATFLOAB_6 class java.lang.Double 0.0
 * SPEEDAAB_6 class java.lang.Double 0.0
 * LOADAB class java.lang.Double 0.07822362
 * COSTAB class java.lang.Double 0.28906184
 * CALCSPEEAB class java.lang.Double 119.03069305
 * LOADAB_2 class java.lang.Double 4040.8034668
 * COSTAB_2 class java.lang.Double 0.29288939
 * CALCSPAB_2 class java.lang.Double 116.03796387
 * LANESAB class java.lang.Long 2
 * SPEED_WEAB class java.lang.Long 120
 * SPEED_MOAB class java.lang.Long 120
 * WEGTYPEBA class java.lang.String 
 * TYPEWEGVBA class java.lang.String 
 * TYPEWEG_BA class java.lang.String 
 * SPEEDBA class java.lang.Double 0.0
 * CAPACITYBA class java.lang.Double 0.0
 * FREESPEEBA class java.lang.Double 0.0
 * SATFLOWBA class java.lang.Double 0.0
 * SPEEDATCBA class java.lang.Double 0.0
 * SPEEDBA_2 class java.lang.Double 0.0
 * CAPACIBA_2 class java.lang.Double 0.0
 * FREESPBA_2 class java.lang.Double 0.0
 * SATFLOBA_2 class java.lang.Double 0.0
 * SPEEDABA_2 class java.lang.Double 0.0
 * SPEEDBA_3 class java.lang.Double 0.0
 * CAPACIBA_3 class java.lang.Double 0.0
 * FREESPBA_3 class java.lang.Double 0.0
 * SATFLOBA_3 class java.lang.Double 0.0
 * SPEEDABA_3 class java.lang.Double 0.0
 * SPEEDBA_4 class java.lang.Double 0.0
 * CAPACIBA_4 class java.lang.Double 0.0
 * FREESPBA_4 class java.lang.Double 0.0
 * SATFLOBA_4 class java.lang.Double 0.0
 * SPEEDABA_4 class java.lang.Double 0.0
 * SPEEDBA_5 class java.lang.Double 0.0
 * CAPACIBA_5 class java.lang.Double 0.0
 * FREESPBA_5 class java.lang.Double 0.0
 * SATFLOBA_5 class java.lang.Double 0.0
 * SPEEDABA_5 class java.lang.Double 0.0
 * SPEEDBA_6 class java.lang.Double 0.0
 * CAPACIBA_6 class java.lang.Double 0.0
 * FREESPBA_6 class java.lang.Double 0.0
 * SATFLOBA_6 class java.lang.Double 0.0
 * SPEEDABA_6 class java.lang.Double 0.0
 * LOADBA class java.lang.Double 0.0
 * COSTBA class java.lang.Double 0.0
 * CALCSPEEBA class java.lang.Double 0.0
 * LOADBA_2 class java.lang.Double 0.0
 * COSTBA_2 class java.lang.Double 0.0
 * CALCSPBA_2 class java.lang.Double 0.0
 * LANESBA class java.lang.Long 0
 * SPEED_WEBA class java.lang.Long 0
 * SPEED_MOBA class java.lang.Long 0
 * PROVINCIAB class java.lang.String 
 * PROVINCIBA class java.lang.String 
 * GEMEENTEAB class java.lang.String 
 * GEMEENTEBA class java.lang.String 
 * NAMENR class java.lang.Long 0
 * TYPESHAAAB class java.lang.String 
 * TYPESHAABA class java.lang.String 
 * NETWERKAAB class java.lang.String 
 * NETWERKABA class java.lang.String 
 * LANESMASBA class java.lang.String 
 * WIDTHCRBA class java.lang.Double 0.0
 * EXITLANEBA class java.lang.Long 0
 * SLOWTRAFBA class java.lang.Long 0
 * SIGNBA class java.lang.Long 0
 * ENABLEDBA class java.lang.Long 0
 * INCDMASKBA class java.lang.Long 0
 * MILIEUCOAB class java.lang.String 
 * MILIEUCOBA class java.lang.String 
 * LANESMASAB class java.lang.String 
 * WIDTHCRAB class java.lang.Double 0.0
 * EXITLANEAB class java.lang.Long 0
 * SLOWTRAFAB class java.lang.Long 0
 * SIGNAB class java.lang.Long 0
 * ENABLEDAB class java.lang.Long 0
 * INCDMASKAB class java.lang.Long 0
 * X0KMWEGEAB class java.lang.String 
 * X0KMWEGEBA class java.lang.String 
 * FUNCCLASAB class java.lang.String 
 * FUNCCLASBA class java.lang.String 
 * AR_TRUCKAB class java.lang.String 
 * AR_TRUCKBA class java.lang.String 
 * AR_PEDESAB class java.lang.String 
 * AR_PEDESBA class java.lang.String 
 * AR_MOTORAB class java.lang.String 
 * AR_MOTORBA class java.lang.String 
 * AR_BUSAB class java.lang.String 
 * AR_BUSBA class java.lang.String 
 * AR_AUTOAB class java.lang.String 
 * AR_AUTOBA class java.lang.String 
 * GEMEENAB_2 class java.lang.String 
 * GEMEENBA_2 class java.lang.String 
 * FIETSPADBA class java.lang.String 
 * LINKSMETBA class java.lang.String 
 * NOMO_2E_AB class java.lang.String 
 * WEEFVAKKAB class java.lang.String 
 * LINKSMETAB class java.lang.String 
 * NOMO_1E_BA class java.lang.String 
 * NOMO_2E_BA class java.lang.String 
 * APPROACHAB class java.lang.Long 0
 * APPROACHBA class java.lang.Long 0
 * KRUISPUNAB class java.lang.String 
 * KRUISPUNBA class java.lang.String 
 * STREETNAME class java.lang.String 
 * PROMILAB class java.lang.String 
 * PROMILBA class java.lang.String 
 * GEMEENAB_3 class java.lang.String 
 * GEMEENBA_3 class java.lang.String 
 * VRACHTWEAB class java.lang.String 
 * VRACHTWEBA class java.lang.String 
 * FIETSPADAB class java.lang.String 
 * ROADNUMBER class java.lang.String 
 * ONTWIKKELI class java.lang.String 
 * WEEFVAKKBA class java.lang.String 
 * PROGNOSEAB class java.lang.String 
 * PROGNOSEBA class java.lang.String 
 * OVSYSTEEAB class java.lang.String 
 * OVSYSTEEBA class java.lang.String 
 * STATIONSAB class java.lang.String 
 * STATIONSBA class java.lang.String 
 * TRAMSYSTAB class java.lang.String 
 * TRAMSYSTBA class java.lang.String 
 * TELLINGJAB class java.lang.String 
 * TELLINGJBA class java.lang.String
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
public class Link extends AbstractLink<String, Node> implements LocatableInterface
{
    /** SPEEDAB class java.lang.Double 120.0 */
    private DoubleScalar<SpeedUnit> speed;

    /** the lines for the animation, relative to the centroid */
    private Set<Path2D> lines = null;

    private LinkData linkData;

    /** traffic behaviour */
    private TrafficBehaviourType behaviourType;

    private ArrayList<FlowCell> flowCells;

    /** */
    private Geometry geometry;

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
    public Link(final Geometry geometry, final String nr, final DoubleScalar<LengthUnit> length, final Node startNode,
            final Node endNode, DoubleScalar<SpeedUnit> speed, final DoubleScalar<FrequencyUnit> capacity,
            final TrafficBehaviourType behaviourType, LinkData linkData)
    {
        super(nr, startNode, endNode, length, capacity);
        this.geometry = geometry;
        this.speed = speed;
        this.behaviourType = behaviourType;
        this.linkData = linkData;
        Coordinate[] cc = geometry.getCoordinates();
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

    /**
     * @param link
     */
    public Link(Link link)
    {
        super(link.getId(), link.getStartNode(), link.getEndNode(), link.getLength(), link.getCapacity());
        this.geometry = link.getGeometry();
        this.speed = link.speed;

        Coordinate[] cc = this.getGeometry().getCoordinates();
        if (cc.length == 0)
            System.out.println("cc.length = 0 for " + this.getId() + " (" + this.getId() + ")");
        else
        {
            if (Math.abs(cc[0].x - this.getStartNode().getPoint().getX()) > 0.001
                    && Math.abs(cc[0].x - this.getEndNode().getPoint().getX()) > 0.001
                    && Math.abs(cc[cc.length - 1].x - this.getStartNode().getPoint().getX()) > 0.001
                    && Math.abs(cc[cc.length - 1].x - this.getEndNode().getPoint().getX()) > 0.001)
                System.out.println("x coordinate non-match for " + this.getId() + " (" + this.getId() + "); cc[0].x="
                        + cc[0].x + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x="
                        + this.getStartNode().getPoint().getX() + ", nodeB.x=" + this.getEndNode().getPoint().getX());
        }
    }

    /**
     * @param startNode
     * @param endNode
     * @param capacity
     * @param speed
     * @return
     */
    public static Link createLink(Node startNode, Node endNode, DoubleScalar<FrequencyUnit> capacity,
            DoubleScalar<SpeedUnit> speed)
    {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate coordStart = new Coordinate(startNode.getPoint().getX(), startNode.getPoint().getY());
        Coordinate coordEnd = new Coordinate(endNode.getPoint().getX(), endNode.getPoint().getY());
        Coordinate[] coords = new Coordinate[]{coordStart, coordEnd};
        LineString line = geometryFactory.createLineString(coords);
        Geometry geometry = line.getGeometryN(0);
        DoubleScalar<LengthUnit> length =
                new DoubleScalar.Abs<LengthUnit>(startNode.getPoint().distance(endNode.getPoint()), LengthUnit.METER);

        String nr = startNode.getId() + " - " + endNode.getId();
        Link newLink =
                new Link(geometry, nr, length, startNode, endNode, speed, capacity, TrafficBehaviourType.NTM, null);
        return newLink;
    }

    /**
     * @param links
     */
    private static void joinEqualLinks(Map<String, Link> links)
    {
        ;
    }
    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        Point c = this.getGeometry().getCentroid();
        return new DirectedPoint(new double[]{c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        DirectedPoint c = getLocation();
        Envelope envelope = this.getGeometry().getEnvelopeInternal();
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
        if (this.behaviourType == TrafficBehaviourType.NTM)
        {
            System.out.println("stop");
        }
        if (this.lines == null)
        {
            double dx = this.getLocation().getX();
            double dy = this.getLocation().getY();
            // double dx = 0;
            // double dy = 0;
            this.lines = new HashSet<Path2D>();
            for (int i = 0; i < this.getGeometry().getNumGeometries(); i++)
            {
                Path2D line = new Path2D.Double();
                Geometry g = this.getGeometry().getGeometryN(i);
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
    public DoubleScalar<SpeedUnit> getSpeed()
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
     * @return geometry.
     */
    public Geometry getGeometry()
    {
        return geometry;
    }

    /**
     * @param geometry set geometry.
     */
    public void setGeometry(Geometry geometry)
    {
        this.geometry = geometry;
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
