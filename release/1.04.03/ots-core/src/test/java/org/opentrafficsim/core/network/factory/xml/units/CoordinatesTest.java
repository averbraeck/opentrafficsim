package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * Test the Coordinates parser class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CoordinatesTest
{

    /**
     * Test the Coordinates parser class.
     */
    @Test
    public final void testCoordinates()
    {
        OTSPoint3D p = Coordinates.parseCoordinate("(1, 2, 3)");
        assertEquals("x", 1, p.x, 0);
        assertEquals("y", 2, p.y, 0);
        assertEquals("z", 3, p.z, 0);
        p = Coordinates.parseCoordinate("(1, 2)");
        assertEquals("x", 1, p.x, 0);
        assertEquals("y", 2, p.y, 0);
        assertEquals("z", 0, p.z, 0);

        OTSPoint3D[] points = Coordinates.parseCoordinates("(1, 2, 3)(4, 5, 6)");
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
