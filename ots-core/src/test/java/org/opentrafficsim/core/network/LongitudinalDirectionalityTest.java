package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * Test the methods in the LongitudinalDirectionality enum.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
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

    /**
     * Test the intersect method.
     */
    @Test
    public void testIntersectPermitsInvertAndGetDirectionalities()
    {
        LongitudinalDirectionality plus = LongitudinalDirectionality.DIR_PLUS;
        LongitudinalDirectionality minus = LongitudinalDirectionality.DIR_MINUS;
        LongitudinalDirectionality none = LongitudinalDirectionality.DIR_NONE;
        LongitudinalDirectionality both = LongitudinalDirectionality.DIR_BOTH;

        assertEquals("PLUS intersect PLUS yields PLUS", plus, plus.intersect(plus));
        assertEquals("PLUS intersect MINUS yields NONE", none, plus.intersect(minus));
        assertEquals("PLUS intersect NONE yields NONE", none, plus.intersect(none));
        assertEquals("PLUS intersect BOTH yields PLUS", plus, plus.intersect(both));

        assertEquals("MINUS intersect PLUS yields NONE", none, minus.intersect(plus));
        assertEquals("MINUS intersect MINUS yields MINUS", minus, minus.intersect(minus));
        assertEquals("MINUS intersect NONE yields NONE", none, minus.intersect(none));
        assertEquals("MINUS intersect BOTH yields MINUS", minus, minus.intersect(both));

        assertEquals("NONE intersect PLUS yields NONE", none, none.intersect(plus));
        assertEquals("NONE intersect MINUS yields NONE", none, none.intersect(minus));
        assertEquals("NONE intersect NONE yields NONE", none, none.intersect(none));
        assertEquals("NONE intersect BOTH yields NONE", none, none.intersect(both));

        assertEquals("BOTH intersect PLUS yields PLUS", plus, both.intersect(plus));
        assertEquals("BOTH intersect MINUS yields MINUS", minus, both.intersect(minus));
        assertEquals("BOTH intersect NONE yields NONE", none, both.intersect(none));
        assertEquals("BOTH intersect BOTH yields BOTH", both, both.intersect(both));

        assertEquals("PLUS intersect null yields NONE", none, plus.intersect(null));
        assertEquals("MINUS intersect null yields NONE", none, minus.intersect(null));
        assertEquals("NONE intersect null yields NONE", none, none.intersect(null));
        assertEquals("BOTH intersect null yields NONE", none, both.intersect(null));

        assertTrue("PLUS allows plus", plus.permits(GTUDirectionality.DIR_PLUS));
        assertFalse("PLUS does not allow minus", plus.permits(GTUDirectionality.DIR_MINUS));

        assertFalse("MINUS does not allow plus", minus.permits(GTUDirectionality.DIR_PLUS));
        assertTrue("MINUS allows minus", minus.permits(GTUDirectionality.DIR_MINUS));

        assertFalse("NONE does not allow plus", none.permits(GTUDirectionality.DIR_PLUS));
        assertFalse("NONE does not allow minus", none.permits(GTUDirectionality.DIR_MINUS));

        assertTrue("BOTH allows plus", both.permits(GTUDirectionality.DIR_PLUS));
        assertTrue("BOTH allows minus", both.permits(GTUDirectionality.DIR_MINUS));

        assertEquals("invert of PLUS is MINUS", minus, plus.invert());
        assertEquals("invert of MINUS is PLUS", plus, minus.invert());
        assertEquals("invert of NONE is NONE", none, none.invert());
        assertEquals("invert of BOTH is BOTH", both, both.invert());

        assertFalse("isNone of PLUS is false", plus.isNone());
        assertFalse("isNone of MINUS is false", minus.isNone());
        assertTrue("isNone of NONE is false", none.isNone());
        assertFalse("isNone of BOTH is false", both.isNone());

        assertTrue("directions of PLUS contains DIR_PLUS", plus.getDirectionalities().contains(GTUDirectionality.DIR_PLUS));
        assertEquals("directions of PLUS contains one element", 1, plus.getDirectionalities().size());

        assertTrue("directions of MINUS contains DIR_MINUS", minus.getDirectionalities().contains(GTUDirectionality.DIR_MINUS));
        assertEquals("directions of MINUS contains one element", 1, minus.getDirectionalities().size());

        assertEquals("directions of PLUS contains no elements", 0, none.getDirectionalities().size());

        assertTrue("directions of BOTH contains DIR_PLUS", both.getDirectionalities().contains(GTUDirectionality.DIR_PLUS));
        assertTrue("directions of BOTH contains DIR_MINUS", both.getDirectionalities().contains(GTUDirectionality.DIR_MINUS));
        assertEquals("directions of PLUS contains two elements", 2, both.getDirectionalities().size());
    }

}
