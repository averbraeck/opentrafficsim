package org.opentrafficsim.imb.transceiver;

import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducerInterface;

/**
 * Relay events between IMB domain and OTS domain, enabled by pub/sub.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface EventTransceiver extends Transceiver, EventListenerInterface, EventProducerInterface
{
    // no methods added; combines Transceiver with an EventListener and EventProducer
}
