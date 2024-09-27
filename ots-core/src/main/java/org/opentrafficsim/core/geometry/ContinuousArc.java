package org.opentrafficsim.core.geometry;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Continuous definition of an arc.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param startPoint starting point.
     * @param radius radius (must be positive).
     * @param left left curve, or right.
     * @param length arc length.
     */
    public ContinuousArc(final OrientedPoint2d startPoint, final double radius, final boolean left, final double length)
    {
        this(startPoint, radius, left, Angle.instantiateSI(
                Throw.when(length, length <= 0.0, IllegalArgumentException.class, "Length must be above 0.") / radius));
    }

    /**
     * Define arc by starting point, radius, curve direction, and angle.
     * @param startPoint starting point.
     * @param radius radius (must be positive).
     * @param left left curve, or right.
     * @param angle angle of arc (must be positive).
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
        Point2d point = getPoint(1.0, 0.0);
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

    /**
     * Returns a point on the arc at a fraction along the arc.
     * @param fraction fraction along the arc.
     * @param offset offset relative to radius.
     * @return point on the arc at a fraction along the arc.
     */
    private Point2d getPoint(final double fraction, final double offset)
    {
        double len = this.radius - this.sign * offset;
        double angle = this.startPoint.dirZ + this.sign * (this.angle.si * fraction);
        double dx = this.sign * Math.cos(angle) * len;
        double dy = this.sign * Math.sin(angle) * len;
        return new Point2d(this.center.x + dy, this.center.y - dx);
    }

    /**
     * Returns the direction at given fraction along the arc.
     * @param fraction fraction along the arc.
     * @return direction at given fraction along the arc.
     */
    private double getDirection(final double fraction)
    {
        double d = this.startPoint.dirZ + this.sign * (this.angle.si) * fraction;
        while (d > Math.PI)
        {
            d -= (2 * Math.PI);
        }
        while (d < -Math.PI)
        {
            d += (2 * Math.PI);
        }
        return d;
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d flatten(final Flattener flattener)
    {
        Throw.whenNull(flattener, "Flattener may not be null.");
        return flattener.flatten(new FlattableLine()
        {
            /** {@inheritDoc} */
            @Override
            public Point2d get(final double fraction)
            {
                return getPoint(fraction, 0.0);
            }

            /** {@inheritDoc} */
            @Override
            public double getDirection(final double fraction)
            {
                return ContinuousArc.this.getDirection(fraction);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d flattenOffset(final FractionalLengthData offsets, final Flattener flattener)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        Throw.whenNull(flattener, "Flattener may not be null.");
        return flattener.flatten(new FlattableLine()
        {
            /** {@inheritDoc} */
            @Override
            public Point2d get(final double fraction)
            {
                return getPoint(fraction, offsets.get(fraction));
            }

            /** {@inheritDoc} */
            @Override
            public double getDirection(final double fraction)
            {
                /*-
                 * x = cos(phi) * (r - s(phi))
                 * y = sin(phi) * (r - s(phi)) 
                 * 
                 * with,
                 *   phi    = angle of circle arc point at fraction, relative to circle center
                 *   r      = radius
                 *   s(phi) = offset at phi (or at fraction)
                 * 
                 * then using the product rule: 
                 * 
                 * x' = -sin(phi) * (r - s(phi)) - cos(phi) * s'(phi)
                 * y' = cos(phi) * (r - s(phi)) - sin(phi) * s'(phi)
                 */
                double phi = (ContinuousArc.this.startPoint.dirZ
                        + ContinuousArc.this.sign * (ContinuousArc.this.angle.si * fraction - Math.PI / 2));
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double sPhi = ContinuousArc.this.sign * offsets.get(fraction);
                double sPhiD = offsets.getDerivative(fraction) / ContinuousArc.this.angle.si;
                double dx = -sinPhi * (ContinuousArc.this.radius - sPhi) - cosPhi * sPhiD;
                double dy = cosPhi * (ContinuousArc.this.radius - sPhi) - sinPhi * sPhiD;
                return Math.atan2(ContinuousArc.this.sign * dy, ContinuousArc.this.sign * dx);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.angle.si * this.radius;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContinuousArc [startPoint=" + this.startPoint + ", radius=" + this.radius + ", angle=" + this.angle + ", left="
                + (this.sign > 0.0) + "]";
    }

}
