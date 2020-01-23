package org.opentrafficsim.core.network;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the methods in the LongitudinalDirectionality enum.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 2, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LongitudinalDirectionalityTest
{

    /**
     * Test all methods of the LongitudinalDirectionality class.
     */
    @Test
    public final void testLongitudinalDirectionality()
    {
        // Test all 16 combinations
        assertTrue("PLUS contains PLUS", LongitudinalDirectionality.DIR_PLUS.contains(LongitudinalDirectionality.DIR_PLUS));
        assertFalse("PLUS does not contain MINUS",
                LongitudinalDirectionality.DIR_PLUS.contains(LongitudinalDirectionality.DIR_MINUS));
        assertFalse("PLUS does not contain BOTH",
                LongitudinalDirectionality.DIR_PLUS.contains(LongitudinalDirectionality.DIR_BOTH));
        assertTrue("PLUS contains NONE", LongitudinalDirectionality.DIR_PLUS.contains(LongitudinalDirectionality.DIR_NONE));
        assertFalse("MINUS does not contain PLUS",
                LongitudinalDirectionality.DIR_MINUS.contains(LongitudinalDirectionality.DIR_PLUS));
        assertTrue("MINUS contains MINUS", LongitudinalDirectionality.DIR_MINUS.contains(LongitudinalDirectionality.DIR_MINUS));
        assertFalse("MINUS does not contain BOTH",
                LongitudinalDirectionality.DIR_MINUS.contains(LongitudinalDirectionality.DIR_BOTH));
        assertTrue("MINUS contains NONE", LongitudinalDirectionality.DIR_MINUS.contains(LongitudinalDirectionality.DIR_NONE));
        assertTrue("BOTH contains PLUS", LongitudinalDirectionality.DIR_BOTH.contains(LongitudinalDirectionality.DIR_PLUS));
        assertTrue("BOTH contains MINUS", LongitudinalDirectionality.DIR_BOTH.contains(LongitudinalDirectionality.DIR_MINUS));
        assertTrue("BOTH contains BOTH", LongitudinalDirectionality.DIR_BOTH.contains(LongitudinalDirectionality.DIR_BOTH));
        assertTrue("BOTH contains NONE", LongitudinalDirectionality.DIR_BOTH.contains(LongitudinalDirectionality.DIR_NONE));
        // These four are not specified in the JavaDoc...
        assertFalse("NONE does not contain PLUS",
                LongitudinalDirectionality.DIR_NONE.contains(LongitudinalDirectionality.DIR_PLUS));
        assertFalse("NONE does not contain MINUS",
                LongitudinalDirectionality.DIR_NONE.contains(LongitudinalDirectionality.DIR_MINUS));
        assertFalse("NONE does not contain BOTH",
                LongitudinalDirectionality.DIR_NONE.contains(LongitudinalDirectionality.DIR_BOTH));
        assertTrue("NONE contains NONE", LongitudinalDirectionality.DIR_NONE.contains(LongitudinalDirectionality.DIR_NONE));

        assertTrue("PLUS implies forward or both", LongitudinalDirectionality.DIR_PLUS.isForwardOrBoth());
        assertTrue("PLUS implies forward", LongitudinalDirectionality.DIR_PLUS.isForward());
        assertFalse("PLUS does not imply backward or both both", LongitudinalDirectionality.DIR_PLUS.isBackwardOrBoth());
        assertFalse("PLUS does not imply backward", LongitudinalDirectionality.DIR_PLUS.isBackward());
        assertFalse("PLUS does not imply both", LongitudinalDirectionality.DIR_PLUS.isBoth());
        assertFalse("MINUS does not imply forward or both", LongitudinalDirectionality.DIR_MINUS.isForwardOrBoth());
        assertFalse("MINUS does not imply forward", LongitudinalDirectionality.DIR_MINUS.isForward());
        assertTrue("MINUS implies backward or both both", LongitudinalDirectionality.DIR_MINUS.isBackwardOrBoth());
        assertTrue("MINUS implies backward", LongitudinalDirectionality.DIR_MINUS.isBackward());
        assertFalse("MINUS does not imply both", LongitudinalDirectionality.DIR_MINUS.isBoth());
        assertTrue("BOTH implies forward or both", LongitudinalDirectionality.DIR_BOTH.isForwardOrBoth());
        assertFalse("BOTH dies not imply forward", LongitudinalDirectionality.DIR_BOTH.isForward());
        assertTrue("BOTH implies backward or both both", LongitudinalDirectionality.DIR_BOTH.isBackwardOrBoth());
        assertFalse("BOTH does not imply backward", LongitudinalDirectionality.DIR_BOTH.isBackward());
        assertTrue("BOTH implies both", LongitudinalDirectionality.DIR_BOTH.isBoth());
        assertFalse("NONE does not imply forward or both", LongitudinalDirectionality.DIR_NONE.isForwardOrBoth());
        assertFalse("NONE does not imply forward", LongitudinalDirectionality.DIR_NONE.isForward());
        assertFalse("NONE does not imply backward or both both", LongitudinalDirectionality.DIR_NONE.isBackwardOrBoth());
        assertFalse("NONE does not imply backward", LongitudinalDirectionality.DIR_NONE.isBackward());
        assertFalse("NONE does not imply both", LongitudinalDirectionality.DIR_NONE.isBoth());
    }

}
