package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.perception.AbstractHistorical;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.collections.AbstractHistoricalCollection.EventCollection;

/**
 * Collection-valued historical state. The current collection is always maintained, and past states of the collection are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * This implementation is suitable for sets, as add and remove events to retrieve historical states are only created if indeed
 * the underlying collection is changed. {@code Set} introduces no new methods relative to {@code Collection}.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 * @param <C> collection type
 */
public abstract class AbstractHistoricalCollection<E, C extends Collection<E>>
        extends AbstractHistorical<E, EventCollection<E, C>> implements HistoricalCollection<E>
{

    /** Current collection. */
    private final C current;

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param collection initial collection
     */
    protected AbstractHistoricalCollection(final HistoryManager historyManager, final Object owner, final C collection)
    {
        super(historyManager, owner);
        this.current = collection;
    }

    /**
     * Returns the internal collection.
     * @return internal collection
     */
    protected final C getCollection()
    {
        return this.current;
    }

    /**
     * Fill collection with the current collection.
     * @param collection collection to fill
     * @return input collection filled
     */
    protected final C fill(final C collection)
    {
        collection.addAll(this.current);
        return collection;
    }

    /**
     * Fill collection with the collection at the given simulation time.
     * @param time simulation time
     * @param collection collection to fill
     * @return input collection filled
     */
    protected final C fill(final Duration time, final C collection)
    {
        // copy all current elements and decrement per event
        collection.addAll(this.current);
        for (EventCollection<E, C> event : getEvents(time))
        {
            event.restore(collection);
        }
        return collection;
    }

    // Altering Collection methods

    @Override
    public boolean add(final E value)
    {
        boolean added = getCollection().add(value);
        if (added)
        {
            addEvent(new AddEvent<>(now().si, value));
        }
        return added;
    }

    @Override
    public boolean addAll(final Collection<? extends E> c)
    {
        boolean changed = false;
        for (E value : c)
        {
            changed |= add(value);
        }
        return changed;
    }

    @Override
    public void clear()
    {
        new LinkedHashSet<>(this.current).forEach(this::remove);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object value)
    {
        boolean removed = getCollection().remove(value);
        if (removed)
        {
            addEvent(new RemoveEvent<>(now().si, (E) value)); // contains, so safe cast
        }
        return removed;
    }

    @Override
    public boolean removeAll(final Collection<?> c)
    {
        boolean changed = false;
        for (Object value : c)
        {
            changed |= remove(value);
        }
        return changed;
    }

    @Override
    public boolean retainAll(final Collection<?> c)
    {
        boolean changed = false;
        Set<E> values = new LinkedHashSet<>(this.current);
        for (E value : values)
        {
            if (!c.contains(value))
            {
                changed |= remove(value);
            }
        }
        return changed;
    }

    // Non-altering Collection methods

    @Override
    public int size()
    {
        return this.current.size();
    }

    @Override
    public boolean isEmpty()
    {
        return this.current.isEmpty();
    }

    @Override
    public boolean contains(final Object o)
    {
        return this.current.contains(o);
    }

    @Override
    public Iterator<E> iterator()
    {
        return Collections.unmodifiableCollection(this.current).iterator();
    }

    @Override
    public Object[] toArray()
    {
        return this.current.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a)
    {
        return this.current.toArray(a);
    }

    @Override
    public boolean containsAll(final Collection<?> c)
    {
        return this.current.containsAll(c);
    }

    // Events

    /**
     * Abstract super class for events that add or remove a value from the collection.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <E> element type
     * @param <C> collection type
     */
    public abstract static class EventCollection<E, C extends Collection<E>> extends AbstractHistorical.EventValue<E>
    {

        /**
         * Constructor.
         * @param time time of event
         * @param value value of event
         */
        public EventCollection(final double time, final E value)
        {
            super(time, value);
        }

        /**
         * Restores the collection to the state of before the event.
         * @param collection collection to restore
         */
        public abstract void restore(C collection);

        @Override
        public String toString()
        {
            return "EventCollection []";
        }

    }

    /**
     * Event for adding value to the collection.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <E> element type
     * @param <C> collection type
     */
    public static class AddEvent<E, C extends Collection<E>> extends EventCollection<E, C>
    {

        /**
         * Constructor.
         * @param time time of event
         * @param value value of event
         */
        public AddEvent(final double time, final E value)
        {
            super(time, value);
        }

        @Override
        public void restore(final C collection)
        {
            collection.remove(getValue()); // events are only created upon effective addition, so we can remove it
        }

        @Override
        public String toString()
        {
            return "AddEvent []";
        }

    }

    /**
     * Event for removing value from the collection.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <E> element type
     * @param <C> collection type
     */
    public static class RemoveEvent<E, C extends Collection<E>> extends EventCollection<E, C>
    {

        /**
         * Constructor.
         * @param time time of event
         * @param value value of event
         */
        public RemoveEvent(final double time, final E value)
        {
            super(time, value);
        }

        @Override
        public void restore(final C collection)
        {
            collection.add(getValue()); // events are only created upon effective removal, so we can add it
        }

        @Override
        public String toString()
        {
            return "RemoveEvent []";
        }

    }

}
