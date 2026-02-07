package org.opentrafficsim.base.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;

/**
 * Tests OtsShape implementations.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ShapeTest
{

    /** */
    private ShapeTest()
    {
        // do not instantiate test class
    }

    /**
     * Tests CircleShape, and equal RoundedRectangleShape.
     */
    @Test
    public void testCircle()
    {
        for (double r : new double[] {1.0, 2.0, 4.0})
        {
            testSingleCircle(r, new CircleShape(r)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            });
            testSingleCircle(r, new RoundedRectangleShape(r * 2.0, r * 2.0, r)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            });
        }
        try
        {
            new CircleShape(0.0)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            };
            fail("CircleShape with 0.0 radius should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {
            //
        }
    }

    /**
     * Test single circle.
     * @param r radius
     * @param circle shape
     */
    private void testSingleCircle(final double r, final OtsShape circle)
    {
        double p = r * .5;
        circle.toString();

        // signed distance
        assertEquals(p - r, circle.signedDistance(p, 0.0), 0.001);
        assertEquals(p - r, circle.signedDistance(0.0, p), 0.001);
        assertEquals(p - r, circle.signedDistance(-p, 0.0), 0.001);
        assertEquals(p - r, circle.signedDistance(0.0, -p), 0.001);
        assertEquals(Math.sqrt(2.0 * p * p) - r, circle.signedDistance(p, p), 0.001);
        assertEquals(Math.sqrt(2.0 * p * p) - r, circle.signedDistance(-p, p), 0.001);
        assertEquals(Math.sqrt(2.0 * p * p) - r, circle.signedDistance(p, -p), 0.001);
        assertEquals(Math.sqrt(2.0 * p * p) - r, circle.signedDistance(-p, -p), 0.001);
        assertEquals(p, circle.signedDistance(r + p, 0.0), 0.001);
        assertEquals(p, circle.signedDistance(0.0, r + p), 0.001);
        assertEquals(p, circle.signedDistance(-r - p, 0.0), 0.001);
        assertEquals(p, circle.signedDistance(0.0, -r - p), 0.001);
        assertEquals(Math.sqrt(2.0 * (r + p) * (r + p)) - r, circle.signedDistance(r + p, r + p), 0.001);
        assertEquals(Math.sqrt(2.0 * (r + p) * (r + p)) - r, circle.signedDistance(-r - p, r + p), 0.001);
        assertEquals(Math.sqrt(2.0 * (r + p) * (r + p)) - r, circle.signedDistance(r + p, -r - p), 0.001);
        assertEquals(Math.sqrt(2.0 * (r + p) * (r + p)) - r, circle.signedDistance(-r - p, -r - p), 0.001);

        // points in and out
        assertTrue(circle.contains(p, 0.0));
        assertTrue(circle.contains(0.0, p));
        assertTrue(circle.contains(-p, 0.0));
        assertTrue(circle.contains(0.0, -p));
        assertFalse(circle.contains(r + p, 0.0));
        assertFalse(circle.contains(0.0, r + p));
        assertFalse(circle.contains(-r - p, 0.0));
        assertFalse(circle.contains(0.0, -r - p));

        // points in and out but based on polygon
        // SKL 2024.10.21, 0.01 is added to avoid bug https://github.com/averbraeck/djutils/issues/15 (fix not yet
        // published)
        assertTrue(circle.getRelativeContour().contains(new Point2d(p + 0.01, 0.01)));
        assertTrue(circle.getRelativeContour().contains(new Point2d(0.01, p + 0.01)));
        assertTrue(circle.getRelativeContour().contains(new Point2d(-p + 0.01, 0.01)));
        assertTrue(circle.getRelativeContour().contains(new Point2d(0.01, -p + 0.01)));
        assertFalse(circle.getRelativeContour().contains(new Point2d(r + p + 0.01, 0.01)));
        assertFalse(circle.getRelativeContour().contains(new Point2d(0.01, r + p + 0.01)));
        assertFalse(circle.getRelativeContour().contains(new Point2d(-r - p + 0.01, 0.01)));
        assertFalse(circle.getRelativeContour().contains(new Point2d(0.01, -r - p + 0.01)));

        // bounds
        assertEquals(2.0 * r, circle.getRelativeBounds().getDeltaX(), 0.001);
        assertEquals(2.0 * r, circle.getRelativeBounds().getDeltaY(), 0.001);
        assertEquals(-r, circle.getRelativeBounds().getMinX(), 0.001);
        assertEquals(r, circle.getRelativeBounds().getMaxX(), 0.001);
        assertEquals(-r, circle.getRelativeBounds().getMinY(), 0.001);
        assertEquals(r, circle.getRelativeBounds().getMaxY(), 0.001);
        assertEquals(0.0, circle.getRelativeBounds().midPoint().x, 0.001);
        assertEquals(0.0, circle.getRelativeBounds().midPoint().y, 0.001);
    }

    /**
     * Tests RectangleShape, and equal RoundedRectangleShape.
     */
    @Test
    public void testRectangle()
    {
        for (double r1 : new double[] {1.0, 2.0, 4.0})
        {
            double r2 = r1 * 2.0;
            testSingleRectangle(r1, r2, 0.0, 0.0, new RectangleShape(r1 * 2.0, r2 * 2.0)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            });
            testSingleRectangle(r1, r2, 0.0, 0.0, new RoundedRectangleShape(r1 * 2.0, r2 * 2.0, 0.0)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            });
            testSingleRectangle(r1, r2, 1.0, 1.0, new OffsetRectangleShape(-r1 + 1.0, r1 + 1.0, -r2 + 1.0, r2 + 1.0)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            });
        }
        try
        {
            new RoundedRectangleShape(1.0, 2.0, 3.0)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            };
            fail("RoundedRectangleShape with large radius to 'eat up' dx and dy should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {
            //
        }
        try
        {
            new RoundedRectangleShape(1.0, 2.0, -1.0)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            };
            fail("RoundedRectangleShape with negative radius should throw IllegalArgumentException.");
        }
        catch (IllegalArgumentException ex)
        {
            //
        }
        new RoundedRectangleShape(1.0, 2.0, 1.5)
        {
            @Override
            public DirectedPoint2d getLocation()
            {
                return new DirectedPoint2d(0.0, 0.0, 0.0);
            }
        }.getRelativeContour(); // non-complete quarter circle corners due to relative large r
    }

    /**
     * Tests shape as rectangle.
     * @param r1 horizontal extent
     * @param r2 vertical extent
     * @param dx x shift for OffsetRectangleShape
     * @param dy y shift for OffsetRectangleShape
     * @param rectangle shape
     */
    private void testSingleRectangle(final double r1, final double r2, final double dx, final double dy,
            final OtsShape rectangle)
    {
        double p = r1 * .5;
        rectangle.toString();

        // signed distance
        assertEquals(p - r1, rectangle.signedDistance(new Point2d(p + dx, 0.0 + dy)), 0.001);
        assertEquals(-r1, rectangle.signedDistance(new Point2d(0.0 + dx, p + dy)), 0.001);
        assertEquals(p - r1, rectangle.signedDistance(new Point2d(-p + dx, 0.0 + dy)), 0.001);
        assertEquals(-r1, rectangle.signedDistance(new Point2d(0.0 + dx, -p + dy)), 0.001);
        assertEquals(Math.max(p - r2, p - r1), rectangle.signedDistance(new Point2d(p + dx, p + dy)), 0.001);
        assertEquals(Math.max(p - r2, p - r1), rectangle.signedDistance(new Point2d(-p + dx, p + dy)), 0.001);
        assertEquals(Math.max(p - r2, p - r1), rectangle.signedDistance(new Point2d(p + dx, -p + dy)), 0.001);
        assertEquals(Math.max(p - r2, p - r1), rectangle.signedDistance(new Point2d(-p + dx, -p + dy)), 0.001);
        assertEquals(p, rectangle.signedDistance(new Point2d(r1 + p + dx, 0.0 + dy)), 0.001);
        assertEquals(p, rectangle.signedDistance(new Point2d(0.0 + dx, r2 + p + dy)), 0.001);
        assertEquals(p, rectangle.signedDistance(new Point2d(-r1 - p + dx, 0.0 + dy)), 0.001);
        assertEquals(p, rectangle.signedDistance(new Point2d(0.0 + dx, -r2 - p + dy)), 0.001);
        assertEquals(Math.hypot(p, p), rectangle.signedDistance(new Point2d(r1 + p + dx, r2 + p + dy)), 0.001);
        assertEquals(Math.hypot(p, p), rectangle.signedDistance(new Point2d(-r1 - p + dx, r2 + p + dy)), 0.001);
        assertEquals(Math.hypot(p, p), rectangle.signedDistance(new Point2d(r1 + p + dx, -r2 - p + dy)), 0.001);
        assertEquals(Math.hypot(p, p), rectangle.signedDistance(new Point2d(-r1 - p + dx, -r2 - p + dy)), 0.001);

        // points in and out
        assertTrue(rectangle.contains(new Point2d(p + dx, 0.0 + dy)));
        assertTrue(rectangle.contains(new Point2d(0.0 + dx, p + dy)));
        assertTrue(rectangle.contains(new Point2d(-p + dx, 0.0 + dy)));
        assertTrue(rectangle.contains(new Point2d(0.0 + dx, -p + dy)));
        assertFalse(rectangle.contains(new Point2d(r1 + dx + p, 0.0 + dy)));
        assertFalse(rectangle.contains(new Point2d(0.0 + dx, r2 + p + dy)));
        assertFalse(rectangle.contains(new Point2d(-r1 + dx - p, 0.0 + dy)));
        assertFalse(rectangle.contains(new Point2d(0.0 + dx, -r2 - p + dy)));

        // points in and out but based on polygon
        assertTrue(rectangle.getRelativeContour().contains(new Point2d(p + dx, 0.0 + dy)));
        assertTrue(rectangle.getRelativeContour().contains(new Point2d(0.0 + dx, p + dy)));
        assertTrue(rectangle.getRelativeContour().contains(new Point2d(-p + dx, 0.0 + dy)));
        assertTrue(rectangle.getRelativeContour().contains(new Point2d(0.0 + dx, -p + dy)));
        assertFalse(rectangle.getRelativeContour().contains(new Point2d(r1 + p + dx, 0.0 + dy)));
        assertFalse(rectangle.getRelativeContour().contains(new Point2d(0.0 + dx, r2 + p + dy)));
        assertFalse(rectangle.getRelativeContour().contains(new Point2d(-r1 - p + dx, 0.0 + dy)));
        assertFalse(rectangle.getRelativeContour().contains(new Point2d(0.0 + dx, -r2 - p + dy)));

        // bounds
        assertEquals(2.0 * r1, rectangle.getRelativeBounds().getDeltaX(), 0.001);
        assertEquals(2.0 * r2, rectangle.getRelativeBounds().getDeltaY(), 0.001);
        assertEquals(-r1 + dx, rectangle.getRelativeBounds().getMinX(), 0.001);
        assertEquals(r1 + dx, rectangle.getRelativeBounds().getMaxX(), 0.001);
        assertEquals(-r2 + dy, rectangle.getRelativeBounds().getMinY(), 0.001);
        assertEquals(r2 + dy, rectangle.getRelativeBounds().getMaxY(), 0.001);
        assertEquals(dx, rectangle.getRelativeBounds().midPoint().x, 0.001);
        assertEquals(dy, rectangle.getRelativeBounds().midPoint().y, 0.001);
    }

    /**
     * Test polygon.
     */
    @Test
    public void testPolygon()
    {
        for (double r : new double[] {1.0, 2.0, 4.0})
        {
            double r2 = r * 0.25;
            double p = r * 0.5;

            PolygonShape polygon = new PolygonShape(new Polygon2d(new double[] {r, r2, 0.0, -r2, -r, -r2, 0.0, r2},
                    new double[] {0.0, r2, r, r2, 0.0, -r2, -r, -r2}))
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return new DirectedPoint2d(0.0, 0.0, 0.0);
                }
            };
            polygon.toString();

            // signed distance
            assertEquals(-Math.hypot(r2, r2), polygon.signedDistance(new Point2d(0.0, 0.0 + 0.00001)), 0.001);
            assertEquals(p, polygon.signedDistance(new Point2d(r + p, 0.0)), 0.001);
            assertEquals(p, polygon.signedDistance(new Point2d(0.0, r + p)), 0.001);
            assertEquals(p, polygon.signedDistance(new Point2d(-r - p, 0.0)), 0.001);
            assertEquals(p, polygon.signedDistance(new Point2d(0.0, -r - p)), 0.001);

            // points in and out
            assertTrue(polygon.contains(new Point2d(p, 0.0 + 0.01)));
            assertTrue(polygon.contains(new Point2d(0.0, p + 0.01)));
            assertTrue(polygon.contains(new Point2d(-p, 0.0 + 0.01)));
            assertTrue(polygon.contains(new Point2d(0.0, -p + 0.01)));
            assertFalse(polygon.contains(new Point2d(r + p, 0.0 + 0.01)));
            assertFalse(polygon.contains(new Point2d(0.0, r + p + 0.01)));
            assertFalse(polygon.contains(new Point2d(-r - p, 0.0 + 0.01)));
            assertFalse(polygon.contains(new Point2d(0.0, -r - p + 0.01)));

            // bounds
            assertEquals(2.0 * r, polygon.getRelativeBounds().getDeltaX(), 0.001);
            assertEquals(2.0 * r, polygon.getRelativeBounds().getDeltaY(), 0.001);
            assertEquals(-r, polygon.getRelativeBounds().getMinX(), 0.001);
            assertEquals(r, polygon.getRelativeBounds().getMaxX(), 0.001);
            assertEquals(-r, polygon.getRelativeBounds().getMinY(), 0.001);
            assertEquals(r, polygon.getRelativeBounds().getMaxY(), 0.001);
            assertEquals(0.0, polygon.getRelativeBounds().midPoint().x, 0.001);
            assertEquals(0.0, polygon.getRelativeBounds().midPoint().y, 0.001);
        }
    }

}
