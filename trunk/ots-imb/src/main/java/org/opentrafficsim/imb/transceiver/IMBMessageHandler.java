package org.opentrafficsim.imb.transceiver;

import org.opentrafficsim.imb.IMBException;

import nl.tno.imb.TByteBuffer;

/**
 * Handle a message received over IMB for OTS.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface IMBMessageHandler
{
    /**
     * Handle an IMB payload for OTS.
     * @param imbPayload TByteBuffer; the IMB payload
     * @throws IMBException in case the message cannot be handled
     */
    void handle(TByteBuffer imbPayload) throws IMBException;

    /**
     * Return the IMB event name for which this hander is registered.
     * @return the IMB event name for which this hander is registered.
     */
    String getIMBEventName();
}
