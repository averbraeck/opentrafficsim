package org.opentrafficsim.core.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Tests the generation of clothoids with various input.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContinuousClothoidTest
{

    /** Number of segments for the clothoid lines to generated. */
    private static final int SEGMENTS = 64;

    /** Number of random runs per test. */
    private static final int RUNS = 10000; // this test was run 10.000.000 times, 10.000 is to check no change broke the logic

    /**
     * A reasonable S-shaped clothoid can make a total angle transition of 2 circles, one on the positive, and one on the
     * negative side of the clothoid. With small radii and a large A-value, many more circles might be required. Test that
     * involve checking theoretical angles with resulting angles of the line endpoints, should use reasonable total angles for
     * both S-shaped and C-shaped clothoids.
     */
    private static final double ANGLE_TOLERANCE = 4 * Math.PI / SEGMENTS;

    /** Allowable distance between resulting and theoretical endpoints of a clothoid. */
    private static final double DISTANCE_TOLERANCE = 1e-2;

    /**
     * Tests whether clothoid between two directed points are correct.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    @Test
    public void testPoints() throws OtsGeometryException
    {
        StreamInterface r = new MersenneTwister(3L);
        for (int i = 0; i < RUNS; i++)
        {
            OrientedPoint2d start =
                    new OrientedPoint2d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, (r.nextDouble() * 2 - 1) * Math.PI);
            OrientedPoint2d end =
                    new OrientedPoint2d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, (r.nextDouble() * 2 - 1) * Math.PI);
            ContinuousClothoid clothoid = new ContinuousClothoid(start, end);
            PolyLine2d line = clothoid.flatten(new NumSegments(64));
            VerifyLine(start, clothoid, line, null, null, null);
        }
    }

    /**
     * Tests whether clothoid between two directed points on a line, or just not on a line, are correct. This test is separate
     * from {@code TestPoints()} because the random procedure generates very few straight situations.
     */
    @Test
    public void testStraight()
    {
        StreamInterface r = new MersenneTwister(3L);
        double tolerance = 2.0 * Math.PI / 3600.0; // see ContinuousClothoid.ANGLE_TOLERANCE
        double startAng = -Math.PI;
        double dAng = Math.PI * 2 / 100;
        double sign = 1.0;
        for (double ang = startAng; ang < Math.PI; ang += dAng)
        {
            double x = Math.cos(ang);
            double y = Math.sin(ang);
            OrientedPoint2d start = new OrientedPoint2d(x, y, ang - tolerance + r.nextDouble() * tolerance * 2);
            OrientedPoint2d end = new OrientedPoint2d(3 * x, 3 * y, ang - tolerance + r.nextDouble() * tolerance * 2);

            ContinuousClothoid clothoid = new ContinuousClothoid(start, end);
            NumSegments numSegments64 = new NumSegments(64);
            PolyLine2d line = clothoid.flatten(numSegments64);
            assertEquals(line.size(), 2, "Clothoid between point on line did not become a straight");

            start = new OrientedPoint2d(x, y, ang + sign * tolerance * 1.1);
            end = new OrientedPoint2d(3 * x, 3 * y, ang + sign * tolerance * 1.1);
            sign *= -1.0;
            clothoid = new ContinuousClothoid(start, end);
            line = clothoid.flatten(numSegments64);
            assertTrue(line.size() > 2, "Clothoid between point just not on line should not become a straight");
        }
    }

    /**
     * Test clothoids created with curvatures and a length.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    @Test
    public void testLength() throws OtsGeometryException
    {
        StreamInterface r = new MersenneTwister(3L);
        for (int i = 0; i < RUNS; i++)
        {
            OrientedPoint2d start =
                    new OrientedPoint2d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, (r.nextDouble() * 2 - 1) * Math.PI);
            Length length = Length.instantiateSI(10.0 + r.nextDouble() * 500.0);
            double sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity startCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity endCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));

            ContinuousClothoid clothoid = ContinuousClothoid.withLength(start, length.si, startCurvature.si, endCurvature.si);
            PolyLine2d line = clothoid.flatten(new NumSegments(64));
            VerifyLine(start, clothoid, line, startCurvature, endCurvature, null);
        }
    }

    /**
     * Test clothoids created with curvatures and an A-value.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    @Test
    public void testA() throws OtsGeometryException
    {
        StreamInterface r = new MersenneTwister(3L);
        for (int i = 0; i < RUNS; i++)
        {
            OrientedPoint2d start =
                    new OrientedPoint2d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, (r.nextDouble() * 2 - 1) * Math.PI);
            double sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity startCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity endCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            Length a = Length
                    .instantiateSI(Math.sqrt((10.0 + r.nextDouble() * 500.0) / Math.abs(endCurvature.si - startCurvature.si)));

            ContinuousClothoid clothoid = new ContinuousClothoid(start, a.si, startCurvature.si, endCurvature.si);
            PolyLine2d line = clothoid.flatten(new NumSegments(64));
            VerifyLine(start, clothoid, line, startCurvature, endCurvature, a);
        }
    }

    /**
     * Verifies a line by comparing theoretical and numerical values.
     * @param start OtsPoint3d; theoretical start point.
     * @param clothoid ClothoidInfo; created clothoid.
     * @param line PolyLine2d; flattened line.
     * @param startCurvature LinearDensity; start curvature, may be {@code null} if no theoretical value available.
     * @param endCurvature LinearDensity; end curvature, may be {@code null} if no theoretical value available.
     * @param a Length A-value, may be {@code null} if no theoretical value available.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    private void VerifyLine(final OrientedPoint2d start, final ContinuousClothoid clothoid, final PolyLine2d line,
            final LinearDensity startCurvature, final LinearDensity endCurvature, final Length a) throws OtsGeometryException
    {
        assertEquals(0.0, Math.hypot(start.x - line.get(0).x, start.y - line.get(0).y), DISTANCE_TOLERANCE,
                "Start location deviates");
        assertEquals(0.0, Math.hypot(clothoid.getEndPoint().x - line.get(line.size() - 1).x,
                clothoid.getEndPoint().y - line.get(line.size() - 1).y), DISTANCE_TOLERANCE, "End location deviates");
        assertEquals(0.0, normalizeAngle(start.dirZ - getAngle(line, 0)), ANGLE_TOLERANCE, "Start direction deviates");
        assertEquals(0.0, normalizeAngle(clothoid.getEndPoint().dirZ - getAngle(line, line.size() - 2)), ANGLE_TOLERANCE,
                "End direction deviates");
        double lengthRatio = line.getLength() / clothoid.getLength();
        assertEquals(1.0, lengthRatio, 0.01, "Length is more than 1% shorter or longer than theoretical");
        if (startCurvature != null)
        {
            double curveatureRatio = clothoid.getStartCurvature() / startCurvature.si;
            assertEquals(1.0, curveatureRatio, 0.01, "Start curvature is more than 1% shorter or longer than theoretical");
        }
        if (endCurvature != null)
        {
            double curveatureRatio = clothoid.getEndCurvature() / endCurvature.si;
            assertEquals(1.0, curveatureRatio, 0.01, "End curvature is more than 1% shorter or longer than theoretical");
        }
        if (a != null)
        {
            double aRadius = clothoid.getA() / a.si;
            assertEquals(1.0, aRadius, 0.01, "A-value is more than 1% less or more than theoretical");
        }
    }

    /**
     * Tests that a clothoid offset is on the right side and at the right direction, for clothoids that are reflected or not,
     * and clothoids that are opposite or not.
     */
    @Test
    public void testOffset()
    {
        Flattener flattener = new NumSegments(32);
        // point A somewhere on y-axis 
        for (double yA = -30.0; yA < 35.0; yA += 20.0)
        {
            // point B somewhere on x-axis
            for (double xB = -20.0; xB < 25.0; xB += 20.0 * 2.0 / 3.0)
            {
                // point A pointing left/right towards B
                OrientedPoint2d a = new OrientedPoint2d(0.0, yA, xB < 0.0 ? Math.PI : 0.0);
                // point B pointing up/down away from A
                OrientedPoint2d b = new OrientedPoint2d(xB, 0.0, yA < 0.0 ? Math.PI / 2 : -Math.PI / 2);
                ContinuousClothoid clothoid = new ContinuousClothoid(a, b);
                // offset -2.0 or 2.0
                for (double offset = -2.0; offset < 3.0; offset += 4.0)
                {
                    PolyLine2d line = clothoid.flattenOffset(new FractionalLengthData(0.0, offset, 1.0, offset), flattener);
                    Point2d start = line.get(0);
                    Point2d end = line.get(line.size() - 1);
                    assertEquals(0.0, start.x, 0.00001); // offset on y-axis
                    assertEquals(yA + (xB > 0.0 ? offset : -offset), start.y, 0.00001); // offset above or below
                    assertEquals(xB + (yA > 0.0 ? offset : -offset), end.x, 0.00001); // offset left or right
                    assertEquals(0.0, end.y, 0.00001); // offset on x-axis
                }
            }
        }
    }

    /**
     * Return the angle from a line segment.
     * @param line PolyLine2d; line.
     * @param segment int; segment number.
     * @return double; angle of the line segment.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    private static double getAngle(final PolyLine2d line, final int segment) throws OtsGeometryException
    {
        return Math.atan2(line.get(segment + 1).y - line.get(segment).y, line.get(segment + 1).x - line.get(segment).x);
    }

    /**
     * Normalizes the angle to be in the range [-pi pi].
     * @param angle double; angle.
     * @return double; angle in the range [-pi pi].
     */
    private static double normalizeAngle(final double angle)
    {
        double out = angle;
        while (out > Math.PI)
        {
            out -= 2 * Math.PI;
        }
        while (out < -Math.PI)
        {
            out += 2 * Math.PI;
        }
        return out;
    }

}
