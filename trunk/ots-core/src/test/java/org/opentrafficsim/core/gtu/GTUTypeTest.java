package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import org.junit.Test;

/**
 * Test the methods and fields in the GTUType class.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUTypeTest
{
    /**
     * Test the static fields of the GTUType class.
     */
    @Test
    public final void staticsTest()
    {
        GTUType t = GTUType.ALL;
        assertTrue("Id of ALL is \"ALL\"", "ALL".equals(t.getId()));
        t = GTUType.NONE;
        assertTrue("Id of NONE is \"NONE\"", "NONE".equals(t.getId()));
    }

    /**
     * Run the constructor and verify that all fields get correctly initialized.
     */
    @Test
    public final void constructorTest()
    {
        GTUType t = new GTUType("abc", VEHICLE);
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
        GTUType t2 = new GTUType("pqr", VEHICLE);
        assertTrue("Id is stored in the newly created GTUType", "pqr".equals(t2.getId()));
        // prove that the two are really distinct (do not use the same storage for the type string
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
    }
}
