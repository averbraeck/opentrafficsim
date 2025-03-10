package org.opentrafficsim.core.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.geometry.Flattener.MaxAngle;
import org.opentrafficsim.core.geometry.Flattener.MaxDeviation;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;

/**
 * Tests for ContinuousArc.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ContinuousArcTest
{

    /** Verbose test. */
    private static final boolean VERBOSE = false;

    /** */
    private ContinuousArcTest()
    {
        // do not instantiate test class
    }

    /** Approximate coordinate equality margin. */
    private static final double MARGIN = 1e-9;

    /**
     * Test input.
     */
    @Test
    public void inputTest()
    {
        Try.testFail(() -> new ContinuousArc(null, 1.0, true, Angle.instantiateSI(Math.PI)), "Should fail on null start point.",
                NullPointerException.class);
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        Try.testFail(() -> new ContinuousArc(start, -1.0, true, Angle.instantiateSI(Math.PI)),
                "Should fail on negative radius.", IllegalArgumentException.class);
        Try.testFail(() -> new ContinuousArc(start, 1.0, true, Angle.instantiateSI(-Math.PI)), "Should fail on negative angle.",
                IllegalArgumentException.class);
        Try.testFail(() -> new ContinuousArc(start, 1.0, true, -Math.PI), "Should fail on negative length.",
                IllegalArgumentException.class);
    }

    /**
     * Tests the return values of standard continuous line methods.
     */
    @Test
    public void continuousLineTest()
    {
        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));

        isApproximal(arc.getStartPoint(), 1.0, 0.0);
        isApproximal(arc.getEndPoint(), -1.0, 0.0);
        assertEquals(Math.PI / 2.0, arc.getStartDirection().si, MARGIN, "Start direction is incorrect.");
        assertEquals(-Math.PI / 2.0, arc.getEndDirection().si, MARGIN, "End direction is incorrect.");
        assertEquals(1.0, arc.getStartRadius(), MARGIN, "Start radius is incorrect.");
        assertEquals(1.0, arc.getEndRadius(), MARGIN, "End radius is incorrect.");
        assertEquals(1.0, arc.getStartCurvature(), MARGIN, "Start curvature is incorrect.");
        assertEquals(1.0, arc.getEndCurvature(), MARGIN, "End curvature is incorrect.");
    }

    /**
     * Test flattened arc.
     */
    @Test
    public void flattenArcTest()
    {
        double u = Math.sin(Math.PI / 4.0);

        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));
        NumSegments numSegments4 = new NumSegments(4);
        PolyLine2d line = arc.flatten(numSegments4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), u, u);
        isApproximal(line.get(2), 0.0, 1.0);
        isApproximal(line.get(3), -u, u);
        isApproximal(line.get(4), -1.0, 0.0);

        // same, but rotating to right away from origin
        arc = new ContinuousArc(start, 1.0, false, Angle.instantiateSI(Math.PI));
        line = arc.flatten(numSegments4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), 2.0 - u, u);
        isApproximal(line.get(2), 2.0, 1.0);
        isApproximal(line.get(3), 2.0 + u, u);
        isApproximal(line.get(4), 3.0, 0.0);

        // half unit circle but with r=2.0
        start = new OrientedPoint2d(2.0, 0.0, Math.PI / 2.0);
        arc = new ContinuousArc(start, 2.0, true, Angle.instantiateSI(Math.PI));
        line = arc.flatten(numSegments4);
        isApproximal(line.get(0), 2.0, 0.0);
        isApproximal(line.get(1), 2.0 * u, 2.0 * u);
        isApproximal(line.get(2), 0.0, 2.0);
        isApproximal(line.get(3), -2.0 * u, 2.0 * u);
        isApproximal(line.get(4), -2.0, 0.0);

        // negative half unit circle
        start = new OrientedPoint2d(1.0, 0.0, -Math.PI / 2.0);
        arc = new ContinuousArc(start, 1.0, false, Angle.instantiateSI(Math.PI));
        line = arc.flatten(numSegments4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), u, -u);
        isApproximal(line.get(2), 0.0, -1.0);
        isApproximal(line.get(3), -u, -u);
        isApproximal(line.get(4), -1.0, 0.0);

        // same, but rotating to left away from origin
        arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));
        line = arc.flatten(numSegments4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), 2.0 - u, -u);
        isApproximal(line.get(2), 2.0, -1.0);
        isApproximal(line.get(3), 2.0 + u, -u);
        isApproximal(line.get(4), 3.0, 0.0);
    }

    /**
     * Test flattened arc based on max errors.
     */
    @Test
    public void flattenArcErrorTest()
    {
        for (boolean left : new boolean[] {true, false})
        {
            OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
            ContinuousArc arc = new ContinuousArc(start, 1.0, left, Angle.instantiateSI(Math.PI));

            // 10 degrees
            PolyLine2d line = arc.flatten(new MaxAngle(new Angle(10.0, AngleUnit.DEGREE).si));
            assertEquals(numSegExpect(10.0, Math.PI), line.size(), "Number of segments incorrect");

            // 1 degree
            line = arc.flatten(new MaxAngle(new Angle(1.0, AngleUnit.DEGREE).si));
            assertEquals(numSegExpect(1.0, Math.PI), line.size(), "Number of segments incorrect");

            // different spatial errors
            PolyLine2d line1 = arc.flatten(new MaxDeviation(0.1));
            PolyLine2d line2 = arc.flatten(new MaxDeviation(0.01));
            PolyLine2d line3 = arc.flatten(new MaxDeviation(0.001));
            assertTrue(line1.size() < line2.size() && line2.size() < line3.size(),
                    "Reduced spatial error should result in more segments");
        }
    }

    /**
     * Test offset arc.
     */
    @Test
    public void offsetArcTest()
    {
        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));

        // right-hand increasing offset
        FractionalLengthData offsets = FractionalLengthData.of(0.0, 0.0, 1.0, -1.0);
        NumSegments numSegments4 = new NumSegments(4);
        PolyLine2d line = arc.flattenOffset(offsets, numSegments4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(2), 0.0, 1.5);
        isApproximal(line.get(4), -2.0, 0.0);

        // left-hand increasing offset
        offsets = FractionalLengthData.of(0.0, 0.0, 1.0, 1.0);
        line = arc.flattenOffset(offsets, numSegments4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(2), 0.0, 0.5);
        isApproximal(line.get(4), 0.0, 0.0);

        // constant right-hand offset
        offsets = FractionalLengthData.of(0.0, -0.5);
        line = arc.flattenOffset(offsets, numSegments4);
        isApproximal(line.get(0), 1.5, 0.0);
        isApproximal(line.get(2), 0.0, 1.5);
        isApproximal(line.get(4), -1.5, 0.0);

        // constant left-hand offset
        offsets = FractionalLengthData.of(0.0, 0.5);
        line = arc.flattenOffset(offsets, numSegments4);
        isApproximal(line.get(0), 0.5, 0.0);
        isApproximal(line.get(2), 0.0, 0.5);
        isApproximal(line.get(4), -0.5, 0.0);
    }

    /**
     * Test offset arc based on max errors.
     */
    @Test
    public void offsetArcErrorTest()
    {
        for (boolean left : new boolean[] {true, false})
        {
            // half standard unit circle
            OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
            ContinuousArc arc = new ContinuousArc(start, 1.0, left, Angle.instantiateSI(Math.PI));

            // 10 degrees
            FractionalLengthData offsets = FractionalLengthData.of(0.0, -0.5);
            PolyLine2d line = arc.flattenOffset(offsets, new MaxAngle(new Angle(10.0, AngleUnit.DEGREE).si));
            assertEquals(numSegExpect(10.0, Math.PI), line.size(), "Number of segments incorrect");

            // 1 degree
            MaxAngle maxAngle1 = new MaxAngle(new Angle(1.0, AngleUnit.DEGREE).si);
            line = arc.flattenOffset(offsets, maxAngle1);
            if (VERBOSE)
            {
                System.out.println(line.toExcel());
            }
            assertEquals(numSegExpect(1.0, Math.PI), line.size(), "Number of segments incorrect");

            // variable radius
            offsets = FractionalLengthData.of(0.0, -0.5, 1.0, 0.25);
            PolyLine2d line1 = arc.flattenOffset(offsets, maxAngle1);
            offsets = FractionalLengthData.of(0.0, 0.25, 1.0, -0.5);
            PolyLine2d line2 = arc.flattenOffset(offsets, maxAngle1);
            assertEquals(line1.size(), line2.size(),
                    "Mirrored half circles result in the same number of segments, whereever it is on the arc");

            // different spatial errors
            offsets = FractionalLengthData.of(0.0, -0.5);
            line1 = arc.flattenOffset(offsets, new MaxDeviation(0.1));
            line2 = arc.flattenOffset(offsets, new MaxDeviation(0.01));
            PolyLine2d line3 = arc.flattenOffset(offsets, new MaxDeviation(0.001));
            assertTrue(line1.size() < line2.size() && line2.size() < line3.size(),
                    "Reduced spatial error should result in more segments");
        }
    }

    /**
     * Test arc defined by length.
     */
    public void lengthTest()
    {
        for (boolean left : new boolean[] {true, false})
        {
            // half standard unit circle
            OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
            ContinuousArc arc = new ContinuousArc(start, 1.0, left, Angle.instantiateSI(Math.PI));
            NumSegments numSegments4 = new NumSegments(4);
            PolyLine2d line1 = arc.flatten(numSegments4);
            arc = new ContinuousArc(start, 1.0, true, Math.PI);
            PolyLine2d line2 = arc.flatten(numSegments4);
            for (int i = 1; i < line1.size(); i++)
            {
                isApproximal(line1.get(i), line2.get(i).x, line2.get(i).y);
            }
        }
    }

    /**
     * Test point is approximately the same.
     * @param point point to test.
     * @param x expected x coordinate.
     * @param y expected y coordinate.
     */
    private void isApproximal(final Point2d point, final double x, final double y)
    {
        assertEquals(x, point.x, MARGIN, "Resulting x-coordinate is incorrect");
        assertEquals(y, point.y, MARGIN, "Resulting y-coordinate is incorrect");
    }

    /**
     * Returns the expected number of segments on an arc.
     * @param maxAngleDegrees maxAngle allowed in degrees.
     * @param arcAngle total circle arc.
     * @return expected number of segments on an arc.
     */
    private int numSegExpect(final double maxAngleDegrees, final double arcAngle)
    {
        double r = Math.ceil(Math.toDegrees(arcAngle) / (2 * maxAngleDegrees));
        return 1 + (int) Math.pow(2, Math.ceil(Math.log(r) / Math.log(2)));
    }

}
