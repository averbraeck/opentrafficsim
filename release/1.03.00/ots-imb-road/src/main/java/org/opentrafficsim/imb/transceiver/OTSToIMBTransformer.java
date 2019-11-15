package org.opentrafficsim.imb.transceiver;

import nl.tudelft.simulation.event.EventInterface;

/**
 * Convert an OTS/DSOL event received to an equivalent event for IMB.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface OTSToIMBTransformer
{
    /**
     * Transform an IMB payload to an OTS (DSOL) event.
     * @param event EventInterface; the OTS (DSOL) event
     * @return Object[]; the IMB payload
     */
    Object[] transform(EventInterface event);

}
