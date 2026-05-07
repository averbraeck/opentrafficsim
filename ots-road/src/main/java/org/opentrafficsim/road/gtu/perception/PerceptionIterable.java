package org.opentrafficsim.road.gtu.perception;

import org.opentrafficsim.road.gtu.perception.object.PerceivedObject;

/**
 * Iterable set of elements, sorted close to far.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <P> perceived object type
 */
public interface PerceptionIterable<P extends PerceivedObject> extends Iterable<P>
{

    /**
     * Returns the first element.
     * @return first element
     */
    P first();

    /**
     * Returns whether this iterable is empty.
     * @return whether this iterable is empty
     */
    boolean isEmpty();

}
