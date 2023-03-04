package org.opentrafficsim.core.geometry;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.Unit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.base.DoubleScalarInterface;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djunits.value.vdouble.vector.base.DoubleVectorInterface;
import org.djutils.draw.point.Point3d;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * An OtsPoint3d implements a 3D-coordinate for OTS. X, y and z are stored as doubles, but it is assumed that the scale is in SI
 * units, i.e. in meters. A distance between two points is therefore also in meters.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OtsPoint3d implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The internal representation of the point; x-coordinate. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public final double x;

    /** The internal representation of the point; y-coordinate. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public final double y;

    /** The internal representation of the point; z-coordinate. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public final double z;

    /**
     * The x, y and z in the point are assumed to be in meters relative to an origin.
     * @param x double; x-coordinate
     * @param y double; y-coordinate
     * @param z double; z-coordinate
     */
    public OtsPoint3d(final double x, final double y, final double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @param xyz array with three elements; x, y and z are assumed to be in meters relative to an origin.
     */
    public OtsPoint3d(final double[] xyz)
    {
        this(xyz[0], xyz[1], (xyz.length > 2) ? xyz[2] : 0.0);
    }

    /**
     * @param point OtsPoint3d; a point to "clone".
     */
    public OtsPoint3d(final OtsPoint3d point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point javax.vecmath 3D double point; the x, y and z in the point are assumed to be in meters relative to an
     *            origin.
     */
    public OtsPoint3d(final Point3d point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point javax.vecmath 3D double point; the x, y and z in the point are assumed to be in meters relative to an
     *            origin.
     */
    public OtsPoint3d(final CartesianPoint point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point javax.vecmath 3D double point; the x, y and z in the point are assumed to be in meters relative to an
     *            origin.
     */
    public OtsPoint3d(final DirectedPoint point)
    {
        this(point.x, point.y, point.z);
    }

    /**
     * @param point2d java.awt 2D point, z-coordinate will be zero; the x and y in the point are assumed to be in meters
     *            relative to an origin.
     */
    public OtsPoint3d(final Point2D point2d)
    {
        this(point2d.getX(), point2d.getY(), 0.0);
    }

    /**
     * @param coordinate geotools coordinate; the x, y and z in the coordinate are assumed to be in meters relative to an
     *            origin.
     */
    public OtsPoint3d(final Coordinate coordinate)
    {
        this(coordinate.x, coordinate.y, Double.isNaN(coordinate.getZ()) ? 0.0 : coordinate.getZ());
    }

    /**
     * @param point geotools point; z-coordinate will be zero; the x and y in the point are assumed to be in meters relative to
     *            an origin.
     */
    public OtsPoint3d(final Point point)
    {
        this(point.getX(), point.getY(), 0.0);
    }

    /**
     * The x and y in the point are assumed to be in meters relative to an origin. z will be set to 0.
     * @param x double; x-coordinate
     * @param y double; y-coordinate
     */
    public OtsPoint3d(final double x, final double y)
    {
        this(x, y, 0.0);
    }

    /**
     * Interpolate (or extrapolate) between (outside) two given points.
     * @param ratio double; 0 selects the zeroValue point, 1 selects the oneValue point, 0.5 selects a point halfway, etc.
     * @param zeroValue OtsPoint3d; the point that is returned when ratio equals 0
     * @param oneValue OtsPoint3d; the point that is returned when ratio equals 1
     * @return OtsPoint3d
     */
    public static OtsPoint3d interpolate(final double ratio, final OtsPoint3d zeroValue, final OtsPoint3d oneValue)
    {
        double complement = 1 - ratio;
        return new OtsPoint3d(complement * zeroValue.x + ratio * oneValue.x, complement * zeroValue.y + ratio * oneValue.y,
                complement * zeroValue.z + ratio * oneValue.z);
    }

    /**
     * Compute the 2D intersection of two line segments. The Z-component of the lines is ignored. Both line segments are defined
     * by two points (that should be distinct). This version suffers loss of precision when called with very large coordinate
     * values.
     * @param line1P1 OtsPoint3d; first point of line segment 1
     * @param line1P2 OtsPoint3d; second point of line segment 1
     * @param line2P1 OtsPoint3d; first point of line segment 2
     * @param line2P2 OtsPoint3d; second point of line segment 2
     * @return OtsPoint3d; the intersection of the two lines, or null if the lines are (almost) parallel, or do not intersect
     */
    @Deprecated
    public static OtsPoint3d intersectionOfLineSegmentsDumb(final OtsPoint3d line1P1, final OtsPoint3d line1P2,
            final OtsPoint3d line2P1, final OtsPoint3d line2P2)
    {
        double denominator =
                (line2P2.y - line2P1.y) * (line1P2.x - line1P1.x) - (line2P2.x - line2P1.x) * (line1P2.y - line1P1.y);
        if (denominator == 0f)
        {
            return null; // lines are parallel (they might even be on top of each other, but we don't check that)
        }
        double uA = ((line2P2.x - line2P1.x) * (line1P1.y - line2P1.y) - (line2P2.y - line2P1.y) * (line1P1.x - line2P1.x))
                / denominator;
        if ((uA < 0f) || (uA > 1f))
        {
            return null; // intersection outside line 1
        }
        double uB = ((line1P2.x - line1P1.x) * (line1P1.y - line2P1.y) - (line1P2.y - line1P1.y) * (line1P1.x - line2P1.x))
                / denominator;
        if (uB < 0 || uB > 1)
        {
            return null; // intersection outside line 2
        }
        return new OtsPoint3d(line1P1.x + uA * (line1P2.x - line1P1.x), line1P1.y + uA * (line1P2.y - line1P1.y), 0);
    }

    /**
     * Compute the 2D intersection of two line segments. The Z-component of the lines is ignored. Both line segments are defined
     * by two points (that should be distinct).
     * @param line1P1 OtsPoint3d; first point of line segment 1
     * @param line1P2 OtsPoint3d; second point of line segment 1
     * @param line2P1 OtsPoint3d; first point of line segment 2
     * @param line2P2 OtsPoint3d; second point of line segment 2
     * @return OtsPoint3d; the intersection of the two lines, or null if the lines are (almost) parallel, or do not intersect
     */
    public static OtsPoint3d intersectionOfLineSegments(final OtsPoint3d line1P1, final OtsPoint3d line1P2,
            final OtsPoint3d line2P1, final OtsPoint3d line2P2)
    {
        double l1p1x = line1P1.x;
        double l1p1y = line1P1.y;
        double l1p2x = line1P2.x - l1p1x;
        double l1p2y = line1P2.y - l1p1y;
        double l2p1x = line2P1.x - l1p1x;
        double l2p1y = line2P1.y - l1p1y;
        double l2p2x = line2P2.x - l1p1x;
        double l2p2y = line2P2.y - l1p1y;
        double denominator = (l2p2y - l2p1y) * l1p2x - (l2p2x - l2p1x) * l1p2y;
        if (denominator == 0.0)
        {
            return null; // lines are parallel (they might even be on top of each other, but we don't check that)
        }
        double uA = ((l2p2x - l2p1x) * (-l2p1y) - (l2p2y - l2p1y) * (-l2p1x)) / denominator;
        // System.out.println("uA is " + uA);
        if ((uA < 0.0) || (uA > 1.0))
        {
            return null; // intersection outside line 1
        }
        double uB = (l1p2y * l2p1x - l1p2x * l2p1y) / denominator;
        // System.out.println("uB is " + uB);
        if (uB < 0.0 || uB > 1.0)
        {
            return null; // intersection outside line 2
        }
        return new OtsPoint3d(line1P1.x + uA * l1p2x, line1P1.y + uA * l1p2y, 0);
    }

    /**
     * Compute the 2D intersection of two infinite lines. The Z-component of the lines is ignored. Both lines are defined by two
     * points (that should be distinct). This version suffers loss of precision when called with very large coordinate values.
     * @param line1P1 OtsPoint3d; first point of line 1
     * @param line1P2 OtsPoint3d; second point of line 1
     * @param line2P1 OtsPoint3d; first point of line 2
     * @param line2P2 OtsPoint3d; second point of line 2
     * @return OtsPoint3d; the intersection of the two lines, or null if the lines are (almost) parallel
     */
    @Deprecated
    public static OtsPoint3d intersectionOfLinesDumb(final OtsPoint3d line1P1, final OtsPoint3d line1P2,
            final OtsPoint3d line2P1, final OtsPoint3d line2P2)
    {
        double determinant =
                (line1P1.x - line1P2.x) * (line2P1.y - line2P2.y) - (line1P1.y - line1P2.y) * (line2P1.x - line2P2.x);
        if (Math.abs(determinant) < 0.0000001)
        {
            return null;
        }
        return new OtsPoint3d(
                ((line1P1.x * line1P2.y - line1P1.y * line1P2.x) * (line2P1.x - line2P2.x)
                        - (line1P1.x - line1P2.x) * (line2P1.x * line2P2.y - line2P1.y * line2P2.x)) / determinant,
                ((line1P1.x * line1P2.y - line1P1.y * line1P2.x) * (line2P1.y - line2P2.y)
                        - (line1P1.y - line1P2.y) * (line2P1.x * line2P2.y - line2P1.y * line2P2.x)) / determinant);
    }

    /**
     * Compute the 2D intersection of two infinite lines. The Z-component of the lines is ignored. Both lines are defined by two
     * points (that should be distinct).
     * @param line1P1 OtsPoint3d; first point of line 1
     * @param line1P2 OtsPoint3d; second point of line 1
     * @param line2P1 OtsPoint3d; first point of line 2
     * @param line2P2 OtsPoint3d; second point of line 2
     * @return OtsPoint3d; the intersection of the two lines, or null if the lines are (almost) parallel
     */
    public static OtsPoint3d intersectionOfLines(final OtsPoint3d line1P1, final OtsPoint3d line1P2, final OtsPoint3d line2P1,
            final OtsPoint3d line2P2)
    {
        double l1p1x = line1P1.x;
        double l1p1y = line1P1.y;
        double l1p2x = line1P2.x - l1p1x;
        double l1p2y = line1P2.y - l1p1y;
        double l2p1x = line2P1.x - l1p1x;
        double l2p1y = line2P1.y - l1p1y;
        double l2p2x = line2P2.x - l1p1x;
        double l2p2y = line2P2.y - l1p1y;
        double determinant = (0 - l1p2x) * (l2p1y - l2p2y) - (0 - l1p2y) * (l2p1x - l2p2x);
        if (Math.abs(determinant) < 0.0000001)
        {
            return null;
        }
        return new OtsPoint3d(l1p1x + (l1p2x * (l2p1x * l2p2y - l2p1y * l2p2x)) / determinant,
                l1p1y + (l1p2y * (l2p1x * l2p2y - l2p1y * l2p2x)) / determinant);
    }

    /**
     * Project a point on a line segment (2D - Z-component is ignored). If the the projected points lies outside the line
     * segment, the nearest end point of the line segment is returned. Otherwise the returned point lies between the end points
     * of the line segment. <br>
     * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java">example code provided by Paul
     * Bourke</a>.
     * @param segmentPoint1 OtsPoint3d; start of line segment
     * @param segmentPoint2 OtsPoint3d; end of line segment
     * @return Point2D.Double; either <cite>lineP1</cite>, or <cite>lineP2</cite> or a new OtsPoint3d that lies somewhere in
     *         between those two. The Z-component of the result matches the Z-component of the line segment at that point
     */
    public final OtsPoint3d closestPointOnSegment(final OtsPoint3d segmentPoint1, final OtsPoint3d segmentPoint2)
    {
        double dX = segmentPoint2.x - segmentPoint1.x;
        double dY = segmentPoint2.y - segmentPoint1.y;
        if ((0 == dX) && (0 == dY))
        {
            return segmentPoint1;
        }
        final double u = ((this.x - segmentPoint1.x) * dX + (this.y - segmentPoint1.y) * dY) / (dX * dX + dY * dY);
        if (u < 0)
        {
            return segmentPoint1;
        }
        else if (u > 1)
        {
            return segmentPoint2;
        }
        else
        {
            return interpolate(u, segmentPoint1, segmentPoint2);
        }
    }

    /**
     * Return the closest point on an OtsLine3d.
     * @param line OtsLine3d; the line
     * @param useHorizontalDistance boolean; if true; the horizontal distance is used to determine the closest point; if false;
     *            the 3D distance is used to determine the closest point
     * @return OtsPoint3d; the Z component of the returned point matches the Z-component of the line at that point
     */
    private OtsPoint3d internalClosestPointOnLine(final OtsLine3d line, final boolean useHorizontalDistance)
    {
        OtsPoint3d prevPoint = null;
        double distance = Double.MAX_VALUE;
        OtsPoint3d result = null;
        for (OtsPoint3d nextPoint : line.getPoints())
        {
            if (null != prevPoint)
            {
                OtsPoint3d closest = closestPointOnSegment(prevPoint, nextPoint);
                double thisDistance = useHorizontalDistance ? horizontalDistanceSI(closest) : distanceSI(closest);
                if (thisDistance < distance)
                {
                    result = closest;
                    distance = thisDistance;
                }
            }
            prevPoint = nextPoint;
        }
        return result;
    }

    /**
     * Return the closest point on an OtsLine3d. This method takes the Z-component of this point and the line into account.
     * @param line OtsLine3d; the line
     * @return OtsPoint3d; the Z-component of the returned point matches the Z-component of the line at that point
     */
    public final OtsPoint3d closestPointOnLine(final OtsLine3d line)
    {
        return internalClosestPointOnLine(line, false);
    }

    /**
     * Return the closest point on an OtsLine3d. This method ignores the Z-component of this point and the line when computing
     * the distance.
     * @param line OtsLine3d; the line
     * @return OtsPoint3d; the Z-component of the returned point matches the Z-component of the line at that point
     */
    public final OtsPoint3d closestPointOnLine2D(final OtsLine3d line)
    {
        return internalClosestPointOnLine(line, true);
    }

    /**
     * Return the point with a length of 1 to the origin.
     * @return OtsPoint3d; the normalized point
     */
    public final OtsPoint3d normalize()
    {
        double length = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return this.translate(length);
    }

    /**
     * Return this point translated by a factor from the origin.
     * @param factor double; the translation factor
     * @return OtsPoint3d; the translated point
     */
    public final OtsPoint3d translate(final double factor)
    {
        return new OtsPoint3d(this.x / factor, this.y / factor, this.z / factor);
    }

    /**
     * Return the possible center points of a circle (sphere), given two points and a radius. Only points with Z-coordinate
     * equal to the mean of the given points are returned. (Without this restriction on the Z-coordinate, the result set would
     * be either empty, a single point, or all points on a circle.)
     * @param point1 OtsPoint3d; the first point
     * @param point2 OtsPoint3d; the second point
     * @param radius double; the radius
     * @return List&lt;OtsPoint3d&gt; a list of zero, one or two points
     */
    public static final List<OtsPoint3d> circleCenter(final OtsPoint3d point1, final OtsPoint3d point2, final double radius)
    {
        List<OtsPoint3d> result = new ArrayList<>();
        OtsPoint3d m = interpolate(0.5, point1, point2);
        double h = point1.distanceSI(m);
        if (radius < h) // no intersection
        {
            return result;
        }
        if (radius == h) // intersection at m
        {
            result.add(m);
            return result;
        }
        OtsPoint3d p = new OtsPoint3d(point2.y - point1.y, point1.x - point2.x).normalize();
        double d = Math.sqrt(radius * radius - h * h); // distance of center from m
        d = Math.sqrt(radius * radius - h * h);
        result.add(new OtsPoint3d(m.x + d * p.x, m.y + d * p.y, m.z));
        result.add(new OtsPoint3d(m.x - d * p.x, m.y - d * p.y, m.z));
        return result;
    }

    /**
     * Return the possible intersections between two circles.
     * @param center1 OtsPoint3d; the center of circle 1
     * @param radius1 double; the radius of circle 1
     * @param center2 OtsPoint3d; the center of circle 2
     * @param radius2 double; the radius of circle 2
     * @return List&lt;OtsPoint3d&gt; a list of zero, one or two points
     */
    public static final List<OtsPoint3d> circleIntersections(final OtsPoint3d center1, final double radius1,
            final OtsPoint3d center2, final double radius2)
    {
        List<OtsPoint3d> center = new ArrayList<>();
        OtsPoint3d m = interpolate(radius1 / (radius1 + radius2), center1, center2);
        double h = center1.distanceSI(m);
        if (radius1 < h) // no intersection
        {
            return center;
        }
        if (radius1 == h) // intersection at m
        {
            center.add(m);
            return center;
        }
        OtsPoint3d p = new OtsPoint3d(center2.y - center1.y, center1.x - center2.x).normalize();
        double d = Math.sqrt(radius1 * radius1 - h * h); // distance of center from m
        center.add(new OtsPoint3d(m.x + d * p.x, m.y + d * p.y, m.z));
        center.add(new OtsPoint3d(m.x - d * p.x, m.y - d * p.y, m.z));
        return center;
    }

    /**
     * @param point OtsPoint3d; the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras, expressed in the SI unit for length (meter)
     */
    public final double distanceSI(final OtsPoint3d point)
    {
        double dx = point.x - this.x;
        double dy = point.y - this.y;
        double dz = point.z - this.z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * @param point OtsPoint3d; the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras, expressed in the SI unit for length (meter)
     */
    public final double horizontalDistanceSI(final OtsPoint3d point)
    {
        double dx = point.x - this.x;
        double dy = point.y - this.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * @param point OtsPoint3d; the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras
     */
    public final Length horizontalDistance(final OtsPoint3d point)
    {
        return new Length(horizontalDistanceSI(point), LengthUnit.SI);
    }

    /**
     * Compute the distance to another point.
     * @param point OtsPoint3d; the point to which the distance has to be calculated.
     * @return the distance in 3D according to Pythagoras
     */
    public final Length distance(final OtsPoint3d point)
    {
        return new Length(distanceSI(point), LengthUnit.SI);
    }

    /**
     * Compute the horizontal direction to another point.
     * @param point OtsPoint3d; the other point
     * @return double; the direction in radians
     */
    public final double horizontalDirectionSI(final OtsPoint3d point)
    {
        return Math.atan2(point.y - this.y, point.x - this.x);
    }

    /**
     * Compute the horizontal direction to another point.
     * @param point OtsPoint3d; the other point
     * @return double; the direction in radians
     */
    public final Direction horizontalDirection(final OtsPoint3d point)
    {
        return Direction.instantiateSI(Math.atan2(point.y - this.y, point.x - this.x));
    }

    /**
     * @return the equivalent geotools Coordinate of this point.
     */
    public final Coordinate getCoordinate()
    {
        return new Coordinate(this.x, this.y, this.z);
    }

    /**
     * @return the equivalent DSOL DirectedPoint of this point. Should the result be cached?
     */
    public final DirectedPoint getDirectedPoint()
    {
        return new DirectedPoint(this.x, this.y, this.z);
    }

    /**
     * @return a Point2D with the x and y structure.
     */
    public final Point2D getPoint2D()
    {
        return new Point2D.Double(this.x, this.y);
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
        return new Bounds();
    }

    /**
     * Create a DENSE DoubleVector with the x, y and z values of this OtsPoint3d.
     * @param unit U; unit of the values in this OtsPoint3d (and also the display unit of the returned DoubleVector)
     * @return DoubleVector; the constructed DoubleVector, size is 3; first value is x, second is y, third is z
     * @param <U> the unit type
     * @param <S> the corresponding scalar type
     * @param <V> the corresponding vector type
     */
    public <U extends Unit<U>, S extends DoubleScalarInterface<U, S>,
            V extends DoubleVectorInterface<U, S, V>> V doubleVector(final U unit)
    {
        return DoubleVector.instantiate(new double[] {this.x, this.y, this.z}, unit, StorageType.DENSE);
    }

    /**
     * Construct a Direction from the rotZ component of a DirectedPoint.
     * @param directedPoint DirectedPoint; the DirectedPoint
     * @param directionUnit DirectionUnit; the unit in which the rotZ of <code>directedPoint</code> is expressed and which is
     *            also the display unit of the returned Direction
     * @return Direction; the horizontal direction (rotZ) of the <code>directedPoint</code> with display unit
     *         <code>directionUnit</code>
     */
    public static Direction direction(final DirectedPoint directedPoint, final DirectionUnit directionUnit)
    {
        return new Direction(directedPoint.getRotZ(), directionUnit);
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
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OtsPoint3d other = (OtsPoint3d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }

}
