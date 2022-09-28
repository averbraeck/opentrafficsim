package org.opentrafficsim.core.gis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

import org.junit.Test;

/**
 * Test the Dutch RD to and from WGS84 transformations.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 30, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RDTests
{

    /**
     * Perform conversion to WGS84 and back and compare the results.
     * @param description String; description of the test
     * @param rdIn Point2D; location to test
     */
    final void forwardReverseCompare(final String description, final Point2D rdIn)
    {
        System.out.println(description + ":");
        System.out.println(String.format(Locale.US, "in:         (%9.2f,%9.2f)", rdIn.getX(), rdIn.getY()));
        Point2D wgs = TransformWGS84DutchRDNew.toWGS84(rdIn);
        System.out.println(String.format(Locale.US, "wgs84:      (%9.6f,%9.6f)", wgs.getX(), wgs.getY()));
        Point2D back = TransformWGS84DutchRDNew.fromWGS84(wgs);
        System.out.println(String.format(Locale.US, "back:       (%9.2f,%9.2f)", back.getX(), back.getY()));
        double distance = rdIn.distance(back);
        System.out.println(String.format("difference: %8.6fm", distance));
        assertTrue("Distance should be less than 0.5m", distance < 0.5);
    }

    /**
     * Test the transformations.
     */
    @Test
    public final void testMain()
    {
        forwardReverseCompare("Westertoren Amsterdam", new Point2D.Double(120700.723, 487525.501));
        forwardReverseCompare("Martinitoren Groningen", new Point2D.Double(233883.131, 582065.167));
        Point2D rdIn = new Point2D.Double(155000, 463000);
        forwardReverseCompare("OLV kerk Amersfoort", rdIn);
        Point2D wgs = TransformWGS84DutchRDNew.toWGS84(rdIn);
        // Dutch RD detects wrong order of coordinates (we could even fix it)
        try
        {
            TransformWGS84DutchRDNew.toWGS84(new Point2D.Double(463000, 155000));
            fail("RD coordinates in wrong order should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            // Ignore expected exception
        }
        // Valid WGS84 coordinates are never valid as Dutch RD coordinates
        try
        {
            TransformWGS84DutchRDNew.toWGS84(wgs);
            fail("Supplied WGS84 coordinates should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            // Ignore expected exception
        }
        // Dutch RD coordinates are never valid as WGS84 coordinates
        try
        {
            TransformWGS84DutchRDNew.fromWGS84(rdIn);
            fail("Supplied RD coordinates should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException exception)
        {
            // Ignore expected exception
        }
        // Check that attempts to convert coordinates slightly outside the bounding box do throw an exception
        Rectangle2D boundingBox = TransformWGS84DutchRDNew.fromWGS84Bounds();
        double centerX = boundingBox.getCenterX();
        double centerY = boundingBox.getCenterY();
        double halfWidth = boundingBox.getWidth() / 2;
        double halfHeight = boundingBox.getHeight() / 2;
        Rectangle2D boundingBoxWGS = TransformWGS84DutchRDNew.toWGS84Bounds();
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
                    TransformWGS84DutchRDNew.fromWGS84(rdIn);
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
                    TransformWGS84DutchRDNew.toWGS84(wgs);
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
        boundingBox = TransformWGS84DutchRDNew.toWGS84Bounds();
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
                    TransformWGS84DutchRDNew.fromWGS84(rdIn);
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
