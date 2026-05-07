package org.opentrafficsim.core.network;

import org.djunits.value.vdouble.scalar.Frequency;

/**
 * Interface of elements with capacity.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 */
public interface Capacity
{

    /**
     * Returns the capacity.
     * @return link capacity.
     */
    Frequency getCapacity();

    /**
     * Set the link capacity.
     * @param capacity the new capacity of the link as a frequency in GTUs per time unit.
     */
    void setCapacity(Frequency capacity);

}
