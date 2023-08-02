package org.opentrafficsim.core.geometry;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Continuous definition of a straight.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * @param startPoint DirectedPoint; start point.
     * @param length double; length.
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
     * Returns the line. Number of segments is ignored.
     * @param numSegments int; minimum number of segments (ignored).
     * @return OtsLine2d; polyline.
     */
    @Override
    public OtsLine2d flatten(final int numSegments)
    {
        return flatten();
    }

    /**
     * Returns the line. Maximum errors are ignored.
     * @param maxAngleError Angle; maximum angle error in polyline (ignored).
     * @param maxSpatialError double; maximum spatial error in polyline (ignored).
     * @return OtsLine2d; polyline.
     */
    @Override
    public OtsLine2d flatten(final Angle maxAngleError, final double maxSpatialError)
    {
        return flatten();
    }

    /**
     * Polyline from continuous line. A straight uses no segments.
     * @return OtsLine2d; polyline.
     */
    public OtsLine2d flatten()
    {
        return Try.assign(
                () -> new OtsLine2d(new Point2d(this.startPoint.x, this.startPoint.y),
                        new Point2d(this.endPoint.x, this.endPoint.y)),
                "Unexpected exception while creating straight OtsLine2d.");
    }

    /**
     * Offset polyline based on variable offset. The number of segments is ignored.
     * @param offsets FractionalLengthData; offsets at fractional lengths.
     * @param numSegments int; minimum number of segments (ignored).
     * @return OtsLine2d; offset polyline.
     */
    @Override
    public OtsLine2d offset(final FractionalLengthData offsets, final int numSegments)
    {
        return offset(offsets);
    }

    /**
     * Offset polyline based on variable offset. Maximum errors are ignored.
     * @param offsets FractionalLengthData; offsets at fractional lengths.
     * @param maxAngleError Angle; maximum angle error in polyline (ignored).
     * @param maxSpatialError double; maximum spatial error in polyline (ignored).
     * @return OtsLine2d; offset polyline.
     */
    @Override
    public OtsLine2d offset(final FractionalLengthData offsets, final Angle maxAngleError, final double maxSpatialError)
    {
        return offset(offsets);
    }

    /**
     * Offset polyline based on variable offset. A straight uses no segments, other than for varying offset.
     * @param offsets FractionalLengthData; offsets, should contain keys 0.0 and 1.0.
     * @return OtsLine2d; offset polyline.
     */
    public OtsLine2d offset(final FractionalLengthData offsets)
    {
        Throw.whenNull(offsets, "Offsets may not be null.");
        return Try.assign(() -> flatten(0).offsetLine(offsets.getFractionalLengthsAsArray(), offsets.getValuesAsArray()),
                "Unexpected exception while creating straigh OtsLine2d.");
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.length;
    }

}
