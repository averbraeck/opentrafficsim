package org.opentrafficsim.road.network.factory;

import nl.javel.gisbeans.io.esri.CoordinateTransform;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CoordinateTransformLonLatToXY implements CoordinateTransform
{
    /** The x-center of the center point (0, 0). */
    private double centerX = 0.0;

    /** The y-center of the center point (0, 0). */
    private double centerY = 0.0;

    /** The x-center of the center point (0, 0). */
    private final double lonCenter;

    /** The y-center of the center point (0, 0). */
    private final double latCenter;

    /** One percent of a lat degree to y meters. */
    private double latToM;

    /** One percent of a lon degree to x meters. */
    private double lonToM;

    /** Earth constants. */
    private static final double Re = 6378137;

    /** Earth constants. */
    private static final double Rp = 6356752.31424518;

    /**
     * Transformation from: https://en.wikipedia.org/wiki/Geographic_coordinate_system.
     * @param latCenter the latitude of the center point (0, 0)
     * @param lonCenter the longitude of the center point (0, 0)
     */
    public CoordinateTransformLonLatToXY(final double lonCenter, final double latCenter)
    {
        super();
        this.latCenter = latCenter;
        this.lonCenter = lonCenter;
        double lr = Math.toRadians(latCenter);
        this.latToM =
            111132.92 - 559.82 * Math.cos(2.0 * lr) + 1.175 * Math.cos(4.0 * lr) - 0.0023 * Math.cos(6.0 * lr); // 111.31;
        this.lonToM = 111412.84 * Math.cos(lr) - 93.5 * Math.cos(3.0 * lr) - 0.118 * Math.cos(5.0 * lr); // 88.32;
    }

    /** {@inheritDoc} */
    @Override
    public float[] floatTransform(double lon, double lat)
    {
        double[] dt = doubleTransform(lon, lat);
        return new float[]{(float) dt[0], (float) dt[1]};
    }

    /**
     * Convert WGS84 coordinates to Cartesian coordinates.
     * @param lon double; longitude in degrees
     * @param lat double; latitude in degrees
     * @return double[]
     */
    public double[] doubleTransformWSG84toCartesianXY(double lon, double lat)
    {
        double latrad = lat / 180.0 * Math.PI;
        double lonrad = lon / 180.0 * Math.PI;

        double coslat = Math.cos(latrad);
        double sinlat = Math.sin(latrad);
        double coslon = Math.cos(lonrad);
        double sinlon = Math.sin(lonrad);

        double term1 = (Re * Re * coslat) / Math.sqrt(Re * Re * coslat * coslat + Rp * Rp * sinlat * sinlat);

        double term2 = lat * coslat + term1;

        double x = coslon * term2 - this.centerX;
        double y = sinlon * term2 - this.centerY;

        return new double[]{x, y};
    }

    public double[] doubleTransform(double lon, double lat)
    {
        double x = (lon - this.lonCenter) * this.lonToM;
        double y = (lat - this.latCenter) * this.latToM;

        return new double[]{x, y};
    }

}
