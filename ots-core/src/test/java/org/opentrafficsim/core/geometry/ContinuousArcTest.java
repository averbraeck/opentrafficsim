package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.junit.Test;

/**
 * Tests for ContinuousArc.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContinuousArcTest
{

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
        assertEquals("Start direction is incorrect.", Math.PI / 2.0, arc.getStartDirection().si, MARGIN);
        assertEquals("End direction is incorrect.", -Math.PI / 2.0, arc.getEndDirection().si, MARGIN);
        assertEquals("Start radius is incorrect.", 1.0, arc.getStartRadius(), MARGIN);
        assertEquals("End radius is incorrect.", 1.0, arc.getEndRadius(), MARGIN);
        assertEquals("Start curvature is incorrect.", 1.0, arc.getStartCurvature(), MARGIN);
        assertEquals("End curvature is incorrect.", 1.0, arc.getEndCurvature(), MARGIN);
    }

    /**
     * Test flattened arc.
     * @throws OtsGeometryException if test is ill defined.
     */
    @Test
    public void flattenArcTest() throws OtsGeometryException
    {
        double u = Math.sin(Math.PI / 4.0);

        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));
        OtsLine2d line = arc.flatten(4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), u, u);
        isApproximal(line.get(2), 0.0, 1.0);
        isApproximal(line.get(3), -u, u);
        isApproximal(line.get(4), -1.0, 0.0);

        // same, but rotating to right away from origin
        arc = new ContinuousArc(start, 1.0, false, Angle.instantiateSI(Math.PI));
        line = arc.flatten(4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), 2.0 - u, u);
        isApproximal(line.get(2), 2.0, 1.0);
        isApproximal(line.get(3), 2.0 + u, u);
        isApproximal(line.get(4), 3.0, 0.0);

        // half unit circle but with r=2.0
        start = new OrientedPoint2d(2.0, 0.0, Math.PI / 2.0);
        arc = new ContinuousArc(start, 2.0, true, Angle.instantiateSI(Math.PI));
        line = arc.flatten(4);
        isApproximal(line.get(0), 2.0, 0.0);
        isApproximal(line.get(1), 2.0 * u, 2.0 * u);
        isApproximal(line.get(2), 0.0, 2.0);
        isApproximal(line.get(3), -2.0 * u, 2.0 * u);
        isApproximal(line.get(4), -2.0, 0.0);

        // negative half unit circle
        start = new OrientedPoint2d(1.0, 0.0, -Math.PI / 2.0);
        arc = new ContinuousArc(start, 1.0, false, Angle.instantiateSI(Math.PI));
        line = arc.flatten(4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(1), u, -u);
        isApproximal(line.get(2), 0.0, -1.0);
        isApproximal(line.get(3), -u, -u);
        isApproximal(line.get(4), -1.0, 0.0);

        // same, but rotating to left away from origin
        arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));
        line = arc.flatten(4);
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
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));

        // 10 degrees
        OtsLine2d line = arc.flatten(new Angle(10.0, AngleUnit.DEGREE), Double.POSITIVE_INFINITY);
        assertEquals("Number of segments incorrect", 19, line.size());

        // 1 degree
        line = arc.flatten(new Angle(1.0, AngleUnit.DEGREE), Double.POSITIVE_INFINITY);
        assertEquals("Number of segments incorrect", 181, line.size());

        // different spatial errors
        OtsLine2d line1 = arc.flatten(Angle.POSITIVE_INFINITY, 0.1);
        OtsLine2d line2 = arc.flatten(Angle.POSITIVE_INFINITY, 0.01);
        OtsLine2d line3 = arc.flatten(Angle.POSITIVE_INFINITY, 0.001);
        assertTrue("Reduced spatial error should result in more segments",
                line1.size() < line2.size() && line2.size() < line3.size());
    }

    /**
     * Test offset arc.
     * @throws OtsGeometryException if test is ill defined.
     */
    @Test
    public void offsetArcTest() throws OtsGeometryException
    {
        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));

        // right-hand increasing offset
        FractionalLengthData offsets = FractionalLengthData.of(0.0, 0.0, 1.0, -1.0);
        OtsLine2d line = arc.offset(offsets, 4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(2), 0.0, 1.5);
        isApproximal(line.get(4), -2.0, 0.0);

        // same through start/end offset method
        line = arc.offset(0.0, -1.0, 4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(2), 0.0, 1.5);
        isApproximal(line.get(4), -2.0, 0.0);

        // left-hand increasing offset
        offsets = FractionalLengthData.of(0.0, 0.0, 1.0, 1.0);
        line = arc.offset(offsets, 4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(2), 0.0, 0.5);
        isApproximal(line.get(4), 0.0, 0.0);

        // same through start/end offset method
        line = arc.offset(0.0, 1.0, 4);
        isApproximal(line.get(0), 1.0, 0.0);
        isApproximal(line.get(2), 0.0, 0.5);
        isApproximal(line.get(4), 0.0, 0.0);

        // constant right-hand offset
        line = arc.offset(-0.5, 4);
        isApproximal(line.get(0), 1.5, 0.0);
        isApproximal(line.get(2), 0.0, 1.5);
        isApproximal(line.get(4), -1.5, 0.0);

        // constant left-hand offset
        line = arc.offset(0.5, 4);
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
        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));

        // 10 degrees
        OtsLine2d line = arc.offset(-1.0, new Angle(10.0, AngleUnit.DEGREE), Double.POSITIVE_INFINITY);
        assertEquals("Number of segments incorrect", 19, line.size());

        // 1 degree
        line = arc.offset(-1.0, new Angle(1.0, AngleUnit.DEGREE), Double.POSITIVE_INFINITY);
        assertEquals("Number of segments incorrect", 181, line.size());

        // variable radius
        OtsLine2d line1 = arc.offset(-0.5, 0.25, new Angle(1.0, AngleUnit.DEGREE), Double.POSITIVE_INFINITY);
        OtsLine2d line2 = arc.offset(0.25, -0.5, new Angle(1.0, AngleUnit.DEGREE), Double.POSITIVE_INFINITY);
        assertEquals("Maximum radius should result in the same number of segments, whereever it is on the arc", line1.size(),
                line2.size());

        // different spatial errors
        line1 = arc.offset(-0.5, Angle.POSITIVE_INFINITY, 0.1);
        line2 = arc.offset(-0.5, Angle.POSITIVE_INFINITY, 0.01);
        OtsLine2d line3 = arc.offset(-0.5, Angle.POSITIVE_INFINITY, 0.001);
        assertTrue("Reduced spatial error should result in more segments",
                line1.size() < line2.size() && line2.size() < line3.size());
    }

    /**
     * Test arc defined by length.
     * @throws OtsGeometryException if test is ill defined.
     */
    public void lengthTest() throws OtsGeometryException
    {
        // half standard unit circle
        OrientedPoint2d start = new OrientedPoint2d(1.0, 0.0, Math.PI / 2.0);
        ContinuousArc arc = new ContinuousArc(start, 1.0, true, Angle.instantiateSI(Math.PI));
        OtsLine2d line1 = arc.flatten(4);
        arc = new ContinuousArc(start, 1.0, true, Math.PI);
        OtsLine2d line2 = arc.flatten(4);
        for (int i = 1; i < line1.size(); i++)
        {
            isApproximal(line1.get(i), line2.get(i).x, line2.get(i).y);
        }
    }

    /**
     * Test point is approximately the same.
     * @param point Point2d; point to test.
     * @param x double; expected x coordinate.
     * @param y double; expected y coordinate.
     */
    private void isApproximal(final Point2d point, final double x, final double y)
    {
        assertEquals("Resulting x-coordinate is incorrect", x, point.x, MARGIN);
        assertEquals("Resulting y-coordinate is incorrect", y, point.y, MARGIN);
    }

}
