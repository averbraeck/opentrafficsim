package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the TurnIndicatorStatus class
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TurnIndicatorStatusTest
{

    /** Test the TurnIndicatorStatus class. */
    @Test
    public void turnIndicatorStatusTest()
    {
        assertTrue("NONE is none", TurnIndicatorStatus.NONE.isNone());
        assertFalse("NONE is not left", TurnIndicatorStatus.NONE.isLeft());
        assertFalse("NONE is not left or both", TurnIndicatorStatus.NONE.isLeftOrBoth());
        assertFalse("NONE is not right", TurnIndicatorStatus.NONE.isRight());
        assertFalse("NONE is not right or both", TurnIndicatorStatus.NONE.isRightOrBoth());
        assertFalse("NONE is not hazard", TurnIndicatorStatus.NONE.isHazard());

        assertFalse("LEFT is not none", TurnIndicatorStatus.LEFT.isNone());
        assertTrue("LEFT is left", TurnIndicatorStatus.LEFT.isLeft());
        assertTrue("LEFT is left or both", TurnIndicatorStatus.LEFT.isLeftOrBoth());
        assertFalse("LEFT is not right", TurnIndicatorStatus.LEFT.isRight());
        assertFalse("LEFT is not right or both", TurnIndicatorStatus.LEFT.isRightOrBoth());
        assertFalse("LEFT is not hazard", TurnIndicatorStatus.LEFT.isHazard());

        assertFalse("RIGHT is not none", TurnIndicatorStatus.RIGHT.isNone());
        assertFalse("RIGHT is not left", TurnIndicatorStatus.RIGHT.isLeft());
        assertFalse("RIGHT is not left or both", TurnIndicatorStatus.RIGHT.isLeftOrBoth());
        assertTrue("RIGHT is right", TurnIndicatorStatus.RIGHT.isRight());
        assertTrue("RIGHT is right or both", TurnIndicatorStatus.RIGHT.isRightOrBoth());
        assertFalse("RIGHT is not hazard", TurnIndicatorStatus.RIGHT.isHazard());

        assertFalse("HAZARD is not none", TurnIndicatorStatus.HAZARD.isNone());
        assertFalse("HAZARD is not left", TurnIndicatorStatus.HAZARD.isLeft());
        assertTrue("HAZARD is not left or both", TurnIndicatorStatus.HAZARD.isLeftOrBoth());
        assertFalse("HAZARD is not right", TurnIndicatorStatus.HAZARD.isRight());
        assertTrue("HAZARD is not right or both", TurnIndicatorStatus.HAZARD.isRightOrBoth());
        assertTrue("HAZARD is not hazard", TurnIndicatorStatus.HAZARD.isHazard());

        assertFalse("NOTPRESENT is not none", TurnIndicatorStatus.NOTPRESENT.isNone());
        assertFalse("NOTPRESENT is not left", TurnIndicatorStatus.NOTPRESENT.isLeft());
        assertFalse("NOTPRESENT is not left or both", TurnIndicatorStatus.NOTPRESENT.isLeftOrBoth());
        assertFalse("NOTPRESENT is not right", TurnIndicatorStatus.NOTPRESENT.isRight());
        assertFalse("NOTPRESENT is not right or both", TurnIndicatorStatus.NOTPRESENT.isRightOrBoth());
        assertFalse("NOTPRESENT is not hazard", TurnIndicatorStatus.NOTPRESENT.isHazard());

    }

}
