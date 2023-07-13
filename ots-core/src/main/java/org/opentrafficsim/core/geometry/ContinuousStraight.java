package org.opentrafficsim.core.geometry;

import java.util.NavigableMap;

import org.djunits.value.vdouble.scalar.Angle;
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
    private final DirectedPoint startPoint;

    /** End point with direction. */
    private final DirectedPoint endPoint;

    /** Length. */
    private final double length;

    /**
     * Constructor.
     * @param startPoint DirectedPoint; start point.
     * @param length double; length.
     */
    public ContinuousStraight(final DirectedPoint startPoint, final double length)
    {
        Throw.whenNull(startPoint, "Start point may not be null.");
        Throw.when(length <= 0.0, IllegalArgumentException.class, "Length must be above 0.");
        this.startPoint = startPoint;
        this.endPoint = new DirectedPoint(startPoint.x + length * Math.cos(startPoint.dirZ),
                startPoint.y + length * Math.sin(startPoint.dirZ), startPoint.z, 0.0, 0.0, startPoint.dirZ);
        this.length = length;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getStartPoint()
    {
        return this.startPoint;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getEndPoint()
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
     * @return OtsLine3d; polyline.
     */
    @Override
    public OtsLine3d flatten(final int numSegments)
    {
        return flatten();
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
        return flatten();
    }

    /**
     * Polyline from continuous line. A straight uses no segments.
     * @return OtsLine3d; polyline.
     */
    public OtsLine3d flatten()
    {
        return Try.assign(() -> new OtsLine3d(new OtsPoint3d(this.startPoint), new OtsPoint3d(this.endPoint)),
                "Unexpected exception while creating straight OtsLine3d.");
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
     * Offset polyline based on variable offset. A straight uses no segments, other than for varying offset.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offsets, should contain keys 0.0 and 1.0.
     * @return OtsLine3d; offset polyline.
     */
    public OtsLine3d offset(final NavigableMap<Double, Double> offsets)
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
        return Try.assign(() -> flatten(0).offsetLine(relativeFractions, offs),
                "Unexpected exception while creating straigh OtsLine3d.");
    }

    /** {@inheritDoc} */
    @Override
    public double getLength()
    {
        return this.length;
    }

}
