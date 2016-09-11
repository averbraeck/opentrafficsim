package org.opentrafficsim.imb.transceiver;

import org.opentrafficsim.imb.IMBException;

import nl.tno.imb.TEventEntry;

/**
 * IMB listener and publisher.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
     * Register the transceiver as the interested party when an IMB message identified by the imbEventName is received. When an IMB messsage
     * with that imbEventName is received, the postOTSMessage method on the transceiver is called.
     * @param imbEventName 
     * @param transceiver 
     * @throws IMBException 
     */
    void register(String imbEventName, Transceiver transceiver) throws IMBException;
    
    /**
     * Enum for IMB event types.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
         * @param eventEntry
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
