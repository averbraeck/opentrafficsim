package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the TurnIndicatorIntent class
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TurnIndicatorIntentTest
{
    /** Test the TurnIndicatorStatus class. */
    @Test
    public void turnIndicatorIntentTest()
    {
        assertTrue("NONE is none", TurnIndicatorIntent.NONE.isNone());
        assertFalse("NONE is not left", TurnIndicatorIntent.NONE.isLeft());
        assertFalse("NONE is not right", TurnIndicatorIntent.NONE.isRight());
        assertFalse("NONE is not conflicting", TurnIndicatorIntent.NONE.isConflicting());

        assertFalse("LEFT is not none", TurnIndicatorIntent.LEFT.isNone());
        assertTrue("LEFT is left", TurnIndicatorIntent.LEFT.isLeft());
        assertFalse("LEFT is not right", TurnIndicatorIntent.LEFT.isRight());
        assertFalse("LEFT is not conflicting", TurnIndicatorIntent.LEFT.isConflicting());

        assertFalse("RIGHT is not none", TurnIndicatorIntent.RIGHT.isNone());
        assertFalse("RIGHT is not left", TurnIndicatorIntent.RIGHT.isLeft());
        assertTrue("RIGHT is right", TurnIndicatorIntent.RIGHT.isRight());
        assertFalse("RIGHT is not conflicting", TurnIndicatorIntent.RIGHT.isConflicting());

        assertFalse("CONFLICTING is not none", TurnIndicatorIntent.CONFLICTING.isNone());
        assertFalse("CONFLICTING is not left", TurnIndicatorIntent.CONFLICTING.isLeft());
        assertFalse("CONFLICTING is not right", TurnIndicatorIntent.CONFLICTING.isRight());
        assertTrue("CONFLICTING is conflicting", TurnIndicatorIntent.CONFLICTING.isConflicting());

    }

}
