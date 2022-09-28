package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Test the OTSShape class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class OTSShapeTest
{
    /**
     * Test the OTSShape class.
     * @throws OTSGeometryException if this happens uncaught; this test has failed
     */
    @Test
    public final void testOTSShape() throws OTSGeometryException
    {
        OTSPoint3D p1 = new OTSPoint3D(1, 2, 3);
        OTSPoint3D p2 = new OTSPoint3D(10, 2, 3);
        OTSPoint3D p3 = new OTSPoint3D(11, 22, 3);

        OTSShape s = new OTSShape(p1, p2, p3);
        verifyShape(s, true, p1, p2, p3);
        s = new OTSShape(new Coordinate[] {p1.getCoordinate(), p2.getCoordinate(), p3.getCoordinate()});
        verifyShape(s, true, p1, p2, p3);
        GeometryFactory factory = new GeometryFactory();
        CoordinateSequence cs =
                new CoordinateArraySequence(new Coordinate[] {p1.getCoordinate(), p2.getCoordinate(), p3.getCoordinate()});
        LineString ls = new LineString(cs, factory);
        s = new OTSShape(ls);
        verifyShape(s, true, p1, p2, p3);
        Geometry g = ls;
        s = new OTSShape(g);
        verifyShape(s, true, p1, p2, p3);
        List<OTSPoint3D> list = new ArrayList<>();
        list.add(p1);
        list.add(p2);
        list.add(p3);
        s = new OTSShape(list);
        verifyShape(s, true, p1, p2, p3);
        Path2D path = new Path2D.Double();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        s = new OTSShape(path);
        verifyShape(s, false, p1, p2, p3);
        Path2D shape = s.getShape();
        PathIterator pi = shape.getPathIterator(null);
        assertFalse(pi.isDone());
        double[] coords = new double[6];
        assertEquals("must be SEG_MOVETO", PathIterator.SEG_MOVETO, pi.currentSegment(coords));
        assertEquals("x", p1.x, coords[0], 0.00001);
        assertEquals("y", p1.y, coords[1], 0.00001);
        pi.next();
        assertFalse(pi.isDone());
        assertEquals("must be SEG_LINETO", PathIterator.SEG_LINETO, pi.currentSegment(coords));
        assertEquals("x", p2.x, coords[0], 0.00001);
        assertEquals("y", p2.y, coords[1], 0.00001);
        pi.next();
        assertFalse(pi.isDone());
        assertEquals("must be SEG_LINETO", PathIterator.SEG_LINETO, pi.currentSegment(coords));
        assertEquals("x", p3.x, coords[0], 0.00001);
        assertEquals("y", p3.y, coords[1], 0.00001);
        pi.next();
        assertFalse(pi.isDone());
        assertEquals("must be SEG_CLOSE", PathIterator.SEG_CLOSE, pi.currentSegment(coords));
        pi.next();
        assertTrue(pi.isDone());
        Path2D shape2 = s.getShape();
        assertEquals("repeated get returns same thing", shape, shape2);
        OTSPoint3D insidePoint = new OTSPoint3D(5, 5, 5);
        assertTrue("shape contains inSide point", s.contains(insidePoint));
        OTSPoint3D outsidePoint = new OTSPoint3D(-1, -2, -3);
        assertFalse("shape does not contains unrelated point", s.contains(outsidePoint));
        outsidePoint = new OTSPoint3D(0, 5, 5);
        assertFalse("shape does not contains unrelated point", s.contains(outsidePoint));
        outsidePoint = new OTSPoint3D(12, 5, 5);
        assertFalse("shape does not contains unrelated point", s.contains(outsidePoint));
        outsidePoint = new OTSPoint3D(5, 30, 5);
        assertFalse("shape does not contains unrelated point", s.contains(outsidePoint));
        Rectangle2D inRect = new Rectangle2D.Double(5, 5, 1, 1);
        assertTrue("rectangle is inside shape", s.contains(inRect));
        Rectangle2D partlyOutRect = new Rectangle2D.Double(5, 5, 20, 1);
        assertFalse("Rectangle is not fully inside shape", s.contains(partlyOutRect));
        partlyOutRect = new Rectangle2D.Double(5, 5, 1, 30);
        assertFalse("Rectangle is not fully inside shape", s.contains(partlyOutRect));
        assertTrue("toString result contains class name", s.toString().contains("OTSShape"));
        path.closePath();
        OTSShape s2 = new OTSShape(path);
        // System.out.println("s: " + s);
        // System.out.println("s2: " + s2);
        assertNotEquals("shape from closed path is not equal to shape from unclosed path", s, s2);
        assertEquals("Size of shape from closed path is one longer than size of shape from unclosed path", s.size() + 1,
                s2.size());
        for (int index = 0; index < s.size(); index++)
        {
            assertEquals("point at index matches", s.getCoordinates()[index], s2.getCoordinates()[index]);
        }
        assertEquals("Last coordinate of closed shape equals first coordinate", s2.getCoordinates()[0],
                s2.getCoordinates()[s2.size() - 1]);
    }

    /**
     * Test the intersects OTSShape method.
     * @throws OTSGeometryException if that happens this test has failed
     */
    @Test
    public final void testIntersects() throws OTSGeometryException
    {
        double radius = 10;
        double cx = 5;
        double cy = -5;
        OTSShape reference = new OTSShape(makePolygon(cx, cy, radius, 18));
        for (int dx = -20; dx <= 20; dx++)
        {
            for (int dy = -20; dy <= 20; dy++)
            {
                boolean hit = true;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double radius2 = 2;
                if (distance > radius + radius2)
                {
                    hit = false;
                }
                else if (distance > radius + radius2 - 0.1)
                {
                    continue; // too close to be sure
                }
                OTSShape other = new OTSShape(makePolygon(cx + dx, cy + dy, radius2, 16));
                if (hit)
                {
                    assertTrue("shapes hit", reference.intersects(other));
                }
                else
                {
                    assertFalse("shapes do not hit", reference.intersects(other));
                }
            }
        }
        reference =
                new OTSShape(new OTSPoint3D[] {new OTSPoint3D(0, 0, 0), new OTSPoint3D(10, 0, 0), new OTSPoint3D(10, 10, 0)});
        // Make shapes that overlap along the X axis
        for (int dx = -20; dx <= 20; dx++)
        {
            OTSShape other = new OTSShape(
                    new OTSPoint3D[] {new OTSPoint3D(dx, 0, 0), new OTSPoint3D(dx + 5, 0, 0), new OTSPoint3D(dx, -20, 0)});
            boolean hit = dx >= -5 && dx <= 10;
            if (hit)
            {
                assertTrue("shapes hit", reference.intersects(other));
            }
            else
            {
                assertFalse("shapes do not hit", reference.intersects(other));
            }
        }
        // Make shapes that overlap along the Y axis
        for (int dy = -20; dy <= 20; dy++)
        {
            OTSShape other = new OTSShape(
                    new OTSPoint3D[] {new OTSPoint3D(20, dy, 0), new OTSPoint3D(10, dy, 0), new OTSPoint3D(10, dy + 10, 0)});
            boolean hit = dy >= -10 && dy <= 10;
            if (hit)
            {
                assertTrue("shapes hit", reference.intersects(other));
            }
            else
            {
                assertFalse("shapes do not hit", reference.intersects(other));
            }
        }
        // Make vertical and horizontal box
        OTSShape vertical = new OTSShape(new OTSPoint3D[] {new OTSPoint3D(-1, -10, 0), new OTSPoint3D(1, -10, 0),
                new OTSPoint3D(1, 10, 0), new OTSPoint3D(-1, 10, 0), new OTSPoint3D(-1, -10, 0)});
        OTSShape horizontal = new OTSShape(new OTSPoint3D[] {new OTSPoint3D(-10, -1, 0), new OTSPoint3D(10, -1, 0),
                new OTSPoint3D(10, 1, 0), new OTSPoint3D(-10, 1, 0), new OTSPoint3D(-10, -1, 0)});
        assertTrue("shapes hit", vertical.intersects(horizontal));
    }

    /**
     * Test the create and clean constructors.
     * @throws OTSGeometryException should not happen uncaught
     */
    @Test
    public final void testCleanConstructors() throws OTSGeometryException
    {
        try
        {
            OTSShape.createAndCleanOTSShape(new OTSPoint3D[] {});
            fail("empty array should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }
        try
        {
            OTSShape.createAndCleanOTSShape(new OTSPoint3D[] {new OTSPoint3D(1, 2, 3)});
            fail("array of one point should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }
        try
        {
            OTSShape.createAndCleanOTSShape(new OTSPoint3D[] {new OTSPoint3D(1, 2, 3), new OTSPoint3D(1, 2, 3)});
            fail("array of two identical points should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }
        try
        {
            OTSShape.createAndCleanOTSShape(
                    new OTSPoint3D[] {new OTSPoint3D(1, 2, 3), new OTSPoint3D(1, 2, 3), new OTSPoint3D(1, 2, 3)});
            fail("array of three identical points should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }
        // FIXME: No geometry exception if thrown for two points that only differ in Z
        OTSShape.createAndCleanOTSShape(new OTSPoint3D[] {new OTSPoint3D(1, 2, 3), new OTSPoint3D(1, 2, 1)});
        OTSShape.createAndCleanOTSShape(
                new OTSPoint3D[] {new OTSPoint3D(1, 2, 3), new OTSPoint3D(1, 3, 3), new OTSPoint3D(1, 2, 3)});
        List<OTSPoint3D> points = new ArrayList<>();
        points.add(new OTSPoint3D(1, 2, 3));
        points.add(new OTSPoint3D(1, 2, 3));
        points.add(new OTSPoint3D(1, 2, 3));
        points.add(new OTSPoint3D(1, 2, 3));
        try
        {
            OTSShape.createAndCleanOTSShape(points);
            fail("list of four identical points should have thrown an OTSGeometryException");
        }
        catch (OTSGeometryException oge)
        {
            // Ignore expected exception
        }
        // FIXME: do we really want this behavior?
        assertEquals("list now has only one point", 1, points.size());
    }

    /**
     * Construct a list of OTSPoin3D spread out regularly over a circle.
     * @param centerX double; center X of the circle
     * @param centerY double; center Y of the circle
     * @param radius double; radius of the circle
     * @param size int; number of points in the polygon
     * @return List&lt;OTSPoin3D&gt;; the points that lie on a regular polygon
     * @throws OTSGeometryException if the number of points is too small or the radius is 0
     */
    private List<OTSPoint3D> makePolygon(final double centerX, final double centerY, final double radius, final int size)
            throws OTSGeometryException
    {
        List<OTSPoint3D> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            double angle = Math.PI * 2 * i / size;
            points.add(new OTSPoint3D(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle)));
        }
        return points;
    }

    /**
     * Verify that an OTSShape contains exactly the given points in the given order.
     * @param shape OTSShape; the shape to test
     * @param verifyZ boolean; if true; also check the Z coordinates; if false; check that all Z coordinates are 0
     * @param points OTSPoint3D...; the points to expect
     * @throws OTSGeometryException if that happens; this test has failed
     */
    private void verifyShape(final OTSShape shape, final boolean verifyZ, final OTSPoint3D... points)
            throws OTSGeometryException
    {
        assertEquals("shape contains correct number of points", shape.size(), points.length);
        for (int i = 0; i < points.length; i++)
        {
            assertEquals("point 1 matches x", points[i].x, shape.get(i).x, 0.0001);
            assertEquals("point 1 matches y", points[i].y, shape.get(i).y, 0.0001);
            if (verifyZ)
            {
                assertEquals("point 1 matches z", points[i].z, shape.get(i).z, 0.0001);
            }
            else
            {
                assertEquals("point 1 z is 0", 0, shape.get(i).z, 0.0000001);
            }
        }
    }

}
