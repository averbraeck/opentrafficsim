package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable set of elements, sorted close to far.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 */
public interface PerceptionIterable<H extends Headway> extends Iterable<H>
{

    /**
     * Returns the first element.
     * @return H; first element
     */
    H first();

    /**
     * Returns whether this iterable is empty.
     * @return whether this iterable is empty
     */
    boolean isEmpty();

}
