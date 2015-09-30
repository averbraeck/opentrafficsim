package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.CartesianPoint;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test the methods in OTSPoint.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 sep. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSPoint3DTest
{
    /**
     * Test the various constructors of OTSPoint3D
     */
    @Test
    public void constructorsTest()
    {
        OTSPoint3D previousPoint = null;
        int previousHashCode = 0;
        double[] values =
                { Double.NEGATIVE_INFINITY, -99999999, -Math.PI, -1, -0.0000001, 0, 0.0000001, 1, Math.PI, 99999999,
                        Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN };
        for (double x : values)
        {
            for (double y : values)
            {
                for (double z : values)
                {
                    OTSPoint3D p = new OTSPoint3D(x, y, z);
                    checkXYZ(p, x, y, z);
                    checkXYZ(new OTSPoint3D(new double[] { x, y, z }), x, y, z);
                    checkXYZ(new OTSPoint3D(p), x, y, z);
                    checkXYZ(new OTSPoint3D(new double[] { x, y }), x, y, 0d);
                    checkXYZ(new OTSPoint3D(new Point3d(x, y, z)), x, y, z);
                    checkXYZ(new OTSPoint3D(new CartesianPoint(x, y, z)), x, y, z);
                    checkXYZ(new OTSPoint3D(new DirectedPoint(x, y, z)), x, y, z);
                    checkXYZ(new OTSPoint3D(new Point2D.Double(x, y)), x, y, 0d);
                    checkXYZ(new OTSPoint3D(new Coordinate(x, y)), x, y, 0d);
                    checkXYZ(new OTSPoint3D(new Coordinate(x, y, Double.NaN)), x, y, 0d);
                    checkXYZ(new OTSPoint3D(new Coordinate(x, y, z)), x, y, Double.isNaN(z) ? 0d : z);
                    // Point pp = new com.vividsolutions.jts.geom.Point(new Coordinate(x, y), precisionModel, SRID)
                    // checkXYZ(new OTSPoint3D(new Point(x, y)), x, y, 0d);
                    checkXYZ(new OTSPoint3D(x, y), x, y, 0d);
                    // Also check the getCoordinate method
                    Coordinate c = p.getCoordinate();
                    assertEquals("x value", x, c.x, Math.ulp(x));
                    assertEquals("y value", y, c.y, Math.ulp(y));
                    assertEquals("z value", z, c.z, Math.ulp(z));
                    DirectedPoint dp = p.getDirectedPoint();
                    assertEquals("x value", x, dp.x, Math.ulp(x));
                    assertEquals("y value", y, dp.y, Math.ulp(y));
                    assertEquals("z value", z, dp.z, Math.ulp(z));

                    String s = p.toString();
                    assertNotNull("toString returns something", s);
                    assertTrue("toString returns string of reasonable length", s.length() > 10);
                    int hashCode = p.hashCode();
                    // A collision with the previous hashCode is extremely unlikely.
                    assertFalse("Hash code should be different", previousHashCode == hashCode);
                    previousHashCode = hashCode;
                    // Building a set of all seen hash codes and checking against that actually gives a collision!
                    assertFalse("Successively generated points are all different", p.equals(previousPoint));
                    assertTrue("Point is equal to itself", p.equals(p));
                    assertTrue("Point is equals to duplicate of itself", p.equals(new OTSPoint3D(p)));
                    assertFalse("Point is not equal to some other object", p.equals(s));
                    previousPoint = p;
                }
            }
        }
    }

    /**
     * Check the x, y and z of a OTS3DCoordinate.
     * @param otsPoint3D OTS3DCoordinate; the coordinate to check
     * @param expectedX double; the expected x coordinate
     * @param expectedY double; the expected y coordinate
     * @param expectedZ double; the expected z coordinate
     */
    private void checkXYZ(OTSPoint3D otsPoint3D, double expectedX, double expectedY, double expectedZ)
    {
        assertEquals("x value", expectedX, otsPoint3D.x, Math.ulp(expectedX));
        assertEquals("y value", expectedY, otsPoint3D.y, Math.ulp(expectedY));
        assertEquals("z value", expectedZ, otsPoint3D.z, Math.ulp(expectedZ));
    }

}
