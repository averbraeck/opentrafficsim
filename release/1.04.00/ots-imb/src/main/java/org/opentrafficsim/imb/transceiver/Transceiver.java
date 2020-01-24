package org.opentrafficsim.imb.transceiver;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;

import nl.tno.imb.TByteBuffer;

/**
 * Relay events between IMB domain and OTS domain.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 18, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Transceiver extends Identifiable
{
    /**
     * Retrieve the id for the Transceiver channel, e.g. "GTU" or "Simulator Control".
     * @return String; the id for the Transceiver channel, e.g. "GTU" or "Simulator Control".
     */
    String getId();

    /**
     * Retrieve the IMB connector.
     * @return Connector the IMB connector.
     */
    Connector getConnector();

    /**
     * Handle an IMB message sent to OTS.
     * @param imbEventName String; the IMB event name of the message that was received from IMB
     * @param imbPayload TByteBuffer; the packed IMB message payload
     * @throws IMBException in case the IMB event cannot be handled by this Transceiver
     */
    void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException;
}
