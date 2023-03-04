package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the B&eacute;zier class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
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
        OtsPoint3d from = new OtsPoint3d(10, 0, 0);
        OtsPoint3d control1 = new OtsPoint3d(20, 0, 10);
        OtsPoint3d control2 = new OtsPoint3d(00, 20, 20);
        OtsPoint3d to = new OtsPoint3d(0, 10, 30);
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine3d line = Bezier.cubic(n, from, control1, control2, to);
            assertTrue("result has n points", line.size() == n);
            assertTrue("result starts with from", line.get(0).equals(from));
            assertTrue("result ends with to", line.get(line.size() - 1).equals(to));
            for (int i = 1; i < line.size() - 1; i++)
            {
                OtsPoint3d p = line.get(i);
                assertTrue("z of intermediate point has reasonable value", p.z > line.get(i - 1).z && p.z < line.get(i + 1).z);
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
                    DirectedPoint start = new DirectedPoint(from.x, from.y, from.z, Math.PI / 2, -Math.PI / 2, 0);
                    DirectedPoint end = new DirectedPoint(to.x, to.y, to.z, Math.PI, 0, -Math.PI / 2);
                    OtsLine3d line = 1.0 == shape ? Bezier.cubic(n, start, end) : Bezier.cubic(n, start, end, shape, weighted);
                    for (int i = 1; i < line.size() - 1; i++)
                    {
                        OtsPoint3d p = line.get(i);
                        assertTrue("z of intermediate point has reasonable value",
                                p.z > line.get(i - 1).z && p.z < line.get(i + 1).z);
                        assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 15);
                        assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 15);
                    }
                }
            }
        }
        // Pity that the value 64 is private in the Bezier class.
        assertEquals("Number of points is 64", 64,
                Bezier.cubic(new DirectedPoint(from.x, from.y, from.z, Math.PI / 2, -Math.PI / 2, 0),
                        new DirectedPoint(to.x, to.y, to.z, Math.PI, 0, -Math.PI / 2)).size());
        assertEquals("Number of points is 64", 64, Bezier.bezier(from, control1, control2, to).size());
        control1 = new OtsPoint3d(5, 0, 10);
        control2 = new OtsPoint3d(0, 5, 20);
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine3d line = Bezier.cubic(n, from, control1, control2, to);
            for (int i = 1; i < line.size() - 1; i++)
            {
                OtsPoint3d p = line.get(i);
                // System.out.println("Point " + i + " of " + n + " is " + p);
                assertTrue("z of intermediate point has reasonable value", p.z > line.get(i - 1).z && p.z < line.get(i + 1).z);
                assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 10);
                assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 10);
            }
        }
        for (int n : new int[] {2, 3, 4, 100})
        {
            OtsLine3d line = Bezier.cubic(n, new DirectedPoint(from.x, from.y, from.z, Math.PI / 2, Math.PI / 2, Math.PI),
                    new DirectedPoint(to.x, to.y, to.z, 0, 0, Math.PI / 2));
            for (int i = 1; i < line.size() - 1; i++)
            {
                OtsPoint3d p = line.get(i);
                assertTrue("z of intermediate point has reasonable value", p.z > line.get(i - 1).z && p.z < line.get(i + 1).z);
                assertTrue("x of intermediate point has reasonable value", p.x > 0 && p.x < 10);
                assertTrue("y of intermediate point has reasonable value", p.y > 0 && p.y < 10);
            }
        }
    }

}
