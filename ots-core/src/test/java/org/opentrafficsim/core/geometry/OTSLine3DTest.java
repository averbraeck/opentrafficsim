package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
     */
    @Test
    public void constructorsTest() throws OTSGeometryException
    {
        double[] values = { -999, 0, 99, 9999 }; // Keep this list short; execution time grows with 9th power of length
        OTSPoint3D[] points = new OTSPoint3D[0]; // Empty array
        runConstructors(points);
        for (double x0 : values)
        {
            for (double y0 : values)
            {
                for (double z0 : values)
                {
                    points = new OTSPoint3D[1]; // Degenerate array holding one point
                    points[0] = new OTSPoint3D(x0, y0, z0);
                    runConstructors(points);
                    for (double x1 : values)
                    {
                        for (double y1 : values)
                        {
                            for (double z1 : values)
                            {
                                points = new OTSPoint3D[2]; // Straight line; two points
                                points[0] = new OTSPoint3D(x0, y0, z0);
                                points[1] = new OTSPoint3D(x1, y1, z1);
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

    /**
     * Test all the constructors of OTSPoint3D.
     * @param points OTSPoint3D[]; array of OTSPoint3D to test with
     * @throws OTSGeometryException
     */
    private void runConstructors(final OTSPoint3D[] points) throws OTSGeometryException
    {
        verifyPoints(new OTSLine3D(points), points);
        Coordinate[] coordinates = new Coordinate[points.length];
        for (int i = 0; i < points.length; i++)
        {
            coordinates[i] = new Coordinate(points[i].x, points[i].y, points[i].z);
        }
        verifyPoints(new OTSLine3D(coordinates), points);
        if (points.length > 1)
        {
            GeometryFactory gm = new GeometryFactory();
            LineString lineString = gm.createLineString(coordinates);
            verifyPoints(new OTSLine3D(lineString), points);
            verifyPoints(new OTSLine3D((Geometry) lineString), points);
        }
        List<OTSPoint3D> list = new ArrayList<>();
        for (int i = 0; i < points.length; i++)
        {
            list.add(points[i]);
        }
        OTSLine3D line = new OTSLine3D(list);
        verifyPoints(line, points);
        // Convert it to Coordinate[], create another OTSLine3D from that and check that
        verifyPoints(new OTSLine3D(line.getCoordinates()), points);
        if (points.length > 1)
        {
            // Convert it to a LineString, create another OTSLine3D from that and check that
            verifyPoints(new OTSLine3D(line.getLineString()), points);
        }
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
     */
    @Test
    public void exceptionTest()
    {
        OTSLine3D line = new OTSLine3D(new OTSPoint3D[0]);
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
            line.get(0);
            fail("Should have thrown an exception");
        }
        catch (OTSGeometryException oe)
        {
            // Ignore expected exception
        }
        try
        {
            line.get(1);
            fail("Should have thrown an exception");
        }
        catch (OTSGeometryException oe)
        {
            // Ignore expected exception
        }
        line = new OTSLine3D(new OTSPoint3D[] { new OTSPoint3D(1, 2, 3), new OTSPoint3D(4, 5, 6) });
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

}
