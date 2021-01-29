package org.opentrafficsim.sim0mq.publisher;

/**
 * Interface for handlers of incoming data and commands that are not directly processed by the Publisher.
 * <p>
 * Copyright (c) 2021-2020=1 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
 * <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface IncomingDataHandler
{
    /**
     * Obtain a descriptive name of the IncomingDataHandler.
     * @return String; a descriptive name of the IncomingDataHandler
     */
    String getId();

    /**
     * Obtain the key that is used to identify incoming data destined for this IncomingDataHandler, (this key should appear in
     * field 5 of the Sim0MQ messages).
     * @return String; the key that identify incoming data destined for this IncomingDataHandler
     */
    String getKey();

    /**
     * Process incoming data.
     * @param decodedMessage Object[]; decoded Sim0MQMessages that has been converted by the createObjectArray method
     * @return String; null on success, nonNull on failure to handle the data
     */
    String handleIncomingData(Object[] decodedMessage);
    
}
