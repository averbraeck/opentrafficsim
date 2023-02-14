package org.opentrafficsim.core.gis;

import nl.tudelft.simulation.dsol.animation.gis.transform.CoordinateTransform;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TestCoordinateTransformLonLatToXY
{

    /**
     * @param args args
     */
    public static void main(final String[] args)
    {
        // double latCenter = 37.419933552777, lonCenter = -122.05752616111;
        double latCenter = 37.40897623275873, lonCenter = -122.0246091728831;
        CoordinateTransform latLonToXY = new CoordinateTransformLonLatToXy(lonCenter, latCenter);
        // double[] xy = latLonToXY.doubleTransform(37.419933552777, -122.05752616111); // Moffett
        double[] xy = latLonToXY.doubleTransform(-122.0246091728831, 37.40897623275873);
        System.out.println(xy[0] + ", " + xy[1]);
        xy = latLonToXY.doubleTransform(-122.0283934, 37.4025104);
        System.out.println(xy[0] + ", " + xy[1]);
        System.out.println();
        xy = latLonToXY.doubleTransform(-122.0256091728831, 37.40897623275873); // minx,y
        System.out.println(xy[0] + ", " + xy[1]);
        xy = latLonToXY.doubleTransform(-122.0246091728831, 37.40997623275873); // maxx,y
        System.out.println(xy[0] + ", " + xy[1]);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Test []";
    }

}
