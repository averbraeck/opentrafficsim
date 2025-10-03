package org.opentrafficsim.core.perception.collections;

import java.util.EnumMap;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * EnumMap-valued historical state. The current enum map is always maintained, and past states of the enum map are obtained by
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
public class HistoricalEnumMap<K extends Enum<K>, V> extends AbstractHistoricalMap<K, V, EnumMap<K, V>>
{

    /** Enum class. */
    private final Class<K> clazz;

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param clazz enum class
     */
    public HistoricalEnumMap(final HistoryManager historyManager, final Object owner, final Class<K> clazz)
    {
        super(historyManager, owner, new EnumMap<>(clazz));
        this.clazz = clazz;
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param m initial map
     * @param clazz enum class
     */
    public HistoricalEnumMap(final HistoryManager historyManager, final Object owner, final EnumMap<K, ? extends V> m,
            final Class<K> clazz)
    {
        super(historyManager, owner, new EnumMap<>(m));
        this.clazz = clazz;
    }

    @Override
    public EnumMap<K, V> get()
    {
        return getMap();
    }

    @Override
    public EnumMap<K, V> get(final Duration time)
    {
        if (isLastState(time))
        {
            return getMap();
        }
        return fill(time, new EnumMap<>(this.clazz));
    }

    @Override
    public String toString()
    {
        return "HistoricalEnumMap [clazz=" + this.clazz + "]";
    }

}
