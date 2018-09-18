package org.opentrafficsim.core.perception;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for single valued historicals.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param time T; time to get the value
     * @return T; value at current time
     * @throws NullPointerException when time is null
     */
    T get(Time time);
    
}
