package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.opentrafficsim.core.geometry.OTSLine3D.FractionalFallback;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSLine3DTest
{
    /**
     * Test the constructors of OTSLine3D.
     * @throws OTSGeometryException on failure
     * @throws NetworkException on failure
     */
    @Test
    public final void constructorsTest() throws OTSGeometryException, NetworkException
    {
        double[] values = {-999, 0, 99, 9999}; // Keep this list short; execution time grows with 9th power of length
        OTSPoint3D[] points = new OTSPoint3D[0]; // Empty array
        try
        {
            runConstructors(points);
            fail("Should have thrown a NetworkException");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        for (double x0 : values)
        {
            for (double y0 : values)
            {
                for (double z0 : values)
                {
                    points = new OTSPoint3D[1]; // Degenerate array holding one point
                    points[0] = new OTSPoint3D(x0, y0, z0);
                    try
                    {
                        runConstructors(points);
                        fail("Should have thrown a NetworkException");
                    }
                    catch (OTSGeometryException exception)
                    {
                        // Ignore expected exception
                    }
                    for (double x1 : values)
                    {
                        for (double y1 : values)
                        {
                            for (double z1 : values)
                            {
                                points = new OTSPoint3D[2]; // Straight line; two points
                                points[0] = new OTSPoint3D(x0, y0, z0);
                                points[1] = new OTSPoint3D(x1, y1, z1);
                                if (0 == points[0].distance(points[1]).si)
                                {
                                    try
                                    {
                                        runConstructors(points);
                                        fail("Should have thrown a NetworkException");
                                    }
                                    catch (OTSGeometryException exception)
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
                                            for (double z2 : values)
                                            {
                                                points = new OTSPoint3D[3]; // Line with intermediate point
                                                points[0] = new OTSPoint3D(x0, y0, z0);
                                                points[1] = new OTSPoint3D(x1, y1, z1);
                                                points[2] = new OTSPoint3D(x2, y2, z2);
                                                if (0 == points[1].distance(points[2]).si)
                                                {
                                                    try
                                                    {
                                                        runConstructors(points);
                                                        fail("Should have thrown a NetworkException");
                                                    }
                                                    catch (OTSGeometryException exception)
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
            }
        }
    }

    /**
     * Test all the constructors of OTSPoint3D.
     * @param points OTSPoint3D[]; array of OTSPoint3D to test with
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     * @throws NetworkException should not happen; this test has failed if it does happen
     */
    private void runConstructors(final OTSPoint3D[] points) throws OTSGeometryException, NetworkException
    {
        verifyPoints(new OTSLine3D(points), points);
        Coordinate[] coordinates = new Coordinate[points.length];
        for (int i = 0; i < points.length; i++)
        {
            coordinates[i] = new Coordinate(points[i].x, points[i].y, points[i].z);
        }
        verifyPoints(new OTSLine3D(coordinates), points);
        GeometryFactory gm = new GeometryFactory();
        LineString lineString = gm.createLineString(coordinates);
        verifyPoints(new OTSLine3D(lineString), points);
        verifyPoints(new OTSLine3D((Geometry) lineString), points);
        List<OTSPoint3D> list = new ArrayList<>();
        for (int i = 0; i < points.length; i++)
        {
            list.add(points[i]);
        }
        OTSLine3D line = new OTSLine3D(list);
        verifyPoints(line, points);
        // Convert it to Coordinate[], create another OTSLine3D from that and check that
        verifyPoints(new OTSLine3D(line.getCoordinates()), points);
        // Convert it to a LineString, create another OTSLine3D from that and check that
        verifyPoints(new OTSLine3D(line.getLineString()), points);
        // Convert it to OTSPoint3D[], create another OTSLine3D from that and check that
        verifyPoints(new OTSLine3D(line.getPoints()), points);
        double length = 0;
        for (int i = 1; i < points.length; i++)
        {
            length += Math.sqrt(Math.pow(points[i].x - points[i - 1].x, 2) + Math.pow(points[i].y - points[i - 1].y, 2)
                    + Math.pow(points[i].z - points[i - 1].z, 2));
        }
        assertEquals("length", length, line.getLength().si, 10 * Math.ulp(length));
        assertEquals("length", length, line.getLength().si, 10 * Math.ulp(length));
        assertEquals("length", length, line.getLengthSI(), 10 * Math.ulp(length));
        assertEquals("length", length, line.getLengthSI(), 10 * Math.ulp(length));
        // Construct a Path3D.Double that contains the horizontal moves.
        int horizontalMoves = 0;
        Path2D path = new Path2D.Double();
        path.moveTo(points[0].x, points[0].y);
        // System.out.print("path is "); printPath2D(path);
        for (int i = 1; i < points.length; i++)
        {
            if (points[i].x != points[i - 1].x || points[i].y != points[i - 1].y)
            {
                path.lineTo(points[i].x, points[i].y); // Path2D is somehow corrupt if same point is added twice
                // System.out.print("path is"); printPath2D(path);
                horizontalMoves++;
            }
        }
        try
        {
            line = new OTSLine3D(path);
            if (0 == horizontalMoves)
            {
                fail("Construction of OTSLine3D from path with degenerate projection should have failed");
            }
            // This new OTSLine3D has z=0 for all points so veryfyPoints won't work
            assertEquals("number of points should match", horizontalMoves + 1, line.size());
            int indexInLine = 0;
            for (int i = 0; i < points.length; i++)
            {
                if (i > 0 && (points[i].x != points[i - 1].x || points[i].y != points[i - 1].y))
                {
                    indexInLine++;
                }
                assertEquals("x in line", points[i].x, line.get(indexInLine).x, 0.001);
                assertEquals("y in line", points[i].y, line.get(indexInLine).y, 0.001);
            }
        }
        catch (OTSGeometryException e)
        {
            if (0 != horizontalMoves)
            {
                fail("Construction of OTSLine3D from path with non-degenerate projection should not have failed");
            }
        }
    }

    /**
     * Print a Path2D to the console.
     * @param path Path2D; the path
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
                System.out.print(" move to " + new OTSPoint3D(p[0], p[1]));
            }
            if (segType == PathIterator.SEG_LINETO)
            {
                System.out.print(" line to " + new OTSPoint3D(p[0], p[1]));
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
     * Verify that a OTSLine3D contains the same points as an array of OTSPoint3D.
     * @param line OTSLine3D; the OTS line
     * @param points OTSPoint3D[]; the OTSPoint array
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    private void verifyPoints(final OTSLine3D line, final OTSPoint3D[] points) throws OTSGeometryException
    {
        assertEquals("Line should have same number of points as point array", line.size(), points.length);
        for (int i = 0; i < points.length; i++)
        {
            assertEquals("x of point i should match", points[i].x, line.get(i).x, Math.ulp(points[i].x));
            assertEquals("y of point i should match", points[i].y, line.get(i).y, Math.ulp(points[i].y));
            assertEquals("z of point i should match", points[i].z, line.get(i).z, Math.ulp(points[i].z));
        }
    }

    /**
     * Test that exception is thrown when it should be.
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    @Test
    public final void exceptionTest() throws OTSGeometryException
    {
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(1, 2, 3), new OTSPoint3D(4, 5, 6)});
        try
        {
            line.get(-1);
            fail("Should have thrown an exception");
        }
        catch (OTSGeometryException oe)
        {
            // Ignore expected exception
        }
        try
        {
            line.get(2);
            fail("Should have thrown an exception");
        }
        catch (OTSGeometryException oe)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the getLocationExtended method and friends.
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    @Test
    public final void locationExtendedTest() throws OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(10, 20, 30);
        OTSPoint3D p1 = new OTSPoint3D(40, 50, 60);
        OTSPoint3D p2 = new OTSPoint3D(90, 80, 70);
        OTSLine3D l = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2});
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
     * @param line OTSLine3D; the line
     * @param fraction double; relative position to check
     * @param expectedPoint OTSPoint3D; expected location of the result
     * @param expectedZRotation double; expected Z rotation of the result
     * @throws OTSGeometryException on failure
     */
    private void checkGetLocation(final OTSLine3D line, final double fraction, final OTSPoint3D expectedPoint,
            final double expectedZRotation) throws OTSGeometryException
    {
        double length = line.getLengthSI();
        checkDirectedPoint(line.getLocationExtendedSI(fraction * length), expectedPoint, expectedZRotation);
        Length typedLength = new Length(fraction * length, LengthUnit.METER);
        checkDirectedPoint(line.getLocationExtended(typedLength), expectedPoint, expectedZRotation);
        if (fraction < 0 || fraction > 1)
        {
            try
            {
                line.getLocationSI(fraction * length);
                fail("getLocation should have thrown a OTSGeometryException");
            }
            catch (OTSGeometryException ne)
            {
                // Ignore expected exception
            }
            try
            {
                line.getLocation(typedLength);
                fail("getLocation should have thrown a OTSGeometryException");
            }
            catch (OTSGeometryException ne)
            {
                // Ignore expected exception
            }
            try
            {
                line.getLocationFraction(fraction);
                fail("getLocation should have thrown a OTSGeometryException");
            }
            catch (OTSGeometryException ne)
            {
                // Ignore expected exception
            }
        }
        else
        {
            checkDirectedPoint(line.getLocationSI(fraction * length), expectedPoint, expectedZRotation);
            checkDirectedPoint(line.getLocation(typedLength), expectedPoint, expectedZRotation);
            checkDirectedPoint(line.getLocationFraction(fraction), expectedPoint, expectedZRotation);
        }

    }

    /**
     * Verify the location and direction of a DirectedPoint.
     * @param dp DirectedPoint; the DirectedPoint that should be verified
     * @param expectedPoint OTSPoint3D; the expected location (or null if location should not be checked)
     * @param expectedZRotation double; the expected Z rotation
     */
    private void checkDirectedPoint(final DirectedPoint dp, final OTSPoint3D expectedPoint, final double expectedZRotation)
    {
        // TODO verify rotations around x and y
        if (null != expectedPoint)
        {
            OTSPoint3D p = new OTSPoint3D(dp);
            assertEquals("locationExtendedSI(0) returns approximately expected point", 0, expectedPoint.distanceSI(p), 0.1);
        }
        assertEquals("z-rotation at 0", expectedZRotation, dp.getRotZ(), 0.001);
    }

    /**
     * Test getLocation method.
     * @throws OTSGeometryException on failure
     */
    @Test
    public final void locationTest() throws OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(10, 20, 60);
        OTSPoint3D p1 = new OTSPoint3D(40, 50, 60);
        OTSPoint3D p2 = new OTSPoint3D(90, 70, 90);
        OTSLine3D l = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2});
        DirectedPoint dp = l.getLocation();
        assertEquals("centroid x", 50, dp.x, 0.001);
        assertEquals("centroid y", 45, dp.y, 0.001);
        assertEquals("centroid z", 75, dp.z, 0.001);
        l = new OTSLine3D(new OTSPoint3D[] {p1, p0, p2}); // Some arguments swapped
        dp = l.getLocation();
        assertEquals("centroid x", 50, dp.x, 0.001);
        assertEquals("centroid y", 45, dp.y, 0.001);
        assertEquals("centroid z", 75, dp.z, 0.001);
        l = new OTSLine3D(new OTSPoint3D[] {p0, p1}); // Two points; all in same Z-plane
        dp = l.getLocation();
        assertEquals("centroid x", 25, dp.x, 0.001);
        assertEquals("centroid y", 35, dp.y, 0.001);
        assertEquals("centroid z", 60, dp.z, 0.001);
    }

    /**
     * Test the createAndCleanOTSLine3D method.
     * @throws OTSGeometryException should never happen
     */
    @Test
    public final void cleanTest() throws OTSGeometryException
    {
        OTSPoint3D[] tooShort = new OTSPoint3D[] {};
        try
        {
            OTSLine3D.createAndCleanOTSLine3D(tooShort);
            fail("Array with no points should have thrown an exception");
        }
        catch (OTSGeometryException ne)
        {
            // Ignore expected exception
        }
        tooShort = new OTSPoint3D[] {new OTSPoint3D(1, 2, 3)};
        try
        {
            OTSLine3D.createAndCleanOTSLine3D(tooShort);
            fail("Array with no points should have thrown an exception");
        }
        catch (OTSGeometryException ne)
        {
            // Ignore expected exception
        }
        OTSPoint3D p0 = new OTSPoint3D(1, 2, 3);
        OTSPoint3D p1 = new OTSPoint3D(4, 5, 6);
        OTSPoint3D[] points = new OTSPoint3D[] {p0, p1};
        OTSLine3D result = OTSLine3D.createAndCleanOTSLine3D(points);
        assertTrue("first point is p0", p0.equals(result.get(0)));
        assertTrue("second point is p1", p1.equals(result.get(1)));
        OTSPoint3D p1Same = new OTSPoint3D(4, 5, 6);
        result = OTSLine3D.createAndCleanOTSLine3D(new OTSPoint3D[] {p0, p0, p0, p0, p1Same, p0, p1, p1, p1Same, p1, p1});
        assertEquals("result should contain 4 points", 4, result.size());
        assertTrue("first point is p0", p0.equals(result.get(0)));
        assertTrue("second point is p1", p1.equals(result.get(1)));
        assertTrue("third point is p0", p0.equals(result.get(0)));
        assertTrue("last point is p1", p1.equals(result.get(1)));
    }

    /**
     * Test the equals method.
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    @Test
    public final void equalsTest() throws OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(1.1, 2.2, 3.3);
        OTSPoint3D p1 = new OTSPoint3D(2.1, 2.2, 3.3);
        OTSPoint3D p2 = new OTSPoint3D(3.1, 2.2, 3.3);

        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2});
        assertTrue("OTSLine3D is equal to itself", line.equals(line));
        assertFalse("OTSLine3D is not equal to null", line.equals(null));
        assertFalse("OTSLine3D is not equals to some other kind of Object", line.equals(new String("hello")));
        OTSLine3D line2 = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2});
        assertTrue("OTSLine3D is equal ot other OTSLine3D that has the exact same list of OTSPoint3D", line.equals(line2));
        OTSPoint3D p2Same = new OTSPoint3D(3.1, 2.2, 3.3);
        line2 = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2Same});
        assertTrue("OTSLine3D is equal ot other OTSLine3D that has the exact same list of OTSPoint3D; even if some of "
                + "those point are different instances with the same coordinates", line.equals(line2));
        OTSPoint3D p2NotSame = new OTSPoint3D(3.1, 2.2, 3.35);
        line2 = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2NotSame});
        assertFalse("OTSLine3D is not equal ot other OTSLine3D that differs in one coordinate", line.equals(line2));
        line2 = new OTSLine3D(new OTSPoint3D[] {p0, p1, p2, p2NotSame});
        assertFalse("OTSLine3D is not equal ot other OTSLine3D that has more points (but is identical up to the common length)",
                line.equals(line2));
        assertFalse(
                "OTSLine3D is not equal ot other OTSLine3D that has fewer points  (but is identical up to the common length)",
                line2.equals(line));
    }

    /**
     * Test the concatenate method.
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    @Test
    public final void concatenateTest() throws OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(1.1, 2.2, 3.3);
        OTSPoint3D p1 = new OTSPoint3D(2.1, 2.2, 3.3);
        OTSPoint3D p2 = new OTSPoint3D(3.1, 2.2, 3.3);
        OTSPoint3D p3 = new OTSPoint3D(4.1, 2.2, 3.3);
        OTSPoint3D p4 = new OTSPoint3D(5.1, 2.2, 3.3);
        OTSPoint3D p5 = new OTSPoint3D(6.1, 2.2, 3.3);

        OTSLine3D l0 = new OTSLine3D(p0, p1, p2);
        OTSLine3D l1 = new OTSLine3D(p2, p3);
        OTSLine3D l2 = new OTSLine3D(p3, p4, p5);
        OTSLine3D ll = OTSLine3D.concatenate(l0, l1, l2);
        assertEquals("size is 6", 6, ll.size());
        assertEquals("point 0 is p0", p0, ll.get(0));
        assertEquals("point 1 is p1", p1, ll.get(1));
        assertEquals("point 2 is p2", p2, ll.get(2));
        assertEquals("point 3 is p3", p3, ll.get(3));
        assertEquals("point 4 is p4", p4, ll.get(4));
        assertEquals("point 5 is p5", p5, ll.get(5));

        ll = OTSLine3D.concatenate(l1);
        assertEquals("size is 2", 2, ll.size());
        assertEquals("point 0 is p2", p2, ll.get(0));
        assertEquals("point 1 is p3", p3, ll.get(1));

        try
        {
            OTSLine3D.concatenate(l0, l2);
            fail("Gap should have throw an exception");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }
        try
        {
            OTSLine3D.concatenate();
            fail("concatenate of empty list should have thrown an exception");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }

        // Test concatenate methods with tolerance
        OTSLine3D thirdLine = new OTSLine3D(p4, p5);
        for (double tolerance : new double[] {0.1, 0.01, 0.001, 0.0001, 0.00001})
        {
            for (double actualError : new double[] {tolerance * 0.9, tolerance * 1.1})
            {
                int maxDirection = 10;
                for (int direction = 0; direction < maxDirection; direction++)
                {
                    double dx = actualError * Math.cos(Math.PI * 2 * direction / maxDirection);
                    double dy = actualError * Math.sin(Math.PI * 2 * direction / maxDirection);
                    OTSLine3D otherLine = new OTSLine3D(new OTSPoint3D(p2.x + dx, p2.y + dy, p2.z), p3, p4);
                    if (actualError < tolerance)
                    {
                        try
                        {
                            OTSLine3D.concatenate(tolerance, l0, otherLine);
                        }
                        catch (OTSGeometryException oge)
                        {
                            OTSLine3D.concatenate(tolerance, l0, otherLine);
                            fail("concatenation with error " + actualError + " and tolerance " + tolerance
                                    + " should not have failed");
                        }
                        try
                        {
                            OTSLine3D.concatenate(tolerance, l0, otherLine, thirdLine);
                        }
                        catch (OTSGeometryException oge)
                        {
                            fail("concatenation with error " + actualError + " and tolerance " + tolerance
                                    + " should not have failed");
                        }
                    }
                    else
                    {
                        try
                        {
                            OTSLine3D.concatenate(tolerance, l0, otherLine);
                        }
                        catch (OTSGeometryException oge)
                        {
                            // Ignore expected exception
                        }
                        try
                        {
                            OTSLine3D.concatenate(tolerance, l0, otherLine, thirdLine);
                        }
                        catch (OTSGeometryException oge)
                        {
                            // Ignore expected exception
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the noiseFilterRamerDouglasPeuker filter method.
     * @throws OTSGeometryException if that happens uncaught, this test has failed
     */
    @Test
    public final void noiseFilterRamerDouglasPeuckerTest() throws OTSGeometryException
    {
        OTSPoint3D start = new OTSPoint3D(1, 2, 3);
        int maxDirection = 20; // 20 means every step of 18 degrees is tested
        double length = 100;
        for (boolean useHorizontalDistance : new boolean[] {true, false})
        {
            for (int direction = 0; direction < maxDirection; direction++)
            {
                double angle = Math.PI * 2 * direction / maxDirection;
                double dx = length * Math.cos(angle);
                double dy = length * Math.sin(angle);
                OTSPoint3D end = new OTSPoint3D(start.x + dx, start.y + dy, start.z);
                OTSLine3D straightLine = new OTSLine3D(start, end);
                int intermediatePointCount = 5;
                for (double tolerance : new double[] {0.1, 0.01, 0.001})
                {
                    double error = tolerance * 0.9;
                    List<OTSPoint3D> pointsOnTestLine = new ArrayList<>();
                    pointsOnTestLine.add(start);
                    for (int intermediatePoint = 0; intermediatePoint < intermediatePointCount; intermediatePoint++)
                    {
                        double iAngle = Math.PI * 2 * intermediatePoint / intermediatePointCount;
                        double idx = error * Math.cos(iAngle);
                        double idy = error * Math.sin(iAngle);
                        double idz = useHorizontalDistance ? (intermediatePoint % 2 * 2 - 1) * 10 : 0;
                        DirectedPoint exactPoint =
                                straightLine.getLocationFraction((intermediatePoint + 0.5) / intermediatePointCount);
                        OTSPoint3D additionalPoint = new OTSPoint3D(exactPoint.x + idx, exactPoint.y + idy, exactPoint.z + idz);
                        pointsOnTestLine.add(additionalPoint);
                    }
                    pointsOnTestLine.add(end);
                    OTSLine3D testLine = new OTSLine3D(pointsOnTestLine);
                    OTSLine3D filteredLine = testLine.noiseFilterRamerDouglasPeucker(tolerance, useHorizontalDistance);
                    assertEquals("RamerDouglasPeuker filter should have removed all intermediate points", 2,
                            filteredLine.size());
                    // Now add a couple of points that should not be removed and will not cause the current start and end point
                    // to be removed
                    OTSPoint3D newStart = new OTSPoint3D(start.x + 10 * tolerance * dy / length,
                            start.y - 10 * tolerance * dx / length, start.z);
                    pointsOnTestLine.add(0, newStart);
                    // This filter does not find optimal solutions in many cases. Only case where one serious (really far)
                    // "outlier" is added on only one end work most of the time.
                    testLine = new OTSLine3D(pointsOnTestLine);
                    filteredLine = testLine.noiseFilterRamerDouglasPeucker(tolerance, useHorizontalDistance);
                    // if (3 != filteredLine.size())
                    // {
                    // testLine.noiseFilterRamerDouglasPeuker(tolerance, useHorizontalDistance);
                    // }
                    assertEquals("RamerDouglasPeuker filter should have left three points", 3, filteredLine.size());
                    pointsOnTestLine.remove(0);
                    OTSPoint3D newEnd =
                            new OTSPoint3D(end.x + 10 * tolerance * dy / length, end.y - 10 * tolerance * dx / length, end.z);
                    pointsOnTestLine.add(newEnd);
                    testLine = new OTSLine3D(pointsOnTestLine);
                    filteredLine = testLine.noiseFilterRamerDouglasPeucker(tolerance, useHorizontalDistance);
                    // if (3 != filteredLine.size())
                    // {
                    // testLine.noiseFilterRamerDouglasPeuker(tolerance, useHorizontalDistance);
                    // }
                    assertEquals("RamerDouglasPeuker filter should have left three points", 3, filteredLine.size());
                }
            }
        }
    }

    /**
     * Test the reverse method.
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    @Test
    public final void reverseTest() throws OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(1.1, 2.2, 3.3);
        OTSPoint3D p1 = new OTSPoint3D(2.1, 2.2, 3.3);
        OTSPoint3D p2 = new OTSPoint3D(3.1, 2.2, 3.3);
        OTSPoint3D p3 = new OTSPoint3D(4.1, 2.2, 3.3);
        OTSPoint3D p4 = new OTSPoint3D(5.1, 2.2, 3.3);
        OTSPoint3D p5 = new OTSPoint3D(6.1, 2.2, 3.3);

        OTSLine3D l01 = new OTSLine3D(p0, p1);
        OTSLine3D r = l01.reverse();
        assertEquals("result has size 2", 2, r.size());
        assertEquals("point 0 is p1", p1, r.get(0));
        assertEquals("point 1 is p0", p0, r.get(1));

        OTSLine3D l05 = new OTSLine3D(p0, p1, p2, p3, p4, p5);
        r = l05.reverse();
        assertEquals("result has size 6", 6, r.size());
        assertEquals("point 0 is p5", p5, r.get(0));
        assertEquals("point 1 is p4", p4, r.get(1));
        assertEquals("point 2 is p3", p3, r.get(2));
        assertEquals("point 3 is p2", p2, r.get(3));
        assertEquals("point 4 is p1", p1, r.get(4));
        assertEquals("point 5 is p0", p0, r.get(5));

    }

    /**
     * Test the extract and extractFraction methods.
     * @throws OTSGeometryException should not happen; this test has failed if it does happen
     */
    @SuppressWarnings("checkstyle:methodlength")
    @Test
    public final void extractTest() throws OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(1, 2, 3);
        OTSPoint3D p1 = new OTSPoint3D(2, 3, 4);
        OTSPoint3D p1a = new OTSPoint3D(2.01, 3.01, 4.01);
        OTSPoint3D p1b = new OTSPoint3D(2.02, 3.02, 4.02);
        OTSPoint3D p1c = new OTSPoint3D(2.03, 3.03, 4.03);
        OTSPoint3D p2 = new OTSPoint3D(12, 13, 14);

        OTSLine3D l = new OTSLine3D(p0, p1);
        OTSLine3D e = l.extractFractional(0, 1);
        assertEquals("size of extraction is 2", 2, e.size());
        assertEquals("point 0 is p0", p0, e.get(0));
        assertEquals("point 1 is p1", p1, e.get(1));
        try
        {
            l.extractFractional(-0.1, 1);
            fail("negative start should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(Double.NaN, 1);
            fail("NaN start should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(0, 1.1);
            fail("end > 1 should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(0, Double.NaN);
            fail("NaN end should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extractFractional(0.6, 0.4);
            fail("start > end should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(-0.1, 1);
            fail("negative start should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(Double.NaN, 1);
            fail("NaN start should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(0, l.getLengthSI() + 0.1);
            fail("end > length should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(0, Double.NaN);
            fail("NaN end should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }
        try
        {
            l.extract(0.6, 0.4);
            fail("start > end should have thrown an exception");
        }
        catch (OTSGeometryException exception)
        {
            // Ignore expected exception
        }

        for (int i = 0; i < 10; i++)
        {
            for (int j = i + 1; j < 10; j++)
            {
                double start = i * l.getLengthSI() / 10;
                double end = j * l.getLengthSI() / 10;
                // System.err.println("i=" + i + ", j=" + j);
                for (OTSLine3D extractedLine : new OTSLine3D[] {l.extract(start, end),
                        l.extract(new Length(start, LengthUnit.SI), new Length(end, LengthUnit.SI)),
                        l.extractFractional(1.0 * i / 10, 1.0 * j / 10)})
                {
                    assertEquals("size of extract is 2", 2, extractedLine.size());
                    assertEquals("x of 0", p0.x + (p1.x - p0.x) * i / 10, extractedLine.get(0).x, 0.0001);
                    assertEquals("y of 0", p0.y + (p1.y - p0.y) * i / 10, extractedLine.get(0).y, 0.0001);
                    assertEquals("z of 0", p0.z + (p1.z - p0.z) * i / 10, extractedLine.get(0).z, 0.0001);
                    assertEquals("x of 1", p0.x + (p1.x - p0.x) * j / 10, extractedLine.get(1).x, 0.0001);
                    assertEquals("y of 1", p0.y + (p1.y - p0.y) * j / 10, extractedLine.get(1).y, 0.0001);
                    assertEquals("z of 1", p0.z + (p1.z - p0.z) * j / 10, extractedLine.get(1).z, 0.0001);
                }
            }
        }

        for (OTSLine3D line : new OTSLine3D[] {new OTSLine3D(p0, p1, p2), new OTSLine3D(p0, p1, p1a, p1b, p1c, p2)})
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
                    double start = i * line.getLengthSI() / 110;
                    double end = j * line.getLengthSI() / 110;
                    // System.err.println("first length is " + firstLength);
                    // System.err.println("second length is " + line.getLengthSI());
                    // System.err.println("i=" + i + ", j=" + j);
                    for (OTSLine3D extractedLine : new OTSLine3D[] {line.extract(start, end),
                            line.extract(new Length(start, LengthUnit.SI), new Length(end, LengthUnit.SI)),
                            line.extractFractional(1.0 * i / 110, 1.0 * j / 110)})
                    {
                        int expectedSize = i < 10 && j > 10 ? line.size() : 2;
                        assertEquals("size is " + expectedSize, expectedSize, extractedLine.size());
                        if (i < 10)
                        {
                            assertEquals("x of 0", p0.x + (p1.x - p0.x) * i / 10, extractedLine.get(0).x, 0.0001);
                            assertEquals("y of 0", p0.y + (p1.y - p0.y) * i / 10, extractedLine.get(0).y, 0.0001);
                            assertEquals("z of 0", p0.z + (p1.z - p0.z) * i / 10, extractedLine.get(0).z, 0.0001);
                        }
                        else
                        {
                            assertEquals("x of 0", p1.x + (p2.x - p1.x) * (i - 10) / 100, extractedLine.get(0).x, 0.0001);
                            assertEquals("y of 0", p1.y + (p2.y - p1.y) * (i - 10) / 100, extractedLine.get(0).y, 0.0001);
                            assertEquals("z of 0", p1.z + (p2.z - p1.z) * (i - 10) / 100, extractedLine.get(0).z, 0.0001);
                        }
                        if (j < 10)
                        {
                            assertEquals("x of 1", p0.x + (p1.x - p0.x) * j / 10, extractedLine.get(1).x, 0.0001);
                            assertEquals("y of 1", p0.y + (p1.y - p0.y) * j / 10, extractedLine.get(1).y, 0.0001);
                            assertEquals("z of 1", p0.z + (p1.z - p0.z) * j / 10, extractedLine.get(1).z, 0.0001);
                        }
                        else
                        {
                            assertEquals("x of last", p1.x + (p2.x - p1.x) * (j - 10) / 100, extractedLine.getLast().x, 0.0001);
                            assertEquals("y of last", p1.y + (p2.y - p1.y) * (j - 10) / 100, extractedLine.getLast().y, 0.0001);
                            assertEquals("z of last", p1.z + (p2.z - p1.z) * (j - 10) / 100, extractedLine.getLast().z, 0.0001);
                        }
                        if (extractedLine.size() > 2)
                        {
                            assertEquals("x of mid", p1.x, extractedLine.get(1).x, 0.0001);
                            assertEquals("y of mid", p1.y, extractedLine.get(1).y, 0.0001);
                            assertEquals("z of mid", p1.z, extractedLine.get(1).z, 0.0001);
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the offsetLine method. Only tests a few easy cases.
     * @throws OTSGeometryException should not happen (if it does; this test has failed)
     */
    @Test
    public final void offsetLineTest() throws OTSGeometryException
    {
        OTSPoint3D from = new OTSPoint3D(1, 2, 3);
        OTSPoint3D to = new OTSPoint3D(4, 3, 2);
        OTSLine3D line = new OTSLine3D(from, to);
        double lineLengthHorizontal = new OTSPoint3D(from.x, from.y).distanceSI(new OTSPoint3D(to.x, to.y));
        for (int step = -5; step <= 5; step++)
        {
            OTSLine3D offsetLine = line.offsetLine(step);
            assertEquals("Offset line of a single straight segment has two points", 2, offsetLine.size());
            assertEquals("Distance between start points should be equal to offset", Math.abs(step),
                    offsetLine.getFirst().horizontalDistanceSI(line.getFirst()), 0.0001);
            assertEquals("Distance between end points should be equal to offset", Math.abs(step),
                    offsetLine.getLast().horizontalDistanceSI(line.getLast()), 0.0001);
            // System.out.println("step: " + step);
            // System.out.println("reference: " + line);
            // System.out.println("offset: " + offsetLine);
            assertEquals("Length of offset line of straight segment should equal length of reference line",
                    lineLengthHorizontal, offsetLine.getLengthSI(), 0.001);
        }
        OTSPoint3D via = new OTSPoint3D(4, 3, 3);
        line = new OTSLine3D(from, via, to);
        for (int step = -5; step <= 5; step++)
        {
            OTSLine3D offsetLine = line.offsetLine(step);
            // System.out.println("step: " + step);
            // System.out.println("reference: " + line);
            // System.out.println("offset: " + offsetLine);
            assertTrue("Offset line has > 2 points", 2 <= offsetLine.size());
            assertEquals("Distance between start points should be equal to offset", Math.abs(step),
                    offsetLine.getFirst().horizontalDistanceSI(line.getFirst()), 0.0001);
            assertEquals("Distance between end points should be equal to offset", Math.abs(step),
                    offsetLine.getLast().horizontalDistanceSI(line.getLast()), 0.0001);
        }
    }

    /**
     * Test the noiseFilteredLine method.
     * @throws OTSGeometryException should not happen (if it does, this test has failed)
     */
    @Test
    public final void testFilter() throws OTSGeometryException
    {
        OTSPoint3D from = new OTSPoint3D(1, 2, 3);
        OTSPoint3D to = new OTSPoint3D(4, 3, 2);
        for (int steps = 0; steps < 10; steps++)
        {
            List<OTSPoint3D> points = new ArrayList<>(2 + steps);
            points.add(from);
            for (int i = 0; i < steps; i++)
            {
                points.add(OTSPoint3D.interpolate(1.0 * (i + 1) / (steps + 2), from, to));
            }
            points.add(to);
            OTSLine3D line = new OTSLine3D(points);
            // System.out.println("ref: " + line);
            double segmentLength = line.getFirst().distanceSI(line.get(1));
            OTSLine3D filteredLine = line.noiseFilteredLine(segmentLength * 0.9);
            assertEquals("filtering with a filter that is smaller than any segment should return the original", line.size(),
                    filteredLine.size());
            filteredLine = line.noiseFilteredLine(segmentLength * 1.1);
            int expectedSize = 2 + steps / 2;
            assertEquals("filtering with a filter slightly larger than each segment should return a line with " + expectedSize
                    + " points", expectedSize, filteredLine.size());
            filteredLine = line.noiseFilteredLine(segmentLength * 2.1);
            // System.out.println("flt: " + filteredLine);
            expectedSize = 2 + (steps - 1) / 3;
            assertEquals("filtering with a filter slightly larger than twice the length of each segment should return a "
                    + "line with " + expectedSize + " points", expectedSize, filteredLine.size());
            // Special case where start and end point are equal and all intermediate points are within the margin
            points.clear();
            points.add(from);
            for (int i = 1; i < 10; i++)
            {
                points.add(new OTSPoint3D(from.x + 0.0001 * i, from.y + 0.0001 * i, from.z));
            }
            points.add(from);
            line = new OTSLine3D(points);
            filteredLine = line.noiseFilteredLine(0.2);
            assertEquals("filter returns line of three points", 3, filteredLine.getPoints().length);
            assertEquals("first point matches", from, filteredLine.getPoints()[0]);
            assertEquals("last point matches", from, filteredLine.getPoints()[filteredLine.getPoints().length - 1]);
            assertNotEquals("intermediate point differs from first point", from, filteredLine.getPoints()[1]);
        }
    }

    /**
     * Tests the fractional projection method.
     * @throws OTSGeometryException should not happen (if it does, this test has failed)
     */
    @Test
    public final void testFractionalProjection() throws OTSGeometryException
    {
        Direction zeroDir = Direction.ZERO;
        // test correct projection with parallel helper lines on line /\/\
        OTSLine3D line = new OTSLine3D(new OTSPoint3D(0, 0), new OTSPoint3D(1, 1), new OTSPoint3D(2, 0), new OTSPoint3D(3, 1),
                new OTSPoint3D(4, 0));
        double fraction;
        fraction = line.projectFractional(zeroDir, zeroDir, 1.5, -5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new OTSPoint3D(1.5, .5, 0), Math.atan2(-1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 1.5, 5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new OTSPoint3D(1.5, .5, 0), Math.atan2(-1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 2.5, -5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new OTSPoint3D(2.5, .5, 0), Math.atan2(1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 2.5, 5.0, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new OTSPoint3D(2.5, .5, 0), Math.atan2(1, 1));
        // test correct projection with parallel helper lines on line ---
        line = new OTSLine3D(new OTSPoint3D(0, 0), new OTSPoint3D(2, 2), new OTSPoint3D(4, 4), new OTSPoint3D(6, 6));
        fraction = line.projectFractional(zeroDir, zeroDir, 2, 4, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new OTSPoint3D(3, 3, 0), Math.atan2(1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, 4, 2, FractionalFallback.ORTHOGONAL);
        checkGetLocation(line, fraction, new OTSPoint3D(3, 3, 0), Math.atan2(1, 1));
        // test correct projection without parallel helper lines on just some line
        line = new OTSLine3D(new OTSPoint3D(-2, -2), new OTSPoint3D(2, -2), new OTSPoint3D(2, 2), new OTSPoint3D(-2, 2));
        for (double f = 0; f < 0; f += .1)
        {
            fraction = line.projectFractional(zeroDir, zeroDir, 1, -1 + f * 2, FractionalFallback.ORTHOGONAL); // from y = -1 to
                                                                                                               // 1, projecting
                                                                                                               // to 3rd
            // segment
            checkGetLocation(line, fraction, new OTSPoint3D(2, -2 + f * 4, 0), Math.atan2(1, 0)); // from y = -2 to 2
        }
        // test projection on barely parallel lines outside of bend
        double[] e = new double[] {1e-3, 1e-6, 1e-9, 1e-12, 1e-16, 1e-32};
        double[] d = new double[] {1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e9, 1e12}; // that's pretty far from a line...
        for (int i = 0; i < e.length; i++)
        {
            line = new OTSLine3D(new OTSPoint3D(e[i], 0), new OTSPoint3D(2 + e[i], 2), new OTSPoint3D(4, 4),
                    new OTSPoint3D(6, 6 - e[i]), new OTSPoint3D(8, 8 - e[i]));
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
                    assertTrue("Projection of point on outside of very slight bend was wrong with e=" + e[i] + " and d=" + d[j],
                            Math.abs(fraction - 0.5) < 0.001);
                }
                else
                {
                    assertTrue("Projection of point on outside of very slight bend was wrong with e=" + e[i] + " and d=" + d[j],
                            fraction >= 0.0 && fraction <= 1.0);
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
                List<OTSPoint3D> list = new ArrayList<>();
                for (double r = 0; r <= Math.PI; r += Math.PI / n)
                {
                    list.add(new OTSPoint3D(Math.cos(r) * radius, Math.sin(r) * radius));
                }
                line = new OTSLine3D(list);
                for (double x : new double[] {0, 1e-3, 1e-6, 1e-9, 1e-12})
                {
                    for (double y : new double[] {0, 1e-3, 1e-6, 1e-9, 1e-12})
                    {
                        double f = line.projectFractional(start, end, x, y, FractionalFallback.ORTHOGONAL);
                        assertTrue("Fractional projection on circle is not between 0.0 and 1.0.", f >= 0.0 && f <= 1.0);
                    }
                }
            }
        }
        // random line test
        Random random = new Random(0);
        for (int n = 0; n < 10; n++)
        {
            // System.out.println(n);
            List<OTSPoint3D> list = new ArrayList<>();
            double prevX = 0;
            for (int i = 0; i < 100; i++)
            {
                double x = prevX + random.nextDouble() - 0.4;
                prevX = x;
                list.add(new OTSPoint3D(x, random.nextDouble()));
                // System.out.println(list.get(list.size() - 1).x + ", " + list.get(list.size() - 1).y);
            }
            line = new OTSLine3D(list);
            for (double x = -2; x < 12; x += 0.01)
            {
                for (double y = -1; y <= 2; y += 0.1)
                {
                    double f = line.projectFractional(zeroDir, zeroDir, x, y, FractionalFallback.ORTHOGONAL);
                    assertTrue("Fractional projection on circle is not between 0.0 and 1.0.", f >= 0.0 && f <= 1.0);
                }
            }
        }
        // 2-point line
        line = new OTSLine3D(new OTSPoint3D(0, 0), new OTSPoint3D(1, 1));
        fraction = line.projectFractional(zeroDir, zeroDir, .5, 1, FractionalFallback.ORTHOGONAL);
        assertTrue("Projection on line with single segment is not correct.", Math.abs(fraction - 0.5) < 0.001);
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
        // line = new OTSLine3D(new OTSPoint3D(0, 0), new OTSPoint3D(4, 0), new OTSPoint3D(4, 4), new OTSPoint3D(0, 4));
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
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     * @throws SecurityException if that happens uncaught; this test has failed
     * @throws NoSuchMethodException if that happens uncaught; this test has failed
     * @throws InvocationTargetException if that happens uncaught; this test has failed
     * @throws IllegalArgumentException if that happens uncaught; this test has failed
     * @throws IllegalAccessException if that happens uncaught; this test has failed
     */
    @Test
    public final void testFind() throws OTSGeometryException, NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException
    {
        // Construct a line with exponentially increasing distances
        List<OTSPoint3D> points = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            points.add(new OTSPoint3D(Math.pow(2, i) - 1, 10, 20));
        }
        OTSLine3D line = new OTSLine3D(points);
        double end = points.get(points.size() - 1).x;
        Method findMethod = line.getClass().getDeclaredMethod("find", double.class);
        findMethod.setAccessible(true);
        for (int i = 0; i < end; i++)
        {
            double pos = i + 0.5;
            int index = (int) findMethod.invoke(line, pos);
            assertTrue("segment starts before pos", line.get(index).x <= pos);
            assertTrue("next segment starts after pos", line.get(index + 1).x >= pos);
        }
        assertEquals("pos 0 returns index 0", 0, (int) findMethod.invoke(line, 0.0));
    }

    /**
     * Test the truncate method.
     * @throws OTSGeometryException if that happens uncaught; this test has failed
     */
    @Test
    public final void testTruncate() throws OTSGeometryException
    {
        OTSPoint3D from = new OTSPoint3D(10, 20, 30);
        OTSPoint3D to = new OTSPoint3D(70, 80, 90);
        double length = from.distanceSI(to);
        OTSLine3D line = new OTSLine3D(from, to);
        OTSLine3D truncatedLine = line.truncate(length);
        assertEquals("Start of line truncated at full length is the same as start of the input line", truncatedLine.get(0),
                from);
        assertEquals("End of line truncated at full length is about the same as end of input line", 0,
                truncatedLine.get(1).distance(to).si, 0.0001);
        try
        {
            line.truncate(-0.1);
            fail("truncate at negative length should have thrown OTSGeometryException");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }
        try
        {
            line.truncate(length + 0.1);
            fail("truncate at length beyond length of line should have thrown OTSGeometryException");
        }
        catch (OTSGeometryException e)
        {
            // Ignore expected exception
        }
        truncatedLine = line.truncate(length / 2);
        assertEquals("Start of truncated line is the same as start of the input line", truncatedLine.get(0), from);
        OTSPoint3D halfWay = new OTSPoint3D((from.x + to.x) / 2, (from.y + to.y) / 2, (from.z + to.z) / 2);
        assertEquals("End of 50%, truncated 2-point line should be at the half way point", 0,
                halfWay.distanceSI(truncatedLine.get(1)), 0.0001);
        OTSPoint3D intermediatePoint = new OTSPoint3D(20, 20, 20);
        line = new OTSLine3D(from, intermediatePoint, to);
        length = from.distance(intermediatePoint).plus(intermediatePoint.distance(to)).si;
        truncatedLine = line.truncate(length);
        assertEquals("Start of line truncated at full length is the same as start of the input line", truncatedLine.get(0),
                from);
        assertEquals("End of line truncated at full length is about the same as end of input line", 0,
                truncatedLine.get(2).distance(to).si, 0.0001);
        truncatedLine = line.truncate(from.distanceSI(intermediatePoint));
        assertEquals("Start of line truncated at full length is the same as start of the input line", truncatedLine.get(0),
                from);
        assertEquals("Line truncated at intermediate point ends at that intermediate point", 0,
                truncatedLine.get(1).distanceSI(intermediatePoint), 0.0001);
    }

    /**
     * Test the getRadius method.
     * @throws OTSGeometryException when that happens uncaught; this test has failed
     */
    @Test
    public void testRadius() throws OTSGeometryException
    {
        // Single segment line is always straight
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(10, 20, 30), new OTSPoint3D(20, 30, 30)});
        Length radius = line.getProjectedRadius(0.5);
        assertTrue("should be NaN", Double.isNaN(radius.getSI()));
        // Two segment line that is perfectly straight
        line = new OTSLine3D(
                new OTSPoint3D[] {new OTSPoint3D(10, 20, 30), new OTSPoint3D(20, 30, 30), new OTSPoint3D(30, 40, 30)});
        radius = line.getProjectedRadius(0.5);
        assertTrue("should be NaN", Double.isNaN(radius.getSI()));
        // Two segment line that is not straight
        line = new OTSLine3D(
                new OTSPoint3D[] {new OTSPoint3D(10, 30, 30), new OTSPoint3D(20, 30, 30), new OTSPoint3D(30, 40, 30)});
        // for a 2-segment OTSLine3D, the result should be independent of the fraction
        for (int step = 0; step <= 10; step++)
        {
            double fraction = step / 10.0;
            radius = line.getProjectedRadius(fraction);
            assertEquals("radius should be about 12", 12, radius.si, 0.1);
        }
        System.out.println("radius is " + radius);
        // Now a bit harder
        line = new OTSLine3D(new OTSPoint3D[] {new OTSPoint3D(10, 30, 30), new OTSPoint3D(20, 30, 30),
                new OTSPoint3D(30, 40, 30), new OTSPoint3D(30, 30, 30)});
        System.out.println(line.toPlot());
        double boundary = 1 / (2 + Math.sqrt(2));
        double length = line.getLengthSI();
        for (int percentage = 0; percentage <= 100; percentage++)
        {
            double fraction = percentage / 100.0;
            double radiusAtFraction = line.getProjectedRadius(fraction).si;
            OTSPoint3D pointAtFraction = new OTSPoint3D(line.getLocationSI(fraction * length));
            // System.out.println(
            // "At fraction " + fraction + " (point " + pointAtFraction + "), radius at fraction " + radiusAtFraction);
            if (fraction < boundary)
            {
                assertEquals("in first segment radius should be about 12", 12, radiusAtFraction, 0.1);
            }
            else
            {
                assertEquals("in other segments radius shoudl be about -2", -2, radiusAtFraction, 0.1);
            }
        }
    }

}
