package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the directions parser.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 17, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectionsTest
{

    /**
     * Test the direction parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testDirections() throws NetworkException
    {
        assertEquals("forward", LongitudinalDirectionality.DIR_PLUS, Directions.parseDirection("FORWARD"));
        assertEquals("reverse", LongitudinalDirectionality.DIR_MINUS, Directions.parseDirection("BACKWARD"));
        assertEquals("both", LongitudinalDirectionality.DIR_BOTH, Directions.parseDirection("BOTH"));
        try
        {
            Directions.parseDirection("ANY");
            fail("Undefined direction should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            Directions.parseDirection(null);
            fail("null String should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
    }

}
