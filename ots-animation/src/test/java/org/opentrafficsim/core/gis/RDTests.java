package org.opentrafficsim.core.gis;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.animation.gis.TransformWgs84DutchRdNew;

/**
 * Test the Dutch RD to and from WGS84 transformations.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class RDTests
{

    /** Verbose test. */
    private static final boolean VERBOSE = false;

    /** */
    private RDTests()
    {
        // do not instantiate test class
    }

    /**
     * Perform conversion to WGS84 and back and compare the results.
     * @param description description of the test
     * @param rdIn location to test
     */
    void forwardReverseCompare(final String description, final Point2D rdIn)
    {

        Point2D wgs = TransformWgs84DutchRdNew.toWgs84(rdIn);
        Point2D back = TransformWgs84DutchRdNew.fromWgs84(wgs);
        double distance = rdIn.distance(back);
        if (VERBOSE)
        {
            System.out.println(description + ":");
            System.out.println(String.format(Locale.US, "in:         (%9.2f,%9.2f)", rdIn.getX(), rdIn.getY()));
            System.out.println(String.format(Locale.US, "wgs84:      (%9.6f,%9.6f)", wgs.getX(), wgs.getY()));
            System.out.println(String.format(Locale.US, "back:       (%9.2f,%9.2f)", back.getX(), back.getY()));
            System.out.println(String.format("difference: %8.6fm", distance));
        }
        assertTrue(distance < 0.5, "Distance should be less than 0.5m");
    }

    /**
     * Test the transformations.
     */
    @Test
    public void testMain()
    {
        forwardReverseCompare("Westertoren Amsterdam", new Point2D.Double(120700.723, 487525.501));
        forwardReverseCompare("Martinitoren Groningen", new Point2D.Double(233883.131, 582065.167));
        Point2D rdIn = new Point2D.Double(155000, 463000);
        forwardReverseCompare("OLV kerk Amersfoort", rdIn);
        Point2D wgs = TransformWgs84DutchRdNew.toWgs84(rdIn);
        // Dutch RD detects wrong order of coordinates (we could even fix it)
        try
        {
            TransformWgs84DutchRdNew.toWgs84(new Point2D.Double(463000, 155000));
            fail("RD coordinates in wrong order should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            // Ignore expected exception
        }
        // Valid WGS84 coordinates are never valid as Dutch RD coordinates
        try
        {
            TransformWgs84DutchRdNew.toWgs84(wgs);
            fail("Supplied WGS84 coordinates should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            // Ignore expected exception
        }
        // Dutch RD coordinates are never valid as WGS84 coordinates
        try
        {
            TransformWgs84DutchRdNew.fromWgs84(rdIn);
            fail("Supplied RD coordinates should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            // Ignore expected exception
        }
        // Check that attempts to convert coordinates slightly outside the bounding box do throw an exception
        Rectangle2D boundingBox = TransformWgs84DutchRdNew.fromWgs84Bounds();
        double centerX = boundingBox.getCenterX();
        double centerY = boundingBox.getCenterY();
        double halfWidth = boundingBox.getWidth() / 2;
        double halfHeight = boundingBox.getHeight() / 2;
        Rectangle2D boundingBoxWGS = TransformWgs84DutchRdNew.toWgs84Bounds();
        double centerEW = boundingBoxWGS.getCenterX();
        double centerNS = boundingBoxWGS.getCenterY();
        double halfWidthWGS = boundingBoxWGS.getWidth() / 2;
        double halfHeightWGS = boundingBoxWGS.getHeight() / 2;
        for (int xFactor = -1; xFactor <= 1; xFactor += 1)
        {
            for (int yFactor = -1; yFactor <= 1; yFactor += 1)
            {
                rdIn = new Point2D.Double(centerX + xFactor * 1.05 * halfWidth, centerY + yFactor * 1.05 * halfHeight);
                try
                {
                    TransformWgs84DutchRdNew.fromWgs84(rdIn);
                    if (xFactor != 0 || yFactor != 0)
                    {
                        fail("Supplied RD coordinates (" + rdIn + ") should have thrown an " + "IllegalArgumentException");
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    if (xFactor == 0 && yFactor == 0)
                    {
                        fail("Supplied RD coordinates (" + rdIn + ") should NOT have thrown an IllegalArgumentException");
                    }
                }
                wgs = new Point2D.Double(centerEW + xFactor * 1.05 * halfWidthWGS, centerNS + yFactor * 1.05 * halfHeightWGS);
                try
                {
                    TransformWgs84DutchRdNew.toWgs84(wgs);
                    if (xFactor != 0 || yFactor != 0)
                    {
                        fail("Supplied WGS coordinates (" + wgs + ") should have thrown an " + "IllegalArgumentException");
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    if (xFactor == 0 && yFactor == 0)
                    {
                        fail("Supplied WGS coordinates (" + wgs + ") should NOT have thrown an IllegalArgumentException");
                    }
                }
            }
        }
        boundingBox = TransformWgs84DutchRdNew.toWgs84Bounds();
        centerX = boundingBox.getCenterX();
        centerY = boundingBox.getCenterY();
        halfWidth = boundingBox.getWidth() / 1;
        halfHeight = boundingBox.getHeight() / 2;
        for (int xFactor = -1; xFactor <= 1; xFactor += 1)
        {
            for (int yFactor = -1; yFactor <= 1; yFactor += 2)
            {
                rdIn = new Point2D.Double(centerX + xFactor * 1.05 * halfWidth, centerY + yFactor * 1.05 * halfHeight);
                try
                {
                    TransformWgs84DutchRdNew.fromWgs84(rdIn);
                    if (xFactor != 0 || yFactor != 0)
                    {
                        fail("Supplied RD coordinates should have thrown an IllegalArgumentException");
                    }
                }
                catch (IllegalArgumentException exception)
                {
                    if (xFactor == 0 && yFactor == 0)
                    {
                        fail("Supplied RD coordinates should NOT have thrown an IllegalArgumentException");
                    }
                }
            }
        }
        // Show precision at the corners of the bounding box
        forwardReverseCompare("South West corner", new Point2D.Double(boundingBox.getMinX(), boundingBox.getMinY()));
        forwardReverseCompare("South East corner", new Point2D.Double(boundingBox.getMaxX(), boundingBox.getMinY()));
        forwardReverseCompare("North West corner", new Point2D.Double(boundingBox.getMinX(), boundingBox.getMaxY()));
        forwardReverseCompare("North East corner", new Point2D.Double(boundingBox.getMaxX(), boundingBox.getMaxY()));
    }

}
