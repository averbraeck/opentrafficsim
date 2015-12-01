package org.opentrafficsim.core.geometry;

import java.util.Locale;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 9, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Test
{
    /** */
    private Test()
    {
    }

    /**
     * Apply all offset methods and check the results.
     * @param reference OTSLine3D; reference line
     * @param offset double; the offset
     * @return int; the number of failures
     */
    public static int checkAll(final OTSLine3D reference, final double offset)
    {
        int result = 0;
        for (OTSLine3D.OffsetMethod offsetMethod : new OTSLine3D.OffsetMethod[] { OTSLine3D.OffsetMethod.JTS,
                OTSLine3D.OffsetMethod.PK, OTSLine3D.OffsetMethod.AV })
        {
            if (!checkOffsetLine(reference, offset, offsetMethod))
            {
                result++;
            }
            if (!checkOffsetLine(reference, -offset, offsetMethod))
            {
                result++;
            }
        }
        return result;
    }

    /**
     * @param args args
     * @throws NetworkException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws NetworkException, OTSGeometryException
    {
        OTSLine3D reference;
        /*-
        // OTSLine3D.debugOffsetLine = true;

        reference =
                new OTSLine3D(new OTSPoint3D(5, 2.5), new OTSPoint3D(4.8, 2.5), new OTSPoint3D(4.6, 2.7), new OTSPoint3D(2.2,
                        2.7), new OTSPoint3D(2.2, 5));
        // fails JTS checkAll(reference, 2);

        reference = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(10, 5, 0), new OTSPoint3D(20, 0, 0));
        // fails AV checkAll(reference, 2);

        reference =
                new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(20, 10, 0), new OTSPoint3D(21, 10, 0), new OTSPoint3D(22,
                        9.5, 0), new OTSPoint3D(30, 0, 0));
        // fails AV checkAll(reference, -3);

        // Reference line closely spaced points on a (relatively large) circle
        OTSPoint3D[] designLinePoints = new OTSPoint3D[8];
        double radius = 10;
        double angleStep = Math.PI / 1000;
        double initialAngle = Math.PI / 4;
        for (int i = 0; i < designLinePoints.length; i++)
        {
            double angle = initialAngle + i * angleStep;
            designLinePoints[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle) - radius, 0);
        }
        reference = new OTSLine3D(designLinePoints);
        // passes all
        checkAll(reference, 2);

        // Straight design line with some <i>noise</i> (sufficiently far from the end points).
        reference =
                new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                        new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                        new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(10, 2, 0));
        checkAll(reference, 2);
         */
        // Straight design line with some <i>noise</i> (close to the end points).
        reference =
                new OTSLine3D(new OTSPoint3D(5, -1, 0), new OTSPoint3D(5, -2, 0), new OTSPoint3D(4.9, -2.01, 0),
                        new OTSPoint3D(5.1, -2.03, 0), new OTSPoint3D(5, -2.04, 0), new OTSPoint3D(5, -6, 0), new OTSPoint3D(
                                4.9, -6.01, 0), new OTSPoint3D(5.1, -6.03, 0), new OTSPoint3D(5, -6.04, 0), new OTSPoint3D(5,
                                -7.04, 0));
        // checkAll(reference, 2);
        OTSOffsetLinePK.debugOffsetLine = true;
        checkOffsetLine(reference, -2, OTSLine3D.OffsetMethod.PK);

        /*-
        int i = 8;

        switch (i)
        {
            case 1:
                test1();
                break;
            case 2:
                test2();
                break;
            case 3:
                test3();
                break;
            case 4:
                test4();
                break;
            case 5:
                test5();
                break;
            case 6:
                test6();
                break;
            case 7:
                test7();
                break;
            case 8:
                test8();
                break;
        }
         */
    }

    /**
     * Check the offsetLine method.
     * @param referenceLine OTSLine3D; the reference line
     * @param offset double; the offset
     * @param offsetMethod OTSLine3D.OffsetMethod; the offset method
     * @return boolean; false if the result is obviously wrong; true if the result (appears to be) fine
     */
    public static boolean checkOffsetLine(final OTSLine3D referenceLine, final double offset,
            final OTSLine3D.OffsetMethod offsetMethod)
    {
        double absOffset = Math.abs(offset);
        double maxErrorFar = 0.01;
        double maxErrorClose = 0.002;
        try
        {
            OTSLine3D.OFFSETMETHOD = offsetMethod;
            OTSLine3D offsetLine = referenceLine.offsetLine(offset);
            if (null == offsetLine)
            {
                if (OTSOffsetLinePK.debugOffsetLine)
                {
                    System.out.println(String.format(Locale.US, "#offset %7.3f, method %3.3s returned null referenceLine %s",
                            offset, offsetMethod, referenceLine));
                }
                return false;
            }
            // Walk the length of the reference line in small steps.
            final int numSteps = 1000;
            double[] closestToResult = new double[numSteps + 1];
            double[] closestToReference = new double[numSteps + 1];
            for (int i = 0; i < closestToResult.length; i++)
            {
                closestToResult[i] = closestToReference[i] = Double.MAX_VALUE;
            }
            double referenceLength = referenceLine.getLengthSI();
            double resultLength = offsetLine.getLengthSI();
            double resultEndFirst = offsetLine.getFirst().distanceSI(offsetLine.get(1));
            double resultStartLast =
                    offsetLine.getLengthSI() - offsetLine.getLast().distanceSI(offsetLine.get(offsetLine.size() - 2));
            for (int referenceStep = 0; referenceStep < numSteps + 1; referenceStep++)
            {
                double referencePosition = referenceLength * referenceStep / numSteps;
                OTSPoint3D referencePoint = new OTSPoint3D(referenceLine.getLocationExtendedSI(referencePosition));
                for (int resultStep = 0; resultStep < numSteps + 1; resultStep++)
                {
                    double resultPosition = resultLength * resultStep / numSteps;
                    OTSPoint3D resultPoint = new OTSPoint3D(offsetLine.getLocationExtendedSI(resultPosition));
                    double distance = referencePoint.horizontalDistanceSI(resultPoint);
                    if (distance <= absOffset)
                    {
                        if (resultPosition <= resultEndFirst)
                        {
                            continue;
                        }
                        if (resultPosition >= resultStartLast)
                        {
                            continue;
                        }
                    }
                    if (distance < closestToResult[resultStep])
                    {
                        closestToResult[resultStep] = distance;
                    }
                    if (distance < closestToReference[referenceStep])
                    {
                        closestToReference[referenceStep] = distance;
                    }
                }
            }
            int referenceTooClose = 0;
            int resultTooClose = 0;
            int resultTooFar = 0;
            for (int i = 0; i < closestToResult.length; i++)
            {
                if (closestToResult[i] > absOffset + maxErrorFar)
                {
                    resultTooFar++;
                }
                if (closestToResult[i] < absOffset - maxErrorClose)
                {
                    resultTooClose++;
                }
                if (closestToReference[i] < absOffset - maxErrorClose)
                {
                    referenceTooClose++;
                }
            }
            if (0 == referenceTooClose && 0 == resultTooClose && 0 == resultTooFar)
            {
                if (OTSOffsetLinePK.debugOffsetLine)
                {
                    System.out.println("#No errors detected");
                    System.out.println(OTSGeometry.printCoordinates("#reference: \nc1,0,0\n#", referenceLine, "\n    "));
                    System.out.println(OTSGeometry.printCoordinates("#offset: \nc0,1,0\n#", offsetLine, "\n    "));
                }
                return true;
            }
            double factor = 100d / (numSteps + 1);
            if (OTSOffsetLinePK.debugOffsetLine)
            {
                System.out.println(String.format(Locale.US, "#offset %7.3f, method %3.3s: result line too close for %5.1f%%, "
                        + "too far for %5.1f%%, reference too close for %5.1f%%", offset, offsetMethod,
                        resultTooClose * factor, resultTooFar * factor, referenceTooClose * factor));
            }
            for (int i = 0; i < closestToReference.length; i++)
            {
                if (closestToReference[i] > absOffset + maxErrorFar)
                {
                    DirectedPoint p = referenceLine.getLocationSI(i * referenceLength / numSteps);
                    System.out.println(String.format("sc0.7,0.7,0.7w0.2M%.3f,%.3fl0,0r", p.x, p.y));
                }
            }
            for (int i = 0; i < closestToResult.length; i++)
            {
                if (closestToResult[i] > absOffset + maxErrorFar || closestToResult[i] < absOffset - maxErrorClose)
                {
                    DirectedPoint p = offsetLine.getLocationSI(i * resultLength / numSteps);
                    System.out.println(String.format("sw0.2M%.3f,%.3fl0,0r", p.x, p.y));
                }
            }
            System.out.println(OTSGeometry.printCoordinates("#reference: \nc1,0,0\n#", referenceLine, "\n    "));
            System.out.println(OTSGeometry.printCoordinates("#offset: \nc0,1,0\n#", offsetLine, "\n    "));
            return false;
        }
        catch (NetworkException | OTSGeometryException exception)
        {
            System.err.println("Caught unexpected exception.");
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Kink near end of design line.
     * @throws NetworkException
     */
    public static void test1() throws NetworkException
    {
        System.out.println("Dcirc,sm-2,0a2,0,360r");
        System.out.println("M5.0,2.5dcirc");
        System.out.println("M4.8,2.5dcirc");
        System.out.println("M4.6,2.7dcirc");
        System.out.println("M2.2,2.7dcirc");
        System.out.println("M2.2,5dcirc");
        System.out.println("");
        OTSLine3D referenceLine =
                new OTSLine3D(new OTSPoint3D(5, 2.5), new OTSPoint3D(4.8, 2.5), new OTSPoint3D(4.6, 2.7), new OTSPoint3D(2.2,
                        2.7), new OTSPoint3D(2.2, 5));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        // OTSLine3D.debugOffsetLine = true;
        OTSLine3D left = referenceLine.offsetLine(2.0);
        System.out.println(OTSGeometry.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OTSLine3D right = referenceLine.offsetLine(-2.0);
        System.out.println(OTSGeometry.printCoordinates("#right: \nc0,1,0\n#", right, "\n   "));
    }

    /**
     * Kink halfway (far from any endpoint).
     * @throws NetworkException
     */
    public static void test2() throws NetworkException
    {
        OTSLine3D otsLine = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(10, 5, 0), new OTSPoint3D(20, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", otsLine, "\n    "));
        OTSLine3D left = otsLine.offsetLine(2.0);
        System.out.println(OTSGeometry.printCoordinates("#left: \nc0,1,0\n#", left, "\n   "));
        OTSLine3D right = otsLine.offsetLine(-2.0);
        System.out.println(OTSGeometry.printCoordinates("#buffer: \nc0,1,0\n#", right, "\n   "));
    }

    /**
     * Kink plus decreasing width.
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    public static void test3() throws NetworkException, OTSGeometryException
    {
        OTSLine3D referenceLine =
                new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(200, 100, 0), new OTSPoint3D(1000, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference line: \nc1,0,0\n#", referenceLine, "\n    "));
        OTSLine3D centerLine = referenceLine.offsetLine(-8, -5);
        System.out.println(OTSGeometry.printCoordinates("#center line: \nc0,1,0\n#", centerLine, "\n   "));
        for (int i = 1; i < centerLine.size(); i++)
        {
            OTSPoint3D from = centerLine.get(i - 1);
            OTSPoint3D to = centerLine.get(i);
            double angle = Math.atan2(to.y - from.y, to.x - from.x);
            System.out.println("#Direction in segment " + i + " is " + Math.toDegrees(angle));
        }
        OTSLine3D leftEdge = centerLine.offsetLine(1.5, 2);
        System.out.println(OTSGeometry.printCoordinates("#left edge: \nc0,0,1\n#", leftEdge, "\n   "));
        OTSLine3D rightEdge = centerLine.offsetLine(-1.5, -2);
        System.out.println(OTSGeometry.printCoordinates("#right edge: \nc0,0,1\n#", rightEdge, "\n   "));
    }

    /**
     * Two kinks, (too) close together.
     * @throws NetworkException
     */
    public static void test4() throws NetworkException
    {
        OTSLine3D reference =
                new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(20, 10, 0), new OTSPoint3D(21, 10, 0), new OTSPoint3D(22,
                        9.5, 0), new OTSPoint3D(30, 0, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference: \nc1,0,0\n#", reference, "\n    "));
        OTSLine3D offset = reference.offsetLine(-3);
        System.out.println(OTSGeometry.printCoordinates("#offset: \nc0,1,0\n#", offset, "\n    "));
    }

    /**
     * Two-segment design line with minimal change of direction.
     * @throws NetworkException
     * @throws OTSGeometryException
     */
    public static void test5() throws NetworkException, OTSGeometryException
    {
        OTSPoint3D[] designLinePoints = new OTSPoint3D[8];
        double radius = 10;
        double angleStep = Math.PI / 1000;
        double initialAngle = Math.PI / 4;
        for (int i = 0; i < designLinePoints.length; i++)
        {
            double angle = initialAngle + i * angleStep;
            designLinePoints[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle) - radius, 0);
        }
        // OTSLine3D.debugOffsetLine = true;
        OTSLine3D reference = new OTSLine3D(designLinePoints);
        System.out.println(OTSGeometry.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OTSLine3D centerLine = reference.offsetLine(5);
        System.out.println(OTSGeometry.printCoordinates("#center:\nc0,1,0\n#", centerLine, "\n    "));
        for (int i = 1; i < centerLine.size() - 1; i++)
        {
            double distance =
                    OTSPoint3D.distanceLineSegmentToPoint(centerLine.get(0), centerLine.get(centerLine.size() - 1),
                            centerLine.get(i));
            System.out.println("#distance of intermediate point " + i + " to overall line is " + distance);
        }
        OTSLine3D right = centerLine.offsetLine(-2);
        System.out.println(OTSGeometry.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OTSLine3D left = centerLine.offsetLine(2);
        System.out.println(OTSGeometry.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

    /**
     * Straight design line with some <i>noise</i> (sufficiently far from the end points).
     * @throws NetworkException
     */
    public static void test6() throws NetworkException
    {
        System.out.println("O0,-10");
        OTSLine3D reference =
                new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                        new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                        new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(10, 2, 0));
        System.out.println(OTSGeometry.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OTSLine3D right = reference.offsetLine(-2);
        System.out.println(OTSGeometry.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OTSLine3D left = reference.offsetLine(2);
        System.out.println(OTSGeometry.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

    /**
     * Straight design line with more <i>noise</i> (sufficiently far from the end points).
     * @throws NetworkException
     */
    public static void test7() throws NetworkException
    {
        System.out.println("O0,-10");
        OTSLine3D reference =
                new OTSLine3D(new OTSPoint3D(10, 10, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
                        new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
                        new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(9.999, 6, 0), new OTSPoint3D(
                                9.996, 5.99, 0), new OTSPoint3D(9.999, 5.98, 0), new OTSPoint3D(10.03, 5.95, 0),
                        new OTSPoint3D(10.01, 5.94, 0), new OTSPoint3D(10.0, 5.94, 0), new OTSPoint3D(10, 2, 0));

        System.out.println(OTSGeometry.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OTSLine3D right = reference.offsetLine(-2);
        System.out.println(OTSGeometry.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OTSLine3D left = reference.offsetLine(2);
        System.out.println(OTSGeometry.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

    /**
     * Straight design line with more <i>noise</i> (close to the end points).
     * @throws NetworkException
     */
    public static void test8() throws NetworkException
    {
        // System.out.println("O0,-10");
        // OTSLine3D reference =
        // new OTSLine3D(new OTSPoint3D(10, 9, 0), new OTSPoint3D(9.999, 8, 0), new OTSPoint3D(9.996, 7.99, 0),
        // new OTSPoint3D(9.999, 7.98, 0), new OTSPoint3D(10.03, 7.95, 0), new OTSPoint3D(10.01, 7.94, 0),
        // new OTSPoint3D(10.0, 7.94, 0), new OTSPoint3D(10, 6, 0), new OTSPoint3D(9.999, 6, 0), new OTSPoint3D(
        // 9.996, 5.99, 0), new OTSPoint3D(9.999, 5.98, 0), new OTSPoint3D(10.03, 5.95, 0),
        // new OTSPoint3D(10.01, 5.94, 0), new OTSPoint3D(10.0, 5.94, 0), new OTSPoint3D(10, 5, 0));
        OTSLine3D reference =
                new OTSLine3D(new OTSPoint3D(5, -1, 0), new OTSPoint3D(5, -2, 0), new OTSPoint3D(4.9, -2.01, 0),
                        new OTSPoint3D(5.1, -2.03, 0), new OTSPoint3D(5, -2.04, 0), new OTSPoint3D(5, -6, 0), new OTSPoint3D(
                                4.9, -6.01, 0), new OTSPoint3D(5.1, -6.03, 0), new OTSPoint3D(5, -6.04, 0), new OTSPoint3D(5,
                                -7.04, 0));

        System.out.println(OTSGeometry.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        OTSLine3D right = reference.offsetLine(-2);
        System.out.println(OTSGeometry.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        OTSLine3D left = reference.offsetLine(2);
        System.out.println(OTSGeometry.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));

        reference =
                new OTSLine3D(new OTSPoint3D(10, 0.5, 0), new OTSPoint3D(10, -2, 0), new OTSPoint3D(9.9, -2.01, 0),
                        new OTSPoint3D(10.1, -2.03, 0), new OTSPoint3D(10, -2.04, 0), new OTSPoint3D(10, -6, 0),
                        new OTSPoint3D(9.9, -6.01, 0), new OTSPoint3D(10.1, -6.03, 0), new OTSPoint3D(10, -6.04, 0),
                        new OTSPoint3D(10, -8.54, 0));

        System.out.println(OTSGeometry.printCoordinates("#reference:\nc1,0,0\n#", reference, "\n    "));
        right = reference.offsetLine(-2);
        System.out.println(OTSGeometry.printCoordinates("#right:\nc0,0,1\n#", right, "\n    "));
        left = reference.offsetLine(2);
        System.out.println(OTSGeometry.printCoordinates("#left:\nc0,0,1\n#", left, "\n    "));
    }

}
