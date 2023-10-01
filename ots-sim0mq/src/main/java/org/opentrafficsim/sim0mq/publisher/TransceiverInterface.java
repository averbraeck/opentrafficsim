package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

/**
 * Transceivers with machine interpretable description of address and result types. A transceiver converts DSOL events to Sim0MQ
 * messages and Sim0MQ messages to DJUTILS event (un-)subscriptions, or DSOL events.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface TransceiverInterface extends Identifiable
{
    /**
     * Specification of arguments needed in a request.
     * @return MetaData; the specification of arguments needed in a request
     */
    MetaData getAddressFields();

    /**
     * Retrieve the TransceiverInterface that can be used to get detailed information about a single object.
     * @param addressLevel int; index of the argument in the address fields
     * @param returnWrapper ReturnWrapper; to be used to report problems
     * @return TransceiverInterface; to be used to get valid values for argument <code>addressLevel</code>, or null if valid
     *         values for the argument at index <code>addressLevel</code> can not be obtained through a TransceiverInterface
     *         object
     * @throws SerializationException when the ReturnWrapper fails
     * @throws Sim0MQException when the ReturnWrapper fails
     */
    default TransceiverInterface getIdSource(final int addressLevel, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        // There is no id source by default. Override this method (and the hasIdSource method) if there is one.
        Throw.whenNull(returnWrapper, "returnWrapper may not be null");
        throw new IndexOutOfBoundsException("No id source");
    }

    /**
     * Report if this transceiver has an id source.
     * @return boolean; true if this transceiver has an id source; false if this transceiver does not have an id source
     */
    default boolean hasIdSource()
    {
        // There is no id source by default. Override this method if there is one.
        return false;
    }

    /**
     * Report the specification of a result of the transceiver.
     * @return MetaData; the specification of a result from the transceiver
     */
    MetaData getResultFields();

    /**
     * Retrieve the data.
     * @param address Object[]; the address of the data to retrieve
     * @param returnWrapper ReturnWrapper; to be used to report problems
     * @return Object[]; the retrieved data, or null when no object with the address could be found
     * @throws RemoteException when communication needed to retrieve the data failed
     * @throws SerializationException when encoding an error message fails
     * @throws Sim0MQException when encoding an error message fails
     */
    Object[] get(Object[] address, ReturnWrapper returnWrapper) throws RemoteException, Sim0MQException, SerializationException;

}
