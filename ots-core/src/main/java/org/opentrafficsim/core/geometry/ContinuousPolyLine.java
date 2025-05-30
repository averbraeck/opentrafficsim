package org.opentrafficsim.core.geometry;

import java.util.List;

import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction.TupleSt;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;

/**
 * Continuous definition of a PolyLine. Naive approaches are applied for offsets, since polylines have no exact information for
 * this.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContinuousPolyLine implements ContinuousLine
{

    /** Line. */
    private final PolyLine2d line;

    /** Start point. */
    private final DirectedPoint2d startPoint;

    /** End points. */
    private final DirectedPoint2d endPoint;

    /**
     * Define continuous line from polyline. Start and end point direction are derived from the line.
     * @param line line.
     */
    public ContinuousPolyLine(final PolyLine2d line)
    {
        Throw.whenNull(line, "Line may not be null.");
        this.line = line;
        DirectedPoint2d startPt = line.getLocationFractionExtended(0.0);
        DirectedPoint2d endPt = line.getLocationFractionExtended(1.0);
        this.startPoint = new DirectedPoint2d(startPt.x, startPt.y, startPt.dirZ);
        this.endPoint = new DirectedPoint2d(endPt.x, endPt.y, endPt.dirZ);
    }

    /**
     * Define continuous line from polyline. Start and end point are given and may alter the direction at the endpoints
     * (slightly).
     * @param line line.
     * @param startPoint start point.
     * @param endPoint end point.
     */
    public ContinuousPolyLine(final PolyLine2d line, final DirectedPoint2d startPoint, final DirectedPoint2d endPoint)
    {
        Throw.whenNull(line, "Line may not be null.");
        this.line = line;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
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
        return 1.0 / getStartRadius();
    }

    @Override
    public double getEndCurvature()
    {
        return 1.0 / getEndRadius();
    }

    @Override
    public double getStartRadius()
    {
        return new OtsLine2d(this.line).getProjectedRadius(0.0).si;
    }

    @Override
    public double getEndRadius()
    {
        return new OtsLine2d(this.line).getProjectedRadius(1.0).si;
    }

    /**
     * Polyline from continuous line. Returns the line as is.
     * @return polyline.
     */
    public PolyLine2d flatten()
    {
        return this.line;
    }

    /**
     * Returns the line as is. Flattener is ignored.
     * @param flattener flattener (ignored).
     * @return flattened line.
     */
    @Override
    public PolyLine2d flatten(final Flattener flattener)
    {
        return this.line;
    }

    /**
     * Returns an offset line. This is a regular offset line, with start and end points moved to be perpendicular to end point
     * directions.
     * @param offset offset data.
     * @return flattened line.
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
        PolyLine2d offsetLine = OtsGeometryUtil.offsetLine(this.line, knots, knotOffset);
        Point2d start = OtsGeometryUtil.offsetPoint(this.startPoint, offset.get(0.0));
        Point2d end = OtsGeometryUtil.offsetPoint(this.endPoint, offset.get(1.0));
        List<Point2d> points = offsetLine.getPointList();
        points.set(0, start);
        points.set(points.size() - 1, end);
        return new PolyLine2d(points);
    }

    /**
     * Returns the regular offset. Flattener is ignored.
     * @param offset offset data.
     * @param flattener flattener (ignored).
     * @return flattened line.
     */
    @Override
    public PolyLine2d flattenOffset(final ContinuousPiecewiseLinearFunction offset, final Flattener flattener)
    {
        return offset(offset);
    }

    @Override
    public double getLength()
    {
        return this.line.getLength();
    }

    @Override
    public String toString()
    {
        return "ContinuousPolyLine [startPoint=" + this.startPoint + ", endPoint=" + this.endPoint + "]";
    }

}
