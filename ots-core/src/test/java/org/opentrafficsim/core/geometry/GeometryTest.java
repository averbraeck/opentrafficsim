package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

/**
 * Test the methods in the OTSGeometry class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GeometryTest
{
    /**
     * Test the printCoordinate family of functions.
     */
    @Test
    public final void geometryTest()
    {
        OtsPoint3d p0 = new OtsPoint3d(1.2, 2.3, 3.4);
        String prefix = "Prefix";
        String result = OtsGeometryUtil.printCoordinate(prefix, p0);
        assertTrue("output starts with prefix", result.startsWith(prefix));
        assertTrue("output contains x coordinate in three decimal digits",
                result.contains(String.format(Locale.US, "%8.3f", p0.x)));
        assertTrue("output contains y coordinate in three decimal digits",
                result.contains(String.format(Locale.US, "%8.3f", p0.y)));
    }
}
