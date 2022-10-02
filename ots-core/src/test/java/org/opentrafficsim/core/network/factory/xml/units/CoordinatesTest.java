package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.geometry.OtsPoint3D;

/**
 * Test the Coordinates parser class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CoordinatesTest
{

    /**
     * Test the Coordinates parser class.
     */
    @Test
    public final void testCoordinates()
    {
        OtsPoint3D p = Coordinates.parseCoordinate("(1, 2, 3)");
        assertEquals("x", 1, p.x, 0);
        assertEquals("y", 2, p.y, 0);
        assertEquals("z", 3, p.z, 0);
        p = Coordinates.parseCoordinate("(1, 2)");
        assertEquals("x", 1, p.x, 0);
        assertEquals("y", 2, p.y, 0);
        assertEquals("z", 0, p.z, 0);

        OtsPoint3D[] points = Coordinates.parseCoordinates("(1, 2, 3)(4, 5, 6)");
        assertEquals("length is 2", 2, points.length);
        assertEquals("x", 1, points[0].x, 0);
        assertEquals("y", 2, points[0].y, 0);
        assertEquals("z", 3, points[0].z, 0);
        assertEquals("x", 4, points[1].x, 0);
        assertEquals("y", 5, points[1].y, 0);
        assertEquals("z", 6, points[1].z, 0);
        points = Coordinates.parseCoordinates("(1, 2)(4, 5, 6)");
        assertEquals("length is 2", 2, points.length);
        assertEquals("x", 1, points[0].x, 0);
        assertEquals("y", 2, points[0].y, 0);
        assertEquals("z", 0, points[0].z, 0);
        assertEquals("x", 4, points[1].x, 0);
        assertEquals("y", 5, points[1].y, 0);
        assertEquals("z", 6, points[1].z, 0);
    }

}
