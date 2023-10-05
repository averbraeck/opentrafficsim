package org.opentrafficsim.core.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.djunits.value.vdouble.scalar.Angle;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;

/**
 * Utility class for OTS geometry.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
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
     * @param prefix String; text to put before the output
     * @param point Point2d; the coordinate to print
     * @return String
     */
    public static String printCoordinate(final String prefix, final Point2d point)
    {
        return String.format(Locale.US, "%s %8.3f,%8.3f   ", prefix, point.x, point.y);
    }

    /**
     * Build a string description from an array of coordinates.
     * @param prefix String; text to put before the coordinates
     * @param coordinates Point2d[]; the points to print
     * @param separator String; prepended to each coordinate
     * @return String; description of the array of coordinates
     */
    public static String printCoordinates(final String prefix, final Point2d[] coordinates, final String separator)
    {
        return printCoordinates(prefix + "(" + coordinates.length + " pts)", coordinates, 0, coordinates.length, separator);
    }

    /**
     * Build a string description from an OtsLine2d.
     * @param prefix String; text to put before the coordinates
     * @param line OtsLine2d; the line for which to print the points
     * @param separator String; prepended to each coordinate
     * @return String; description of the OtsLine2d
     */
    public static String printCoordinates(final String prefix, final OtsLine2d line, final String separator)
    {
        return printCoordinates(prefix + "(" + line.size() + " pts)", line.getPoints(), 0, line.size(), separator);
    }

    /**
     * Built a string description from part of an array of coordinates.
     * @param prefix String; text to put before the output
     * @param points OtsPoint3d[]; the coordinates to print
     * @param fromIndex int; index of the first coordinate to print
     * @param toIndex int; one higher than the index of the last coordinate to print
     * @param separator String; prepended to each coordinate
     * @return String; description of the selected part of the array of coordinates
     */
    public static String printCoordinates(final String prefix, final Point2d[] points, final int fromIndex, final int toIndex,
            final String separator)
    {
        StringBuilder result = new StringBuilder();
        result.append(prefix);
        String operator = "M"; // Move absolute
        for (int i = fromIndex; i < toIndex; i++)
        {
            result.append(separator);
            result.append(printCoordinate(operator, points[i]));
            operator = "L"; // LineTo Absolute
        }
        return result.toString();
    }

    /**
     * Returns the number of segments to use for a given maximum spatial error, and radius.
     * @param maxSpatialError double; maximum spatial error.
     * @param angle Angle; angle of arc at radius.
     * @param r double; critical radius (largest radius).
     * @return int; number of segments to use for a given maximum spatial error, and radius.
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
     * @param point OrientedPoint2d; point.
     * @param offset double; offset, negative values are to the right.
     * @return OrientedPoint2d; offset point.
     */
    public static OrientedPoint2d offsetPoint(final OrientedPoint2d point, final double offset)
    {
        return new OrientedPoint2d(point.x - Math.sin(point.dirZ) * offset, point.y + Math.cos(point.dirZ) * offset,
                point.dirZ);
    }

    /**
     * Create a line at linearly varying offset from this line. The offset may change linearly from its initial value at the
     * start of the reference line via a number of intermediate offsets at intermediate positions to its final offset value at
     * the end of the reference line.
     * @param line PolyLine2d; reference line.
     * @param relativeFractions double[]; positional fractions for which the offsets have to be generated
     * @param offsets double[]; offsets at the relative positions (positive value is Left, negative value is Right)
     * @return PolyLine2d; the PolyLine2d of the line at multi-linearly changing offset of the reference line
     * @throws OtsGeometryException when this method fails to create the offset line
     */
    // TODO: move this to PolyLine2d in djutils?
    public static final PolyLine2d offsetLine(final PolyLine2d line, final double[] relativeFractions, final double[] offsets)
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
        // TODO make tooClose a parameter of this method.
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
        return new PolyLine2d(out.toArray(new Point2d[out.size()]));
    }

}
