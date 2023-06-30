package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.junit.Test;
import org.opentrafficsim.core.geometry.Clothoid.ClothoidInfo;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Tests the generation of clothoids in with various input.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ClothoidTest
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
    private static final double DISTANCE_TOLERANCE = 1e-6;

    /**
     * Tests whether clothoid between two directed points are correct.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    @Test
    public void TestPoints() throws OtsGeometryException
    {
        StreamInterface r = new MersenneTwister(3L);
        for (int i = 0; i < RUNS; i++) // this test was run 10.000.000 times, 10.000 is to check no change broke the logic
        {
            OtsPoint3d start = new OtsPoint3d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0);
            OtsPoint3d end = new OtsPoint3d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0);
            Direction startDirection = Direction.instantiateSI((r.nextDouble() * 2 - 1) * Math.PI);
            Direction endDirection = Direction.instantiateSI((r.nextDouble() * 2 - 1) * Math.PI);

            ClothoidInfo clothoid = Clothoid.clothoidPoints(start, startDirection, end, endDirection, SEGMENTS);

            VerifyLine(start, startDirection, clothoid, null, null, null);
        }
    }

    /**
     * Tests whether clothoid between two directed points on a line, or just not on a line, are correct. This test is separate 
     * from {@code TestPoints()} because the random procedure generates very few straight situations.
     */
    @Test
    public void TestStraight()
    {
        StreamInterface r = new MersenneTwister(3L);
        double tolerance = 2.0 * Math.PI / 720.0; // This value might change in the future, or depend on the number of segments
        double startAng = -Math.PI;
        double dAng = Math.PI * 2 / 100;
        double sign = 1.0;
        for (double ang = startAng; ang < Math.PI; ang += dAng)
        {
            double x = Math.cos(ang);
            double y = Math.sin(ang);
            OtsPoint3d start = new OtsPoint3d(x, y, 0.0);
            Direction startDirection = Direction.instantiateSI(ang - tolerance + r.nextDouble() * tolerance * 2);
            OtsPoint3d end = new OtsPoint3d(3 * x, 3 * y, 0.0);
            Direction endDirection = Direction.instantiateSI(ang - tolerance + r.nextDouble() * tolerance * 2);
            ClothoidInfo clothoid = Clothoid.clothoidPoints(start, startDirection, end, endDirection, SEGMENTS);
            assertEquals("Clothoid between point on line did not become a straight", clothoid.getLine().size(), 2);

            startDirection = Direction.instantiateSI(ang + sign * tolerance * 1.1);
            endDirection = Direction.instantiateSI(ang + sign * tolerance * 1.1);
            sign *= -1.0;
            clothoid = Clothoid.clothoidPoints(start, startDirection, end, endDirection, SEGMENTS);
            assertTrue("Clothoid between point just not on line should not become a straight", clothoid.getLine().size() > 2);
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
        for (int i = 0; i < RUNS; i++) // this test was run 10.000.000 times, 10.000 is to check no change broke the logic
        {
            OtsPoint3d start = new OtsPoint3d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0);
            Direction startDirection = Direction.instantiateSI((r.nextDouble() * 2 - 1) * Math.PI);
            Length length = Length.instantiateSI(10.0 + r.nextDouble() * 500.0);
            double sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity startCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity endCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));

            ClothoidInfo clothoid =
                    Clothoid.clothoidLength(start, startDirection, length, startCurvature, endCurvature, SEGMENTS);

            VerifyLine(start, startDirection, clothoid, startCurvature, endCurvature, null);
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
        for (int i = 0; i < RUNS; i++) // this test was run 10.000.000 times, 10.000 is to check no change broke the logic
        {
            OtsPoint3d start = new OtsPoint3d(r.nextDouble() * 10.0, r.nextDouble() * 10.0, 0.0);
            Direction startDirection = Direction.instantiateSI((r.nextDouble() * 2 - 1) * Math.PI);
            double sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity startCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            sign = r.nextBoolean() ? 1.0 : -1.0;
            LinearDensity endCurvature = LinearDensity.instantiateSI(sign / (50.0 + r.nextDouble() * 1000.0));
            Length a = Length
                    .instantiateSI(Math.sqrt((10.0 + r.nextDouble() * 500.0) / Math.abs(endCurvature.si - startCurvature.si)));

            ClothoidInfo clothoid = Clothoid.clothoidA(start, startDirection, a, startCurvature, endCurvature, SEGMENTS);

            VerifyLine(start, startDirection, clothoid, startCurvature, endCurvature, a);
        }
    }

    /**
     * Verifies a line by comparing theoretical and numerical values.
     * @param start OtsPoint3d; theoretical start point.
     * @param startDirection Direction; theoretical start location.
     * @param clothoid ClothoidInfo; created clothoid.
     * @param startCurvature LinearDensity; start curvature, may be {@code null} if no theoretical value available.
     * @param endCurvature LinearDensity; end curvature, may be {@code null} if no theoretical value available.
     * @param a Length A-value, may be {@code null} if no theoretical value available.
     * @throws OtsGeometryException if segment number is not available on the line
     */
    private void VerifyLine(final OtsPoint3d start, final Direction startDirection, final ClothoidInfo clothoid,
            final LinearDensity startCurvature, final LinearDensity endCurvature, final Length a) throws OtsGeometryException
    {
        OtsLine3d line = clothoid.getLine();
        assertEquals("Start location deviates", 0.0, start.distance(line.get(0)).si, DISTANCE_TOLERANCE);
        assertEquals("End location deviates", 0.0, clothoid.getEndPoint().distance(line.get(line.size() - 1)).si,
                DISTANCE_TOLERANCE);
        assertEquals("Start direction deviates", 0.0, normalizeAngle(startDirection.si - getAngle(line, 0)), ANGLE_TOLERANCE);
        assertEquals("End direction deviates", 0.0,
                normalizeAngle(clothoid.getEndDirection().si - getAngle(line, line.size() - 2)), ANGLE_TOLERANCE);
        double lengthRatio = line.getLength().si / clothoid.getLength().si;
        assertEquals("Length is more than 1% shorter or longer than theoretical", 1.0, lengthRatio, 0.01);
        if (startCurvature != null)
        {
            double curveatureRatio = clothoid.getStartCurvature().si / startCurvature.si;
            assertEquals("Start curvature is more than 1% shorter or longer than theoretical", 1.0, curveatureRatio, 0.01);
        }
        if (endCurvature != null)
        {
            double curveatureRatio = clothoid.getEndCurvature().si / endCurvature.si;
            assertEquals("End curvature is more than 1% shorter or longer than theoretical", 1.0, curveatureRatio, 0.01);
        }
        if (a != null)
        {
            double aRadius = clothoid.getA().si / a.si;
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
