package org.opentrafficsim.core.perception.collections;

import java.util.NavigableMap;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical navigable maps.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public interface HistoricalNavigableMap<K, V> extends HistoricalSortedMap<K, V>, NavigableMap<K, V>
{

    /**
     * Returns the current navigable map.
     * @return NavigableMap; current navigable map
     */
    @Override
    NavigableMap<K, V> get();

    /**
     * Returns a past navigable map.
     * @param time Time; time to obtain the navigable map at
     * @return NavigableMap; past navigable map
     */
    @Override
    NavigableMap<K, V> get(Time time);

}
