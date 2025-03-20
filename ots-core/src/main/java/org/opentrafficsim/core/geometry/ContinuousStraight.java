package org.opentrafficsim.core.geometry;

import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction.TupleSt;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;

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
    private final DirectedPoint2d startPoint;

    /** End point with direction. */
    private final DirectedPoint2d endPoint;

    /** Length. */
    private final double length;

    /**
     * Constructor.
     * @param startPoint start point.
     * @param length length.
     */
    public ContinuousStraight(final DirectedPoint2d startPoint, final double length)
    {
        Throw.whenNull(startPoint, "Start point may not be null.");
        Throw.when(length <= 0.0, IllegalArgumentException.class, "Length must be above 0.");
        this.startPoint = startPoint;
        this.endPoint = new DirectedPoint2d(startPoint.x + length * Math.cos(startPoint.dirZ),
                startPoint.y + length * Math.sin(startPoint.dirZ), startPoint.dirZ);
        this.length = length;
    }

    @Override
    public DirectedPoint2d getStartPoint()
    {
        return this.startPoint;
    }

    @Override
    public DirectedPoint2d getEndPoint()
    {
        return this.endPoint;
    }

    @Override
    public double getStartCurvature()
    {
        return 0.0;
    }

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
     * @param offset offset, should contain keys 0.0 and 1.0.
     * @return offset polyline
     */
    public PolyLine2d offset(final ContinuousPiecewiseLinearFunction offset)
    {
        Throw.whenNull(offset, "Offsets may not be null.");
        double[] knots = new double[offset.size()];
        double[] knotOffset = new double[offset.size()];
        int i = 0;
        for (TupleSt st : offset)
        {
            knots[i] = st.s();
            knotOffset[i] = st.t();
            i++;
        }
        return OtsGeometryUtil.offsetLine(flatten(), knots, knotOffset);
    }

    /**
     * Returns the regular offset line of a 2-point line. Flattener is ignored.
     * @param offsets offset data.
     * @param flattener flattener (ignored).
     * @return flattened line.
     */
    @Override
    public PolyLine2d flattenOffset(final ContinuousPiecewiseLinearFunction offsets, final Flattener flattener)
    {
        return offset(offsets);
    }

    @Override
    public double getLength()
    {
        return this.length;
    }

    @Override
    public String toString()
    {
        return "ContinuousStraight [startPoint=" + this.startPoint + ", endPoint=" + this.endPoint + ", length=" + this.length
                + "]";
    }

}
