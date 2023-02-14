package org.opentrafficsim.core.gis;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.animation.gis.transform.CoordinateTransform;

/**
 * Transformation from lat-lon to X-Y in meters. Source: https://en.wikipedia.org/wiki/Geographic_coordinate_system.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class CoordinateTransformLonLatToXy implements CoordinateTransform, Serializable
{
    /** */
    private static final long serialVersionUID = 20151130L;

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
    private static final double RE = 6378137;

    /** Earth constants. */
    private static final double RP = 6356752.31424518;

    /**
     * Transformation from: https://en.wikipedia.org/wiki/Geographic_coordinate_system.
     * @param latCenter double; the latitude of the center point (0, 0)
     * @param lonCenter double; the longitude of the center point (0, 0)
     */
    public CoordinateTransformLonLatToXy(final double lonCenter, final double latCenter)
    {
        this.latCenter = latCenter;
        this.lonCenter = lonCenter;
        double lr = Math.toRadians(latCenter);
        this.latToM = 111132.92 - 559.82 * Math.cos(2.0 * lr) + 1.175 * Math.cos(4.0 * lr) - 0.0023 * Math.cos(6.0 * lr); // 111.31;
        this.lonToM = 111412.84 * Math.cos(lr) - 93.5 * Math.cos(3.0 * lr) - 0.118 * Math.cos(5.0 * lr); // 88.32;
    }

    /** {@inheritDoc} */
    @Override
    public final float[] floatTransform(final double lon, final double lat)
    {
        double[] dt = doubleTransform(lon, lat);
        return new float[] {(float) dt[0], (float) dt[1]};
    }

    /**
     * Convert WGS84 coordinates to Cartesian coordinates.
     * @param lon double; longitude in degrees
     * @param lat double; latitude in degrees
     * @return double[]
     */
    public final double[] doubleTransformWgs84ToCartesianXy(final double lon, final double lat)
    {
        double latrad = lat / 180.0 * Math.PI;
        double lonrad = lon / 180.0 * Math.PI;

        double coslat = Math.cos(latrad);
        double sinlat = Math.sin(latrad);
        double coslon = Math.cos(lonrad);
        double sinlon = Math.sin(lonrad);

        double term1 = (RE * RE * coslat) / Math.sqrt(RE * RE * coslat * coslat + RP * RP * sinlat * sinlat);

        double term2 = lat * coslat + term1;

        double x = coslon * term2 - this.centerX;
        double y = sinlon * term2 - this.centerY;

        return new double[] {x, y};
    }

    /** {@inheritDoc} */
    @Override
    public final double[] doubleTransform(final double lon, final double lat)
    {
        double x = (lon - this.lonCenter) * this.lonToM;
        double y = (lat - this.latCenter) * this.latToM;

        return new double[] {x, y};
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CoordinateTransformLonLatToXY [centerX=" + this.centerX + ", centerY=" + this.centerY + ", lonCenter="
                + this.lonCenter + ", latCenter=" + this.latCenter + ", latToM=" + this.latToM + ", lonToM=" + this.lonToM
                + "]";
    }

}
