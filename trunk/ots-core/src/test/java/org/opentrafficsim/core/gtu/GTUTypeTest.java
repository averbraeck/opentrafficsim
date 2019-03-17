package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Test the methods and fields in the GTUType class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 jan. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUTypeTest
{

    /**
     * Run the constructor and verify that all fields get correctly initialized.
     */
    @Test
    public final void constructorTest()
    {
        OTSNetwork network = new OTSNetwork("network", true);
        GTUType t = new GTUType("abc", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
        GTUType t2 = new GTUType("pqr", network.getGtuType(GTUType.DEFAULTS.VEHICLE));
        assertTrue("Id is stored in the newly created GTUType", "pqr".equals(t2.getId()));
        // prove that the two are really distinct (do not use the same storage for the type string
        assertTrue("Id is stored in the newly created GTUType", "abc".equals(t.getId()));
        assertEquals("parent can be retrieved", network.getGtuType(GTUType.DEFAULTS.VEHICLE), t.getParent());
    }
}
