package org.opentrafficsim.core.geometry;

import java.util.NavigableMap;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Continuous definition of a PolyLine. Naive approaches are applied for offsets, since polylines have no exact information for
 * this.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContinuousPolyLine implements ContinuousLine
{

    /** Line. */
    private final OtsLine3d line;

    /** Start point. */
    private final OrientedPoint2d startPoint;

    /** End points. */
    private final OrientedPoint2d endPoint;

    /**
     * Define continuous line from polyline. Start and end point direction are derived from the line.
     * @param line OtsLine3d; line.
     */
    public ContinuousPolyLine(final OtsLine3d line)
    {
        Throw.whenNull(line, "Line may not be null.");
        this.line = line;
        this.startPoint = line.getLocationFractionExtended(0.0);
        this.endPoint = line.getLocationFractionExtended(1.0);
    }

    /**
     * Define continuous line from polyline. Start and end point are given and may alter the direction at the endpoints
     * (slightly).
     * @param line OtsLine3d; line.
     * @param startPoint OrientedPoint2d; start point.
     * @param endPoint OrientedPoint2d; end point.
     */
    public ContinuousPolyLine(final OtsLine3d line, final OrientedPoint2d startPoint, final OrientedPoint2d endPoint)
    {
        Throw.whenNull(line, "Line may not be null.");
        this.line = line;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
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
        return 1.0 / getStartRadius();
    }

    /** {@inheritDoc} */
    @Override
    public double getEndCurvature()
    {
        return 1.0 / getEndRadius();
    }

    /** {@inheritDoc} */
    @Override
    public double getStartRadius()
    {
        return Try.assign(() -> this.line.getProjectedRadius(0.0).si, "0.0 should be in range.");
    }

    /** {@inheritDoc} */
    @Override
    public double getEndRadius()
    {
        return Try.assign(() -> this.line.getProjectedRadius(1.0).si, "1.0 should be in range.");
    }

    /**
     * Returns the line. Number of segments is ignored.
     * @param numSegments int; minimum number of segments (ignored).
     * @return OtsLine3d; polyline.
     */
    @Override
    public OtsLine3d flatten(final int numSegments)
    {
        return this.line;
    }

    /**
     * Returns the line. Maximum errors are ignored.
     * @param maxAngleError Angle; maximum angle error in polyline (ignored).
     * @param maxSpatialError double; maximum spatial error in polyline (ignored).
     * @return OtsLine3d; polyline.
     */
    @Override
    public OtsLine3d flatten(final Angle maxAngleError, final double maxSpatialError)
    {
        // TODO: possibly apply smoothing algorithm
        return this.line;
    }

    /**
     * Offset polyline based on variable offset. The number of segments is ignored.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offsets, should contain keys 0.0 and 1.0.
     * @param numSegments int; minimum number of segments (ignored).
     * @return OtsLine3d; offset polyline.
     */
    @Override
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets, final int numSegments)
    {
        return offset(offsets);
    }

    /**
     * Offset polyline based on variable offset. Maximum errors are ignored.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offsets, should contain keys 0.0 and 1.0.
     * @param maxAngleError Angle; maximum angle error in polyline (ignored).
     * @param maxSpatialError double; maximum spatial error in polyline (ignored).
     * @return OtsLine3d; offset polyline.
     */
    @Override
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets, final Angle maxAngleError, final double maxSpatialError)
    {
        return offset(offsets);
    }

    /**
     * Applies a naive offset on the line, and then adjusts the start and end point to be on the line perpendicular through each
     * end point.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offsets, should contain keys 0.0 and 1.0.
     * @return OtsLine3d; offset line.
     */
    private OtsLine3d offset(final NavigableMap<Double, Double> offsets)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        Throw.when(!offsets.containsKey(0.0), IllegalArgumentException.class, "Offsets need to contain key 0.0.");
        Throw.when(!offsets.containsKey(1.0), IllegalArgumentException.class, "Offsets need to contain key 1.0.");
        double[] relativeFractions = new double[offsets.size()];
        double[] offs = new double[offsets.size()];
        int i = 0;
        for (double f : offsets.navigableKeySet())
        {
            relativeFractions[i] = f;
            offs[i] = offsets.get(f);
            i++;
        }
        OtsLine3d offsetLine = Try.assign(() -> this.line.offsetLine(relativeFractions, offs),
                "Unexpected exception while creating offset line.");
        Point2d start = OtsGeometryUtil.offsetPoint(this.startPoint, offs[0]);
        Point2d end = OtsGeometryUtil.offsetPoint(this.endPoint, offs[offs.length - 1]);
        Point2d[] points = offsetLine.getPoints();
        points[0] = start;
        points[points.length - 1] = end;
        return Try.assign(() -> new OtsLine3d(points), "Unexpected exception while creating offset line.");
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.line.getLength().si;
    }

}
