package org.opentrafficsim.core.geometry;

import java.awt.geom.Point2D;
import java.io.Serializable;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * An OTSPoint3D implements a 3D-coordinate for OTS. X, y and z are stored as doubles, but it is assumed that the scale is in SI
 * units, i.e. in meters. A distance between two points is therefore also in meters.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        this(coordinate.x, coordinate.y, Double.isNaN(coordinate.z) ? 0.0 : coordinate.z);
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
     * Interpolate (or extrapolate) between (outside) two given points.
     * @param ratio double; 0 selects the zeroValue point, 1 selects the oneValue point, 0.5 selects a point halfway, etc.
     * @param zeroValue OTSPoint3D; the point that is returned when ratio equals 0
     * @param oneValue OTSPoint3D; the point that is returned when ratio equals 1
     * @return OTSPoint3D
     */
    public static OTSPoint3D interpolate(final double ratio, final OTSPoint3D zeroValue, final OTSPoint3D oneValue)
    {
        double complement = 1 - ratio;
        return new OTSPoint3D(complement * zeroValue.x + ratio * oneValue.x, complement * zeroValue.y + ratio * oneValue.y,
                complement * zeroValue.z + ratio * oneValue.z);
    }

    /**
     * Compute the 2D intersection of two line segments. The Z-component of the lines is ignored. Both line segments are defined
     * by two points (that should be distinct).
     * @param line1P1 OTSPoint3D; first point of line segment 1
     * @param line1P2 OTSPoint3D; second point of line segment 1
     * @param line2P1 OTSPoint3D; first point of line segment 2
     * @param line2P2 OTSPoint3D; second point of line segment 2
     * @return OTSPoint3D; the intersection of the two lines, or null if the lines are (almost) parallel, or do not intersect
     */
    public static OTSPoint3D intersectionOfLineSegments(final OTSPoint3D line1P1, final OTSPoint3D line1P2,
            final OTSPoint3D line2P1, final OTSPoint3D line2P2)
    {
        double denominator =
                (line2P2.y - line2P1.y) * (line1P2.x - line1P1.x) - (line2P2.x - line2P1.x) * (line1P2.y - line1P1.y);
        if (denominator == 0f)
        {
            return null; // lines are parallel (they might even be on top of each other, but we don't check that)
        }
        double uA =
                ((line2P2.x - line2P1.x) * (line1P1.y - line2P1.y) - (line2P2.y - line2P1.y) * (line1P1.x - line2P1.x))
                        / denominator;
        if ((uA < 0f) || (uA > 1f))
        {
            return null; // intersection outside line 1
        }
        double uB =
                ((line1P2.x - line1P1.x) * (line1P1.y - line2P1.y) - (line1P2.y - line1P1.y) * (line1P1.x - line2P1.x))
                        / denominator;
        if (uB < 0 || uB > 1)
        {
            return null; // intersection outside line 2
        }
        return new OTSPoint3D(line1P1.x + uA * (line1P2.x - line1P1.x), line1P1.y + uA * (line1P2.y - line1P1.y), 0);
    }

    /**
     * Compute the 2D intersection of two infinite lines. The Z-component of the lines is ignored. Both lines are defined by two
     * points (that should be distinct).
     * @param line1P1 OTSPoint3D; first point of line 1
     * @param line1P2 OTSPoint3D; second point of line 1
     * @param line2P1 OTSPoint3D; first point of line 2
     * @param line2P2 OTSPoint3D; second point of line 2
     * @return OTSPoint3D; the intersection of the two lines, or null if the lines are (almost) parallel
     */
    public static OTSPoint3D intersectionOfLines(final OTSPoint3D line1P1, final OTSPoint3D line1P2, final OTSPoint3D line2P1,
            final OTSPoint3D line2P2)
    {
        double determinant =
                (line1P1.x - line1P2.x) * (line2P1.y - line2P2.y) - (line1P1.y - line1P2.y) * (line2P1.x - line2P2.x);
        if (Math.abs(determinant) < 0.0000001)
        {
            return null;
        }
        return new OTSPoint3D(
                ((line1P1.x * line1P2.y - line1P1.y * line1P2.x) * (line2P1.x - line2P2.x) - (line1P1.x - line1P2.x)
                        * (line2P1.x * line2P2.y - line2P1.y * line2P2.x))
                        / determinant,
                ((line1P1.x * line1P2.y - line1P1.y * line1P2.x) * (line2P1.y - line2P2.y) - (line1P1.y - line1P2.y)
                        * (line2P1.x * line2P2.y - line2P1.y * line2P2.x))
                        / determinant);
    }

    /**
     * Compute the distance to a line segment (2D; Z-component is ignored). If the projection of this point onto the line lies
     * outside the line segment; the distance to nearest end point of the line segment is returned. Otherwise the distance to
     * the line segment is returned. <br>
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java"> example code provided by Paul
     * Bourke</a>.
     * @param segmentPoint1 OTSPoint3D; start of line segment
     * @param segmentPoint2 OTSPoint3D; end of line segment
     * @return double; the distance of this point to (one of the end points of the line segment)
     */
    public double distanceToLineSegment(OTSPoint3D segmentPoint1, OTSPoint3D segmentPoint2)
    {
        return closestPointOnSegment(segmentPoint1, segmentPoint2).distanceSI(this);
    }

    /**
     * Project a point on a line segment (2D - Z-component is ignored). If the the projected points lies outside the line
     * segment, the nearest end point of the line segment is returned. Otherwise the returned point lies between the end points
     * of the line segment. <br>
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java"> example code provided by Paul
     * Bourke</a>.
     * @param segmentPoint1 OTSPoint3D; start of line segment
     * @param segmentPoint2 OTSPoint3D; end of line segment
     * @return Point2D.Double; either <cite>lineP1</cite>, or <cite>lineP2</cite> or a new OTSPoint3D that lies somewhere in
     *         between those two
     */
    public OTSPoint3D closestPointOnSegment(OTSPoint3D segmentPoint1, OTSPoint3D segmentPoint2)
    {
        double dX = segmentPoint2.x - segmentPoint1.x;
        double dY = segmentPoint2.y - segmentPoint1.y;
        if ((0 == dX) && (0 == dY))
            return segmentPoint1;
        final double u = ((this.x - segmentPoint1.x) * dX + (this.y - segmentPoint1.y) * dY) / (dX * dX + dY * dY);
        if (u < 0)
            return segmentPoint1;
        else if (u > 1)
            return segmentPoint2;
        else
            return new OTSPoint3D(segmentPoint1.x + u * dX, segmentPoint1.y + u * dY); // could use interpolate in stead
    }

    /**
     * @param point the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras, expressed in the SI unit for length (meter)
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
     * @return the distance in 3D according to Pythagoras, expressed in the SI unit for length (meter)
     */
    public final double horizontalDistanceSI(final OTSPoint3D point)
    {
        double dx = point.x - this.x;
        double dy = point.y - this.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * @param point the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras
     */
    public final Length.Rel horizontalDistance(final OTSPoint3D point)
    {
        return new Length.Rel(horizontalDistanceSI(point), LengthUnit.SI);
    }

    /**
     * @param point the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras
     */
    public final Length.Rel distance(final OTSPoint3D point)
    {
        return new Length.Rel(distanceSI(point), LengthUnit.SI);
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
    public final DirectedPoint getLocation()
    {
        return getDirectedPoint();
    }

    /**
     * This method returns a sphere with a diameter of half a meter as the default bounds for a point. {@inheritDoc}
     */
    @Override
    public final Bounds getBounds()
    {
        return new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 0.5);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("(%.3f,%.3f,%.3f)", this.x, this.y, this.z);
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
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
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
