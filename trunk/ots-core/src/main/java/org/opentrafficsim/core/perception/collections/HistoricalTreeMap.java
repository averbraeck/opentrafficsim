package org.opentrafficsim.core.perception.collections;

import java.util.Map;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * TreeMap-valued historical state. The current tree map is always maintained, and past states of the tree map are obtained by
 * applying the events between now and the requested time in reverse.<br>
 * <br>
 * The set views returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public class HistoricalTreeMap<K, V> extends AbstractHistoricalNavigableMap<K, V, TreeMap<K, V>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalTreeMap(final HistoryManager historyManager)
    {
        super(historyManager, new TreeMap<>());
    }
    
    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param m Map; initial map
     */
    public HistoricalTreeMap(final HistoryManager historyManager, final Map<? extends K, ? extends V> m)
    {
        super(historyManager, new TreeMap<>(m));
    }

    /** {@inheritDoc} */
    @Override
    public TreeMap<K, V> get()
    {
        return fill(new TreeMap<>());
    }

    /** {@inheritDoc} */
    @Override
    public TreeMap<K, V> get(final Time time)
    {
        return fill(time, new TreeMap<>());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalTreeMap [current=" + getMap() + "]";
    }

}
