package org.opentrafficsim.demo.ntm;

import java.awt.geom.Path2D;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
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
public class ShpLink implements LocatableInterface
{
    /** the_geom class com.vividsolutions.jts.geom.MultiLineString MULTILINESTRING ((232250.38755446894 ... */
    private final Geometry geometry;

    /** LINKNR class java.lang.Long 1 */
    private final long nr;

    /** NAME class java.lang.String */
    private final String name;

    /** DIRECTION class java.lang.Long 1 */
    private final short direction;

    /** LENGTH class java.lang.Double 1.80327678 */
    private final double length;

    /** ANODE class java.lang.Long 684088 */
    private final ShpNode nodeA;

    /** BNODE class java.lang.Long 1090577263 */
    private final ShpNode nodeB;

    /** LINKTAG class java.lang.String 967536 */
    private final String linkTag;

    /** WEGTYPEAB class java.lang.String mvt */
    private final String wegtype;

    /** TYPEWEGVAB class java.lang.String asw 2x2 (8600) */
    private final String typeWegVak;

    /** TYPEWEG_AB class java.lang.String 12 Autosnelweg 2x2 */
    private final String typeWeg;

    /** SPEEDAB class java.lang.Double 120.0 */
    private final double speed;

    /** CAPACITYAB class java.lang.Double 8600.0 */
    private final double capacity;

    /** the lines for the animation, relative to the centroid */
    private Set<Path2D> lines = null;

    /**
     * @param geometry
     * @param nr
     * @param name
     * @param direction
     * @param length
     * @param nodeA
     * @param nodeB
     * @param linkTag
     * @param wegtype
     * @param typeWegVak
     * @param typeWeg
     * @param speed
     * @param capacity
     */
    public ShpLink(Geometry geometry, long nr, String name, short direction, double length, ShpNode nodeA,
            ShpNode nodeB, String linkTag, String wegtype, String typeWegVak, String typeWeg, double speed,
            double capacity)
    {
        super();
        this.geometry = geometry;
        this.nr = nr;
        this.name = name;
        this.direction = direction;
        this.length = length;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.linkTag = linkTag;
        this.wegtype = wegtype;
        this.typeWegVak = typeWegVak;
        this.typeWeg = typeWeg;
        this.speed = speed;
        this.capacity = capacity;

        Coordinate[] cc = this.geometry.getCoordinates();
        if (cc.length == 0)
            System.out.println("cc.length = 0 for " + nr + " (" + name + ")");
        else
        {
            if (Math.abs(cc[0].x - nodeA.getX()) > 0.001 && Math.abs(cc[0].x - nodeB.getX()) > 0.001
                    && Math.abs(cc[cc.length - 1].x - nodeA.getX()) > 0.001
                    && Math.abs(cc[cc.length - 1].x - nodeB.getX()) > 0.001)
                System.out.println("x coordinate non-match for " + nr + " (" + name + "); cc[0].x=" + cc[0].x
                        + ", cc[L].x=" + cc[cc.length - 1].x + ", nodeA.x=" + nodeA.getX() + ", nodeB.x="
                        + nodeB.getX());
        }
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        Point c = this.geometry.getCentroid();
        return new DirectedPoint(new double[]{c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        DirectedPoint c = getLocation();
        Envelope envelope = this.geometry.getEnvelopeInternal();
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
            this.lines = new HashSet<Path2D>();
            for (int i = 0; i < this.geometry.getNumGeometries(); i++)
            {
                Path2D line = new Path2D.Double();
                Geometry g = this.geometry.getGeometryN(i);
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
     * @return geometry
     */
    public Geometry getGeometry()
    {
        return this.geometry;
    }

    /**
     * @return nr
     */
    public long getNr()
    {
        return this.nr;
    }

    /**
     * @return name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return direction
     */
    public short getDirection()
    {
        return this.direction;
    }

    /**
     * @return length
     */
    public double getLength()
    {
        return this.length;
    }

    /**
     * @return nodeA
     */
    public ShpNode getNodeA()
    {
        return this.nodeA;
    }

    /**
     * @return nodeB
     */
    public ShpNode getNodeB()
    {
        return this.nodeB;
    }

    /**
     * @return linkTag
     */
    public String getLinkTag()
    {
        return this.linkTag;
    }

    /**
     * @return wegtype
     */
    public String getWegtype()
    {
        return this.wegtype;
    }

    /**
     * @return typeWegVak
     */
    public String getTypeWegVak()
    {
        return this.typeWegVak;
    }

    /**
     * @return typeWeg
     */
    public String getTypeWeg()
    {
        return this.typeWeg;
    }

    /**
     * @return speed
     */
    public double getSpeed()
    {
        return this.speed;
    }

    /**
     * @return capacity
     */
    public double getCapacity()
    {
        return this.capacity;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ShpLink [nr=" + this.nr + ", name=" + this.name + ", nodeA=" + this.nodeA + ", nodeB=" + this.nodeB
                + "]";
    }

}
