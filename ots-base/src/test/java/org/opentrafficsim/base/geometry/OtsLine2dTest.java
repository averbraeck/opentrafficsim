package org.opentrafficsim.base.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.DrawRuntimeException;
import org.djutils.draw.Export;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsLine2d.FractionalFallback;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class OtsLine2dTest
{

    /** Verbose test. */
    private static final boolean VERBOSE = false;

    /**
     * Constructor.
     */
    public OtsLine2dTest()
    {
        //
    }

    /**
     * Test the constructors of OtsLine2d.
     */
    @Test
    public final void constructorsTest()
    {
        double[] values = {-999, 0, 99, 9999}; // Keep this list short; execution time grows with 9th power of length
        Point2d[] points = new Point2d[0]; // Empty array
        try
        {
            runConstructors(points);
            fail("Should have thrown a NetworkException");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        for (double x0 : values)
        {
            for (double y0 : values)
            {
                points = new Point2d[1]; // Degenerate array holding one point
                points[0] = new Point2d(x0, y0);
                try
                {
                    runConstructors(points);
                    fail("Should have thrown a NetworkException");
                }
                catch (DrawRuntimeException exception)
                {
                    // Ignore expected exception
                }
                for (double x1 : values)
                {
                    for (double y1 : values)
                    {
                        points = new Point2d[2]; // Straight line; two points
                        points[0] = new Point2d(x0, y0);
                        points[1] = new Point2d(x1, y1);
                        if (0 == points[0].distance(points[1]))
                        {
                            try
                            {
                                runConstructors(points);
                                fail("Should have thrown a NetworkException");
                            }
                            catch (DrawRuntimeException exception)
                            {
                                // Ignore expected exception
                            }
                        }
                        else
                        {
                            runConstructors(points);
                            for (double x2 : values)
                            {
                                for (double y2 : values)
                                {
                                    points = new Point2d[3]; // Line with intermediate point
                                    points[0] = new Point2d(x0, y0);
                                    points[1] = new Point2d(x1, y1);
                                    points[2] = new Point2d(x2, y2);
                                    if (0 == points[1].distance(points[2]))
                                    {
                                        try
                                        {
                                            runConstructors(points);
                                            fail("Should have thrown a NetworkException");
                                        }
                                        catch (DrawRuntimeException exception)
                                        {
                                            // Ignore expected exception
                                        }
                                    }
                                    else
                                    {
                                        runConstructors(points);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Test all the constructors of Point2d.
     * @param points array of Point2d to test with
     */
    private void runConstructors(final Point2d[] points)
    {
        verifyPoints(new OtsLine2d(points), points);
        List<Point2d> list = new ArrayList<>();
        for (int i = 0; i < points.length; i++)
        {
            list.add(points[i]);
        }
        OtsLine2d line = new OtsLine2d(list);
        verifyPoints(line, points);
        // Convert it to Point2d[], create another OtsLine2d from that and check that
        verifyPoints(new OtsLine2d(line.iterator()), points);
        double length = 0;
        for (int i = 1; i < points.length; i++)
        {
            length += Math.sqrt(Math.pow(points[i].x - points[i - 1].x, 2) + Math.pow(points[i].y - points[i - 1].y, 2));
        }
        assertEquals(length, line.getLength(), 10 * Math.ulp(length), "length");
        assertEquals(length, line.getLength(), 10 * Math.ulp(length), "length");
        assertEquals(length, line.getLength(), 10 * Math.ulp(length), "length");
        assertEquals(length, line.getLength(), 10 * Math.ulp(length), "length");
    }

    /**
     * Print a Path2D to the console.
     * @param path the path
     */
    public final void printPath2D(final Path2D path)
    {
        PathIterator pi = path.getPathIterator(null);
        double[] p = new double[6];
        while (!pi.isDone())
        {
            int segType = pi.currentSegment(p);
            if (segType == PathIterator.SEG_MOVETO)
            {
                System.out.print(" move to " + new Point2d(p[0], p[1]));
            }
            if (segType == PathIterator.SEG_LINETO)
            {
                System.out.print(" line to " + new Point2d(p[0], p[1]));
            }
            else if (segType == PathIterator.SEG_CLOSE)
            {
                System.out.print(" close");
            }
            pi.next();
        }
        System.out.println("");
    }

    /**
     * Verify that a OtsLine2d contains the same points as an array of Point2d.
     * @param line the OTS line
     * @param points the OTSPoint array
     */
    private void verifyPoints(final OtsLine2d line, final Point2d[] points)
    {
        assertEquals(line.size(), points.length, "Line should have same number of points as point array");
        for (int i = 0; i < points.length; i++)
        {
            assertEquals(points[i].x, line.get(i).x, Math.ulp(points[i].x), "x of point i should match");
            assertEquals(points[i].y, line.get(i).y, Math.ulp(points[i].y), "y of point i should match");
        }
    }

    /**
     * Test that exception is thrown when it should be.
     */
    @Test
    public final void exceptionTest()
    {
        OtsLine2d line = new OtsLine2d(new Point2d[] {new Point2d(1, 2), new Point2d(4, 5)});
        try
        {
            line.get(-1);
            fail("Should have thrown an exception");
        }
        catch (IndexOutOfBoundsException oe)
        {
            // Ignore expected exception
        }
        try
        {
            line.get(2);
            fail("Should have thrown an exception");
        }
        catch (IndexOutOfBoundsException oe)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the getLocationExtended method and friends.
     */
    @Test
    public final void locationExtendedTest()
    {
        Point2d p0 = new Point2d(10, 20);
        Point2d p1 = new Point2d(40, 50);
        Point2d p2 = new Point2d(90, 80);
        OtsLine2d l = new OtsLine2d(new Point2d[] {p0, p1, p2});
        checkGetLocation(l, -10, null, Math.atan2(p1.y - p0.y, p1.x - p0.x));
        checkGetLocation(l, -0.0001, p0, Math.atan2(p1.y - p0.y, p1.x - p0.x));
        checkGetLocation(l, 0, p0, Math.atan2(p1.y - p0.y, p1.x - p0.x));
        checkGetLocation(l, 0.0001, p0, Math.atan2(p1.y - p0.y, p1.x - p0.x));
        checkGetLocation(l, 0.9999, p2, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        checkGetLocation(l, 1, p2, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        checkGetLocation(l, 1.0001, p2, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        checkGetLocation(l, 10, null, Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    /**
     * Check the location returned by the various location methods.
     * @param line the line
     * @param fraction relative position to check
     * @param expectedPoint expected location of the result
     * @param expectedZRotation expected Z rotation of the result
     */
    private void checkGetLocation(final OtsLine2d line, final double fraction, final Point2d expectedPoint,
            final double expectedZRotation)
    {
        double length = line.getLength();
        checkDirectedPoint2d(line.getLocationExtendedSI(fraction * length), expectedPoint, expectedZRotation);
        Length typedLength = new Length(fraction * length, LengthUnit.METER);
        checkDirectedPoint2d(line.getLocationExtended(typedLength), expectedPoint, expectedZRotation);
        if (fraction < 0 || fraction > 1)
        {
            try
            {
                line.getLocationSI(fraction * length);
                fail("getLocation should have thrown a OTSGeometryException");
            }
            catch (DrawRuntimeException ne)
            {
                // Ignore expected exception
            }
            try
            {
                line.getLocation(typedLength);
                fail("getLocation should have thrown a OTSGeometryException");
            }
            catch (DrawRuntimeException ne)
            {
                // Ignore expected exception
            }
            try
            {
                line.getLocationPointFraction(fraction);
                fail("getLocation should have thrown a OTSGeometryException");
            }
            catch (DrawRuntimeException ne)
            {
                // Ignore expected exception
            }
        }
        else
        {
            checkDirectedPoint2d(line.getLocationSI(fraction * length), expectedPoint, expectedZRotation);
            checkDirectedPoint2d(line.getLocation(typedLength), expectedPoint, expectedZRotation);
            checkDirectedPoint2d(line.getLocationPointFraction(fraction), expectedPoint, expectedZRotation);
        }

    }

    /**
     * Verify the location and direction of a DirectedPoint2d.
     * @param dp the DirectedPoint2d that should be verified
     * @param expectedPoint the expected location (or null if location should not be checked)
     * @param expectedZRotation the expected Z rotation
     */
    private void checkDirectedPoint2d(final DirectedPoint2d dp, final Point2d expectedPoint, final double expectedZRotation)
    {
        if (null != expectedPoint)
        {
            assertEquals(0, expectedPoint.distance(dp), 0.1, "locationExtendedSI(0) returns approximately expected point");
        }
        assertEquals(expectedZRotation, dp.getDirZ(), 0.001, "z-rotation at 0");
    }

    /**
     * Test getLocation method.
     */
    @Test
    public final void locationTest()
    {
        Point2d p0 = new Point2d(10, 20);
        Point2d p1 = new Point2d(40, 50);
        Point2d p2 = new Point2d(90, 70);
        OtsLine2d l = new OtsLine2d(new Point2d[] {p0, p1, p2});
        Point2d dp = l.getLocation();
        assertEquals(50, dp.x, 0.001, "centroid x");
        assertEquals(45, dp.y, 0.001, "centroid y");
        l = new OtsLine2d(new Point2d[] {p1, p0, p2}); // Some arguments swapped
        dp = l.getLocation();
        assertEquals(50, dp.x, 0.001, "centroid x");
        assertEquals(45, dp.y, 0.001, "centroid y");
        l = new OtsLine2d(new Point2d[] {p0, p1}); // Two points; all in same Z-plane
        dp = l.getLocation();
        assertEquals(25, dp.x, 0.001, "centroid x");
        assertEquals(35, dp.y, 0.001, "centroid y");
    }

    /**
     * Test the createAndCleanOtsLine2d method.
     */
    @Test
    public final void cleanTest()
    {
        Point2d[] tooShort = new Point2d[] {};
        try
        {
            new OtsLine2d(tooShort);
            fail("Array with no points should have thrown an exception");
        }
        catch (DrawRuntimeException ne)
        {
            // Ignore expected exception
        }
        tooShort = new Point2d[] {new Point2d(1, 2)};
        try
        {
            new OtsLine2d(tooShort);
            fail("Array with no points should have thrown an exception");
        }
        catch (DrawRuntimeException ne)
        {
            // Ignore expected exception
        }
        Point2d p0 = new Point2d(1, 2);
        Point2d p1 = new Point2d(4, 5);
        Point2d[] points = new Point2d[] {p0, p1};
        OtsLine2d result = new OtsLine2d(points);
        assertTrue(p0.equals(result.get(0)), "first point is p0");
        assertTrue(p1.equals(result.get(1)), "second point is p1");
        Point2d p1Same = new Point2d(4, 5);
        result = new OtsLine2d(new PolyLine2d(new Point2d[] {p0, p0, p0, p0, p1Same, p0, p1, p1, p1Same, p1, p1}));
        assertEquals(4, result.size(), "result should contain 4 points");
        assertTrue(p0.equals(result.get(0)), "first point is p0");
        assertTrue(p1.equals(result.get(1)), "second point is p1");
        assertTrue(p0.equals(result.get(0)), "third point is p0");
        assertTrue(p1.equals(result.get(1)), "last point is p1");
    }

    /**
     * Test the equals method.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public final void equalsTest()
    {
        Point2d p0 = new Point2d(1.1, 2.2);
        Point2d p1 = new Point2d(2.1, 2.2);
        Point2d p2 = new Point2d(3.1, 2.2);

        OtsLine2d line = new OtsLine2d(new Point2d[] {p0, p1, p2});
        assertTrue(line.equals(line), "OtsLine2d is equal to itself");
        assertFalse(line.equals(null), "OtsLine2d is not equal to null");
        assertFalse(line.equals(new String("hello")), "OtsLine2d is not equals to some other kind of Object");
        OtsLine2d line2 = new OtsLine2d(new Point2d[] {p0, p1, p2});
        assertTrue(line.equals(line2), "OtsLine2d is equal ot other OtsLine2d that has the exact same list of Point2d");
        Point2d p2Same = new Point2d(3.1, 2.2);
        line2 = new OtsLine2d(new Point2d[] {p0, p1, p2Same});
        assertTrue(line.equals(line2),
                "OtsLine2d is equal ot other OtsLine2d that has the exact same list of Point2d; even if some of "
                        + "those point are different instances with the same coordinates");
        Point2d p2NotSame = new Point2d(3.1, 2.25);
        line2 = new OtsLine2d(new Point2d[] {p0, p1, p2NotSame});
        assertFalse(line.equals(line2), "OtsLine2d is not equal ot other OtsLine2d that differs in one coordinate");
        line2 = new OtsLine2d(new Point2d[] {p0, p1, p2, p2NotSame});
        assertFalse(line.equals(line2),
                "OtsLine2d is not equal ot other OtsLine2d that has more points (but is identical up to the common length)");
        assertFalse(line2.equals(line),
                "OtsLine2d is not equal ot other OtsLine2d that has fewer points  (but is identical up to the common length)");
    }

    /**
     * Test the concatenate method.
     */
    @Test
    public final void concatenateTest()
    {
        Point2d p0 = new Point2d(1.1, 2.2);
        Point2d p1 = new Point2d(2.1, 2.2);
        Point2d p2 = new Point2d(3.1, 2.2);
        Point2d p3 = new Point2d(4.1, 2.2);
        Point2d p4 = new Point2d(5.1, 2.2);
        Point2d p5 = new Point2d(6.1, 2.2);

        OtsLine2d l0 = new OtsLine2d(p0, p1, p2);
        OtsLine2d l1 = new OtsLine2d(p2, p3);
        OtsLine2d l2 = new OtsLine2d(p3, p4, p5);
        OtsLine2d ll = OtsLine2d.concatenate(l0, l1, l2);
        assertEquals(6, ll.size(), "size is 6");
        assertEquals(p0, ll.get(0), "point 0 is p0");
        assertEquals(p1, ll.get(1), "point 1 is p1");
        assertEquals(p2, ll.get(2), "point 2 is p2");
        assertEquals(p3, ll.get(3), "point 3 is p3");
        assertEquals(p4, ll.get(4), "point 4 is p4");
        assertEquals(p5, ll.get(5), "point 5 is p5");

        ll = OtsLine2d.concatenate(l1);
        assertEquals(2, ll.size(), "size is 2");
        assertEquals(p2, ll.get(0), "point 0 is p2");
        assertEquals(p3, ll.get(1), "point 1 is p3");

        try
        {
            OtsLine2d.concatenate(l0, l2);
            fail("Gap should have throw an exception");
        }
        catch (DrawRuntimeException e)
        {
            // Ignore expected exception
        }
        try
        {
            OtsLine2d.concatenate();
            fail("concatenate of empty list should have thrown an exception");
        }
        catch (DrawRuntimeException e)
        {
            // Ignore expected exception
        }

        // Test concatenate methods with tolerance
        OtsLine2d thirdLine = new OtsLine2d(p4, p5);
        for (double tolerance : new double[] {0.1, 0.01, 0.001, 0.0001, 0.00001})
        {
            for (double actualError : new double[] {tolerance * 0.9, tolerance * 1.1})
            {
                int maxDirection = 10;
                for (int direction = 0; direction < maxDirection; direction++)
                {
                    double dx = actualError * Math.cos(Math.PI * 2 * direction / maxDirection);
                    double dy = actualError * Math.sin(Math.PI * 2 * direction / maxDirection);
                    OtsLine2d otherLine = new OtsLine2d(new Point2d(p2.x + dx, p2.y + dy), p3, p4);
                    if (actualError < tolerance)
                    {
                        try
                        {
                            OtsLine2d.concatenate(tolerance, l0, otherLine);
                        }
                        catch (DrawRuntimeException oge)
                        {
                            OtsLine2d.concatenate(tolerance, l0, otherLine);
                            fail("concatenation with error " + actualError + " and tolerance " + tolerance
                                    + " should not have failed");
                        }
                        try
                        {
                            OtsLine2d.concatenate(tolerance, l0, otherLine, thirdLine);
                        }
                        catch (DrawRuntimeException oge)
                        {
                            fail("concatenation with error " + actualError + " and tolerance " + tolerance
                                    + " should not have failed");
                        }
                    }
                    else
                    {
                        try
                        {
                            OtsLine2d.concatenate(tolerance, l0, otherLine);
                        }
                        catch (DrawRuntimeException oge)
                        {
                            // Ignore expected exception
                        }
                        try
                        {
                            OtsLine2d.concatenate(tolerance, l0, otherLine, thirdLine);
                        }
                        catch (DrawRuntimeException oge)
                        {
                            // Ignore expected exception
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the reverse method.
     */
    @Test
    public final void reverseTest()
    {
        Point2d p0 = new Point2d(1.1, 2.2);
        Point2d p1 = new Point2d(2.1, 2.2);
        Point2d p2 = new Point2d(3.1, 2.2);
        Point2d p3 = new Point2d(4.1, 2.2);
        Point2d p4 = new Point2d(5.1, 2.2);
        Point2d p5 = new Point2d(6.1, 2.2);

        OtsLine2d l01 = new OtsLine2d(p0, p1);
        OtsLine2d r = l01.reverse();
        assertEquals(2, r.size(), "result has size 2");
        assertEquals(p1, r.get(0), "point 0 is p1");
        assertEquals(p0, r.get(1), "point 1 is p0");

        OtsLine2d l05 = new OtsLine2d(p0, p1, p2, p3, p4, p5);
        r = l05.reverse();
        assertEquals(6, r.size(), "result has size 6");
        assertEquals(p5, r.get(0), "point 0 is p5");
        assertEquals(p4, r.get(1), "point 1 is p4");
        assertEquals(p3, r.get(2), "point 2 is p3");
        assertEquals(p2, r.get(3), "point 3 is p2");
        assertEquals(p1, r.get(4), "point 4 is p1");
        assertEquals(p0, r.get(5), "point 5 is p0");

    }

    /**
     * Test the extract and extractFraction methods.
     */
    @SuppressWarnings("checkstyle:methodlength")
    @Test
    public final void extractTest()
    {
        Point2d p0 = new Point2d(1, 2);
        Point2d p1 = new Point2d(2, 3);
        Point2d p1a = new Point2d(2.01, 3.01);
        Point2d p1b = new Point2d(2.02, 3.02);
        Point2d p1c = new Point2d(2.03, 3.03);
        Point2d p2 = new Point2d(12, 13);

        OtsLine2d l = new OtsLine2d(p0, p1);
        OtsLine2d e = l.extractFractional(0, 1);
        assertEquals(2, e.size(), "size of extraction is 2");
        assertEquals(p0, e.get(0), "point 0 is p0");
        assertEquals(p1, e.get(1), "point 1 is p1");
        try
        {
            l.extractFractional(-0.1, 1);
            fail("negative start should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(Double.NaN, 1);
            fail("NaN start should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(0, 1.1);
            fail("end > 1 should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(0, Double.NaN);
            fail("NaN end should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(0.6, 0.4);
            fail("start > end should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(-0.1, 1);
            fail("negative start should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(Double.NaN, 1);
            fail("NaN start should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(0, l.getLength() + 0.1);
            fail("end > length should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(0, Double.NaN);
            fail("NaN end should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(0.6, 0.4);
            fail("start > end should have thrown an exception");
        }
        catch (DrawRuntimeException exception)
        {
            // Ignore expected exception
        }

        for (int i = 0; i < 10; i++)
        {
            for (int j = i + 1; j < 10; j++)
            {
                double start = i * l.getLength() / 10;
                double end = j * l.getLength() / 10;
                // System.err.println("i=" + i + ", j=" + j);
                for (OtsLine2d extractedLine : new OtsLine2d[] {l.extract(start, end),
                        l.extract(new Length(start, LengthUnit.SI), new Length(end, LengthUnit.SI)),
                        l.extractFractional(1.0 * i / 10, 1.0 * j / 10)})
                {
                    assertEquals(2, extractedLine.size(), "size of extract is 2");
                    assertEquals(p0.x + (p1.x - p0.x) * i / 10, extractedLine.get(0).x, 0.0001, "x of 0");
                    assertEquals(p0.y + (p1.y - p0.y) * i / 10, extractedLine.get(0).y, 0.0001, "y of 0");
                    assertEquals(p0.x + (p1.x - p0.x) * j / 10, extractedLine.get(1).x, 0.0001, "x of 1");
                    assertEquals(p0.y + (p1.y - p0.y) * j / 10, extractedLine.get(1).y, 0.0001, "y of 1");
                }
            }
        }

        for (OtsLine2d line : new OtsLine2d[] {new OtsLine2d(p0, p1, p2), new OtsLine2d(p0, p1, p1a, p1b, p1c, p2)})
        {
            for (int i = 0; i < 110; i++)
            {
                if (10 == i)
                {
                    continue; // results are not entirely predictable due to rounding errors
                }
                for (int j = i + 1; j < 110; j++)
                {
                    if (10 == j)
                    {
                        continue; // results are not entirely predictable due to rounding errors
                    }
                    double start = i * line.getLength() / 110;
                    double end = j * line.getLength() / 110;
                    // System.err.println("first length is " + firstLength);
                    // System.err.println("second length is " + line.getLength().si);
                    // System.err.println("i=" + i + ", j=" + j);
                    for (OtsLine2d extractedLine : new OtsLine2d[] {line.extract(start, end),
                            line.extract(new Length(start, LengthUnit.SI), new Length(end, LengthUnit.SI)),
                            line.extractFractional(1.0 * i / 110, 1.0 * j / 110)})
                    {
                        int expectedSize = i < 10 && j > 10 ? line.size() : 2;
                        assertEquals(expectedSize, extractedLine.size(), "size is " + expectedSize);
                        if (i < 10)
                        {
                            assertEquals(p0.x + (p1.x - p0.x) * i / 10, extractedLine.get(0).x, 0.0001, "x of 0");
                            assertEquals(p0.y + (p1.y - p0.y) * i / 10, extractedLine.get(0).y, 0.0001, "y of 0");
                        }
                        else
                        {
                            assertEquals(p1.x + (p2.x - p1.x) * (i - 10) / 100, extractedLine.get(0).x, 0.0001, "x of 0");
                            assertEquals(p1.y + (p2.y - p1.y) * (i - 10) / 100, extractedLine.get(0).y, 0.0001, "y of 0");
                        }
                        if (j < 10)
                        {
                            assertEquals(p0.x + (p1.x - p0.x) * j / 10, extractedLine.get(1).x, 0.0001, "x of 1");
                            assertEquals(p0.y + (p1.y - p0.y) * j / 10, extractedLine.get(1).y, 0.0001, "y of 1");
                        }
                        else
                        {
                            assertEquals(p1.x + (p2.x - p1.x) * (j - 10) / 100, extractedLine.getLast().x, 0.0001, "x of last");
                            assertEquals(p1.y + (p2.y - p1.y) * (j - 10) / 100, extractedLine.getLast().y, 0.0001, "y of last");
                        }
                        if (extractedLine.size() > 2)
                        {
                            assertEquals(p1.x, extractedLine.get(1).x, 0.0001, "x of mid");
                            assertEquals(p1.y, extractedLine.get(1).y, 0.0001, "y of mid");
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the offsetLine method. Only tests a few easy cases.
     */
    @Test
    public final void offsetLineTest()
    {
        Point2d from = new Point2d(1, 2);
        Point2d to = new Point2d(4, 3);
        OtsLine2d line = new OtsLine2d(from, to);
        double lineLengthHorizontal = new Point2d(from.x, from.y).distance(new Point2d(to.x, to.y));
        for (int step = -5; step <= 5; step++)
        {
            OtsLine2d offsetLine = line.offsetLine(step);
            assertEquals(2, offsetLine.size(), "Offset line of a single straight segment has two points");
            assertEquals(Math.abs(step), offsetLine.getFirst().distance(line.getFirst()), 0.0001,
                    "Distance between start points should be equal to offset");
            assertEquals(Math.abs(step), offsetLine.getLast().distance(line.getLast()), 0.0001,
                    "Distance between end points should be equal to offset");
            // System.out.println("step: " + step);
            // System.out.println("reference: " + line);
            // System.out.println("offset: " + offsetLine);
            assertEquals(lineLengthHorizontal, offsetLine.getLength(), 0.001,
                    "Length of offset line of straight segment should equal length of reference line");
        }
        Point2d via = new Point2d(2.5, 2.5);
        line = new OtsLine2d(from, via, to);
        for (int step = -5; step <= 5; step++)
        {
            OtsLine2d offsetLine = line.offsetLine(step);
            // System.out.println("step: " + step);
            // System.out.println("reference: " + line);
            // System.out.println("offset: " + offsetLine);
            assertTrue(2 <= offsetLine.size(), "Offset line has > 2 points");
            assertEquals(Math.abs(step), offsetLine.getFirst().distance(line.getFirst()), 0.0001,
                    "Distance between start points should be equal to offset");
            assertEquals(Math.abs(step), offsetLine.getLast().distance(line.getLast()), 0.0001,
                    "Distance between end points should be equal to offset");
        }
    }

    /**
     * Tests the fractional projection method.
     */
    @Test
    public final void testFractionalProjection()
    {
        Direction zeroDir = Direction.ZERO;
        // test correct projection with parallel helper lines on line /\/\
        OtsLine2d line =
                new OtsLine2d(new Point2d(0, 0), new Point2d(1, 1), new Point2d(2, 0), new Point2d(3, 1), new Point2d(4, 0));
        double fraction;
        fraction = line.projectFractional(zeroDir, zeroDir, 1.5, -5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new Point2d(1.5, .5), Math.atan2(-1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 1.5, 5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new Point2d(1.5, .5), Math.atan2(-1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 2.5, -5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new Point2d(2.5, .5), Math.atan2(1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 2.5, 5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new Point2d(2.5, .5), Math.atan2(1, 1));
        // test correct projection with parallel helper lines on line ---
        line = new OtsLine2d(new Point2d(0, 0), new Point2d(2, 2), new Point2d(4, 4), new Point2d(6, 6));
        fraction = line.projectFractional(zeroDir, zeroDir, 2, 4, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new Point2d(3, 3), Math.atan2(1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 4, 2, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new Point2d(3, 3), Math.atan2(1, 1));
        // test correct projection without parallel helper lines on just some line
        line = new OtsLine2d(new Point2d(-2, -2), new Point2d(2, -2), new Point2d(2, 2), new Point2d(-2, 2));
        for (double f = 0; f < 0; f += .1)
        {
            fraction = line.projectFractional(zeroDir, zeroDir, 1, -1 + f * 2, FractionalFallback.ORTHOGONAL); // from y = -1 to
                                                                                                               // 1, projecting
                                                                                                               // to 3rd
            // segment
            checkGetLocation(line, fraction, new Point2d(2, -2 + f * 4), Math.atan2(1, 0)); // from y = -2 to 2
        }
        // test projection on barely parallel lines outside of bend
        double[] e = new double[] {1e-3, 1e-6, 1e-9, 1e-12, 1e-16, 1e-32};
        double[] d = new double[] {1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e9, 1e12}; // that's pretty far from a line...
        for (int i = 0; i < e.length; i++)
        {
            line = new OtsLine2d(new Point2d(e[i], 0), new Point2d(2 + e[i], 2), new Point2d(4, 4), new Point2d(6, 6 - e[i]),
                    new Point2d(8, 8 - e[i]));
            for (int j = 0; j < d.length; j++)
            {
                // on outside of slight bend
                fraction = line.projectFractional(zeroDir, zeroDir, 4 - d[j], 4 + d[j], FractionalFallback.ENDPOINT);
                if (Math.abs(fraction - 0.5) > 0.001)
                {
                    line.projectFractional(zeroDir, zeroDir, 4 - d[j], 4 + d[j], FractionalFallback.ENDPOINT);
                }
                if (e[i] >= 1e-3)
                {
                    assertTrue(Math.abs(fraction - 0.5) < 0.001,
                            "Projection of point on outside of very slight bend was wrong with e=" + e[i] + " and d=" + d[j]);
                }
                else
                {
                    assertTrue(fraction >= 0.0 && fraction <= 1.0,
                            "Projection of point on outside of very slight bend was wrong with e=" + e[i] + " and d=" + d[j]);
                }
            }
        }
        // test circle center
        Direction start = new Direction(Math.PI / 2, DirectionUnit.EAST_RADIAN);
        Direction end = new Direction(-Math.PI / 2, DirectionUnit.EAST_RADIAN);
        for (double radius : new double[] {1e16, 1e12, 1e9, 1e6, 1e3, 1, 0.1, 1e-3, 1e-6, 1e-9, 1e-12})
        {
            for (int n : new int[] {9, 10, 901, 1000})
            {
                List<Point2d> list = new ArrayList<>();
                for (double r = 0; r <= Math.PI; r += Math.PI / n)
                {
                    list.add(new Point2d(Math.cos(r) * radius, Math.sin(r) * radius));
                }
                line = new OtsLine2d(list);
                for (double x : new double[] {0, 1e-3, 1e-6, 1e-9, 1e-12})
                {
                    for (double y : new double[] {0, 1e-3, 1e-6, 1e-9, 1e-12})
                    {
                        double f = line.projectFractional(start, end, x, y, FractionalFallback.ORTHOGONAL);
                        assertTrue(f >= 0.0 && f <= 1.0, "Fractional projection on circle is not between 0.0 and 1.0.");
                    }
                }
            }
        }
        // random line test
        Random random = new Random(0);
        for (int n = 0; n < 10; n++)
        {
            // System.out.println(n);
            List<Point2d> list = new ArrayList<>();
            double prevX = 0;
            for (int i = 0; i < 100; i++)
            {
                double x = prevX + random.nextDouble() - 0.4;
                prevX = x;
                list.add(new Point2d(x, random.nextDouble()));
                // System.out.println(list.get(list.size() - 1).x + ", " + list.get(list.size() - 1).y);
            }
            line = new OtsLine2d(list);
            for (double x = -2; x < 12; x += 0.01)
            {
                for (double y = -1; y <= 2; y += 0.1)
                {
                    double f = line.projectFractional(zeroDir, zeroDir, x, y, FractionalFallback.ORTHOGONAL);
                    assertTrue(f >= 0.0 && f <= 1.0, "Fractional projection on random line is not between 0.0 and 1.0.");
                }
            }
        }
        // 2-point line
        line = new OtsLine2d(new Point2d(0, 0), new Point2d(1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, .5, 1, FractionalFallback.ORTHOGONAL);
        assertTrue(Math.abs(fraction - 0.5) < 0.001, "Projection on line with single segment is not correct.");
        // square test (THIS TEST IS NOT YET SUCCESSFUL, THE POINTS ARE PROJECTED ORTHOGONALLY TO BEFORE END!!!)
        // {@formatter:off}
        /*
         * ---------
         * a\     /|  'a' should project to end, not just before end
         *   /\ /  |  point in the /\ should project to end, not before end
         *  /'' \  |               ''
         * /      \|
         * ---------
         */
        // {@formatter:on}
        // line = new OtsLine2d(new Point2d(0, 0), new Point2d(4, 0), new Point2d(4, 4), new Point2d(0, 4));
        // start = new Direction(Math.atan2(-1, 3), AngleUnit.SI);
        // end = new Direction(Math.atan2(-1, -1), AngleUnit.SI);
        // fraction = line.projectFractional(start, end, 1.25, 2.25); // nearest to top of square, but can only project to
        // bottom
        // assertTrue("Projection should be possible to nearest successful segment.", Math.abs(fraction - 1.0) < 0.001);
        // fraction = line.projectFractional(start, end, 0.25, 3.25); // nearest to top of square, but can only project to
        // bottom
        // assertTrue("Projection should be possible to nearest successful segment.", Math.abs(fraction - 1.0) < 0.001);

    }

    /**
     * Test the find method.
     * @throws SecurityException if that happens uncaught; this test has failed
     * @throws NoSuchMethodException if that happens uncaught; this test has failed
     * @throws InvocationTargetException if that happens uncaught; this test has failed
     * @throws IllegalArgumentException if that happens uncaught; this test has failed
     * @throws IllegalAccessException if that happens uncaught; this test has failed
     */
    @Test
    public final void testFind() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException
    {
        // Construct a line with exponentially increasing distances
        List<Point2d> points = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            points.add(new Point2d(Math.pow(2, i) - 1, 10));
        }
        OtsLine2d line = new OtsLine2d(points);
        double end = points.get(points.size() - 1).x;
        Method findMethod = line.getClass().getMethod("find", double.class);
        findMethod.setAccessible(true);
        for (int i = 0; i < end; i++)
        {
            double pos = i + 0.5;
            int index = (int) findMethod.invoke(line, pos);
            assertTrue(line.get(index).x <= pos, "segment starts before pos");
            assertTrue(line.get(index + 1).x >= pos, "next segment starts after pos");
        }
        assertEquals(0, (int) findMethod.invoke(line, 0.0), "pos 0 returns index 0");
    }

    /**
     * Test the truncate method.
     */
    @Test
    public final void testTruncate()
    {
        Point2d from = new Point2d(10, 20);
        Point2d to = new Point2d(70, 80);
        double length = from.distance(to);
        OtsLine2d line = new OtsLine2d(from, to);
        OtsLine2d truncatedLine = line.truncate(length);
        assertEquals(truncatedLine.get(0), from,
                "Start of line truncated at full length is the same as start of the input line");
        assertEquals(0, truncatedLine.get(1).distance(to), 0.0001,
                "End of line truncated at full length is about the same as end of input line");
        try
        {
            line.truncate(-0.1);
            fail("truncate at negative length should have thrown OTSGeometryException");
        }
        catch (DrawRuntimeException e)
        {
            // Ignore expected exception
        }
        try
        {
            line.truncate(length + 0.1);
            fail("truncate at length beyond length of line should have thrown OTSGeometryException");
        }
        catch (DrawRuntimeException e)
        {
            // Ignore expected exception
        }
        truncatedLine = line.truncate(length / 2);
        assertEquals(truncatedLine.get(0), from, "Start of truncated line is the same as start of the input line");
        Point2d halfWay = new Point2d((from.x + to.x) / 2, (from.y + to.y) / 2);
        assertEquals(0, halfWay.distance(truncatedLine.get(1)), 0.0001,
                "End of 50%, truncated 2-point line should be at the half way point");
        Point2d intermediatePoint = new Point2d(20, 20);
        line = new OtsLine2d(from, intermediatePoint, to);
        length = from.distance(intermediatePoint) + intermediatePoint.distance(to);
        truncatedLine = line.truncate(length);
        assertEquals(truncatedLine.get(0), from,
                "Start of line truncated at full length is the same as start of the input line");
        assertEquals(0, truncatedLine.get(2).distance(to), 0.0001,
                "End of line truncated at full length is about the same as end of input line");
        truncatedLine = line.truncate(from.distance(intermediatePoint));
        assertEquals(truncatedLine.get(0), from,
                "Start of line truncated at full length is the same as start of the input line");
        assertEquals(0, truncatedLine.get(1).distance(intermediatePoint), 0.0001,
                "Line truncated at intermediate point ends at that intermediate point");
    }

    /**
     * Test the getRadius method.
     */
    @Test
    public void testRadius()
    {
        // Single segment line is always straight
        OtsLine2d line = new OtsLine2d(new Point2d[] {new Point2d(10, 20), new Point2d(20, 30)});
        Length radius = line.getProjectedRadius(0.5);
        assertTrue(Double.isNaN(radius.getSI()), "should be NaN");
        // Two segment line that is perfectly straight
        line = new OtsLine2d(new Point2d[] {new Point2d(10, 20), new Point2d(20, 30), new Point2d(30, 40)});
        radius = line.getProjectedRadius(0.5);
        assertTrue(Double.isNaN(radius.getSI()), "should be NaN");
        // Two segment line that is not straight
        line = new OtsLine2d(new Point2d[] {new Point2d(10, 30), new Point2d(20, 30), new Point2d(30, 40)});
        // for a 2-segment OtsLine2d, the result should be independent of the fraction
        for (int step = 0; step <= 10; step++)
        {
            double fraction = step / 10.0;
            radius = line.getProjectedRadius(fraction);
            assertEquals(12, radius.si, 0.1, "radius should be about 12");
        }
        // Now a bit harder
        line = new OtsLine2d(
                new Point2d[] {new Point2d(10, 30), new Point2d(20, 30), new Point2d(30, 40), new Point2d(30, 30)});
        if (VERBOSE)
        {
            System.out.println("radius is " + radius);
            System.out.println(Export.toPlot(line));
        }
        double boundary = 1 / (2 + Math.sqrt(2));
        //double length = line.getLength();
        for (int percentage = 0; percentage <= 100; percentage++)
        {
            double fraction = percentage / 100.0;
            double radiusAtFraction = line.getProjectedRadius(fraction).si;
            //Point2d pointAtFraction = line.getLocationSI(fraction * length);
            // System.out.println(
            // "At fraction " + fraction + " (point " + pointAtFraction + "), radius at fraction " + radiusAtFraction);
            if (fraction < boundary)
            {
                assertEquals(12, radiusAtFraction, 0.1, "in first segment radius should be about 12");
            }
            else
            {
                assertEquals(-2, radiusAtFraction, 0.1, "in other segments radius shoudl be about -2");
            }
        }
    }

}
