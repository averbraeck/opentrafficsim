package org.opentrafficsim.core.geometry;

import java.util.Locale;

/**
 * Utility class for OTS geometry.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
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
     * Print one OTSPoint3D on the console.
     * @param prefix String; text to put before the output
     * @param point OTSPoint3D; the coordinate to print
     * @return String
     */
    public static String printCoordinate(final String prefix, final OtsPoint3D point)
    {
        return String.format(Locale.US, "%s %8.3f,%8.3f   ", prefix, point.x, point.y);
    }

    /**
     * Build a string description from an array of coordinates.
     * @param prefix String; text to put before the coordinates
     * @param coordinates OTSPoint3D[]; the points to print
     * @param separator String; prepended to each coordinate
     * @return String; description of the array of coordinates
     */
    public static String printCoordinates(final String prefix, final OtsPoint3D[] coordinates, final String separator)
    {
        return printCoordinates(prefix + "(" + coordinates.length + " pts)", coordinates, 0, coordinates.length, separator);
    }

    /**
     * Build a string description from an OTSLine3D.
     * @param prefix String; text to put before the coordinates
     * @param line OTSLine3D; the line for which to print the points
     * @param separator String; prepended to each coordinate
     * @return String; description of the OTSLine3D
     */
    public static String printCoordinates(final String prefix, final OtsLine3D line, final String separator)
    {
        return printCoordinates(prefix + "(" + line.size() + " pts)", line.getPoints(), 0, line.size(), separator);
    }

    /**
     * Built a string description from part of an array of coordinates.
     * @param prefix String; text to put before the output
     * @param points OTSPoint3D[]; the coordinates to print
     * @param fromIndex int; index of the first coordinate to print
     * @param toIndex int; one higher than the index of the last coordinate to print
     * @param separator String; prepended to each coordinate
     * @return String; description of the selected part of the array of coordinates
     */
    public static String printCoordinates(final String prefix, final OtsPoint3D[] points, final int fromIndex,
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

}
