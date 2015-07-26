package org.opentrafficsim.core.geometry;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * An OTSPoint3D implements a 3D-coordinate for OTS. X, y and z are stored as doubles, but it is assumed that the scale is in SI
 * units, i.e. in meters. A distance between two points is therefore also in meters.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-16 10:20:53 +0200 (Thu, 16 Jul 2015) $, @version $Revision: 1124 $, by $Author: pknoppers $,
 * initial version Jul 22, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSPoint3D implements LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the internal representation of the point; x-coordinate. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public final double x;

    /** the internal representation of the point; y-coordinate. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public final double y;

    /** the internal representation of the point; z-coordinate. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public final double z;

    /**
     * The x, y and z in the point are assumed to be in meters relative to an origin.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public OTSPoint3D(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @param xyz array with three elements; x, y and z are assumed to be in meters relative to an origin.
     */
    public OTSPoint3D(final double[] xyz)
    {
        this(xyz[0], xyz[1], (xyz.length > 2) ? xyz[2] : 0.0);
    }

    /**
     * @param point a point to "clone".
     */
    public OTSPoint3D(final OTSPoint3D point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point javax.vecmath 3D double point; the x, y and z in the point are assumed to be in meters relative to an
     *            origin.
     */
    public OTSPoint3D(final Point3d point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point javax.vecmath 3D double point; the x, y and z in the point are assumed to be in meters relative to an
     *            origin.
     */
    public OTSPoint3D(final CartesianPoint point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point javax.vecmath 3D double point; the x, y and z in the point are assumed to be in meters relative to an
     *            origin.
     */
    public OTSPoint3D(final DirectedPoint point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point2d java.awt 2D point, z-coordinate will be zero; the x and y in the point are assumed to be in meters
     *            relative to an origin.
     */
    public OTSPoint3D(final Point2D point2d)
    {
        this(point2d.getX(), point2d.getY(), 0.0);
    }

    /**
     * @param coordinate geotools coordinate; the x, y and z in the coordinate are assumed to be in meters relative to an
     *            origin.
     */
    public OTSPoint3D(final Coordinate coordinate)
    {
        this(coordinate.x, coordinate.y, (Double.isNaN(coordinate.z)) ? 0.0 : coordinate.z);
    }

    /**
     * @param point geotools point; z-coordinate will be zero; the x and y in the point are assumed to be in meters relative to
     *            an origin.
     */
    public OTSPoint3D(final Point point)
    {
        this(point.getX(), point.getY(), 0.0);
    }

    /**
     * The x and y in the point are assumed to be in meters relative to an origin. z will be set to 0.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public OTSPoint3D(final double x, final double y)
    {
        this(x, y, 0.0);
    }

    /**
     * @param point the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras, expressed in SI units
     */
    public final double distanceSI(final OTSPoint3D point)
    {
        double dx = point.x - this.x;
        double dy = point.y - this.y;
        double dz = point.z - this.z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * @param point the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras
     */
    public final DoubleScalar.Rel<LengthUnit> distance(final OTSPoint3D point)
    {
        return new DoubleScalar.Rel<LengthUnit>(distanceSI(point), LengthUnit.SI);
    }

    /**
     * @return the equivalent geotools Coordinate of this point.
     */
    public final Coordinate getCoordinate()
    {
        return new Coordinate(this.x, this.y, this.z);
    }

    /**
     * @return the equivalent DSOL DirectedPoint of this point.
     */
    public final DirectedPoint getDirectedPoint()
    {
        return new DirectedPoint(this.x, this.y, this.z);
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return getDirectedPoint();
    }

    /**
     * @return a sphere with a diameter of half a meter as the default bounds for a point.
     * @throws RemoteException in case the location cannot be retrieved
     */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 0.5);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("(%.2f,%.2f,%.2f)", this.x, this.y, this.z);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OTSPoint3D other = (OTSPoint3D) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }

}
