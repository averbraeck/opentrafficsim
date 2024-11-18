package org.opentrafficsim.road.network.lane;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.geometry.OtsGeometryException;
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
        Length startLateralPos = new Length(2, LengthUnit.METER);
        Length startWidth = new Length(3, LengthUnit.METER);

        // Construct geometry using CrossSectionSlices
        OtsLine2d centerLine = new OtsLine2d(new Point2d(0.0, 0.0), new Point2d(100.0, 0.0));
        Polygon2d contour = new Polygon2d(new Point2d(0.0, -1.75), new Point2d(100.0, -1.75), new Point2d(100.0, 1.75),
                new Point2d(0.0, -1.75));
        try
        {
            new CrossSectionGeometry(centerLine, contour, null);
            fail("null pointer for CrossSectionSlices should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        List<CrossSectionSlice> crossSectionSlices = new ArrayList<>();
        try
        {
            new CrossSectionGeometry(centerLine, contour, crossSectionSlices);
            fail("empty CrossSectionSlices should have thrown a NetworkException");
        }
        catch (OtsGeometryException ne)
        {
            // Ignore expected exception
        }
        crossSectionSlices.add(new CrossSectionSlice(Length.ZERO, startLateralPos, startWidth));
        new CrossSectionGeometry(centerLine, contour, crossSectionSlices);
    }

}
