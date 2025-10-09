package org.opentrafficsim.core.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsGeometryException;
import org.opentrafficsim.base.geometry.OtsLine2d;

/**
 * Test the B&eacute;zier class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class BezierTest
{

    /** */
    private BezierTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the various methods in the Bezier class.
     */
    @Test
    public void bezierTest()
    {
        Point2d from = new Point2d(10, 0);
        Point2d control1 = new Point2d(20, 0);
        Point2d control2 = new Point2d(00, 20);
        Point2d to = new Point2d(0, 10);
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine2d line = Bezier.cubic(n, from, control1, control2, to);
            assertTrue(line.size() == n, "result has n points");
            assertTrue(line.get(0).equals(from), "result starts with from");
            assertTrue(line.get(line.size() - 1).equals(to), "result ends with to");
            for (int i = 1; i < line.size() - 1; i++)
            {
                Point2d p = line.get(i);
                assertTrue(p.x > 0 && p.x < 15, "x of intermediate point has reasonable value");
                assertTrue(p.y > 0 && p.y < 15, "y of intermediate point has reasonable value");
            }
        }
        for (int n = -1; n <= 1; n++)
        {
            try
            {
                Bezier.cubic(n, from, control1, control2, to);
            }
            catch (OtsGeometryException e)
            {
                // Ignore expected exception
            }
        }
        for (int n : new int[] {2, 3, 4, 100})
        {
            for (double shape : new double[] {0.5, 1.0, 2.0})
            {
                for (boolean weighted : new boolean[] {false, true})
                {
                    DirectedPoint2d start = new DirectedPoint2d(from.x, from.y, 0);
                    DirectedPoint2d end = new DirectedPoint2d(to.x, to.y, -Math.PI / 2);
                    OtsLine2d line = 1.0 == shape ? Bezier.cubic(n, start, end) : Bezier.cubic(n, start, end, shape, weighted);
                    for (int i = 1; i < line.size() - 1; i++)
                    {
                        Point2d p = line.get(i);
                        assertTrue(p.x > 0 && p.x < 15, "x of intermediate point has reasonable value");
                        assertTrue(p.y > 0 && p.y < 15, "y of intermediate point has reasonable value");
                    }
                }
            }
        }
        // Pity that the value 64 is private in the Bezier class.
        assertEquals(64,
                Bezier.cubic(new DirectedPoint2d(from.x, from.y, 0), new DirectedPoint2d(to.x, to.y, -Math.PI / 2)).size(),
                "Number of points is 64");
        assertEquals(64, Bezier.bezier(from, control1, control2, to).size(), "Number of points is 64");
        control1 = new Point2d(5, 0);
        control2 = new Point2d(0, 5);
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine2d line = Bezier.cubic(n, from, control1, control2, to);
            for (int i = 1; i < line.size() - 1; i++)
            {
                Point2d p = line.get(i);
                // System.out.println("Point " + i + " of " + n + " is " + p);
                assertTrue(p.x > 0 && p.x < 10, "x of intermediate point has reasonable value");
                assertTrue(p.y > 0 && p.y < 10, "y of intermediate point has reasonable value");
            }
        }
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine2d line =
                    Bezier.cubic(n, new DirectedPoint2d(from.x, from.y, Math.PI), new DirectedPoint2d(to.x, to.y, Math.PI / 2));
            for (int i = 1; i < line.size() - 1; i++)
            {
                Point2d p = line.get(i);
                assertTrue(p.x > 0 && p.x < 10, "x of intermediate point has reasonable value");
                assertTrue(p.y > 0 && p.y < 10, "y of intermediate point has reasonable value");
            }
        }
    }

}
