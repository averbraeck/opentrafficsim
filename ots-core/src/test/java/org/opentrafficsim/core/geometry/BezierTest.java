package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.Test;

/**
 * Test the B&eacute;zier class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class BezierTest
{

    /**
     * Test the various methods in the Bezier class.
     * @throws OtsGeometryException when this happens uncaught this test has failed
     */
    @Test
    public final void bezierTest() throws OtsGeometryException
    {
        Point2d from = new Point2d(10, 0);
        Point2d control1 = new Point2d(20, 0);
        Point2d control2 = new Point2d(00, 20);
        Point2d to = new Point2d(0, 10);
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine3d line = Bezier.cubic(n, from, control1, control2, to);
            assertTrue("result has n points", line.size() == n);
            assertTrue("result starts with from", line.get(0).equals(from));
            assertTrue("result ends with to", line.get(line.size() - 1).equals(to));
            for (int i = 1; i < line.size() - 1; i++)
            {
                Point2d p = line.get(i);
                assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 15);
                assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 15);
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
                    OrientedPoint2d start = new OrientedPoint2d(from.x, from.y, 0);
                    OrientedPoint2d end = new OrientedPoint2d(to.x, to.y, -Math.PI / 2);
                    OtsLine3d line = 1.0 == shape ? Bezier.cubic(n, start, end) : Bezier.cubic(n, start, end, shape, weighted);
                    for (int i = 1; i < line.size() - 1; i++)
                    {
                        Point2d p = line.get(i);
                        assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 15);
                        assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 15);
                    }
                }
            }
        }
        // Pity that the value 64 is private in the Bezier class.
        assertEquals("Number of points is 64", 64,
                Bezier.cubic(new OrientedPoint2d(from.x, from.y, 0), new OrientedPoint2d(to.x, to.y, -Math.PI / 2)).size());
        assertEquals("Number of points is 64", 64, Bezier.bezier(from, control1, control2, to).size());
        control1 = new Point2d(5, 0);
        control2 = new Point2d(0, 5);
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine3d line = Bezier.cubic(n, from, control1, control2, to);
            for (int i = 1; i < line.size() - 1; i++)
            {
                Point2d p = line.get(i);
                // System.out.println("Point " + i + " of " + n + " is " + p);
                assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 10);
                assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 10);
            }
        }
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine3d line =
                    Bezier.cubic(n, new OrientedPoint2d(from.x, from.y, Math.PI), new OrientedPoint2d(to.x, to.y, Math.PI / 2));
            for (int i = 1; i < line.size() - 1; i++)
            {
                Point2d p = line.get(i);
                assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 10);
                assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 10);
            }
        }
    }

}
