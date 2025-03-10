package org.opentrafficsim.base.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;

/**
 * Test the methods in the OtsGeometryUtil class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class OtsGeometryUtilTest
{

    /** */
    private OtsGeometryUtilTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the printCoordinate family of functions.
     */
    @Test
    public void geometryTest()
    {
        Point2d p0 = new Point2d(1.2, 2.3);
        String prefix = "Prefix";
        String result = OtsGeometryUtil.printCoordinate(prefix, p0);
        assertTrue(result.startsWith(prefix), "output starts with prefix");
        assertTrue(result.contains(String.format(Locale.US, "%8.3f", p0.x)),
                "output contains x coordinate in three decimal digits");
        assertTrue(result.contains(String.format(Locale.US, "%8.3f", p0.y)),
                "output contains y coordinate in three decimal digits");
    }

    /**
     * Tests that getNumSegmentsForRadius is correct.
     */
    @Test
    public void testNumSegments()
    {
        for (double r : new double[] {1.0, 2.0, 4.0})
        {
            // cut half circle in 2, 4, ..., 512 parts
            int n = 1;
            for (int i = 2; i < 11; i++)
            {
                n *= 2;
                testSlice(n, r);
            }
        }
    }

    /**
     * Tests one case of getNumSegmentsForRadius, with deviation just below and just above.
     * @param n parts in half circle
     * @param radius circle radius
     */
    private void testSlice(final int n, final double radius)
    {
        double deviation = (1.0 - Math.cos(Math.PI / n)) * radius;
        Angle ang = Angle.instantiateSI(Math.PI);
        assertEquals(n / 2, OtsGeometryUtil.getNumSegmentsForRadius(deviation * 1.001, ang, radius));
        assertEquals((n / 2) + 1, OtsGeometryUtil.getNumSegmentsForRadius(deviation * 0.999, ang, radius));
    }

    /**
     * Test offset point.
     */
    @Test
    public void testOffsetPoint()
    {
        double margin = 1e-6;
        DirectedPoint2d p = new DirectedPoint2d(0.0, 0.0, 0.0);
        DirectedPoint2d o = OtsGeometryUtil.offsetPoint(p, 1.0);
        assertEquals(0.0, o.x, margin);
        assertEquals(1.0, o.y, margin);
        assertEquals(0.0, o.dirZ, margin);
        o = OtsGeometryUtil.offsetPoint(p, -1.0);
        assertEquals(0.0, o.x, margin);
        assertEquals(-1.0, o.y, margin);
        assertEquals(0.0, o.dirZ, margin);

        p = new DirectedPoint2d(0.0, 0.0, .5 * Math.PI);
        o = OtsGeometryUtil.offsetPoint(p, 1.0);
        assertEquals(-1.0, o.x, margin);
        assertEquals(0.0, o.y, margin);
        assertEquals(.5 * Math.PI, o.dirZ, margin);
        o = OtsGeometryUtil.offsetPoint(p, -1.0);
        assertEquals(1.0, o.x, margin);
        assertEquals(0.0, o.y, margin);
        assertEquals(.5 * Math.PI, o.dirZ, margin);

        p = new DirectedPoint2d(0.0, 0.0, Math.PI);
        o = OtsGeometryUtil.offsetPoint(p, 1.0);
        assertEquals(0.0, o.x, margin);
        assertEquals(-1.0, o.y, margin);
        assertEquals(Math.PI, o.dirZ, margin);
        o = OtsGeometryUtil.offsetPoint(p, -1.0);
        assertEquals(0.0, o.x, margin);
        assertEquals(1.0, o.y, margin);
        assertEquals(Math.PI, o.dirZ, margin);
    }

}
