package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.perception.AbstractHistorical;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.collections.AbstractHistoricalMap.EventMap;

/**
 * Map-valued historical state. The current map is always maintained, and past states of the map are obtained by applying the
 * events between now and the requested time in reverse.<br>
 * <br>
 * The set views returned by this class are unmodifiable.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 * @param <M> map type
 */
public abstract class AbstractHistoricalMap<K, V, M extends Map<K, V>> extends AbstractHistorical<K, EventMap<K, V, M>>
        implements HistoricalMap<K, V>
{

    /** Current map. */
    private final M current;

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param map M; initial map
     */
    protected AbstractHistoricalMap(final HistoryManager historyManager, final M map)
    {
        super(historyManager);
        Throw.when(!map.isEmpty(), IllegalArgumentException.class, "The initial map should be empty.");
        this.current = map;
    }

    /**
     * Returns the internal map.
     * @return M; internal map
     */
    protected M getMap()
    {
        return this.current;
    }

    /**
     * Fill map with the current map.
     * @param map M; map to fill
     * @return M; input map filled
     */
    protected M fill(final M map)
    {
        map.putAll(this.current);
        return map;
    }

    /**
     * Fill map with the map at the given simulation time.
     * @param time Time; time
     * @param map M; map to fill
     * @return M; input map filled
     */
    protected M fill(final Time time, final M map)
    {
        // copy all current elements and decrement per event
        map.putAll(this.current);
        for (EventMap<K, V, M> event : getEvents(time))
        {
            event.restore(map);
        }
        return map;
    }

    // Altering Map methods

    /** {@inheritDoc} */
    @Override
    public void clear()
    {
        new LinkedHashSet<>(this.current.keySet()).forEach(this::remove);
    }

    /** {@inheritDoc} */
    @Override
    public V put(final K key, final V value)
    {
        boolean contained = this.current.containsKey(key);
        V previousValue = contained ? this.current.get(key) : null;
        if (!contained || !Objects.equals(previousValue, value))
        {
            addEvent(new EventMap<>(now().si, key, contained, previousValue));
            return this.current.put(key, value);
        }
        return previousValue;
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m)
    {
        m.forEach(this::put);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public V remove(final Object key)
    {
        boolean contained = this.current.containsKey(key);
        if (contained)
        {
            V previousValue = this.current.get(key);
            addEvent(new EventMap<>(now().si, (K) key, contained, previousValue)); // contains, so safe cast
            return this.current.remove(key);
        }
        return null;
    }

    // Non-altering Map methods

    /** {@inheritDoc} */
    @Override
    public int size()
    {
        return this.current.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        return this.current.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key)
    {
        return this.current.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value)
    {
        return this.current.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key)
    {
        return this.current.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Set<K> keySet()
    {
        return Collections.unmodifiableSet(this.current.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values()
    {
        return Collections.unmodifiableCollection(this.current.values());
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        // need to ensure that the Entries themselves allow no alterations
        return Collections.unmodifiableMap(this.current).entrySet();
    }

    // Events

    /**
     * Abstract super class for events that add or remove a value from the map.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <K> key type
     * @param <V> value type
     * @param <M> map type
     */
    public static class EventMap<K, V, M extends Map<K, V>> extends AbstractHistorical.EventValue<K>
    {

        /** Whether the map contained the key prior to the event. */
        private final boolean contained;

        /** Previous value in the map. */
        private final V previousValue;

        /**
         * Constructor.
         * @param time double; time of event
         * @param key K; key of event
         * @param contained boolean; whether the map contained the key prior to the event
         * @param previousValue V; previous value in the map
         */
        public EventMap(final double time, final K key, final boolean contained, final V previousValue)
        {
            super(time, key);
            this.contained = contained;
            this.previousValue = previousValue;
        }

        /**
         * Restores the map to the state of before the event.
         * @param map M; map to restore
         */
        public void restore(final M map)
        {
            if (this.contained)
            {
                map.put(getValue(), this.previousValue); // event value = map key
            }
            else
            {
                map.remove(getValue()); // event value = map key
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "EventMap [contained=" + this.contained + ", previousValue=" + this.previousValue + "]";
        }

    }

}
