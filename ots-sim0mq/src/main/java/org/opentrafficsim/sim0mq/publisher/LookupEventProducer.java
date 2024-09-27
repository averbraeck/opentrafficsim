package org.opentrafficsim.sim0mq.publisher;

import org.djutils.event.EventProducer;
import org.djutils.metadata.MetaData;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

/**
 * Object that can find the EventProducer object for an address.
 */
public interface LookupEventProducer
{
    /**
     * Find the EventProducerInterface with the given address.
     * @param address the address
     * @param returnWrapper to be used to send back complaints about bad addresses, etc.
     * @return can be null in case the address is (no longer) valid
     * @throws SerializationException when an error occurs while serializing an error response
     * @throws Sim0MQException when an error occurs while serializing an error response
     */
    EventProducer lookup(Object[] address, ReturnWrapper returnWrapper) throws Sim0MQException, SerializationException;

    /**
     * Return a MetaData object that can be used to verify the correctness of an address for the <code>lookup</code> method.
     * @return to be used to verify the correctness of an address for the <code>lookup</code> method
     */
    MetaData getAddressMetaData();

}
