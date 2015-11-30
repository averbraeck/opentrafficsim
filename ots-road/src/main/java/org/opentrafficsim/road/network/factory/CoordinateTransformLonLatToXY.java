package org.opentrafficsim.road.network.factory;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;

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
    /** the x-center of the center point (0, 0). */
    private double centerX = 0.0;

    /** the y-center of the center point (0, 0). */
    private double centerY = 0.0;
    
    /** the x-center of the center point (0, 0). */
    private final double lonCenter;

    /** the y-center of the center point (0, 0). */
    private final double latCenter;
    
    /** one percent of a lat degree to y meters. */
    private double lat1000dToM;
    
    /** one percent of a lon degree to x meters. */
    private double lon1000dToM;
    
    /** earth constants. */
    private static final double Re = 6378137;

    /** earth constants. */
    private static final double Rp = 6356752.31424518;

    /**
     * @param latCenter the latitude of the center point (0, 0)
     * @param lonCenter the longitude of the center point (0, 0)
     */
    public CoordinateTransformLonLatToXY(final double lonCenter, final double latCenter)
    {
        super();
        this.latCenter = latCenter;
        this.lonCenter = lonCenter;
        this.lat1000dToM = 111.31;
        this.lon1000dToM = 88.32;
    }

    /** {@inheritDoc} */
    @Override
    public float[] floatTransform(double lon, double lat)
    {
        double[] dt = doubleTransform(lon, lat);
        return new float[]{(float) dt[0], (float) dt[1]};
    }

    public double[] doubleTransformX(double lon, double lat)
    {
        CoordinateReferenceSystem srcCRS = DefaultGeographicCRS.WGS84;
        CoordinateReferenceSystem destSRC = DefaultGeocentricCRS.CARTESIAN;
        boolean lenient = true; // allow for some error due to different datums
        try
        {
            MathTransform transform = CRS.findMathTransform(srcCRS, destSRC, lenient);
            Coordinate c = new Coordinate(lon, lat);
            Coordinate xy = JTS.transform(c, null ,transform); 
            return new double[]{xy.x - this.centerX, xy.y - this.centerY};
        }
        catch (FactoryException | TransformException exception)
        {
            exception.printStackTrace();
            return new double[]{this.centerX, this.centerY};
        } 
    }
    
    public double[] doubleTransformY(double lon, double lat)
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
        double x = (lon - this.lonCenter) * 1000 * this.lon1000dToM;
        double y = (lat - this.latCenter) * 1000 * this.lat1000dToM;

        return new double[]{x, y};
    }
    
}
