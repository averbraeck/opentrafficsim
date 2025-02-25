package org.opentrafficsim.core.gis;

import org.opentrafficsim.animation.gis.CoordinateTransformLonLatToXy;

import nl.tudelft.simulation.dsol.animation.gis.DoubleXY;
import nl.tudelft.simulation.dsol.animation.gis.transform.CoordinateTransform;

/**
 * Manual test for lat-lon to xy transform.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class TestCoordinateTransformLonLatToXY
{

    /**
     * Constructor.
     */
    public TestCoordinateTransformLonLatToXY()
    {
        //
    }

    /**
     * Main method.
     * @param args args
     */
    public static void main(final String[] args)
    {
        // double latCenter = 37.419933552777, lonCenter = -122.05752616111;
        double latCenter = 37.40897623275873, lonCenter = -122.0246091728831;
        CoordinateTransform latLonToXY = new CoordinateTransformLonLatToXy(lonCenter, latCenter);
        // double[] xy = latLonToXY.doubleTransform(37.419933552777, -122.05752616111); // Moffett
        DoubleXY xy = latLonToXY.doubleTransform(-122.0246091728831, 37.40897623275873);
        System.out.println(xy.x() + ", " + xy.y());
        xy = latLonToXY.doubleTransform(-122.0283934, 37.4025104);
        System.out.println(xy.x() + ", " + xy.y());
        System.out.println();
        xy = latLonToXY.doubleTransform(-122.0256091728831, 37.40897623275873); // minx,y
        System.out.println(xy.x() + ", " + xy.y());
        xy = latLonToXY.doubleTransform(-122.0246091728831, 37.40997623275873); // maxx,y
        System.out.println(xy.x() + ", " + xy.y());
    }

    @Override
    public final String toString()
    {
        return "Test []";
    }

}
