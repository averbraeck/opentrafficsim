package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.djutils.exceptions.Try;
import org.junit.Test;

/**
 * Tests for ContinuousStraight.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContinuousStraightTest
{
    
    /** Approximate coordinate equality margin. */
    private static final double MARGIN = 1e-9;

    /**
     * Test straight.
     * @throws OtsGeometryException if test is ill defined.
     */
    @Test
    public void straighTest() throws OtsGeometryException
    {
        DirectedPoint startPoint = new DirectedPoint(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        Try.testFail(() -> new ContinuousStraight(startPoint, -100.0), "Negative length should not be allowed.",
                IllegalArgumentException.class);
        ContinuousStraight straight = new ContinuousStraight(startPoint, 100.0);
        
        isApproximal(new OtsPoint3d(straight.getStartPoint()), 0.0, 0.0);
        isApproximal(new OtsPoint3d(straight.getEndPoint()), 100.0, 0.0);
        assertEquals("Start direction is incorrect.", 0.0, straight.getStartDirection().si, MARGIN);
        assertEquals("End direction is incorrect.", 0.0, straight.getEndDirection().si, MARGIN);
        assertTrue("Start radius is incorrect.", Double.isInfinite(straight.getStartRadius()));
        assertTrue("End radius is incorrect.", Double.isInfinite(straight.getEndRadius()));
        assertEquals("Start curvature is incorrect.", 0.0, straight.getStartCurvature(), MARGIN);
        assertEquals("End curvature is incorrect.", 0.0, straight.getEndCurvature(), MARGIN);
        
        NavigableMap<Double, Double> offsets = new TreeMap<>();
        offsets.put(0.0, -1.0);
        offsets.put(0.5, -1.0);
        offsets.put(1.0, -2.0);
        OtsLine3d line = straight.offset(offsets);
        isApproximal(line.get(0), 0.0, -1.0);
        isApproximal(line.get(1), 50.0, -1.0);
        isApproximal(line.get(2), 100.0, -2.0);
        
        offsets = new TreeMap<>();
        offsets.put(0.0, 1.0);
        offsets.put(0.5, 1.0);
        offsets.put(1.0, 2.0);
        line = straight.offset(offsets);
        isApproximal(line.get(0), 0.0, 1.0);
        isApproximal(line.get(1), 50.0, 1.0);
        isApproximal(line.get(2), 100.0, 2.0);
    }

    /**
     * Test point is approximately the same.
     * @param point OtsPoint3d; point to test.
     * @param x double; expected x coordinate.
     * @param y double; expected y coordinate.
     */
    private void isApproximal(final OtsPoint3d point, final double x, final double y)
    {
        assertEquals("Resulting x-coordinate is incorrect", x, point.x, MARGIN);
        assertEquals("Resulting y-coordinate is incorrect", y, point.y, MARGIN);
    }
    
}
