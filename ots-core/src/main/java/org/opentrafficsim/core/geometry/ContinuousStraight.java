package org.opentrafficsim.core.geometry;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Continuous definition of a straight.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContinuousStraight implements ContinuousLine
{

    /** Start point with direction. */
    private final OrientedPoint2d startPoint;

    /** End point with direction. */
    private final OrientedPoint2d endPoint;

    /** Length. */
    private final double length;

    /**
     * Constructor.
     * @param startPoint start point.
     * @param length length.
     */
    public ContinuousStraight(final OrientedPoint2d startPoint, final double length)
    {
        Throw.whenNull(startPoint, "Start point may not be null.");
        Throw.when(length <= 0.0, IllegalArgumentException.class, "Length must be above 0.");
        this.startPoint = startPoint;
        this.endPoint = new OrientedPoint2d(startPoint.x + length * Math.cos(startPoint.dirZ),
                startPoint.y + length * Math.sin(startPoint.dirZ), startPoint.dirZ);
        this.length = length;
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
        return this.endPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double getStartCurvature()
    {
        return 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public double getEndCurvature()
    {
        return 0.0;
    }

    /**
     * Polyline from continuous line. A straight uses no segments.
     * @return polyline.
     */
    public PolyLine2d flatten()
    {
        return new PolyLine2d(new Point2d(this.startPoint.x, this.startPoint.y), new Point2d(this.endPoint.x, this.endPoint.y));
    }

    /**
     * Returns a 2-point line. Flattener is ignored.
     * @param flattener flattener (ignored).
     * @return flattened line.
     */
    @Override
    public PolyLine2d flatten(final Flattener flattener)
    {
        return flatten();
    }

    /**
     * Offset polyline based on variable offset. A straight uses no segments, other than for varying offset.
     * @param offsets offsets, should contain keys 0.0 and 1.0.
     * @return offset polyline.
     */
    public PolyLine2d offset(final FractionalLengthData offsets)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        return Try.assign(
                () -> OtsGeometryUtil.offsetLine(flatten(), offsets.getFractionalLengthsAsArray(), offsets.getValuesAsArray()),
                "Unexpected exception while creating straigh OtsLine2d.");
    }

    /**
     * Returns the regular offset line of a 2-point line. Flattener is ignored.
     * @param offsets offset data.
     * @param flattener flattener (ignored).
     * @return flattened line.
     */
    @Override
    public PolyLine2d flattenOffset(final FractionalLengthData offsets, final Flattener flattener)
    {
        return offset(offsets);
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContinuousStraight [startPoint=" + this.startPoint + ", endPoint=" + this.endPoint + ", length=" + this.length
                + "]";
    }

}
