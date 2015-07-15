package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Test the methods and fields in the GTUType class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUTypeTest
{
    /**
     * Test the static fields of the GTUType class.
     */
    @Test
    public void staticsTest()
    {
        GTUType<String> t = GTUType.ALL;
        assertTrue("Id of ALL is \"ALL\"", "ALL".equals(t.getId()));
        t = GTUType.NONE;
        assertTrue("Id of NONE is \"NONE\"", "NONE".equals(t.getId()));
    }

    /**
     * Run the constructor and verify that all fields get correctly initialized.
     */
    @Test
    public void constructorTest()
    {
        GTUType<String> t = GTUType.makeGTUType("abc");
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
        GTUType<String> t2 = GTUType.makeGTUType("pqr");
        assertTrue("Id is stored in the newly created GTUType", "pqr".equals(t2.getId()));
        // prove that the two are really distinct (do not use the same storage for the type string
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
    }
}
