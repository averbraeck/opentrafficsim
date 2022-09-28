package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the methods in the LateralDirectionality enum.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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

        assertTrue("left is left", left.isLeft());
        assertTrue("right is right", right.isRight());
        assertFalse("left is not right", left.isRight());
        assertFalse("right is not left", right.isLeft());
        assertFalse("none is not left", none.isLeft());
        assertFalse("none is not right", none.isRight());
        assertFalse("left is not none", left.isNone());
        assertFalse("right is not none", right.isNone());
        assertTrue("none is none", none.isNone());

        assertEquals("flip of left is right", right, left.flip());
        assertEquals("flip of right is left", left, right.flip());
        assertEquals("flip of none is none", none, none.flip());
    }

}
