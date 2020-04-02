package org.sim0mq.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;

import org.junit.Test;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Unit tests.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class PublisherTest
{

    /**
     * Test the Publisher class.
     * @throws RemoteException when that happens this test has failed
     */
    @Test
    public void testPublisher() throws RemoteException
    {
        OTSNetwork network = new OTSNetwork("test network for PublisherTest", true);
        Publisher publisher = new Publisher(network);
        assertTrue("id of publisher contains id of network", publisher.getId().contains(network.getId()));
        Object[] transceiverNames = publisher.getIdSource(0).get(null);
        assertNotNull("result of getIdSource should not be null", transceiverNames);
        assertTrue("result of getIdSource should not be empty", transceiverNames.length > 0);
        for (Object o : transceiverNames)
        {
            assertTrue("transceiver name is a String", o instanceof String);
            // System.out.println("transceiver: " + o);
        }
        // See if we can obtain the GTUIdTransceiver
        Object[] gtuIdTransceiver = publisher.get(new Object[] { "GTU id transceiver" });
        assertNotNull("result of get should not be null", gtuIdTransceiver);
        assertEquals("result should contain one element", 1, gtuIdTransceiver.length);
        assertTrue("result should contain a TransceiverInterface", gtuIdTransceiver[0] instanceof GTUIdTransceiver);
        // See if we can obtain the GTUTransceiver
        Object[] gtuTransceiver = publisher.get(new Object[] { "GTU transceiver" });
        assertNotNull("result of get should not be null", gtuTransceiver);
        assertEquals("result should contain one element", 1, gtuTransceiver.length);
        assertTrue("result should contain a TransceiverInterface", gtuTransceiver[0] instanceof GTUTransceiver);
        assertNull("request for non existent transceiver should return null",
                publisher.get(new Object[] { "No such transceiver" }));
        try
        {
            publisher.getIdSource(1);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }
        
        try
        {
            publisher.getIdSource(-1);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected exception
        }
    }

}
