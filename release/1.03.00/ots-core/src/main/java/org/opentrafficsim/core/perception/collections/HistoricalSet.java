package org.opentrafficsim.core.perception.collections;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical sets.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalSet<E> extends HistoricalCollection<E>, Set<E>
{

    /**
     * Returns the current set.
     * @return Set; current set
     */
    @Override
    Set<E> get();

    /**
     * Returns a past set.
     * @param time Time; time to obtain the set at
     * @return Set; past set
     */
    @Override
    Set<E> get(Time time);

}
