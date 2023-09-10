package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
        assertTrue(TurnIndicatorIntent.NONE.isNone(), "NONE is none");
        assertFalse(TurnIndicatorIntent.NONE.isLeft(), "NONE is not left");
        assertFalse(TurnIndicatorIntent.NONE.isRight(), "NONE is not right");
        assertFalse(TurnIndicatorIntent.NONE.isConflicting(), "NONE is not conflicting");

        assertFalse(TurnIndicatorIntent.LEFT.isNone(), "LEFT is not none");
        assertTrue(TurnIndicatorIntent.LEFT.isLeft(), "LEFT is left");
        assertFalse(TurnIndicatorIntent.LEFT.isRight(), "LEFT is not right");
        assertFalse(TurnIndicatorIntent.LEFT.isConflicting(), "LEFT is not conflicting");

        assertFalse(TurnIndicatorIntent.RIGHT.isNone(), "RIGHT is not none");
        assertFalse(TurnIndicatorIntent.RIGHT.isLeft(), "RIGHT is not left");
        assertTrue(TurnIndicatorIntent.RIGHT.isRight(), "RIGHT is right");
        assertFalse(TurnIndicatorIntent.RIGHT.isConflicting(), "RIGHT is not conflicting");

        assertFalse(TurnIndicatorIntent.CONFLICTING.isNone(), "CONFLICTING is not none");
        assertFalse(TurnIndicatorIntent.CONFLICTING.isLeft(), "CONFLICTING is not left");
        assertFalse(TurnIndicatorIntent.CONFLICTING.isRight(), "CONFLICTING is not right");
        assertTrue(TurnIndicatorIntent.CONFLICTING.isConflicting(), "CONFLICTING is conflicting");

    }

}
