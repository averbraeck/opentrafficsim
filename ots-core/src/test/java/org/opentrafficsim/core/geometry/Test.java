package org.opentrafficsim.core.geometry;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.djutils.draw.point.Point2d;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class Test
{
    /** */
    private Test()
    {
    }

    /** Print detailed output. */
    static boolean printDetails = false;

    /**
     * Kink near end of design line.
     * @throws OtsGeometryException on error
     */
    public static void test1() throws OtsGeometryException
    {
        System.out.println("Dcirc,sm-2,0a2,0,360r");
        System.out.println("M5.0,2.5dcirc");
        System.out.println("M4.8,2.5dcirc");
        System.out.println("M4.6,2.7dcirc");
        System.out.println("M2.2,2.7dcirc");
        System.out.println("M2.2,5dcirc");
        System.out.println("");
        OtsLine3d referenceLine = new OtsLine3d(new Point2d(5, 2.5), new Point2d(4.8, 2.5), new Point2d(4.6, 2.7),
                new Point2d(2.2, 2.7), new Point2d(2.2, 5));
        System.out.println(OtsGeometryUtil.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        // OtsLine3d.debugOffsetLine = true;
        OtsLine3d left = referenceLine.offsetLine(2.0);
        System.out.println(OtsGeometryUtil.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OtsLine3d right = referenceLine.offsetLine(-2.0);
        System.out.println(OtsGeometryUtil.printCoordinates("#right: \nc0,1,0\n#", right, "\n   "));
    }

    /**
     * Kink halfway (far from any endpoint).
     * @throws OtsGeometryException on error
     */
    public static void test2() throws OtsGeometryException
    {
        OtsLine3d otsLine = new OtsLine3d(new Point2d(0, 0), new Point2d(10, 5), new Point2d(20, 0));
        System.out.println(OtsGeometryUtil.printCoordinates("#reference line: \nc1,0,0\n#", otsLine, "\n    "));
        OtsLine3d left = otsLine.offsetLine(2.0);
        System.out.println(OtsGeometryUtil.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OtsLine3d right = otsLine.offsetLine(-2.0);
        System.out.println(OtsGeometryUtil.printCoordinates("#buffer: \nc0,1,0\n#", right, "\n   "));
    }

    /**
     * Kink plus decreasing width.
     * @throws OtsGeometryException on error
     */
    public static void test3() throws OtsGeometryException
    {
        OtsLine3d referenceLine = new OtsLine3d(new Point2d(0, 0), new Point2d(200, 100), new Point2d(1000, 0));
        System.out.println(OtsGeometryUtil.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        OtsLine3d centerLine = referenceLine.offsetLine(-8, -5);
        System.out.println(OtsGeometryUtil.printCoordinates("#center line: \nc0,1,0\n#", centerLine, "\n   "));
        for (int i = 1; i < centerLine.size(); i++)
        {
            Point2d from = centerLine.get(i - 1);
            Point2d to = centerLine.get(i);
            double angle = Math.atan2(to.y - from.y, to.x - from.x);
            System.out.println("#Direction in segment " + i + " is " + Math.toDegrees(angle));
        }
        OtsLine3d leftEdge = centerLine.offsetLine(1.5, 2);
        System.out.println(OtsGeometryUtil.printCoordinates("#left edge: \nc0,0,1\n#", leftEdge, "\n   "));
        OtsLine3d rightEdge = centerLine.offsetLine(-1.5, -2);
        System.out.println(OtsGeometryUtil.printCoordinates("#right edge: \nc0,0,1\n#", rightEdge, "\n   "));
    }

    /**
     * Two kinks, (too) close together.
     * @throws OtsGeometryException on error
     */
    public static void test4() throws OtsGeometryException
    {
        OtsLine3d reference = new OtsLine3d(new Point2d(0, 0), new Point2d(20, 10), new Point2d(21, 10), new Point2d(22, 9.5),
                new Point2d(30, 0));
        System.out.println(OtsGeometryUtil.printCoordinates("#reference: \nc1,0,0\n#", reference, "\n    "));
        OtsLine3d offset = reference.offsetLine(-3);
        System.out.println(OtsGeometryUtil.printCoordinates("#offset: \nc0,1,0\n#", offset, "\n    "));
    }

    /**
     * Two-segment design line with minimal change of direction.
     * @throws OtsGeometryException on error
     */
    public static void test5() throws OtsGeometryException
    {
        Point2d[] designLinePoints = new Point2d[8];
        double radius = 10;
        double angleStep = Math.PI / 1000;
        double initialAngle = Math.PI / 4;
        for (int i = 0; i < designLinePoints.length; i++)
        {
            double angle = initialAngle + i * angleStep;
            designLinePoints[i] = new Point2d(radius * Math.cos(angle), radius * Math.sin(angle) - radius);
        }
        // OtsLine3d.debugOffsetLine = true;
        OtsLine3d reference = new OtsLine3d(designLinePoints);
        System.out.println(OtsGeometryUtil.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OtsLine3d centerLine = reference.offsetLine(5);
        System.out.println(OtsGeometryUtil.printCoordinates("#center:\nc0,1,0\n#", centerLine, "\n    "));
        for (int i = 1; i < centerLine.size() - 1; i++)
        {
            // double distance =
            // centerLine.get(i).horizontalDistanceToLineSegment(centerLine.get(0), centerLine.get(centerLine.size() - 1));
            double distance = centerLine.get(i).closestPointOnLine(centerLine.get(0), centerLine.get(centerLine.size() - 1))
                    .distance(centerLine.get(i));
            System.out.println("#distance of intermediate point " + i + " to overall line is " + distance);
        }
        OtsLine3d right = centerLine.offsetLine(-2);
        System.out.println(OtsGeometryUtil.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OtsLine3d left = centerLine.offsetLine(2);
        System.out.println(OtsGeometryUtil.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

    /**
     * Straight design line with some <i>noise</i> (sufficiently far from the end points).
     * @throws OtsGeometryException on error
     */
    public static void test6() throws OtsGeometryException
    {
        System.out.println("O0,-10");
        OtsLine3d reference = new OtsLine3d(new Point2d(10, 10), new Point2d(9.999, 8), new Point2d(9.996, 7.99),
                new Point2d(9.999, 7.98), new Point2d(10.03, 7.95), new Point2d(10.01, 7.94), new Point2d(10.0, 7.94),
                new Point2d(10, 6), new Point2d(10, 2));
        System.out.println(OtsGeometryUtil.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OtsLine3d right = reference.offsetLine(-2);
        System.out.println(OtsGeometryUtil.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OtsLine3d left = reference.offsetLine(2);
        System.out.println(OtsGeometryUtil.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

    /**
     * Straight design line with more <i>noise</i> (sufficiently far from the end points).
     * @throws OtsGeometryException on error
     */
    public static void test7() throws OtsGeometryException
    {
        System.out.println("O0,-10");
        OtsLine3d reference = new OtsLine3d(new Point2d(10, 10), new Point2d(9.999, 8), new Point2d(9.996, 7.99),
                new Point2d(9.999, 7.98), new Point2d(10.03, 7.95), new Point2d(10.01, 7.94), new Point2d(10.0, 7.94),
                new Point2d(10, 6), new Point2d(9.999, 6), new Point2d(9.996, 5.99), new Point2d(9.999, 5.98),
                new Point2d(10.03, 5.95), new Point2d(10.01, 5.94), new Point2d(10.0, 5.94), new Point2d(10, 2));

        System.out.println(OtsGeometryUtil.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OtsLine3d right = reference.offsetLine(-2);
        System.out.println(OtsGeometryUtil.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OtsLine3d left = reference.offsetLine(2);
        System.out.println(OtsGeometryUtil.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

    /**
     * Straight design line with more <i>noise</i> (close to the end points).
     * @throws OtsGeometryException on error
     */
    public static void test8() throws OtsGeometryException
    {
        // System.out.println("O0,-10");
        // OtsLine3d reference =
        // new OtsLine3d(new Point2d(10, 9), new Point2d(9.999, 8), new Point2d(9.996, 7.99),
        // new Point2d(9.999, 7.98), new Point2d(10.03, 7.95), new Point2d(10.01, 7.94),
        // new Point2d(10.0, 7.94), new Point2d(10, 6), new Point2d(9.999, 6), new Point2d(
        // 9.996, 5.99), new Point2d(9.999, 5.98), new Point2d(10.03, 5.95),
        // new Point2d(10.01, 5.94), new Point2d(10.0, 5.94), new Point2d(10, 5));
        OtsLine3d reference = new OtsLine3d(new Point2d(5, -1), new Point2d(5, -2), new Point2d(4.9, -2.01),
                new Point2d(5.1, -2.03), new Point2d(5, -2.04), new Point2d(5, -6), new Point2d(4.9, -6.01),
                new Point2d(5.1, -6.03), new Point2d(5, -6.04), new Point2d(5, -7.04));

        System.out.println(OtsGeometryUtil.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OtsLine3d right = reference.offsetLine(-2);
        System.out.println(OtsGeometryUtil.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OtsLine3d left = reference.offsetLine(2);
        System.out.println(OtsGeometryUtil.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));

        reference = new OtsLine3d(new Point2d(10, 0.5), new Point2d(10, -2), new Point2d(9.9, -2.01), new Point2d(10.1, -2.03),
                new Point2d(10, -2.04), new Point2d(10, -6), new Point2d(9.9, -6.01), new Point2d(10.1, -6.03),
                new Point2d(10, -6.04), new Point2d(10, -8.54));

        System.out.println(OtsGeometryUtil.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        right = reference.offsetLine(-2);
        System.out.println(OtsGeometryUtil.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        left = reference.offsetLine(2);
        System.out.println(OtsGeometryUtil.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

}
