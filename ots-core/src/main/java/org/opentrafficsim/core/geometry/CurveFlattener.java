package org.opentrafficsim.core.geometry;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.curve.Curve2d;
import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.curve.OffsetFlattener2d;
import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;

/**
 * Record combining a {@link Flattener2d} and {@link OffsetFlattener2d}.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param flattener regular flattener
 * @param offsetFlattener offset flattener
 */
public record CurveFlattener(Flattener2d flattener, OffsetFlattener2d offsetFlattener) implements Flattener2d, OffsetFlattener2d
{
    /**
     * Flattener based on number of segments.
     * @param numSegments number of segments
     */
    public CurveFlattener(final int numSegments)
    {
        this(new Flattener2d.NumSegments(numSegments), new OffsetFlattener2d.NumSegments(numSegments));
    }

    /**
     * Flattener based on maximum deviation.
     * @param maxDeviation maximum deviation
     */
    public CurveFlattener(final double maxDeviation)
    {
        this(new Flattener2d.MaxDeviation(maxDeviation), new OffsetFlattener2d.MaxDeviation(maxDeviation));
    }

    /**
     * Flattener based on maximum angle.
     * @param maxAngle maximum angle
     */
    public CurveFlattener(final Angle maxAngle)
    {
        this(new Flattener2d.MaxAngle(maxAngle.si),
                new OffsetFlattener2d.MaxAngle(maxAngle.si));
    }

    /**
     * Flattener based on maximum deviation and angle.
     * @param maxDeviation maximum deviation
     * @param maxAngle maximum angle
     */
    public CurveFlattener(final double maxDeviation, final double maxAngle)
    {
        this(new Flattener2d.MaxDeviationAndAngle(maxDeviation, maxAngle),
                new OffsetFlattener2d.MaxDeviationAndAngle(maxDeviation, maxAngle));
    }

    @Override
    public PolyLine2d flatten(final Curve2d curve)
    {
        return this.flattener.flatten(curve);
    }

    @Override
    public PolyLine2d flatten(final OffsetCurve2d curve, final ContinuousPiecewiseLinearFunction of)
    {
        return this.offsetFlattener.flatten(curve, of);
    }

    @Override
    public boolean checkLoopBack(final Double prevDirection, final Double nextDirection)
    {
        return Flattener2d.super.checkLoopBack(prevDirection, nextDirection);
    }

    @Override
    public boolean checkDirectionError(final Double segmentDirection, final Double curveDirectionAtStart,
            final Double curveDirectionAtEnd, final double maxDirectionDeviation)
    {
        return Flattener2d.super.checkDirectionError(segmentDirection, curveDirectionAtStart, curveDirectionAtEnd,
                maxDirectionDeviation);
    }
}
