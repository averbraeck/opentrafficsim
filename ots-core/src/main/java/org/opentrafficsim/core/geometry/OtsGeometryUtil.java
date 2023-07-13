package org.opentrafficsim.core.geometry;

import java.util.Locale;
import java.util.NavigableMap;
import java.util.Map.Entry;

import org.djunits.value.vdouble.scalar.Angle;

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
     * Print one OtsPoint3d on the console.
     * @param prefix String; text to put before the output
     * @param point OtsPoint3d; the coordinate to print
     * @return String
     */
    public static String printCoordinate(final String prefix, final OtsPoint3d point)
    {
        return String.format(Locale.US, "%s %8.3f,%8.3f   ", prefix, point.x, point.y);
    }

    /**
     * Build a string description from an array of coordinates.
     * @param prefix String; text to put before the coordinates
     * @param coordinates OtsPoint3d[]; the points to print
     * @param separator String; prepended to each coordinate
     * @return String; description of the array of coordinates
     */
    public static String printCoordinates(final String prefix, final OtsPoint3d[] coordinates, final String separator)
    {
        return printCoordinates(prefix + "(" + coordinates.length + " pts)", coordinates, 0, coordinates.length, separator);
    }

    /**
     * Build a string description from an OtsLine3d.
     * @param prefix String; text to put before the coordinates
     * @param line OtsLine3d; the line for which to print the points
     * @param separator String; prepended to each coordinate
     * @return String; description of the OtsLine3d
     */
    public static String printCoordinates(final String prefix, final OtsLine3d line, final String separator)
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
    public static String printCoordinates(final String prefix, final OtsPoint3d[] points, final int fromIndex,
            final int toIndex, final String separator)
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
     * Returns a linearly interpolated offset at the given longitudinal fraction.
     * @param f double; longitudinal fraction.
     * @param offsets NavigableMap&lt;Double, Double&gt;; offset per longitudinal fraction.
     * @return double; linearly interpolated offset at the given longitudinal fraction.
     */
    public static double offsetInterpolation(final double f, final NavigableMap<Double, Double> offsets)
    {
        Entry<Double, Double> entry0 = offsets.floorEntry(f);
        Entry<Double, Double> entry1 = offsets.ceilingEntry(f);
        if (entry1 == null || entry0.equals(entry1))
        {
            return entry0.getValue();
        }
        double r = (f - entry0.getKey()) / (entry1.getKey() - entry0.getKey());
        return (1.0 - r) * entry0.getValue() + r * entry1.getValue();
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
     * @param point DirectedPoint; point.
     * @param offset double; offset, negative values are to the right.
     * @return OtsPoint3d; offset point.
     */
    public static DirectedPoint offsetPoint(final DirectedPoint point, final double offset)
    {
        return new DirectedPoint(point.x - Math.sin(point.dirZ) * offset, point.y + Math.cos(point.dirZ) * offset, point.z,
                point.dirX, point.dirY, point.dirZ);
    }

}
