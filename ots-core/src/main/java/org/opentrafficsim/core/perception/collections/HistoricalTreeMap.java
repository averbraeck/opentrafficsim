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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public class HistoricalTreeMap<K, V> extends AbstractHistoricalNavigableMap<K, V, TreeMap<K, V>>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalTreeMap(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new TreeMap<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param m initial map
     */
    public HistoricalTreeMap(final HistoryManager historyManager, final Object owner, final Map<? extends K, ? extends V> m)
    {
        super(historyManager, owner, new TreeMap<>(m));
    }

    @Override
    public TreeMap<K, V> get()
    {
        return getMap();
    }

    @Override
    public TreeMap<K, V> get(final Time time)
    {
        if (isLastState(time))
        {
            return getMap();
        }
        return fill(time, new TreeMap<>());
    }

    @Override
    public String toString()
    {
        return "HistoricalTreeMap [current=" + getMap() + "]";
    }

}
