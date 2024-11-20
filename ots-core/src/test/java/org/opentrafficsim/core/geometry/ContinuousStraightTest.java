package org.opentrafficsim.core.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;

/**
 * Tests for ContinuousStraight.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContinuousStraightTest
{

    /** Approximate coordinate equality margin. */
    private static final double MARGIN = 1e-9;

    /**
     * Test straight.
     */
    @Test
    public void straightTest()
    {
        OrientedPoint2d startPoint = new OrientedPoint2d(0.0, 0.0, 0.0);
        Try.testFail(() -> new ContinuousStraight(startPoint, -100.0), "Negative length should not be allowed.",
                IllegalArgumentException.class);
        ContinuousStraight straight = new ContinuousStraight(startPoint, 100.0);

        isApproximal(straight.getStartPoint(), 0.0, 0.0);
        isApproximal(straight.getEndPoint(), 100.0, 0.0);
        assertEquals(0.0, straight.getStartDirection().si, MARGIN, "Start direction is incorrect.");
        assertEquals(0.0, straight.getEndDirection().si, MARGIN, "End direction is incorrect.");
        assertTrue(Double.isInfinite(straight.getStartRadius()), "Start radius is incorrect.");
        assertTrue(Double.isInfinite(straight.getEndRadius()), "End radius is incorrect.");
        assertEquals(0.0, straight.getStartCurvature(), MARGIN, "Start curvature is incorrect.");
        assertEquals(0.0, straight.getEndCurvature(), MARGIN, "End curvature is incorrect.");

        FractionalLengthData offsets = FractionalLengthData.of(0.0, -1.0, 0.5, -1.0, 1.0, -2.0);
        PolyLine2d line = straight.offset(offsets);
        isApproximal(line.get(0), 0.0, -1.0);
        isApproximal(line.get(1), 50.0, -1.0);
        isApproximal(line.get(2), 100.0, -2.0);

        offsets = FractionalLengthData.of(0.0, 1.0, 0.5, 1.0, 1.0, 2.0);
        line = straight.offset(offsets);
        isApproximal(line.get(0), 0.0, 1.0);
        isApproximal(line.get(1), 50.0, 1.0);
        isApproximal(line.get(2), 100.0, 2.0);
    }

    /**
     * Test point is approximately the same.
     * @param point point to test.
     * @param x expected x coordinate.
     * @param y expected y coordinate.
     */
    private void isApproximal(final Point2d point, final double x, final double y)
    {
        assertEquals(x, point.x, MARGIN, "Resulting x-coordinate is incorrect");
        assertEquals(y, point.y, MARGIN, "Resulting y-coordinate is incorrect");
    }

}
