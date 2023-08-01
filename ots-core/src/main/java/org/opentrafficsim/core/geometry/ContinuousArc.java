package org.opentrafficsim.core.geometry;

import java.util.NavigableSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Continuous definition of an arc.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContinuousArc implements ContinuousLine
{

    /** Starting point. */
    private final OrientedPoint2d startPoint;

    /** Curve radius. */
    private final double radius;

    /** Angle of the curve. */
    private final Angle angle;

    /** Sign to use for offsets and angles, which depends on the left/right direction. */
    private double sign;

    /** Center point of circle, as calculated in constructor. */
    private final Point2d center;

    /**
     * Define arc by starting point, radius, curve direction, and length.
     * @param startPoint OrientedPoint2d; starting point.
     * @param radius double; radius (must be positive).
     * @param left boolean; left curve, or right.
     * @param length double; arc length.
     */
    public ContinuousArc(final OrientedPoint2d startPoint, final double radius, final boolean left, final double length)
    {
        this(startPoint, radius, left, Angle.instantiateSI(
                Throw.when(length, length <= 0.0, IllegalArgumentException.class, "Length must be above 0.") / radius));
    }

    /**
     * Define arc by starting point, radius, curve direction, and angle.
     * @param startPoint OrientedPoint2d; starting point.
     * @param radius double; radius (must be positive).
     * @param left boolean; left curve, or right.
     * @param angle Angle; angle of arc (must be positive).
     */
    public ContinuousArc(final OrientedPoint2d startPoint, final double radius, final boolean left, final Angle angle)
    {
        Throw.whenNull(startPoint, "Start point may not be null.");
        Throw.when(radius < 0.0, IllegalArgumentException.class, "Radius must be positive.");
        Throw.when(angle.si < 0.0, IllegalArgumentException.class, "Angle must be positive.");
        this.startPoint = startPoint;
        this.radius = radius;
        this.sign = left ? 1.0 : -1.0;
        this.angle = angle;
        double dx = Math.cos(startPoint.dirZ) * this.sign * radius;
        double dy = Math.sin(startPoint.dirZ) * this.sign * radius;

        this.center = new Point2d(startPoint.x - dy, startPoint.y + dx);
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getStartPoint()
    {
        return this.startPoint;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getEndPoint()
    {
        Point2d point = getPoint(this.angle.si, 0.0);
        double dirZ = this.startPoint.dirZ + this.sign * this.angle.si;
        dirZ = dirZ > Math.PI ? dirZ - 2.0 * Math.PI : (dirZ < -Math.PI ? dirZ + 2.0 * Math.PI : 0.0);
        return new OrientedPoint2d(point.x, point.y, dirZ);
    }

    /** {@inheritDoc} */
    @Override
    public double getStartCurvature()
    {
        return 1.0 / this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public double getEndCurvature()
    {
        return getStartCurvature();
    }

    /** {@inheritDoc} */
    @Override
    public double getStartRadius()
    {
        return this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public double getEndRadius()
    {
        return this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d flatten(final int numSegments)
    {
        Throw.when(numSegments < 1, IllegalArgumentException.class, "Number of segments should be at least 1.");
        Point2d[] points = new Point2d[numSegments + 1];
        double da = this.angle.si / numSegments;
        for (int i = 0; i < points.length; i++)
        {
            points[i] = getPoint(i * da, 0.0);
        }
        return Try.assign(() -> new OtsLine3d(points), "Exception while creating flattened arc.");
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d flatten(final Angle maxAngleError, final double maxSpatialError)
    {
        Throw.whenNull(maxAngleError, "Maximum angle error may not be null");
        Throw.when(maxAngleError.si <= 0.0, IllegalArgumentException.class, "Max angle error should be above 0.");
        Throw.when(maxSpatialError <= 0.0, IllegalArgumentException.class, "Max spatial error should be above 0.");
        int numSegmentsAngle = (int) Math.ceil(this.angle.si / maxAngleError.si);
        int numSegmentsDeviation = OtsGeometryUtil.getNumSegmentsForRadius(maxSpatialError, this.angle, this.radius);
        int numSegments = numSegmentsAngle > numSegmentsDeviation ? numSegmentsAngle : numSegmentsDeviation;
        return flatten(numSegments);
    }

    /**
     * Returns a point on the arc at an angle of 'a' from the start angle.
     * @param a double; angle.
     * @param offset double; offset relative to radius.
     * @return OtsPoint3d; point on the arc at an angle of 'a' from the start angle.
     */
    private Point2d getPoint(final double a, final double offset)
    {
        double len = this.radius - this.sign * offset;
        double angle = this.startPoint.dirZ + this.sign * a;
        double dx = this.sign * Math.cos(angle) * len;
        double dy = this.sign * Math.sin(angle) * len;
        return new Point2d(this.center.x + dy, this.center.y - dx);
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d offset(final FractionalLengthData offsets, final int numSegments)
    {
        Throw.when(numSegments < 1, IllegalArgumentException.class, "Number of segments should be at least 1.");
        Throw.whenNull(offsets, "Offsets may not be null.");
        NavigableSet<Double> f = new TreeSet<>();
        for (int i = 0; i < numSegments + 1; i++)
        {
            f.add(((double) i) / numSegments);
        }
        offsets.getFractionalLengths().forEach((r) -> f.add(r));
        Point2d[] points = new Point2d[f.size()];
        int i = 0;
        for (double r : f)
        {
            points[i] = getPoint(this.angle.si * r, offsets.get(r));
            i++;
        }
        return Try.assign(() -> new OtsLine3d(points), "Exception while creating offset arc.");
    }

    /** {@inheritDoc} */
    @Override
    public OtsLine3d offset(final FractionalLengthData offsets, final Angle maxAngleError, final double maxSpatialError)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        Throw.whenNull(maxAngleError, "Maximum angle error may not be null");
        Throw.when(maxAngleError.si <= 0.0, IllegalArgumentException.class, "Max angle error should be above 0.");
        Throw.when(maxSpatialError <= 0.0, IllegalArgumentException.class, "Max spatial error should be above 0.");
        int numSegmentsAngle = (int) Math.ceil(this.angle.si / maxAngleError.si);
        double criticalOffset = 0.0;
        for (double off : offsets.getValues())
        {
            criticalOffset = Math.max(criticalOffset, -this.sign * off);
        }
        int numSegmentsDeviation =
                OtsGeometryUtil.getNumSegmentsForRadius(maxSpatialError, this.angle, this.radius + criticalOffset);
        int numSegments = numSegmentsAngle > numSegmentsDeviation ? numSegmentsAngle : numSegmentsDeviation;
        return offset(offsets, numSegments);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContinuousArc [startPoint=" + this.startPoint + ", radius=" + this.radius + ", angle=" + this.angle + ", left="
                + (this.sign > 0.0) + "]";
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.angle.si * this.radius;
    }

}
