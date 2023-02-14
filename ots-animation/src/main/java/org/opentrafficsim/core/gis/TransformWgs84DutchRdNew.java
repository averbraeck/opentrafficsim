package org.opentrafficsim.core.gis;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Convert geographical coordinates between WGS84 and the Dutch RD (Rijksdriehoek) system. <br>
 * Specific MathTransform for WGS84 (EPSG:4326) to RD_new (EPSG:28992) conversions. Code based on C code by Peter Knoppers as
 * applied <a href="https://www.regiolab-delft.nl/?q=node/36">here</a>, which is based on
 * <a href="https://home.solcon.nl/pvanmanen/Download/Transformatieformules.pdf">this</a> paper.
 * <p>
 * Copyright (c) ~2000-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author Gert-Jan Stolk
 **/
public final class TransformWgs84DutchRdNew
{

    /** Western boundary of the Dutch RD system. */
    private static final double WGS84_WEST_LIMIT = 3.2;

    /** Eastern boundary of the Dutch RD system. */
    private static final double WGS84_EAST_LIMIT = 7.3;

    /** Northern boundary of the Dutch RD system. */
    private static final double WGS84_SOUTH_LIMIT = 50.6;

    /** Southern boundary of the Dutch RD system. */
    private static final double WGS84_NORTH_LIMIT = 53.7;

    /** Western boundary of the Dutch RD system. */
    private static final double RD_MINIMUM_X = 11000;

    /** Eastern boundary of the Dutch RD system. */
    private static final double RD_MAXIMUM_X = 280000;

    /** Southern boundary of the Dutch RD system. */
    private static final double RD_MINIMUM_Y = 300000;

    /** Northern boundary of the Dutch RD system. */
    private static final double RD_MAXIMUM_Y = 630000;

    /** This class is a utility class and instances cannot be constructed. */
    private TransformWgs84DutchRdNew()
    {
        // This class is a utility class and instances cannot be constructed.
    }

    /**
     * Convert from WGS84 to RD coordinates.
     * @param wgs84East double; Degrees East of Greenwich
     * @param wgs84North double; Degrees North of the equator
     * @return Point2D; equivalent location in the Dutch RD system
     */
    private static Point2D.Double ellipseWgs84ToRd(final double wgs84East, final double wgs84North)
    {
        if (wgs84North > WGS84_NORTH_LIMIT || wgs84North < WGS84_SOUTH_LIMIT || wgs84East < WGS84_WEST_LIMIT
                || (wgs84East > WGS84_EAST_LIMIT))
        {
            throw new IllegalArgumentException("ellipswgs842rd input out of range (" + wgs84East + ", " + wgs84North + ")");
        }
        //@formatter:off
        @SuppressWarnings("checkstyle:nowhitespacebefore")
        /** Coefficients for ellipsewgs842rd. */
        final double[][] r = 
        { /* p down, q right */
            {  155000.00, 190094.945,   -0.008, -32.391, 0.0   , },
            {     -0.705, -11832.228,    0.0  ,   0.608, 0.0   , },
            {      0.0  ,   -114.221,    0.0  ,   0.148, 0.0   , },
            {      0.0  ,     -2.340,    0.0  ,   0.0  , 0.0   , },
            {      0.0  ,      0.0  ,    0.0  ,   0.0  , 0.0   , }
        };
        @SuppressWarnings("checkstyle:nowhitespacebefore")
        final double[][] s = 
        { /* p down, q right */
            { 463000.00 ,      0.433, 3638.893,   0.0  ,  0.092, },
            { 309056.544,     -0.032, -157.984,   0.0  , -0.054, },
            {     73.077,      0.0  ,   -6.439,   0.0  ,  0.0  , },
            {     59.788,      0.0  ,    0.0  ,   0.0  ,  0.0  , },
            {      0.0  ,      0.0  ,    0.0  ,   0.0  ,  0.0  , }
        };
        //@formatter:on
        double resultX = 0;
        double resultY = 0;
        double powNorth = 1;
        double dNorth = 0.36 * (wgs84North - 52.15517440);
        double dEast = 0.36 * (wgs84East - 5.38720621);

        for (int p = 0; p < 5; p++)
        {
            double powEast = 1;
            for (int q = 0; q < 5; q++)
            {
                resultX += r[p][q] * powEast * powNorth;
                resultY += s[p][q] * powEast * powNorth;
                powEast *= dEast;
            }
            powNorth *= dNorth;
        }
        return new Point2D.Double(resultX, resultY);
    }

    /**
     * Convert coordinates from WGS84 to the Dutch RD system.
     * @param rdX double; X coordinate in the Dutch RD system
     * @param rdY double; Y coordinate in the Dutch RD system
     * @return Point2D; equivalent location in the WGS84 system
     * @throws IllegalArgumentException when the given coordinates are not within the area of the Dutch RD system
     */
    private static Point2D rdToEllipseWgs84(final double rdX, final double rdY) throws IllegalArgumentException
    {
        if (rdX < RD_MINIMUM_X || rdX > RD_MAXIMUM_X || rdY < RD_MINIMUM_Y || rdY > RD_MAXIMUM_Y)
        {
            throw new IllegalArgumentException(
                    "Location (" + rdX + "," + rdY + ") is not within the range " + "of the Dutch RD system");
        }
        final double dX = (rdX - 155000) / 100000;
        final double dY = (rdY - 463000) / 100000;
        /* Coefficients are for zone 31 (0E .. 6E); roughly west of Apeldoorn */
        //@formatter:off
        @SuppressWarnings("checkstyle:nowhitespacebefore")
        final double[][] k = 
        { /* p down, q right */
            { 3600 * 52.15517440, 3235.65389, -0.24750, -0.06550, 0.0    , },
            {        -0.00738   ,   -0.00012,  0.0    ,  0.0    , 0.0    , },
            {       -32.58297   ,   -0.84978, -0.01709, -0.00039, 0.0    , },
            {         0.0       ,    0.0    ,  0.0    ,  0.0    , 0.0    , },
            {         0.00530   ,    0.00033,  0.0    ,  0.0    , 0.0    , },
            {         0.0       ,    0.0    ,  0.0    ,  0.0    , 0.0    , }
        };
        @SuppressWarnings("checkstyle:nowhitespacebefore")
        final double[][] l = { /* p down, q right */
            {  3600 * 5.38720621,    0.01199,  0.00022,  0.0    , 0.0    , },
            {      5260.52916   ,  105.94684,  2.45656,  0.05594, 0.00128, },
            {        -0.00022   ,    0.0    ,  0.0    ,  0.0    , 0.0    , },
            {        -0.81885   ,   -0.05607, -0.00256,  0.0    , 0.0    , },
            {         0.0       ,    0.0    ,  0.0    ,  0.0    , 0.0    , },
            {         0.00026   ,    0.0    ,  0.0    ,  0.0    , 0.0    , }
        };
        //@formatter:on
        double resultNorth = 0;
        double resultEast = 0;
        double powX = 1;
        for (int p = 0; p < 6; p++)
        {
            double powY = 1;
            for (int q = 0; q < 5; q++)
            {
                resultNorth += k[p][q] * powX * powY / 3600;
                resultEast += l[p][q] * powX * powY / 3600;
                powY *= dY;
            }
            powX *= dX;
        }
        return new Point2D.Double(resultEast, resultNorth);
    }

    /**
     * Convert a coordinate pair in the local system to WGS84 coordinates.
     * @param local Point2D; coordinates in the local system.
     * @return Point2D; the equivalent location in degrees in the WGS84 coordinate system
     * @throws IllegalArgumentException when <cite>local</cite> is not valid in the local system
     */
    public static Point2D toWgs84(final Point2D local) throws IllegalArgumentException
    {
        return toWgs84(local.getX(), local.getY());
    }

    /**
     * Convert a coordinate pair in the local system to WGS84 coordinates.
     * @param localX double; X-coordinate in the local system
     * @param localY double; Y-coordinate in the local system
     * @return Point2D; the equivalent location in degrees in the WGS84 coordinate system
     * @throws IllegalArgumentException when <cite>localX</cite>, <cite>localY</cite> is not valid in the local system
     */
    public static Point2D toWgs84(final double localX, final double localY) throws IllegalArgumentException
    {
        return rdToEllipseWgs84(localX, localY);
    }

    /**
     * Convert a coordinate pair in WGS84 coordinates to local coordinates.
     * @param wgs84 Point2D; coordinates in degrees in the WGS84 coordinate system
     * @return Point2D; the equivalent location in the local coordinate system
     * @throws IllegalArgumentException when <cite>wgs84</cite> is not valid in the local system
     */
    public static Point2D fromWgs84(final Point2D wgs84) throws IllegalArgumentException
    {
        return fromWgs84(wgs84.getX(), wgs84.getY());
    }

    /**
     * Convert a coordinate pair in WGS84 coordinates to local coordinates.
     * @param wgs84East double; East coordinate in degrees in the WGS84 system (negative value indicates West)
     * @param wgs84North double; North coordinate in degrees in the WGS84 system (negative value indicates South)
     * @return Point2D; the equivalent location in the local coordinate system
     * @throws IllegalArgumentException when <cite>wgs84</cite> is not valid in the local system
     */
    public static Point2D fromWgs84(final double wgs84East, final double wgs84North) throws IllegalArgumentException
    {
        return ellipseWgs84ToRd(wgs84East, wgs84North);
    }

    /**
     * Report the bounding box for conversion to the local coordinate system.<br>
     * Conversions from WGS84 to the local coordinate system should fail for locations outside this bounding box. If the valid
     * range is not adequately described by a rectangular bounding box, conversions for some areas within this bounding box may
     * also fail (with an IllegalArgumentException). There is no guarantee that the result of a conversion lies within the
     * bounding box for the reverse conversion.
     * @return Rectangle2D; bounding box in WGS84 degrees
     */
    public static Rectangle2D fromWgs84Bounds()
    {
        return new Rectangle2D.Double(WGS84_WEST_LIMIT, WGS84_SOUTH_LIMIT, WGS84_EAST_LIMIT - WGS84_WEST_LIMIT,
                WGS84_NORTH_LIMIT - WGS84_SOUTH_LIMIT);
    }

    /**
     * Report the bounding box for conversions from the local coordinate system. <br>
     * Conversions from the local coordinate system to WGS84 should fail for locations outside this bounding box. If the valid
     * range is not adequately described by a rectangular bounding box, conversions for some areas within this bounding box may
     * also fail (with an IllegalArgumentException). There is no guarantee that the result of a conversion lies within the
     * bounding box for the reverse conversion.
     * @return Rectangle2D; bounding box of the local coordinate system
     */
    public static Rectangle2D toWgs84Bounds()
    {
        return new Rectangle2D.Double(RD_MINIMUM_X, RD_MINIMUM_Y, RD_MAXIMUM_X - RD_MINIMUM_X, RD_MAXIMUM_Y - RD_MINIMUM_Y);
    }

}
