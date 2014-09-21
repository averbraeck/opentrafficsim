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
 * The area contains the following information:
 * 
 * <pre>
 * the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232, ...
 * AREANR class java.lang.Long 15127
 * NAME class java.lang.String 70 Oostduinen
 * CENTROIDNR class java.lang.Long 1
 * NAMENR class java.lang.Long 175
 * GEMEENTE_N class java.lang.String S Gravenhage
 * GEMEENTEVM class java.lang.String sGravenhage
 * GEBIEDSNAA class java.lang.String Studiegebied
 * REGIO class java.lang.String Den_Haag
 * MATCOMPRES class java.lang.String Scheveningen
 * DHB class java.lang.Double 70.0
 * PARKEERTAR class java.lang.String 
 * AREATAG class java.lang.String
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
public class Area implements LocatableInterface
{
    /** the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232 ... */
    private final Geometry geometry;

    /** AREANR class java.lang.Long 15127 */
    private final long nr;

    /** NAME class java.lang.String 70 Oostduinen */
    private final String name;

    /** GEMEENTEVM class java.lang.String sGravenhage */
    private final String gemeente;

    /** GEBIEDSNAA class java.lang.String Studiegebied */
    private final String gebied;

    /** REGIO class java.lang.String Den_Haag */
    private final String regio;

    /** DHB class java.lang.Double 70.0 */
    private final double dhb;

    /** Centroid as a Point */
    private final Point centroid;

    /** touching areas */
    private final Set<Area> touchingAreas = new HashSet<>();

    /** polygon for drawing relative to centroid */
    private Set<Path2D> polygons = null;

    /**
     * @param geometry the_geom class com.vividsolutions.jts.geom.MultiPolygon MULTIPOLYGON (((81816.4228569232 ...
     * @param nr AREANR class java.lang.Long 15127
     * @param name NAME class java.lang.String 70 Oostduinen
     * @param gemeente GEMEENTEVM class java.lang.String sGravenhage
     * @param gebied GEBIEDSNAA class java.lang.String Studiegebied
     * @param regio REGIO class java.lang.String Den_Haag
     * @param dhb DHB class java.lang.Double 70.0
     * @param centroid Centroid as a Point
     */
    public Area(final Geometry geometry, final long nr, final String name, final String gemeente, final String gebied,
            final String regio, final double dhb, final Point centroid)
    {
        super();
        this.geometry = geometry;
        this.nr = nr;
        this.name = name;
        this.gemeente = gemeente;
        this.gebied = gebied;
        this.regio = regio;
        this.dhb = dhb;
        this.centroid = centroid;
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
        DirectedPoint d = getLocation();
        Envelope envelope = this.geometry.getEnvelopeInternal();
        return new BoundingBox(new Point3d(envelope.getMinX() - d.x, d.y - envelope.getMinY(), 0.0d), new Point3d(
                envelope.getMaxX() - d.x, d.y - envelope.getMaxY(), 0.0d));
    }

    /**
     * @return polygon
     * @throws RemoteException
     */
    public Set<Path2D> getPolygons() throws RemoteException
    {
        // create the polygon if it did not exist before
        if (this.polygons == null)
        {
            double dx = this.getLocation().getX();
            double dy = this.getLocation().getY();
            this.polygons = new HashSet<Path2D>();
            for (int i = 0; i < this.geometry.getNumGeometries(); i++)
            {
                Path2D polygon = new Path2D.Double();
                Geometry g = this.geometry.getGeometryN(i);
                boolean start = true;
                for (Coordinate c : g.getCoordinates())
                {
                    if (start)
                    {
                        polygon.moveTo(c.x - dx, dy - c.y);
                        start = false;
                    }
                    else
                    {
                        polygon.lineTo(c.x - dx, dy - c.y);
                    }
                }
                polygon.closePath();
                this.polygons.add(polygon);
            }
        }
        return this.polygons;
    }

    /**
     * @return centroid
     */
    public Point getCentroid()
    {
        return this.centroid;
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
     * @return gemeente
     */
    public String getGemeente()
    {
        return this.gemeente;
    }

    /**
     * @return gebied
     */
    public String getGebied()
    {
        return this.gebied;
    }

    /**
     * @return regio
     */
    public String getRegio()
    {
        return this.regio;
    }

    /**
     * @return dhb
     */
    public double getDhb()
    {
        return this.dhb;
    }

    /**
     * @return geometry
     */
    public Geometry getGeometry()
    {
        return this.geometry;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Area [nr=" + this.nr + "]";
    }

    /**
     * @return touchingAreas
     */
    public Set<Area> getTouchingAreas()
    {
        return this.touchingAreas;
    }

}
