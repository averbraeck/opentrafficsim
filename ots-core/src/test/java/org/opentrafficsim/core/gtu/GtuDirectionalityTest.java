package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the GTUDirectionality class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuDirectionalityTest
{

    /** Test the GTUDirectionality class. */
    @Test
    public void testGTUDirectionality()
    {
        assertTrue("DIR_PLUS isPlus", GTUDirectionality.DIR_PLUS.isPlus());
        assertFalse("DIR_Plus is not isMinus", GTUDirectionality.DIR_PLUS.isMinus());
        assertTrue("DIR_MINUS isMinus", GTUDirectionality.DIR_MINUS.isMinus());
        assertFalse("DIR_MINUS is not isPlus", GTUDirectionality.DIR_MINUS.isPlus());
        assertEquals("DIR_PLUS flipped is DIR_MINUS", GTUDirectionality.DIR_PLUS.flip(), GTUDirectionality.DIR_MINUS);
        assertEquals("DIR_MINUS flipped is DIR_PLUS", GTUDirectionality.DIR_MINUS.flip(), GTUDirectionality.DIR_PLUS);
        assertNotEquals("DIR_PLUS is not equal to DIR_MINUS", GTUDirectionality.DIR_PLUS, GTUDirectionality.DIR_MINUS);
    }

}
