package org.opentrafficsim.imb.observers;

import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducerInterface;

/**
 * Relay events between IMB domain and DSOL domain.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Transceiver extends EventListenerInterface, EventProducerInterface
{
    /**
     * Retrieve the IMB connector.
     * @return Connector
     */
    Connector getConnector();
    
}
