package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 okt. 2015 <br>
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
    public void constructorsTest() throws OTSGeometryException, NetworkException
    {
        double[] values = { -999, 0, 99, 9999 }; // Keep this list short; execution time grows with 9th power of length
        OTSPoint3D[] points = new OTSPoint3D[0]; // Empty array
        try
        {
            runConstructors(points);
            fail("Should have thrown a NetworkException");
        }
        catch (NetworkException exception)
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
                    catch (NetworkException exception)
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
                                    catch (NetworkException exception)
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
                                                    catch (NetworkException exception)
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
     * @throws OTSGeometryException on failure
     * @throws NetworkException on failure
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
            length +=
                    Math.sqrt(Math.pow(points[i].x - points[i - 1].x, 2) + Math.pow(points[i].y - points[i - 1].y, 2)
                            + Math.pow(points[i].z - points[i - 1].z, 2));
        }
        assertEquals("length", length, line.getLength().si, 10 * Math.ulp(length));
        assertEquals("length", length, line.getLength().si, 10 * Math.ulp(length));
        assertEquals("length", length, line.getLengthSI(), 10 * Math.ulp(length));
        assertEquals("length", length, line.getLengthSI(), 10 * Math.ulp(length));
    }

    /**
     * Verify that a OTSLine3D contains the same points as an array of OTSPoint3D.
     * @param line OTSLine3D; the OTS line
     * @param points OTSPoint3D[]; the OTSPoint array
     * @throws OTSGeometryException
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
     * @throws NetworkException on failure
     */
    @Test
    public void exceptionTest() throws NetworkException
    {
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] { new OTSPoint3D(1, 2, 3), new OTSPoint3D(4, 5, 6) });
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
     * @throws NetworkException on failure
     */
    @Test
    public void locationExtendedTest() throws NetworkException
    {
        OTSPoint3D p0 = new OTSPoint3D(10, 20, 30);
        OTSPoint3D p1 = new OTSPoint3D(40, 50, 60);
        OTSPoint3D p2 = new OTSPoint3D(90, 80, 70);
        OTSLine3D l = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2 });
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
     * @throws NetworkException on failure
     */
    private void checkGetLocation(OTSLine3D line, double fraction, OTSPoint3D expectedPoint, double expectedZRotation)
            throws NetworkException
    {
        double length = line.getLengthSI();
        checkDirectedPoint(line.getLocationExtendedSI(fraction * length), expectedPoint, expectedZRotation);
        Length.Rel typedLength = new Length.Rel(fraction * length, LengthUnit.METER);
        checkDirectedPoint(line.getLocationExtended(typedLength), expectedPoint, expectedZRotation);
        if (fraction < 0 || fraction > 1)
        {
            try
            {
                line.getLocationSI(fraction * length);
                fail("getLocation should have thrown a NetworkException");
            }
            catch (NetworkException ne)
            {
                // Ignore expected exception
            }
            try
            {
                line.getLocation(typedLength);
                fail("getLocation should have thrown a NetworkException");
            }
            catch (NetworkException ne)
            {
                // Ignore expected exception
            }
            try
            {
                line.getLocationFraction(fraction);
                fail("getLocation should have thrown a NetworkException");
            }
            catch (NetworkException ne)
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
    private void checkDirectedPoint(DirectedPoint dp, OTSPoint3D expectedPoint, double expectedZRotation)
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
     * @throws NetworkException on failure
     */
    @Test
    public void locationTest() throws NetworkException
    {
        OTSPoint3D p0 = new OTSPoint3D(10, 20, 60);
        OTSPoint3D p1 = new OTSPoint3D(40, 50, 60);
        OTSPoint3D p2 = new OTSPoint3D(90, 70, 90);
        OTSLine3D l = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2 });
        DirectedPoint dp = l.getLocation();
        assertEquals("centroid x", 50, dp.x, 0.001);
        assertEquals("centroid y", 45, dp.y, 0.001);
        assertEquals("centroid z", 75, dp.z, 0.001);
        l = new OTSLine3D(new OTSPoint3D[] { p1, p0, p2 }); // Some arguments swapped
        dp = l.getLocation();
        assertEquals("centroid x", 50, dp.x, 0.001);
        assertEquals("centroid y", 45, dp.y, 0.001);
        assertEquals("centroid z", 75, dp.z, 0.001);
        l = new OTSLine3D(new OTSPoint3D[] { p0, p1 }); // Two points; all in same Z-plane
        dp = l.getLocation();
        assertEquals("centroid x", 25, dp.x, 0.001);
        assertEquals("centroid y", 35, dp.y, 0.001);
        assertEquals("centroid z", 60, dp.z, 0.001);
    }

    /**
     * Test the createAndCleanOTSLine3D method
     * @throws NetworkException should never happen
     * @throws OTSGeometryException should never happen
     */
    @Test
    public void cleanTest() throws NetworkException, OTSGeometryException
    {
        OTSPoint3D[] tooShort = new OTSPoint3D[] {};
        try
        {
            OTSLine3D.createAndCleanOTSLine3D(tooShort);
            fail("Array with no points should have thrown an exception");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        tooShort = new OTSPoint3D[] { new OTSPoint3D(1, 2, 3) };
        try
        {
            OTSLine3D.createAndCleanOTSLine3D(tooShort);
            fail("Array with no points should have thrown an exception");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        OTSPoint3D p0 = new OTSPoint3D(1, 2, 3);
        OTSPoint3D p1 = new OTSPoint3D(4, 5, 6);
        OTSPoint3D[] points = new OTSPoint3D[] { p0, p1 };
        OTSLine3D result = OTSLine3D.createAndCleanOTSLine3D(points);
        assertTrue("first point is p0", p0.equals(result.get(0)));
        assertTrue("second point is p1", p1.equals(result.get(1)));
        OTSPoint3D p1Same = new OTSPoint3D(4, 5, 6);
        result = OTSLine3D.createAndCleanOTSLine3D(new OTSPoint3D[] { p0, p0, p0, p0, p1Same, p0, p1, p1, p1Same, p1, p1 });
        assertEquals("result should contain 4 points", 4, result.size());
        assertTrue("first point is p0", p0.equals(result.get(0)));
        assertTrue("second point is p1", p1.equals(result.get(1)));
        assertTrue("third point is p0", p0.equals(result.get(0)));
        assertTrue("last point is p1", p1.equals(result.get(1)));
    }

    /**
     * Test the equals method.
     * @throws NetworkException should never happen
     */
    @Test
    public void equalsTest() throws NetworkException
    {
        OTSPoint3D p0 = new OTSPoint3D(1.1, 2.2, 3.3);
        OTSPoint3D p1 = new OTSPoint3D(2.1, 2.2, 3.3);
        OTSPoint3D p2 = new OTSPoint3D(3.1, 2.2, 3.3);

        OTSLine3D line = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2 });
        assertTrue("OTSLine3D is equal to itself", line.equals(line));
        assertFalse("OTSLine3D is not equal to null", line.equals(null));
        assertFalse("OTSLine3D is not equals to some other kind of Object", line.equals(new String("hello")));
        OTSLine3D line2 = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2 });
        assertTrue("OTSLine3D is equal ot other OTSLine3D that has the exact same list of OTSPoint3D", line.equals(line2));
        OTSPoint3D p2Same = new OTSPoint3D(3.1, 2.2, 3.3);
        line2 = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2Same });
        assertTrue("OTSLine3D is equal ot other OTSLine3D that has the exact same list of OTSPoint3D; even if some of "
                + "those point are different instances with the same coordinates", line.equals(line2));
        OTSPoint3D p2NotSame = new OTSPoint3D(3.1, 2.2, 3.35);
        line2 = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2NotSame });
        assertFalse("OTSLine3D is not equal ot other OTSLine3D that differs in one coordinate", line.equals(line2));
        line2 = new OTSLine3D(new OTSPoint3D[] { p0, p1, p2, p2NotSame });
        assertFalse(
                "OTSLine3D is not equal ot other OTSLine3D that has more points (but is identical up to the common length)",
                line.equals(line2));
        assertFalse(
                "OTSLine3D is not equal ot other OTSLine3D that has fewer points  (but is identical up to the common length)",
                line2.equals(line));
    }

    /**
     * Test the concatenate method.
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    @Test
    public void concatenateTest() throws NetworkException, OTSGeometryException
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
    }

    /**
     * Test the reverse method.
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    @Test
    public void reverseTest() throws NetworkException, OTSGeometryException
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
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    @Test
    public void extractTest() throws NetworkException, OTSGeometryException
    {
        OTSPoint3D p0 = new OTSPoint3D(1, 2, 3);
        OTSPoint3D p1 = new OTSPoint3D(2, 3, 4);
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
                //System.err.println("i=" + i + ", j=" + j);
                e = l.extract(start, end);
                assertEquals("size of extract is 2", 2, e.size());
                assertEquals("x of 0", p0.x + (p1.x - p0.x) * i / 10, e.get(0).x, 0.0001);
                assertEquals("y of 0", p0.y + (p1.y - p0.y) * i / 10, e.get(0).y, 0.0001);
                assertEquals("z of 0", p0.z + (p1.z - p0.z) * i / 10, e.get(0).z, 0.0001);
                assertEquals("x of 1", p0.x + (p1.x - p0.x) * j / 10, e.get(1).x, 0.0001);
                assertEquals("y of 1", p0.y + (p1.y - p0.y) * j / 10, e.get(1).y, 0.0001);
                assertEquals("z of 1", p0.z + (p1.z - p0.z) * j / 10, e.get(1).z, 0.0001);
            }
        }

        l = new OTSLine3D(p0, p1, p2);
        for (int i = 0; i < 110; i++)
        {
            if (10 == i)
            {
                continue;   // results are not entirely predictable due to rounding errors
            }
            for (int j = i + 1; j < 110; j++)
            {
                if (10 == j)
                {
                    continue;   // results are not entirely predictable due to rounding errors
                }
                double start = i * l.getLengthSI() / 110;
                double end = j * l.getLengthSI() / 110;
                // System.err.println("first length is " + firstLength);
                // System.err.println("second length is " + l.getLengthSI());
                // System.err.println("i=" + i + ", j=" + j);
                e = l.extract(start, end);
                int expectedSize = i < 10 && j > 10 ? 3 : 2;
                assertEquals("size is " + expectedSize, expectedSize, e.size());
                if (i < 10)
                {
                    assertEquals("x of 0", p0.x + (p1.x - p0.x) * i / 10, e.get(0).x, 0.0001);
                    assertEquals("y of 0", p0.y + (p1.y - p0.y) * i / 10, e.get(0).y, 0.0001);
                    assertEquals("z of 0", p0.z + (p1.z - p0.z) * i / 10, e.get(0).z, 0.0001);
                }
                else
                {
                    assertEquals("x of 0", p1.x + (p2.x - p1.x) * (i - 10) / 100, e.get(0).x, 0.0001);
                    assertEquals("y of 0", p1.y + (p2.y - p1.y) * (i - 10) / 100, e.get(0).y, 0.0001);
                    assertEquals("z of 0", p1.z + (p2.z - p1.z) * (i - 10) / 100, e.get(0).z, 0.0001);
                }
                if (j < 10)
                {
                    assertEquals("x of 1", p0.x + (p1.x - p0.x) * j / 10, e.get(1).x, 0.0001);
                    assertEquals("y of 1", p0.y + (p1.y - p0.y) * j / 10, e.get(1).y, 0.0001);
                    assertEquals("z of 1", p0.z + (p1.z - p0.z) * j / 10, e.get(1).z, 0.0001);
                }
                else
                {
                    assertEquals("x of last", p1.x + (p2.x - p1.x) * (j - 10) / 100, e.getLast().x, 0.0001);
                    assertEquals("y of last", p1.y + (p2.y - p1.y) * (j - 10) / 100, e.getLast().y, 0.0001);
                    assertEquals("z of last", p1.z + (p2.z - p1.z) * (j - 10) / 100, e.getLast().z, 0.0001);
                }
                if (e.size() > 2)
                {
                    assertEquals("x of mid", p1.x, e.get(1).x, 0.0001);
                    assertEquals("y of mid", p1.y, e.get(1).y, 0.0001);
                    assertEquals("z of mid", p1.z, e.get(1).z, 0.0001);
                }
            }
        }

    }

}
