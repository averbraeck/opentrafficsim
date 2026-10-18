package org.opentrafficsim.base.collections;

/**
 * Classes that implement this interface can by used in an {@link IndexedMap}. This allows fast look-up and iteration.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public interface Indexed
{

    /**
     * Returns the unique index for this instance.
     * @return the unique index for this instance
     */
    int index();

}
