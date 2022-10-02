package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.Point3d;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * Test the methods in OTSPoint.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OtsPoint3DTest
{
    /**
     * Test the various constructors of OTSPoint3D.
     */
    @Test
    public final void constructorsTest()
    {
        OtsPoint3D previousPoint = null;
        int previousHashCode = 0;
        double[] values = {Double.NEGATIVE_INFINITY, -99999999, -Math.PI, -1, -0.0000001, 0, 0.0000001, 1, Math.PI, 99999999,
                Double.MAX_VALUE, Double.POSITIVE_INFINITY};
        for (double x : values)
        {
            for (double y : values)
            {
                for (double z : values)
                {
                    OtsPoint3D p = new OtsPoint3D(x, y, z);
                    checkXYZ(p, x, y, z);
                    checkXYZ(new OtsPoint3D(new double[] {x, y, z}), x, y, z);
                    checkXYZ(new OtsPoint3D(p), x, y, z);
                    checkXYZ(new OtsPoint3D(new double[] {x, y}), x, y, 0d);
                    checkXYZ(new OtsPoint3D(new Point3d(x, y, z)), x, y, z);
                    checkXYZ(new OtsPoint3D(new CartesianPoint(x, y, z)), x, y, z);
                    checkXYZ(new OtsPoint3D(new DirectedPoint(x, y, z)), x, y, z);
                    checkXYZ(new OtsPoint3D(new Point2D.Double(x, y)), x, y, 0d);
                    checkXYZ(new OtsPoint3D(new Coordinate(x, y)), x, y, 0d);
                    checkXYZ(new OtsPoint3D(new Coordinate(x, y, Double.NaN)), x, y, 0d);
                    checkXYZ(new OtsPoint3D(new Coordinate(x, y, z)), x, y, Double.isNaN(z) ? 0d : z);
                    GeometryFactory gm = new GeometryFactory();
                    checkXYZ(new OtsPoint3D(gm.createPoint(new Coordinate(x, y, z))), x, y, 0d);
                    checkXYZ(new OtsPoint3D(x, y), x, y, 0d);
                    // Also check the getCoordinate method
                    Coordinate c = p.getCoordinate();
                    assertEquals("x value", x, c.x, Math.ulp(x));
                    assertEquals("y value", y, c.y, Math.ulp(y));
                    assertEquals("z value", z, c.getZ(), Math.ulp(z));
                    DirectedPoint dp = p.getDirectedPoint();
                    assertEquals("x value", x, dp.x, Math.ulp(x));
                    assertEquals("y value", y, dp.y, Math.ulp(y));
                    assertEquals("z value", z, dp.z, Math.ulp(z));
                    double qX = 100;
                    double qY = 200;
                    double qZ = 300;
                    OtsPoint3D q = new OtsPoint3D(qX, qY, qZ);
                    double expectedDistance = Math.sqrt(Math.pow(x - qX, 2) + Math.pow(y - qY, 2) + Math.pow(z - qZ, 2));
                    assertEquals("Distance to q should be " + expectedDistance, expectedDistance, p.distance(q).si,
                            expectedDistance / 99999);
                    Bounds bounds = p.getBounds();
                    DirectedPoint directedPoint = p.getLocation();
                    assertEquals("Location returns a DirectedPoint at the location of p", x, directedPoint.x, Math.ulp(x));
                    assertEquals("Location returns a DirectedPoint at the location of p", y, directedPoint.y, Math.ulp(y));
                    assertEquals("Location returns a DirectedPoint at the location of p", z, directedPoint.z, Math.ulp(z));
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
                    assertTrue("Point is equals to duplicate of itself", p.equals(new OtsPoint3D(p)));
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
    private void checkXYZ(final OtsPoint3D otsPoint3D, final double expectedX, final double expectedY, final double expectedZ)
    {
        assertEquals("x value", expectedX, otsPoint3D.x, Math.ulp(expectedX));
        assertEquals("y value", expectedY, otsPoint3D.y, Math.ulp(expectedY));
        assertEquals("z value", expectedZ, otsPoint3D.z, Math.ulp(expectedZ));
        Point2D.Double p = (Point2D.Double) otsPoint3D.getPoint2D();
        assertEquals("x value", expectedX, p.x, Math.ulp(expectedX));
        assertEquals("y value", expectedY, p.y, Math.ulp(expectedY));
    }

    /**
     * Test the interpolate method.
     */
    @Test
    public final void interpolateTest()
    {
        OtsPoint3D p0 = new OtsPoint3D(123, 234, 345);
        OtsPoint3D p1 = new OtsPoint3D(567, 678, 789);
        for (double ratio : new double[] {0, 1, 0.5, 0.1, -10, 10})
        {
            OtsPoint3D pi = OtsPoint3D.interpolate(ratio, p0, p1);
            assertTrue("result of interpolate is not null", null != pi);
            assertEquals("x of interpolate", (1 - ratio) * p0.x + ratio * p1.x, pi.x, 0.00001);
            assertEquals("y of interpolate", (1 - ratio) * p0.y + ratio * p1.y, pi.y, 0.00001);
            assertEquals("z of interpolate", (1 - ratio) * p0.z + ratio * p1.z, pi.z, 0.00001);
        }
    }

    /**
     * Test the closestPointOnLine methods.
     * @throws OtsGeometryException should not happen; this test has failed if it does
     */
    @Test
    public final void closestPointTest() throws OtsGeometryException
    {
        // Approximate a spiral centered on 0,0 with increasing Z
        final int numPoints = 100;
        final double growthPerRevolution = 5;
        final double heightGainPerPoint = 10;
        final double pointsPerRevolution = 15;
        OtsPoint3D[] spiralPoints = new OtsPoint3D[numPoints];
        final double rotationPerPoint = 2 * Math.PI / pointsPerRevolution;
        final double maxRevolution = 1.0 * numPoints / pointsPerRevolution;
        for (int i = 0; i < numPoints; i++)
        {
            double radius = i * growthPerRevolution / pointsPerRevolution;
            spiralPoints[i] = new OtsPoint3D(radius * Math.cos(i * rotationPerPoint), radius * Math.sin(i * rotationPerPoint),
                    i * heightGainPerPoint);
        }
        OtsLine3D line = new OtsLine3D(spiralPoints);
        // System.out.println("line is " + line);
        for (double x = 0; x < maxRevolution * growthPerRevolution; x += growthPerRevolution)
        {
            OtsPoint3D point = new OtsPoint3D(x, 0, 0);
            OtsPoint3D result = point.closestPointOnLine2D(line);
            // System.out.printf("2D x=%.2f, point=%s, result=%s\n", x, point, result);
            assertEquals("distance to spiral is 0", 0, point.horizontalDistanceSI(result), 0.0001);
            result = point.closestPointOnLine(line);
            // System.out.printf("3D x=%.2f, point=%s, result=%s\n", x, point, result);
            double distance = point.horizontalDistanceSI(result);
            assertEquals("horizontal distance to spiral is x", x, distance, 0.5);
            // Check the horizontalDistance method
            Length horizontalDistance = point.horizontalDistance(result);
            assertEquals("horizontal distance as Length should match result of horizontalDistanceSI", distance,
                    horizontalDistance.si, Math.ulp(distance));
        }
        // TODO: extend by testing at a few other elevations.
    }

    /**
     * Test the 2D line segment intersection method.
     */
    @Test
    public final void lineSegmentIntersectionTest()
    {
        Random doubleRandom = new Random(12345);
        for (double xTranslation = -20; xTranslation <= 20; xTranslation += 10)
        {
            for (double yTranslation = -20; yTranslation <= 20; yTranslation += 10)
            {
                for (double rotation = 0; rotation < 2 * Math.PI; rotation += 0.5)
                {
                    OtsPoint3D p1 = makeRotatedTranslatedPoint(new OtsPoint3D(-2, 0, 100 * (doubleRandom.nextDouble() - 0.5)),
                            rotation, xTranslation, yTranslation);
                    OtsPoint3D p2 = makeRotatedTranslatedPoint(new OtsPoint3D(2, 0, 100 * (doubleRandom.nextDouble() - 0.5)),
                            rotation, xTranslation, yTranslation);
                    OtsPoint3D q1 = makeRotatedTranslatedPoint(new OtsPoint3D(0, 10, 100 * (doubleRandom.nextDouble() - 0.5)),
                            rotation, xTranslation, yTranslation);

                    for (int x = -4; x <= 4; x++)
                    {
                        OtsPoint3D q2 =
                                makeRotatedTranslatedPoint(new OtsPoint3D(x, -1, 100 * (doubleRandom.nextDouble() - 0.5)),
                                        rotation, xTranslation, yTranslation);
                        boolean shouldBeNull = x < -2 || x > 2;
                        checkIntersection(shouldBeNull, OtsPoint3D.intersectionOfLineSegments(p1, p2, q1, q2));
                        // reverse order of q
                        checkIntersection(shouldBeNull, OtsPoint3D.intersectionOfLineSegments(p1, p2, q2, q1));
                        // reverse order of p
                        checkIntersection(shouldBeNull, OtsPoint3D.intersectionOfLineSegments(p2, p1, q1, q2));
                        // reverse order of both p and q
                        checkIntersection(shouldBeNull, OtsPoint3D.intersectionOfLineSegments(p2, p1, q2, q1));
                        q2 = makeRotatedTranslatedPoint(new OtsPoint3D(x, 1, 100 * (doubleRandom.nextDouble() - 0.5)), rotation,
                                xTranslation, yTranslation);
                        checkIntersection(true, OtsPoint3D.intersectionOfLineSegments(p1, p2, q1, q2));
                        // reverse order of q
                        checkIntersection(true, OtsPoint3D.intersectionOfLineSegments(p1, p2, q2, q1));
                        // reverse order of p
                        checkIntersection(true, OtsPoint3D.intersectionOfLineSegments(p2, p1, q1, q2));
                        // reverse order of both p and q
                        checkIntersection(true, OtsPoint3D.intersectionOfLineSegments(p2, p1, q2, q1));
                    }
                }
            }
        }
    }

    /**
     * Create a rotated and translated point.
     * @param p OTSPoint3D; the point before rotation and translation
     * @param rotation double; rotation in radians
     * @param dX double; translation along X direction
     * @param dY double; translation along Y direction
     * @return OTSPoint3D
     */
    private OtsPoint3D makeRotatedTranslatedPoint(final OtsPoint3D p, final double rotation, final double dX, final double dY)
    {
        double sin = Math.sin(rotation);
        double cos = Math.cos(rotation);
        return new OtsPoint3D((p.x * cos + p.y * sin) + dX, (p.y * cos - p.x * cos) + dY, p.z);
    }

    /**
     * Helper for lineSegmentIntersectionTest.
     * @param expectNull boolean; if true; the other parameter should be null; if false; the other parameter should be true
     * @param point OTSPoint3D; an OTSPoint3D or null
     */
    private void checkIntersection(final boolean expectNull, final OtsPoint3D point)
    {
        if (expectNull)
        {
            if (null != point)
            {
                System.out.println("problem");
            }
            assertNull("there should be an intersection", point);
        }
        else
        {
            if (null == point)
            {
                System.out.println("problem");
            }
            assertNotNull("There should not be an intersection", point);
        }
    }

    /**
     * Test the horizontalDirection and horizontalDirectionSI methods.
     */
    @Test
    public void directionTest()
    {
        for (OtsPoint3D reference : new OtsPoint3D[] {new OtsPoint3D(0, 0, 0), new OtsPoint3D(100, 200, 300),
                new OtsPoint3D(-50, -80, 20)})
        {
            for (double actualDirectionDegrees : new double[] {0, 0.1, 10, 30, 89, 90, 91, 150, 179, 181, 269, 271, 359})
            {
                double actualDirectionRadians = Math.toRadians(actualDirectionDegrees);
                for (double actualDistance : new double[] {0.001, 10, 999, 99999})
                {
                    for (double dZ : new double[] {0, 123, 98766, -876})
                    {
                        OtsPoint3D other = new OtsPoint3D(reference.x + Math.cos(actualDirectionRadians) * actualDistance,
                                reference.y + Math.sin(actualDirectionRadians) * actualDistance, reference.z + dZ);
                        double angle = reference.horizontalDirectionSI(other);
                        double angle2 = reference.horizontalDirection(other).si;
                        if (angle < 0)
                        {
                            angle += 2 * Math.PI;
                        }
                        if (angle2 < 0)
                        {
                            angle2 += 2 * Math.PI;
                        }
                        assertEquals("horizontalDirectionSI returns correct direction", actualDirectionRadians, angle, 0.01);
                        assertEquals("horizontalDirection returns correct direction", actualDirectionRadians, angle2, 0.01);
                    }
                }
            }
        }
    }

    /**
     * Test the circleCenter method (and, implicitly, the normalize method).
     */
    @Test
    public void circleCenterTest()
    {
        OtsPoint3D p1 = new OtsPoint3D(1, 0, 0);
        OtsPoint3D p2 = new OtsPoint3D(-1, 0, 0);
        assertEquals("Radius too short returns empty list", 0, OtsPoint3D.circleCenter(p1, p2, 0.999).size());
        List<OtsPoint3D> list = OtsPoint3D.circleCenter(p1, p2, 1);
        assertEquals("Radius exactly right and nice integer coordinates returns list with one point", 1, list.size());
        // Now try it with less neat values (but don't expect to get a list of one point)
        for (OtsPoint3D reference : new OtsPoint3D[] {new OtsPoint3D(0, 0, 0), new OtsPoint3D(100, 200, 300),
                new OtsPoint3D(-50, -80, 20)})
        {
            for (double actualDirectionDegrees : new double[] {0, 0.1, 10, 30, 89, 90, 91, 150, 179, 181, 269, 271, 359})
            {
                double actualDirectionRadians = Math.toRadians(actualDirectionDegrees);
                for (double horizontalDistance : new double[] {0.1, 10, 999, 99999})
                {
                    for (double dZ : new double[] {0, 123, -876})
                    {
                        OtsPoint3D other = new OtsPoint3D(reference.x + Math.cos(actualDirectionRadians) * horizontalDistance,
                                reference.y + Math.sin(actualDirectionRadians) * horizontalDistance, reference.z + dZ);
                        double actualDistance = reference.distanceSI(other);
                        list = OtsPoint3D.circleCenter(reference, other, actualDistance * 0.499);
                        assertEquals("Radius too short returns empty list", 0, list.size());
                        System.out.println(String.format("actualDistance=%f", actualDistance));
                        double factor = Math.sqrt(0.5);
                        list = OtsPoint3D.circleCenter(reference, other, actualDistance * factor);
                        assertEquals("Radius slightly larger returns list with 2 elements", 2, list.size());
                        for (OtsPoint3D p : list)
                        {
                            System.out.println(String.format("ref=%s, oth=%s p=%s, distance should be %f, got %f", reference,
                                    other, p, actualDistance * factor, reference.distanceSI(p)));
                            assertEquals("Z is average of input points", (reference.z + other.z) / 2, p.z, 0.001);
                            assertEquals("horizontal distance from reference is R", actualDistance * factor,
                                    reference.distanceSI(p), 0.001);
                            System.out.println(String.format("ref=%s, oth=%s p=%s, distance should be %f, got %f", reference,
                                    other, p, actualDistance * factor, other.distanceSI(p)));
                            assertEquals("horizontal distance from other is R", actualDistance * factor, other.distanceSI(p),
                                    0.001);
                        }
                    }
                }
            }
        }
    }

    /**
     * Test the circleIntersections method.
     */
    @Test
    public void circleIntersectionsTest()
    {
        OtsPoint3D c1 = new OtsPoint3D(10, 20, 30);
        OtsPoint3D c2 = new OtsPoint3D(20, 10, 0);
        double centerDistance = c1.distanceSI(c2);
        for (int r1 = 0; r1 < 50; r1++)
        {
            for (int r2 = 0; r2 < 50; r2++)
            {
                List<OtsPoint3D> intersections = OtsPoint3D.circleIntersections(c1, r1, c2, r2);
                if (centerDistance < r1 + r2)
                {
                    // TODO (this fails) assertEquals("There should be 0 intersections", 0, intersections.size());
                }
                if (centerDistance > r1 + r2)
                {
                    if (intersections.size() != 2)
                    {
                        System.out.println(
                                String.format("sphere 1: center %s radius %d, sphere2: center %s radius %d", c1, r1, c2, r2));
                        for (int index = 0; index < intersections.size(); index++)
                        {
                            System.out.println("  result " + index + ": " + intersections.get(index));
                        }
                        OtsPoint3D.circleIntersections(c1, r1, c2, r2);
                    }
                    // TODO (this fails) assertEquals("There should be 2 intersections", 2, intersections.size());
                }
            }
        }
    }

}
