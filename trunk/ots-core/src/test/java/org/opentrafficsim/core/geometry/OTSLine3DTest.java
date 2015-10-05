package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
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
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class OTSLine3DTest
{
    /**
     * Test the constructors of OTSLine3D.
     * @throws OTSGeometryException
     * @throws NetworkException
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
     * @throws OTSGeometryException
     * @throws NetworkException
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
     * @throws NetworkException
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
     * Test the getLocationExtended method & friends.
     * @throws NetworkException
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
     * @throws NetworkException
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
     * @throws NetworkException
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
        l = new OTSLine3D(new OTSPoint3D[] { p1, p0, p2 }); // some argument swapped
        dp = l.getLocation();
        assertEquals("centroid x", 50, dp.x, 0.001);
        assertEquals("centroid y", 45, dp.y, 0.001);
        assertEquals("centroid z", 75, dp.z, 0.001);
        l = new OTSLine3D(new OTSPoint3D[] { p0, p1 }); // all in same Z-plane
        dp = l.getLocation();
        assertEquals("centroid x", 25, dp.x, 0.001);
        assertEquals("centroid y", 35, dp.y, 0.001);
        assertEquals("centroid z", 60, dp.z, 0.001);
    }

}
