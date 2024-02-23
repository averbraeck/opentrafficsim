package org.opentrafficsim.core.perception.collections;

import java.util.EnumMap;

import org.djunits.value.vdouble.scalar.Time;
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
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 */
public class HistoricalEnumMap<K extends Enum<K>, V> extends AbstractHistoricalMap<K, V, EnumMap<K, V>>
{

    /** Enum class. */
    private final Class<K> clazz;

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param clazz Class&lt;K&gt;; enum class
     */
    public HistoricalEnumMap(final HistoryManager historyManager, final Class<K> clazz)
    {
        super(historyManager, new EnumMap<>(clazz));
        this.clazz = clazz;
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param m EnumMap&lt;K, ? extends V&gt;; initial map
     * @param clazz Class&lt;K&gt;; enum class
     */
    public HistoricalEnumMap(final HistoryManager historyManager, final EnumMap<K, ? extends V> m, final Class<K> clazz)
    {
        super(historyManager, new EnumMap<>(m));
        this.clazz = clazz;
    }

    /** {@inheritDoc} */
    @Override
    public EnumMap<K, V> get()
    {
        return getMap();
    }

    /** {@inheritDoc} */
    @Override
    public EnumMap<K, V> get(final Time time)
    {
        if (isLastState(time))
        {
            return getMap();
        }
        return fill(time, new EnumMap<>(this.clazz));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalEnumMap [clazz=" + this.clazz + "]";
    }

}
