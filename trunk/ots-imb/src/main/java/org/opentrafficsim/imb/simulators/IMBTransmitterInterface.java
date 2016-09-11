package org.opentrafficsim.imb.simulators;

import org.opentrafficsim.imb.transceiver.OTSIMBConnector;

/**
 * Ability to set, get an IMBTransmitter.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Sep 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface IMBTransmitterInterface
{
    /**
     * Retrieve the IMB transmitter.
     * @return IMBTransmitter (or null if no IMBTransmitter is registered)
     */
    OTSIMBConnector getIMBTransmitter();
    
    /**
     * Set (or update/replace) the IMB transmitter.
     * @param newIMBTransmitter IMBTransmitter (or null to unregister a currently registered IMB transmitter)
     */
    void setIMBTransmitter(OTSIMBConnector newIMBTransmitter);

}
