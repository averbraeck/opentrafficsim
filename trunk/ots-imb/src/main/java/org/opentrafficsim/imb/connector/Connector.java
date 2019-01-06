package org.opentrafficsim.imb.connector;

import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.transceiver.Transceiver;

import nl.tno.imb.TEventEntry;

/**
 * IMB listener and publisher.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 19, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Connector
{
    /**
     * Compose a message containing the specified objects and send it to recipients.
     * @param IMBEventName String; publication
     * @param imbEventType IMBEventType; one of NEW, CHANGE, or DELETE
     * @param args Object[]; the objects to send
     * @return boolean; true on success, false on failure
     * @throws IMBException when the event name is not a registered publication
     */
    boolean postIMBMessage(String IMBEventName, IMBEventType imbEventType, Object[] args) throws IMBException;

    /**
     * Register the transceiver as the interested party when an IMB message identified by the imbEventName is received. When an
     * IMB message with that imbEventName is received, the postOTSMessage method on the transceiver is called.
     * @param fullIMBEventName String; the IMB Event name including the federation prefix, e.g. OTS_RT.SIM_Start
     * @param transceiver Transceiver; the transceiver to handle the incoming event
     * @throws IMBException in case the transceiver is switched to a new one for an event
     */
    void register(String fullIMBEventName, Transceiver transceiver) throws IMBException;

    /**
     * Return the host of the connection, e.g. localhost, or 192,168.1.11
     * @return String; the host of the connection
     */
    String getHost();

    /**
     * Return the port of the connection, e.g. 4000
     * @return int; the port of the connection
     */
    int getPort();

    /**
     * Return the model name (owner name) of the connection, e.g. OTS
     * @return String; the model name (owner name) of the connection
     */
    String getModelName();

    /**
     * Return the model id (owner id) of the connection, e.g. 1
     * @return String; the model id (owner id) of the connection
     */
    int getModelId();

    /**
     * Return the federation name of the connection, e.g. OTS_RT
     * @return String; the federation name of the connection
     */
    String getFederation();

    /**
     * Enum for IMB event types.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum IMBEventType
    {
        /** Corresponds to TEventEntry.ACTION_NEW. */
        NEW(TEventEntry.ACTION_NEW),

        /** Corresponds to TEventEntry.ACTION_CHANGE. */
        CHANGE(TEventEntry.ACTION_CHANGE),

        /** Corresponds to TEventEntry.ACTION_DELETE. */
        DELETE(TEventEntry.ACTION_DELETE);

        /** Equivalent TEventEntry value. */
        private final int eventEntry;

        /**
         * Construct a
         * @param eventEntry int;
         */
        IMBEventType(final int eventEntry)
        {
            this.eventEntry = eventEntry;
        }

        /**
         * Return the corresponding integer IMB type.
         * @return int; the IMB event type
         */
        public int getEventEntry()
        {
            return this.eventEntry;
        }

    }

}
