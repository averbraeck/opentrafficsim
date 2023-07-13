package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.junit.Test;

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
            DirectedPoint start = new DirectedPoint(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0, 0.0, 0.0,
                    (r.nextDouble() * 2 - 1) * Math.PI);
            DirectedPoint end = new DirectedPoint(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0, 0.0, 0.0,
                    (r.nextDouble() * 2 - 1) * Math.PI);
            ContinuousClothoid clothoid = new ContinuousClothoid(start, end);
            OtsLine3d line = clothoid.flatten(SEGMENTS);
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
            DirectedPoint start = new DirectedPoint(x, y, 0.0, 0.0, 0.0, ang - tolerance + r.nextDouble() * tolerance * 2);
            DirectedPoint end =
                    new DirectedPoint(3 * x, 3 * y, 0.0, 0.0, 0.0, ang - tolerance + r.nextDouble() * tolerance * 2);

            ContinuousClothoid clothoid = new ContinuousClothoid(start, end);
            OtsLine3d line = clothoid.flatten(SEGMENTS);
            assertEquals("Clothoid between point on line did not become a straight", line.size(), 2);

            start = new DirectedPoint(x, y, 0.0, 0.0, 0.0, ang + sign * tolerance * 1.1);
            end = new DirectedPoint(3 * x, 3 * y, 0.0, 0.0, 0.0, ang + sign * tolerance * 1.1);
            sign *= -1.0;
            clothoid = new ContinuousClothoid(start, end);
            line = clothoid.flatten(SEGMENTS);
            assertTrue("Clothoid between point just not on line should not become a straight", line.size() > 2);
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
            DirectedPoint start = new DirectedPoint(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0, 0.0, 0.0,
                    (r.nextDouble() * 2 - 1) * Math.PI);
            Length length = Length.instantiateSI(10.0 + r.nextDouble() * 500.0);
            double sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity startCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity endCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));

            ContinuousClothoid clothoid = ContinuousClothoid.withLength(start, length.si, startCurvature.si, endCurvature.si);
            OtsLine3d line = clothoid.flatten(SEGMENTS);
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
            DirectedPoint start = new DirectedPoint(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0, 0.0, 0.0,
                    (r.nextDouble() * 2 - 1) * Math.PI);
            double sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity startCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity endCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            Length a = Length
                    .instantiateSI(Math.sqrt((10.0 + r.nextDouble() * 500.0) / Math.abs(endCurvature.si - startCurvature.si)));

            ContinuousClothoid clothoid = new ContinuousClothoid(start, a.si, startCurvature.si, endCurvature.si);
            OtsLine3d line = clothoid.flatten(SEGMENTS);
            VerifyLine(start, clothoid, line, startCurvature, endCurvature, a);
        }
    }

    /**
     * Verifies a line by comparing theoretical and numerical values.
     * @param start OtsPoint3d; theoretical start point.
     * @param clothoid ClothoidInfo; created clothoid.
     * @param line OtsLine3d; flattened line.
     * @param startCurvature LinearDensity; start curvature, may be {@code null} if no theoretical value available.
     * @param endCurvature LinearDensity; end curvature, may be {@code null} if no theoretical value available.
     * @param a Length A-value, may be {@code null} if no theoretical value available.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    private void VerifyLine(final DirectedPoint start, final ContinuousClothoid clothoid, final OtsLine3d line,
            final LinearDensity startCurvature, final LinearDensity endCurvature, final Length a) throws OtsGeometryException
    {
        assertEquals("Start location deviates", 0.0, Math.hypot(start.x - line.get(0).x, start.y - line.get(0).y),
                DISTANCE_TOLERANCE);
        assertEquals("End location deviates", 0.0, Math.hypot(clothoid.getEndPoint().x - line.get(line.size() - 1).x,
                clothoid.getEndPoint().y - line.get(line.size() - 1).y), DISTANCE_TOLERANCE);
        assertEquals("Start direction deviates", 0.0, normalizeAngle(start.dirZ - getAngle(line, 0)), ANGLE_TOLERANCE);
        assertEquals("End direction deviates", 0.0,
                normalizeAngle(clothoid.getEndPoint().dirZ - getAngle(line, line.size() - 2)), ANGLE_TOLERANCE);
        double lengthRatio = line.getLength().si / clothoid.getLength();
        assertEquals("Length is more than 1% shorter or longer than theoretical", 1.0, lengthRatio, 0.01);
        if (startCurvature != null)
        {
            double curveatureRatio = clothoid.getStartCurvature() / startCurvature.si;
            assertEquals("Start curvature is more than 1% shorter or longer than theoretical", 1.0, curveatureRatio, 0.01);
        }
        if (endCurvature != null)
        {
            double curveatureRatio = clothoid.getEndCurvature() / endCurvature.si;
            assertEquals("End curvature is more than 1% shorter or longer than theoretical", 1.0, curveatureRatio, 0.01);
        }
        if (a != null)
        {
            double aRadius = clothoid.getA() / a.si;
            assertEquals("A-value is more than 1% less or more than theoretical", 1.0, aRadius, 0.01);
        }
    }

    /**
     * Return the angle from a line segment.
     * @param line OtsLine3d; line.
     * @param segment int; segment number.
     * @return double; angle of the line segment.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    private static double getAngle(final OtsLine3d line, final int segment) throws OtsGeometryException
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
