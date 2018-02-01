package org.opentrafficsim.core.perception;

import java.util.List;

/**
 * Extension of {@code HistoryCollection} with index support.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> value type
 * @param <L> list type
 */
public class HistoricalList<E, L extends List<E>> extends HistoricalCollection<E, L>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param list C; empty initial internal list
     */
    public HistoricalList(final HistoryManager historyManager, final L list)
    {
        super(historyManager, list);
    }

    /**
     * Adds a value at the current simulation time. Values should be added or removed in chronological order. Multiple events at
     * one time are accepted.
     * @param index int; index to add the value
     * @param value E; value
     */
    public final synchronized void add(final int index, final E value)
    {
        addEvent(new AddEvent<>(now().si, value, index, getCollection()));
        getCollection().add(index, value);
    }

    /**
     * Adds a value at the current simulation time, at the end of the list. Values should be added or removed in chronological
     * order. Multiple events at one time are accepted.
     * @param value E; value
     * @return boolean; whether the list changed
     */
    @Override
    public final synchronized boolean add(final E value)
    {
        addEvent(new AddEvent<>(now().si, value, getCollection().size(), getCollection()));
        return getCollection().add(value);
    }
    
    /**
     * Removes the value at the given index.
     * @param index int; index
     * @return E; value which is removed from the index
     */
    public final synchronized E remove(final int index)
    {
        addEvent(new RemoveEvent<>(now().si, getCollection().get(index), index));
        return getCollection().remove(index);
    }
    
    /**
     * Removes a value at the current simulation time. Values should be added or removed in chronological order. Multiple events
     * at one time are accepted.
     * @param value Object; value
     * @return boolean; whether the list changed
     */
    @SuppressWarnings("unchecked")
    @Override
    public final synchronized boolean remove(final Object value)
    {
        int index = getCollection().indexOf(value);
        if (index >= 0)
        {
            addEvent(new RemoveEvent<>(now().si, (E) value, index)); // contains, so safe cast
            getCollection().remove(index);
            return true;
        }
        return false;
    }
    
    /**
     * Replaces the value at the given index.
     * @param index int; index
     * @param value E; value
     * @return E; value that is replaced
     */
    public final synchronized E set(final int index, final E value)
    {
        E previousValue = getCollection().get(index);
        if (!getCollection().get(index).equals(value))
        {
            remove(index);
            add(index, value);
        }
        return previousValue;
    }

    /**
     * Abstract super class for events that add or remove a value from the list.
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
     * @param <L> list type
     */
    abstract private static class EventList<E, L extends List<E>> extends EventCollection<E, L>
    {

        /** Index of the value. */
        private final int index;

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         * @param index int; index
         */
        public EventList(final double time, final E value, final int index)
        {
            super(time, value);
            this.index = index;
        }

        /**
         * Returns the index.
         * @return index
         */
        final int getIndex()
        {
            return this.index;
        }

    }

    /**
     * Class for events that add a value to the list.
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
     * @param <L> list type
     */
    private static class AddEvent<E, L extends List<E>> extends EventList<E, L>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         * @param index int; index
         * @param list L; internal list to apply the event on
         */
        public AddEvent(final double time, final E value, final int index, final L list)
        {
            super(time, value, index);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final L list)
        {
            list.remove(getIndex());
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "AddEvent [time=" + getTime() + ", value=" + getValue() + ", index=" + getIndex() + "]";
        }

    }

    /**
     * Class for events that remove a value from the list.
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
     * @param <L> list type
     */
    private static class RemoveEvent<E, L extends List<E>> extends EventList<E, L>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         * @param index int; index the value is at
         */
        public RemoveEvent(final double time, final E value, final int index)
        {
            super(time, value, index);
        }

        /** {@inheritDoc} */
        @Override
        public void restore(final L list)
        {
            if (getIndex() >= 0)
            {
                list.add(getIndex(), getValue());
            }
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "RemoveEvent [time=" + getTime() + ", value=" + getValue() + ", index=" + getIndex() + "]";
        }

    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalList [current=" + getCollection() + "]";
    }

}
