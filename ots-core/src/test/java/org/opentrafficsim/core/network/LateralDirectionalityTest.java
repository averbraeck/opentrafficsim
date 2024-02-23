package org.opentrafficsim.core.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test the methods in the LateralDirectionality enum.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LateralDirectionalityTest
{
    /**
     * Test the LateralDirectionality enum.
     */
    @Test
    public void testLateralDirectionality()
    {
        LateralDirectionality left = LateralDirectionality.LEFT;
        LateralDirectionality right = LateralDirectionality.RIGHT;
        LateralDirectionality none = LateralDirectionality.NONE;

        assertTrue(left.isLeft(), "left is left");
        assertTrue(right.isRight(), "right is right");
        assertFalse(left.isRight(), "left is not right");
        assertFalse(right.isLeft(), "right is not left");
        assertFalse(none.isLeft(), "none is not left");
        assertFalse(none.isRight(), "none is not right");
        assertFalse(left.isNone(), "left is not none");
        assertFalse(right.isNone(), "right is not none");
        assertTrue(none.isNone(), "none is none");

        assertEquals(right, left.flip(), "flip of left is right");
        assertEquals(left, right.flip(), "flip of right is left");
        assertEquals(none, none.flip(), "flip of none is none");
    }

}
