package org.opentrafficsim.core.geometry;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.point.OrientedPoint2d;

/**
 * A continuous line defines a line in an exact manner, from which numerical polylines can be derived. The continuous definition
 * is useful to accurately connect different lines, e.g. based on the direction of the point where they meet. Moreover, this
 * direction may be accurately be determined by either of the lines. For example, an arc can be defined up to a certain angle.
 * Whatever the angle of the last line segment in a polyline for the arc may be, the continuous line contains the final
 * direction exactly. The continuous definition is also useful to define accurate offset lines, which depend on accurate
 * directions especially at the line end points.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface ContinuousLine
{

    /**
     * Start point.
     * @return OrientedPoint2d; start point.
     */
    OrientedPoint2d getStartPoint();

    /**
     * End point.
     * @return OrientedPoint2d; end point.
     */
    OrientedPoint2d getEndPoint();

    /**
     * Start direction.
     * @return Direction; start point.
     */
    default Direction getStartDirection()
    {
        return Direction.instantiateSI(getStartPoint().dirZ);
    }

    /**
     * End direction.
     * @return Direction; end point.
     */
    default Direction getEndDirection()
    {
        return Direction.instantiateSI(getEndPoint().dirZ);
    }

    /**
     * Start curvature.
     * @return double; start curvature.
     */
    double getStartCurvature();

    /**
     * End curvature.
     * @return double; end curvature.
     */
    double getEndCurvature();

    /**
     * Start radius.
     * @return double; start radius.
     */
    default double getStartRadius()
    {
        return 1.0 / getStartCurvature();
    }

    /**
     * End radius.
     * @return double; end radius.
     */
    default double getEndRadius()
    {
        return 1.0 / getEndCurvature();
    }

    /**
     * Polyline from continuous line. The number of segments of the polyline is guaranteed to be at least {@code numSegments},
     * but it may be larger.
     * @param numSegments int; minimum number of segments.
     * @return OtsLine2d; polyline.
     */
    OtsLine2d flatten(int numSegments);

    /**
     * Polyline from continuous line. Implementations of this method guarantee that the resulting polyline shows angle errors
     * and spatial errors smaller than specified. Implementation are free to apply conservative heuristics that create smaller
     * errors and a larger number of points.
     * @param maxAngleError Angle; maximum angle error in polyline.
     * @param maxSpatialError double; maximum spatial error in polyline.
     * @return OtsLine2d; polyline.
     */
    OtsLine2d flatten(Angle maxAngleError, double maxSpatialError);

    /**
     * Offset polyline based on variable offset. The number of segments of the polyline is guaranteed to be at least
     * {@code numSegments}, but it may be larger.
     * @param offsets FractionalLengthData; offsets at fractional lengths.
     * @param numSegments int; minimum number of segments.
     * @return OtsLine2d; offset polyline.
     */
    OtsLine2d offset(FractionalLengthData offsets, int numSegments);

    /**
     * Offset polyline based on variable offset. Implementations of this method guarantee that the resulting polyline shows
     * angle errors and spatial errors smaller than specified. Implementation are free to apply conservative heuristics that
     * create smaller errors and a larger number of points.
     * @param offsets FractionalLengthData; offsets at fractional lengths.
     * @param maxAngleError Angle; maximum angle error in polyline.
     * @param maxSpatialError double; maximum spatial error in polyline.
     * @return OtsLine2d; offset polyline.
     */
    OtsLine2d offset(FractionalLengthData offsets, Angle maxAngleError, double maxSpatialError);

    /**
     * Offset polyline based on single offset. The number of segments of the polyline is guaranteed to be at least
     * {@code numSegments}, but it may be larger.
     * @param offset double; offset.
     * @param numSegments int; minimum number of segments.
     * @return OtsLine2d; offset polyline.
     */
    default OtsLine2d offset(final double offset, final int numSegments)
    {
        return offset(offset, offset, numSegments);
    }

    /**
     * Offset polyline based on single offset. Implementations of this method guarantee that the resulting polyline shows angle
     * errors and spatial errors smaller than specified. Implementation are free to apply conservative heuristics that create
     * smaller errors and a larger number of points.
     * @param offset double; offset.
     * @param maxAngleError Angle; maximum angle error in polyline.
     * @param maxSpatialError double; maximum spatial error in polyline.
     * @return OtsLine2d; offset polyline.
     */
    default OtsLine2d offset(final double offset, final Angle maxAngleError, final double maxSpatialError)
    {
        return offset(offset, offset, maxAngleError, maxSpatialError);
    }

    /**
     * Offset polyline based on start and end offset. The number of segments of the polyline is guaranteed to be at least
     * {@code numSegments}, but it may be larger.
     * @param startOffset double; offset at start.
     * @param endOffset double; offset at end.
     * @param numSegments int; minimum number of segments.
     * @return OtsLine2d; offset polyline.
     */
    default OtsLine2d offset(final double startOffset, final double endOffset, final int numSegments)
    {
        return offset(FractionalLengthData.of(0.0, startOffset, 1.0, endOffset), numSegments);
    }

    /**
     * Offset polyline based on start and end offset. Implementations of this method guarantee that the resulting polyline shows
     * angle errors and spatial errors smaller than specified. Implementation are free to apply conservative heuristics that
     * create smaller errors and a larger number of points.
     * @param startOffset double; offset at start.
     * @param endOffset double; offset at end.
     * @param maxAngleError Angle; maximum angle error in polyline.
     * @param maxSpatialError double; maximum spatial error in polyline.
     * @return OtsLine2d; offset polyline.
     */
    default OtsLine2d offset(final double startOffset, final double endOffset, final Angle maxAngleError,
            final double maxSpatialError)
    {
        return offset(FractionalLengthData.of(0.0, startOffset, 1.0, endOffset), maxAngleError, maxSpatialError);
    }

    /**
     * Return the length of the line.
     * @return double; length of the line.
     */
    double getLength();

}
