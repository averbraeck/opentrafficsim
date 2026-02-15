package org.opentrafficsim.base.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Utility class for OTS geometry.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class OtsGeometryUtil
{
    /** */
    private OtsGeometryUtil()
    {
        // do not instantiate this class.
    }

    /**
     * Print one Point2d on the console.
     * @param prefix text to put before the output
     * @param point the coordinate to print
     * @return String
     */
    public static String printCoordinate(final String prefix, final Point2d point)
    {
        return String.format(Locale.US, "%s %8.3f,%8.3f   ", prefix, point.x, point.y);
    }

    /**
     * Returns the number of segments to use for a given maximum spatial error, and radius.
     * @param maxSpatialError maximum spatial error.
     * @param angle angle of arc at radius.
     * @param r critical radius (largest radius).
     * @return number of segments to use for a given maximum spatial error, and radius.
     */
    public static int getNumSegmentsForRadius(final double maxSpatialError, final Angle angle, final double r)
    {
        /*-
         * Geometric derivation from a right-angled half pizza slice:
         * b = adjacent side of triangle = line from center of circle to middle of straight line arc segment
         * r = radius = hypotenuse
         * a = maxDeviation;
         * r = a + b (middle of straight line segment has largest deviation)
         * phi = |endAng - startAng| / 2n = angle at center of circle in right-angled half pizza slice = half angle of slice
         * n = number of segments
         *
         * r - a = b = r * cos(phi)
         * => 1 - (a / r) = cos(phi)
         * => phi = acos(1 - (a / r)) = |endAng - startAng| / 2n
         * => n = |endAng - startAng| / 2 * acos(1 - (a / r))
         */
        return (int) Math.ceil(angle.si / (2.0 * Math.acos(1.0 - maxSpatialError / r)));
    }

    /**
     * Returns a point on a line through the given point, perpendicular to the given direction, at the offset distance. A
     * negative offset is towards the right hand side relative to the direction.
     * @param point point.
     * @param offset offset, negative values are to the right.
     * @return offset point.
     */
    public static DirectedPoint2d offsetPoint(final DirectedPoint2d point, final double offset)
    {
        return new DirectedPoint2d(point.x - Math.sin(point.dirZ) * offset, point.y + Math.cos(point.dirZ) * offset,
                point.dirZ);
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line via a number of intermediate offsets at intermediate positions to its final offset value at
     * the end of the reference line.
     * @param line reference line.
     * @param relativeFractions positional fractions for which the offsets have to be generated
     * @param offsets offsets at the relative positions (positive value is Left, negative value is Right)
     * @return the PolyLine2d of the line at multi-linearly changing offset of the reference line
     * @throws OtsGeometryException when this method fails to create the offset line
     */
    public static PolyLine2d offsetLine(final PolyLine2d line, final double[] relativeFractions, final double[] offsets)
            throws OtsGeometryException
    {
        Throw.whenNull(relativeFractions, "relativeFraction may not be null");
        Throw.whenNull(offsets, "offsets may not be null");
        Throw.when(relativeFractions.length < 2, OtsGeometryException.class, "size of relativeFractions must be >= 2");
        Throw.when(relativeFractions.length != offsets.length, OtsGeometryException.class,
                "size of relativeFractions must be equal to size of offsets");
        Throw.when(relativeFractions[0] < 0, OtsGeometryException.class, "relativeFractions may not start before 0");
        Throw.when(relativeFractions[relativeFractions.length - 1] > 1, OtsGeometryException.class,
                "relativeFractions may not end beyond 1");
        List<Double> fractionsList = DoubleStream.of(relativeFractions).boxed().collect(Collectors.toList());
        List<Double> offsetsList = DoubleStream.of(offsets).boxed().collect(Collectors.toList());
        if (relativeFractions[0] != 0)
        {
            fractionsList.add(0, 0.0);
            offsetsList.add(0, 0.0);
        }
        if (relativeFractions[relativeFractions.length - 1] < 1.0)
        {
            fractionsList.add(1.0);
            offsetsList.add(0.0);
        }
        PolyLine2d[] offsetLine = new PolyLine2d[fractionsList.size()];
        for (int i = 0; i < fractionsList.size(); i++)
        {
            offsetLine[i] = line.offsetLine(offsetsList.get(i));
        }
        List<Point2d> out = new ArrayList<>();
        Point2d prevCoordinate = null;
        final double tooClose = 0.05; // 5 cm
        for (int i = 0; i < offsetsList.size() - 1; i++)
        {
            Throw.when(fractionsList.get(i + 1) <= fractionsList.get(i), OtsGeometryException.class,
                    "fractions must be in ascending order");
            PolyLine2d startGeometry = offsetLine[i].extractFractional(fractionsList.get(i), fractionsList.get(i + 1));
            PolyLine2d endGeometry = offsetLine[i + 1].extractFractional(fractionsList.get(i), fractionsList.get(i + 1));
            double firstLength = startGeometry.getLength();
            double secondLength = endGeometry.getLength();
            int firstIndex = 0;
            int secondIndex = 0;
            while (firstIndex < startGeometry.size() && secondIndex < endGeometry.size())
            {
                double firstRatio = firstIndex < startGeometry.size() ? startGeometry.lengthAtIndex(firstIndex) / firstLength
                        : Double.MAX_VALUE;
                double secondRatio = secondIndex < endGeometry.size() ? endGeometry.lengthAtIndex(secondIndex) / secondLength
                        : Double.MAX_VALUE;
                double ratio;
                if (firstRatio < secondRatio)
                {
                    ratio = firstRatio;
                    firstIndex++;
                }
                else
                {
                    ratio = secondRatio;
                    secondIndex++;
                }
                Point2d firstCoordinate = startGeometry.getLocation(ratio * firstLength);
                Point2d secondCoordinate = endGeometry.getLocation(ratio * secondLength);
                Point2d resultCoordinate = new Point2d((1 - ratio) * firstCoordinate.x + ratio * secondCoordinate.x,
                        (1 - ratio) * firstCoordinate.y + ratio * secondCoordinate.y);
                if (null == prevCoordinate || resultCoordinate.distance(prevCoordinate) > tooClose)
                {
                    out.add(resultCoordinate);
                    prevCoordinate = resultCoordinate;
                }
            }
        }
        return new PolyLine2d(0.0, out.toArray(new Point2d[out.size()]));
    }

    /**
     * Compute the 2D intersection of two lines. Both lines are defined by two points (that should be distinct).
     * @param line1P1X x-coordinate of start point of line 1
     * @param line1P1Y y-coordinate of start point of line 1
     * @param line1P2X x-coordinate of end point of line 1
     * @param line1P2Y y-coordinate of end point of line 1
     * @param lowLimitLine1 if {@code true}; the intersection may not lie before the start point of line 1
     * @param highLimitLine1 if {@code true}; the intersection may not lie beyond the end point of line 1
     * @param line2P1X x-coordinate of start point of line 2
     * @param line2P1Y y-coordinate of start point of line 2
     * @param line2P2X x-coordinate of end point of line 2
     * @param line2P2Y y-coordinate of end point of line 2
     * @param lowLimitLine2 if {@code true}; the intersection may not lie before the start point of line 2
     * @param highLimitLine2 if {@code true}; the intersection may not lie beyond the end point of line 2
     * @param eps tolerance (conservative to find intersections)
     * @return the intersection of the two lines, or {@code null} if the lines are (almost) parallel, or the intersection point
     *         lies outside the permitted range
     * @throws ArithmeticException when any of the parameters is {@code NaN}
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Point2d intersectionOfLinesEps(final double line1P1X, final double line1P1Y, final double line1P2X,
            final double line1P2Y, final boolean lowLimitLine1, final boolean highLimitLine1, final double line2P1X,
            final double line2P1Y, final double line2P2X, final double line2P2Y, final boolean lowLimitLine2,
            final boolean highLimitLine2, final double eps)
    {
        Throw.when(eps < 0.0, IllegalArgumentException.class, "eps may not be negative");
        double line1DX = line1P2X - line1P1X;
        double line1DY = line1P2Y - line1P1Y;
        double l2p1x = line2P1X - line1P1X;
        double l2p1y = line2P1Y - line1P1Y;
        double l2p2x = line2P2X - line1P1X;
        double l2p2y = line2P2Y - line1P1Y;
        double denominator = (l2p2y - l2p1y) * line1DX - (l2p2x - l2p1x) * line1DY;
        Throw.whenNaN(denominator, "none of the parameters may be NaN");
        if (Math.abs(denominator) < eps)
        {
            return null; // lines are parallel (they might even be on top of each other, but we don't check that)
        }
        double uA = ((l2p2x - l2p1x) * (-l2p1y) - (l2p2y - l2p1y) * (-l2p1x)) / denominator;
        // System.out.println("uA is " + uA);
        if (uA < -eps && lowLimitLine1 || uA > 1.0 + eps && highLimitLine1)
        {
            return null; // intersection outside line 1
        }
        double uB = (line1DY * l2p1x - line1DX * l2p1y) / denominator;
        // System.out.println("uB is " + uB);
        if (uB < -eps && lowLimitLine2 || uB > 1.0 + eps && highLimitLine2)
        {
            return null; // intersection outside line 2
        }
        if (Math.abs(uA - 1.0) < eps) // maximize precision
        {
            return new Point2d(line1P2X, line1P2Y);
        }
        if (Math.abs(uB) < eps)
        {
            return new Point2d(line2P1X, line2P1Y);
        }
        if (Math.abs(uB - 1.0) < eps)
        {
            return new Point2d(line2P2X, line2P2Y);
        }
        return new Point2d(line1P1X + uA * line1DX, line1P1Y + uA * line1DY);
    }

}
