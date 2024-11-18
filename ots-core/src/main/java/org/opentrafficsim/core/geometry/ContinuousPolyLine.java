package org.opentrafficsim.core.geometry;

import java.util.List;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

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
    private final OrientedPoint2d startPoint;

    /** End points. */
    private final OrientedPoint2d endPoint;

    /**
     * Define continuous line from polyline. Start and end point direction are derived from the line.
     * @param line line.
     */
    public ContinuousPolyLine(final PolyLine2d line)
    {
        Throw.whenNull(line, "Line may not be null.");
        this.line = line;
        Ray2d startRay = line.getLocationFractionExtended(0.0);
        Ray2d endRay = line.getLocationFractionExtended(1.0);
        this.startPoint = new OrientedPoint2d(startRay.x, startRay.y, startRay.phi);
        this.endPoint = new OrientedPoint2d(endRay.x, endRay.y, endRay.phi);
    }

    /**
     * Define continuous line from polyline. Start and end point are given and may alter the direction at the endpoints
     * (slightly).
     * @param line line.
     * @param startPoint start point.
     * @param endPoint end point.
     */
    public ContinuousPolyLine(final PolyLine2d line, final OrientedPoint2d startPoint, final OrientedPoint2d endPoint)
    {
        Throw.whenNull(line, "Line may not be null.");
        this.line = line;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    @Override
    public OrientedPoint2d getStartPoint()
    {
        return this.startPoint;
    }

    @Override
    public OrientedPoint2d getEndPoint()
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
    public PolyLine2d offset(final ContinuousDoubleFunction offset)
    {
        Throw.whenNull(offset, "Offsets may not be null.");
        double[] knots = offset.getKnots();
        double[] knotOffset = new double[knots.length];
        for (int i = 0; i < knots.length; i++)
        {
            knotOffset[i] = offset.apply(knots[i]);
        }
        PolyLine2d offsetLine =
                OtsGeometryUtil.offsetLine(this.line, knots, knotOffset);
        Point2d start = OtsGeometryUtil.offsetPoint(this.startPoint, offset.apply(0.0));
        Point2d end = OtsGeometryUtil.offsetPoint(this.endPoint, offset.apply(1.0));
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
    public PolyLine2d flattenOffset(final ContinuousDoubleFunction offset, final Flattener flattener)
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
