package org.opentrafficsim.road.gtu.lane.perception;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable set of elements, sorted close to far.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
