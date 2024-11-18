package org.opentrafficsim.road.network.lane;

import static org.junit.jupiter.api.Assertions.fail;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.geometry.ContinuousLine.ContinuousDoubleFunction;
import org.opentrafficsim.core.geometry.FractionalLengthData;
import org.opentrafficsim.core.geometry.OtsLine2d;

/**
 * Test CrossSectionGeometry.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CrossSectionGeometryTest
{

    /** */
    public CrossSectionGeometryTest()
    {
    }

    /**
     * Test constructors.
     */
    @Test
    public void testConstructor()
    {
        // Construct geometry using CrossSectionSlices
        OtsLine2d centerLine = new OtsLine2d(new Point2d(0.0, 0.0), new Point2d(100.0, 0.0));
        Polygon2d contour = new Polygon2d(new Point2d(0.0, -1.75), new Point2d(100.0, -1.75), new Point2d(100.0, 1.75),
                new Point2d(0.0, -1.75));
        ContinuousDoubleFunction func = FractionalLengthData.of(0.0, 0.0);
        try
        {
            new CrossSectionGeometry(centerLine, contour, null, func);
            fail("null pointer for offset should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new CrossSectionGeometry(centerLine, contour, func, null);
            fail("null pointer for width should have thrown a NullPointerException");
        }
        catch (NullPointerException ne)
        {
            // Ignore expected exception
        }
        new CrossSectionGeometry(centerLine, contour, func, func);
    }

}
