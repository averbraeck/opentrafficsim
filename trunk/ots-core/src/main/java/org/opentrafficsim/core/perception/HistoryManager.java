package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;

/**
 * A history manager has two main tasks: providing time to {@code Historical}s, and to invoke their
 * {@code cleanUpHistory(history)} method. The {@code history} is the guaranteed time over which {@code Historical}s can supply
 * a historical value. 
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface HistoryManager
{

    /**
     * Returns the current simulation time.
     * @return Time; current simulation time.
     */
    Time now();

    /**
     * Registers a historical.
     * @param historical Historical; historical to register.
     */
    void registerHistorical(AbstractHistorical<?, ?> historical);

}
