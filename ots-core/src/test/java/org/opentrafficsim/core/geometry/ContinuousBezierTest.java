package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.junit.Test;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;

/**
 * Test for ContinuousBezier.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContinuousBezierTest
{

    /** Approximate coordinate equality margin. */
    private static final double MARGIN = 1e-6;

    /**
     * Test Bezier.
     */
    @Test
    public void testBezier()
    {
        /*
         * This is a Bezier with two roots, one inflection, and one (intermediate) cross-section for the offset lines. It was
         * tested and visually verified in Matlab. This method tests the right outcome.
         */
        ContinuousBezierCubic bezier = new ContinuousBezierCubic(new Point2d(0.0, 0.0), new Point2d(50.0, 0.0),
                new Point2d(150.0, 100.0), new Point2d(145.0, 50.0));

        isApproximal(bezier.getStartPoint(), 0.0, 0.0);
        isApproximal(bezier.getEndPoint(), 145.0, 50.0);
        assertEquals("Start direction is incorrect.", 0.0, bezier.getStartDirection().si, MARGIN);
        assertEquals("End direction is incorrect.", Math.atan2(50.0 - 100.0, 145.0 - 150.0), bezier.getEndDirection().si,
                MARGIN);
        assertEquals("Start radius is incorrect.", 37.5, bezier.getStartRadius(), MARGIN);
        assertEquals("End radius is incorrect.", -42.2932265722170797, bezier.getEndRadius(), MARGIN);
        assertEquals("Start curvature is incorrect.", 0.0266666666666667, bezier.getStartCurvature(), MARGIN);
        assertEquals("End curvature is incorrect.", -0.0236444480841978, bezier.getEndCurvature(), MARGIN);

        NumSegments numSegments32 = new NumSegments(32);
        PolyLine2d line = bezier.flatten(numSegments32);
        assertEquals("Length of flattened Bezier is not correct", line.getLength(), 171.2213439251704017, MARGIN);

        FractionalLengthData offsets = FractionalLengthData.of(0.0, 2.0, 0.33, 3.0, 1.0, 10.0);
        line = bezier.flattenOffset(offsets, numSegments32);
        /*
         * The Bezier flattening procedure used to divide the number of line segments over the Bezier segments, rounded up for
         * each. This procedure changed to simply stepping in t, where it is known where each Bezier semgent starts, and what
         * t-range it covers. Within each Bezier segment the sub-t value is interpolated linearly from 0.0 to 1.0. Due to this
         * change, the number of segments is now exactly 32, and the flattened length is slightly different. Hence, when
         * checking the length we allow a little margin when comparing to the original lengths found in Matlab. Those values are
         * now also rounded to 1 decimal (190.5 and 161.8).
         */
        double lengthMargin = 0.1;
        assertEquals("Length of offset Bezier is not correct", line.getLength(), 190.5, lengthMargin);
        assertEquals("Number of segments of offset Bezier is not correct", 33, line.size()); // was 36

        offsets = FractionalLengthData.of(0.0, -1.0, 0.33, -1.5, 1.0, -5.0);
        line = bezier.flattenOffset(offsets, numSegments32);
        assertEquals("Length of offset Bezier is not correct", line.getLength(), 161.8, lengthMargin);
        assertEquals("Number of segments of offset Bezier is not correct", 33, line.size()); // was 36
    }

    /**
     * Test point is approximately the same.
     * @param point Point2d; point to test.
     * @param x double; expected x coordinate.
     * @param y double; expected y coordinate.
     */
    private void isApproximal(final Point2d point, final double x, final double y)
    {
        assertEquals("Resulting x-coordinate is incorrect", x, point.x, MARGIN);
        assertEquals("Resulting y-coordinate is incorrect", y, point.y, MARGIN);
    }

}
