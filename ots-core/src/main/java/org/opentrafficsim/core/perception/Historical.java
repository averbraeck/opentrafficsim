package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for single valued historicals.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public interface Historical<T>
{

    /**
     * Set value at the current simulation time. If a value is already given at this time, it is overwritten. Values should be
     * set in chronological order.
     * @param value T; value
     */
    void set(T value);

    /**
     * Get value at current simulation time.
     * @return T; value at current simulation time
     */
    T get();

    /**
     * Get value at given time.
     * @param time Time; time to get the value
     * @return T; value at current time
     * @throws NullPointerException when time is null
     */
    T get(Time time);

}
