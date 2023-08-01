package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.djutils.draw.point.Point2d;
import org.junit.Test;

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

        OtsLine3d line = bezier.flatten(32);
        assertEquals("Length of flattened Bezier is not correct", line.getLength().si, 171.2213439251704017, MARGIN);

        NavigableMap<Double, Double> offsets = new TreeMap<>();
        offsets.put(0.0, 2.0);
        offsets.put(0.33, 3.0);
        offsets.put(1.0, 10.0);
        line = bezier.offset(offsets, 32);
        assertEquals("Length of offset Bezier is not correct", line.getLength().si, 190.5485421127407335, MARGIN);
        assertEquals("Number of segments of offset Bezier is not correct", line.size(), 36);

        offsets = new TreeMap<>();
        offsets.put(0.0, -1.0);
        offsets.put(0.33, -1.5);
        offsets.put(1.0, -5.0);
        line = bezier.offset(offsets, 32);
        assertEquals("Length of offset Bezier is not correct", line.getLength().si, 161.7801902734066459, MARGIN);
        assertEquals("Number of segments of offset Bezier is not correct", line.size(), 36);
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
