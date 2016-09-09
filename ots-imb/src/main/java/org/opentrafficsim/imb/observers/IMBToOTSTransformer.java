package org.opentrafficsim.imb.observers;

import nl.tudelft.simulation.event.EventInterface;

/**
 * Convert an event received over IMB to an equivalent event for OTS (DSOL).
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
public interface IMBToOTSTransformer
{
    /**
     * Transform an IMB payload to an OTS (DSOL) event.
     * @param imbPayload Object[]; the IMB payload
     * @return EventInterface; the OTS (DSOL) event
     */
    EventInterface transform(Object[] imbPayload);
    
}
