package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.Throw;
import org.djutils.metadata.MetaData;
import org.opentrafficsim.base.Identifiable;

/**
 * Transceivers with machine interpretable description of address and result types. A transceiver converts DSOL events to Sim0MQ
 * messages and Sim0MQ messages to DJUTILS event (un-)subscriptions, or DSOL events.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface TransceiverInterface extends Identifiable
{
    /**
     * Specification of arguments needed in a subscription request.
     * @return MetaData; the specification of arguments needed in a subscription request
     */
    MetaData getAddressFields();

    /**
     * Retrieve the TransceiverInterface that can be used to get valid values for one argument in a subscription request.
     * @param addressLevel int; index of the argument in the address fields
     * @return TransceiverInterface; to be used to get valid values for argument <code>addressLevel</code>, or null if valid
     *         values for the argument at index <code>addressLevel</code> can not be obtained through a TransceiverInterface
     *         object
     */
    default TransceiverInterface getIdSource(int addressLevel)
    {
        Throw.when(addressLevel < 0 || addressLevel >= getAddressFields().size(), IndexOutOfBoundsException.class,
                "Invalid addressLevel");
        return null; // Default is no id source. Override this method if there is one.
    }

    /**
     * Report the specification of a result of the transceiver.
     * @return MetaData; the specification of a result from the transceiver
     */
    MetaData getResultFields();

    /**
     * Retrieve the data.
     * @param address Object[]; the address of the data to retrieve
     * @return Object[]; the retrieved data, or null when no object with the address could be found
     * @throws RemoteException when communication needed to retrieve the data failed
     */
    Object[] get(Object[] address) throws RemoteException;

}
