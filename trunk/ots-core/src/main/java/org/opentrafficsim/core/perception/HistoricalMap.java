package org.opentrafficsim.core.perception;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoricalMap.EventMap;

import nl.tudelft.simulation.language.Throw;

/**
 * Map-valued historical state. The current map is always maintained, and past states of the map are obtained by applying the
 * events between now and the requested time in reverse.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> key type
 * @param <V> value type
 * @param <M> map type
 */
public class HistoricalMap<K, V, M extends Map<K, V>> extends AbstractHistorical<K, EventMap<K, V, M>>
{

    /** Current map. */
    private final M internalMap;

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param map M; empty initial internal map
     */
    public HistoricalMap(final HistoryManager historyManager, final M map)
    {
        super(historyManager);
        Throw.when(!map.isEmpty(), IllegalArgumentException.class, "The initial map should be empty.");
        this.internalMap = map;
    }

    /**
     * Returns the internal map.
     * @return M; internal map
     */
    protected final M getMap()
    {
        return this.internalMap;
    }
    
    /**
     * Clears the collection.
     */
    public synchronized void clear()
    {
        Set<K> values = new HashSet<>(this.internalMap.keySet());
        values.forEach(this::remove);
    }

    /**
     * Adds a value at the current simulation time. Values should be added or removed in chronological order. Multiple events at
     * one time are accepted.
     * @param key K; key
     * @param value V; value
     * @return V; value previously stored for the key, {@code null} if there was no mapping
     */
    public synchronized final V put(final K key, final V value)
    {
        addEvent(new EventMap<>(now().si, key, this.internalMap));
        return this.internalMap.put(key, value);
    }

    /**
     * Puts all key-value pairs from the given map into the map.
     * @param m Map; map
     */
    public synchronized void putAll(final Map<? extends K, ? extends V> m)
    {
        for (K key : m.keySet())
        {
            put(key, m.get(key));
        }
    }

    /**
     * Removes a value at the current simulation time. Values should be added or removed in chronological order. Multiple events
     * at one time are accepted.
     * @param key Object; key
     * @return V; value previously stored for the key, {@code null} if there was no mapping
     */
    @SuppressWarnings("unchecked")
    public synchronized final V remove(final Object key)
    {
        if (this.internalMap.containsKey(key))
        {
            addEvent(new EventMap<>(now().si, (K) key, this.internalMap)); // contains, so safe cast
            return this.internalMap.remove(key);
        }
        return null;
    }

    /**
     * Fill map with the current map.
     * @param map M; map to fill
     * @return M; input map filled
     * @throws NullPointerException if map is null
     * @throws IllegalArgumentException if the map is not empty
     */
    public final synchronized M fill(final M map)
    {
        Throw.whenNull(map, "Map may not be null.");
        Throw.when(!map.isEmpty(), IllegalArgumentException.class, "Map should be an empty map.");
        map.putAll(this.internalMap);
        return map;
    }

    /**
     * Fill map with the map at the given simulation time.
     * @param time Time; time
     * @param map M; map to fill
     * @return M; input map filled
     * @throws NullPointerException if time or map is null
     * @throws IllegalArgumentException if the map is not empty
     */
    public final synchronized M fill(final Time time, final M map)
    {
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(map, "Map may not be null.");
        Throw.when(!map.isEmpty(), IllegalArgumentException.class, "Map should be an empty map.");
        // copy all current elements and decrement per event
        map.putAll(this.internalMap);
        for (EventMap<K, V, M> event : getEvents(time))
        {
            event.restore(map);
        }
        return map;
    }

    /**
     * Abstract super class for events that add or remove a value from the map.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <K> key type
     * @param <V> value type
     * @param <M> map type
     */
    static class EventMap<K, V, M extends Map<K, V>> extends AbstractHistorical.EventValue<K> // import is removed
    {

        /** Whether the map contained the key prior to the event. */
        private final boolean contained;

        /** Previous value in the map. */
        private final V previousValue;

        /**
         * Constructor.
         * @param time double; time of event
         * @param key K; key of event
         * @param map M; internal map to apply the event on
         */
        public EventMap(final double time, final K key, final M map)
        {
            super(time, key);
            this.contained = map.containsKey(key);
            this.previousValue = map.get(key);
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalMap [current=" + getMap() + "]";
    }

}
