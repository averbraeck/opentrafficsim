package org.opentrafficsim.sim0mq.publisher;

import org.djutils.event.EventProducerInterface;
import org.djutils.metadata.MetaData;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

/**
 * Object that can find the EventProducerInterface object for an address.
 */
public interface LookupEventProducerInterface
{
    /**
     * Find the EventProducerInterface with the given address.
     * @param address Object[]; the address
     * @param returnWrapper ReturnWrapper; to be used to send back complaints about bad addresses, etc.
     * @return EventProducerInterface; can be null in case the address is (no longer) valid
     * @throws SerializationException when an error occurs while serializing an error response
     * @throws Sim0MQException when an error occurs while serializing an error response
     */
    EventProducerInterface lookup(Object[] address, ReturnWrapper returnWrapper) throws Sim0MQException, SerializationException;

    /**
     * Return a MetaData object that can be used to verify the correctness of an address for the <code>lookup</code> method.
     * @return MetaData; to be used to verify the correctness of an address for the <code>lookup</code> method
     */
    MetaData getAddressMetaData();

}
