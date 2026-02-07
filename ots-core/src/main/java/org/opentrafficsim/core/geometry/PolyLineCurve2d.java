package org.opentrafficsim.core.geometry;

import java.util.List;

import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.OffsetFlattener2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.math.functions.MathFunction.TupleSt;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;

/**
 * Curve based on a poly line implementing all the features of {@link OffsetCurve2d}.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PolyLineCurve2d implements OffsetCurve2d
{

    /** Poly line. */
    private final PolyLine2d line;

    /** Start direction. */
    private final double startDirection;

    /** End direction. */
    private final double endDirection;

    /**
     * Constructor.
     * @param line poly line
     * @param startDirection start direction
     * @param endDirection end direction
     */
    public PolyLineCurve2d(final PolyLine2d line, final double startDirection, final double endDirection)
    {
        this.line = line;
        this.startDirection = startDirection;
        this.endDirection = endDirection;
    }

    @Override
    public Double getDirection(final double fraction)
    {
        return fraction == 0.0 ? this.startDirection
                : (fraction == 1.0 ? this.endDirection : this.line.getLocation(fraction).dirZ);
    }

    @Override
    public double getDirection(final double fraction, final ContinuousPiecewiseLinearFunction of)
    {
        return getDirection(fraction) + of.getDerivative(fraction);
    }

    @Override
    public DirectedPoint2d getPoint(final double fraction)
    {
        return this.line.getLocationFraction(fraction);
    }

    @Override
    public DirectedPoint2d getPoint(final double fraction, final ContinuousPiecewiseLinearFunction of)
    {
        DirectedPoint2d offsetPoint = OtsGeometryUtil.offsetPoint(getPoint(fraction), of.get(fraction));
        return new DirectedPoint2d(offsetPoint.x, offsetPoint.y, offsetPoint.dirZ + of.getDerivative(fraction));
    }

    @Override
    public double getLength()
    {
        return this.line.getLength();
    }

    @Override
    public PolyLine2d toPolyLine(final Flattener2d flattener)
    {
        return this.line;
    }

    @Override
    public PolyLine2d toPolyLine(final OffsetFlattener2d flattener, final ContinuousPiecewiseLinearFunction offsets)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        double[] knots = new double[offsets.size()];
        double[] knotOffset = new double[offsets.size()];
        int i = 0;
        for (TupleSt st : offsets)
        {
            knots[i] = st.s();
            knotOffset[i] = st.t();
            i++;
        }
        PolyLine2d offsetLine = OtsGeometryUtil.offsetLine(this.line, knots, knotOffset);
        Point2d start = OtsGeometryUtil.offsetPoint(getStartPoint(), offsets.get(0.0));
        Point2d end = OtsGeometryUtil.offsetPoint(getEndPoint(), offsets.get(1.0));
        List<Point2d> points = offsetLine.getPointList();
        points.set(0, start);
        points.set(points.size() - 1, end);
        return new PolyLine2d(0.0, points);
    }

}
