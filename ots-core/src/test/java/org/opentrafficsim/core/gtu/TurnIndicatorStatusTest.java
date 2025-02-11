package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test the TurnIndicatorStatus class
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class TurnIndicatorStatusTest
{

    /**
     * Constructor.
     */
    public TurnIndicatorStatusTest()
    {
        //
    }

    /** Test the TurnIndicatorStatus class. */
    @Test
    public void turnIndicatorStatusTest()
    {
        assertTrue(TurnIndicatorStatus.NONE.isNone(), "NONE is none");
        assertFalse(TurnIndicatorStatus.NONE.isLeft(), "NONE is not left");
        assertFalse(TurnIndicatorStatus.NONE.isLeftOrBoth(), "NONE is not left or both");
        assertFalse(TurnIndicatorStatus.NONE.isRight(), "NONE is not right");
        assertFalse(TurnIndicatorStatus.NONE.isRightOrBoth(), "NONE is not right or both");
        assertFalse(TurnIndicatorStatus.NONE.isHazard(), "NONE is not hazard");

        assertFalse(TurnIndicatorStatus.LEFT.isNone(), "LEFT is not none");
        assertTrue(TurnIndicatorStatus.LEFT.isLeft(), "LEFT is left");
        assertTrue(TurnIndicatorStatus.LEFT.isLeftOrBoth(), "LEFT is left or both");
        assertFalse(TurnIndicatorStatus.LEFT.isRight(), "LEFT is not right");
        assertFalse(TurnIndicatorStatus.LEFT.isRightOrBoth(), "LEFT is not right or both");
        assertFalse(TurnIndicatorStatus.LEFT.isHazard(), "LEFT is not hazard");

        assertFalse(TurnIndicatorStatus.RIGHT.isNone(), "RIGHT is not none");
        assertFalse(TurnIndicatorStatus.RIGHT.isLeft(), "RIGHT is not left");
        assertFalse(TurnIndicatorStatus.RIGHT.isLeftOrBoth(), "RIGHT is not left or both");
        assertTrue(TurnIndicatorStatus.RIGHT.isRight(), "RIGHT is right");
        assertTrue(TurnIndicatorStatus.RIGHT.isRightOrBoth(), "RIGHT is right or both");
        assertFalse(TurnIndicatorStatus.RIGHT.isHazard(), "RIGHT is not hazard");

        assertFalse(TurnIndicatorStatus.HAZARD.isNone(), "HAZARD is not none");
        assertFalse(TurnIndicatorStatus.HAZARD.isLeft(), "HAZARD is not left");
        assertTrue(TurnIndicatorStatus.HAZARD.isLeftOrBoth(), "HAZARD is not left or both");
        assertFalse(TurnIndicatorStatus.HAZARD.isRight(), "HAZARD is not right");
        assertTrue(TurnIndicatorStatus.HAZARD.isRightOrBoth(), "HAZARD is not right or both");
        assertTrue(TurnIndicatorStatus.HAZARD.isHazard(), "HAZARD is not hazard");

        assertFalse(TurnIndicatorStatus.NOTPRESENT.isNone(), "NOTPRESENT is not none");
        assertFalse(TurnIndicatorStatus.NOTPRESENT.isLeft(), "NOTPRESENT is not left");
        assertFalse(TurnIndicatorStatus.NOTPRESENT.isLeftOrBoth(), "NOTPRESENT is not left or both");
        assertFalse(TurnIndicatorStatus.NOTPRESENT.isRight(), "NOTPRESENT is not right");
        assertFalse(TurnIndicatorStatus.NOTPRESENT.isRightOrBoth(), "NOTPRESENT is not right or both");
        assertFalse(TurnIndicatorStatus.NOTPRESENT.isHazard(), "NOTPRESENT is not hazard");

    }

}
