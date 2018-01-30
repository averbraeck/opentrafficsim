package org.opentrafficsim.core.perception;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoricalCollection.EventCollection;

import nl.tudelft.simulation.language.Throw;

/**
 * Collection-valued historical state. The current collection is always maintained, and past states of the collection are
 * obtained by applying the events between now and the requested time in reverse.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 * @param <C> collection type
 */
public class HistoricalCollection<T, C extends Collection<T>> extends AbstractHistorical<T, EventCollection<T, C>>
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
     * @param value T; value
     */
    public synchronized void add(final T value)
    {
        addEvent(new AddEvent<>(now().si, value, this.internalCollection));
    }

    /**
     * Removes a value at the current simulation time. Values should be added or removed in chronological order. Multiple events
     * at one time are accepted.
     * @param value T; value
     */
    public synchronized void remove(final T value)
    {
        addEvent(new RemoveEvent<>(now().si, value, this.internalCollection));
    }

    /** {@inheritDoc} */
    @Override
    public final synchronized void clear(final T value)
    {
        this.internalCollection.remove(value);
        super.clear(value);
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
        for (EventCollection<T, C> event : getEvents(time))
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
     * @param <T> value type
     * @param <C> collection type
     */
    abstract static class EventCollection<T, C extends Collection<T>> extends AbstractHistorical.EventValue<T> // import is removed
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         */
        public EventCollection(final double time, final T value)
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
     * Class for events that add a value to the collection.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     * @param <C> collection type
     */
    private static class AddEvent<T, C extends Collection<T>> extends EventCollection<T, C>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         * @param collection C; internal collection to apply the event on
         */
        public AddEvent(final double time, final T value, final C collection)
        {
            super(time, value);
            collection.add(value);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final C collection)
        {
            collection.remove(getValue());
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "AddEvent [time=" + getTime() + ", value=" + getValue() + "]";
        }
        
    }

    /**
     * Class for events that remove a value from the collection.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> value type
     * @param <C> collection type
     */
    private static class RemoveEvent<T, C extends Collection<T>> extends EventCollection<T, C>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value T; value of event
         * @param collection C; internal collection to apply the event on
         */
        public RemoveEvent(final double time, final T value, final C collection)
        {
            super(time, value);
            collection.remove(value);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final C collection)
        {
            collection.add(getValue());
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "RemoveEvent [time=" + getTime() + ", value=" + getValue() + "]";
        }
        
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalCollection [current=" + getCollection() + "]";
    }
    
}
