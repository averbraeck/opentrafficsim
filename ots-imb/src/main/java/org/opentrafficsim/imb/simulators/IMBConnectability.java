package org.opentrafficsim.imb.simulators;

import org.opentrafficsim.imb.transceiver.OTSIMBConnector;

/**
 * Ability to set, get an IMBConnector.
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
public interface IMBConnectability
{
    /**
     * Retrieve the IMB connector.
     * @return IMBConnector (or null if no IMBConnector is registered)
     */
    OTSIMBConnector getIMBConnector();
    
    /**
     * Set (or update/replace) the IMB connector.
     * @param newIMBConnector IMBConnector (or null to unregister a currently registered IMB connector)
     */
    void setIMBConnector(OTSIMBConnector newIMBConnector);

}
