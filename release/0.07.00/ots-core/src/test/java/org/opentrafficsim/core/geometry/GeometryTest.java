package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

/**
 * Test the methods in the OTSGeometry class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 6, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GeometryTest
{
    /**
     * 
     */
    @Test
    public void geometryTest()
    {
        OTSPoint3D p0 = new OTSPoint3D(1.2, 2.3, 3.4);
        String prefix = "Prefix";
        String result = OTSGeometry.printCoordinate(prefix, p0);
        assertTrue("output starts with prefix", result.startsWith(prefix));
        assertTrue("output contains x coordinate in three decimal digits",
                result.contains(String.format(Locale.US, "%8.3f", p0.x)));
        assertTrue("output contains y coordinate in three decimal digits",
                result.contains(String.format(Locale.US, "%8.3f", p0.y)));
    }
}
