package org.opentrafficsim.core.observers;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 19, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Observer
{
    /** 
     * Compose a message containing the specified objects and send it to recipients.
     * @param eventName String; publication
     * @param args Object[]; the objects to send
     * @return boolean; true on success, false on failure
     * @throws Exception when the event name is not a registered publication
     */
    boolean postMessage(String eventName, Object[] args) throws Exception;
    
    /**
     * Add a publication.
     * @param eventName String name of the publication
     * @throws Exception when the name is invalid or already registered
     */
    void publish(final String eventName) throws Exception;

    /**
     * Cancel a publication.
     * @param eventName String name of the publication
     * @throws Exception when the name is invalid or not currently registered
     */
    void unPublish(final String eventName) throws Exception;

    
}
