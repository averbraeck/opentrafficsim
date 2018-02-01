package org.opentrafficsim.core.perception;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoricalCollection.EventCollection;

import nl.tudelft.simulation.language.Throw;

/**
 * Collection-valued historical state. The current collection is always maintained, and past states of the collection are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * This implementation is suitable for sets, as add and remove events to retrieve historical states are only created if indeed
 * the underlying collection is changed.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 * @param <C> collection type
 */
public class HistoricalCollection<E, C extends Collection<E>> extends AbstractHistorical<E, EventCollection<E, C>>
{

    /** Current collection. */
    private final C internalCollection;

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param collection C; empty initial internal collection
     */
    public HistoricalCollection(final HistoryManager historyManager, final C collection)
    {
        super(historyManager);
        Throw.when(!collection.isEmpty(), IllegalArgumentException.class, "The initial collection should be empty.");
        this.internalCollection = collection;
    }

    /**
     * Returns the internal collection.
     * @return C; internal collection
     */
    protected final C getCollection()
    {
        return this.internalCollection;
    }

    /**
     * Adds a value at the current simulation time. Values should be added or removed in chronological order. Multiple events at
     * one time are accepted.
     * @param value E; value
     * @return boolean; whether the collection changed
     */
    public boolean add(final E value)
    {
        boolean added = getCollection().add(value);
        if (added)
        {
            addEvent(new AddEvent<>(now().si, value));
        }
        return added;
    }
    
    /**
     * Adds all values in the given collection.
     * @param c Collection; values to add
     * @return boolean; whether the collection changed
     */
    public synchronized boolean addAll(Collection<? extends E> c)
    {
        boolean changed = false;
        for (E value : c)
        {
            changed |= add(value);
        }
        return changed;
    }
    
    /**
     * Clears the collection.
     */
    public synchronized void clear()
    {
        Set<E> values = new HashSet<>(this.internalCollection);
        values.forEach(this::remove);
    }

    /**
     * Removes a value at the current simulation time. Values should be added or removed in chronological order. Multiple events
     * at one time are accepted.
     * @param value Object; value
     * @return boolean; whether the collection changed
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean remove(Object value)
    {
        boolean removed = getCollection().remove(value);
        if (removed)
        {
            addEvent(new RemoveEvent<>(now().si, (E) value)); // contains, so safe cast
        }
        return removed;
    }
    
    /**
     * Removes all values in the given collection.
     * @param c Collection; values to remove
     * @return boolean; whether the collection changed
     */
    public synchronized boolean removeAll(final Collection<?> c)
    {
        boolean changed = false;
        for (Object value : c)
        {
            changed |= remove(value);
        }
        return changed;
    }
    
    /**
     * Retain all values in the given collection.
     * @param c Collection; values to retain
     * @return boolean; whether the collection changed
     */
    public synchronized boolean retainAll(Collection<?> c)
    {
        boolean changed = false;
        Set<E> values = new HashSet<>(this.internalCollection);
        for (E value : values)
        {
            if (!c.contains(value))
            {
                changed |= remove(value);
            }
        }
        return changed;
    }

    /**
     * Fill collection with the current collection.
     * @param collection C; collection to fill
     * @return C; input collection filled
     * @throws NullPointerException if collection is null
     * @throws IllegalArgumentException if the collection is not empty
     */
    public final synchronized C fill(final C collection)
    {
        Throw.whenNull(collection, "Collection may not be null.");
        Throw.when(!collection.isEmpty(), IllegalArgumentException.class, "Collection should be an empty collection.");
        collection.addAll(this.internalCollection);
        return collection;
    }

    /**
     * Fill collection with the collection at the given simulation time.
     * @param time Time; time
     * @param collection C; collection to fill
     * @return C; input collection filled
     * @throws NullPointerException if time or collection is null
     * @throws IllegalArgumentException if the collection is not empty
     */
    public final synchronized C fill(final Time time, final C collection)
    {
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(collection, "Collection may not be null.");
        Throw.when(!collection.isEmpty(), IllegalArgumentException.class, "Collection should be an empty collection.");
        // copy all current elements and decrement per event
        collection.addAll(this.internalCollection);
        for (EventCollection<E, C> event : getEvents(time))
        {
            event.restore(collection);
        }
        return collection;
    }

    /**
     * Abstract super class for events that add or remove a value from the collection.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <E> element type
     * @param <C> collection type
     */
    abstract static class EventCollection<E, C extends Collection<E>> extends AbstractHistorical.EventValue<E> // import is removed
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         */
        public EventCollection(final double time, final E value)
        {
            super(time, value);
        }

        /**
         * Restores the collection to the state of before the event.
         * @param collection C; collection to restore
         */
        public abstract void restore(final C collection);

    }
    
    /**
     * Event for adding value to the collection.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <E> element type
     * @param <C> collection type
     */
    private static class AddEvent<E, C extends Collection<E>> extends EventCollection<E, C>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         */
        public AddEvent(final double time, final E value)
        {
            super(time, value);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final C collection)
        {
            collection.remove(getValue()); // events are only created upon effective addition, so we can remove it
        }
        
    }
    
    /**
     * Event for removing value from the collection.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <E> element type
     * @param <C> collection type
     */
    private static class RemoveEvent<E, C extends Collection<E>> extends EventCollection<E, C>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         */
        public RemoveEvent(final double time, final E value)
        {
            super(time, value);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final C set)
        {
            set.add(getValue()); // events are only created upon effective removal, so we can add it
        }
        
    }

}
