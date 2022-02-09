package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.opentrafficsim.core.perception.HistoryManager;

/**
 * List-valued historical state. The current list is always maintained, and past states of the list are obtained by applying the
 * events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()}, {@code add()} and {@code set()} methods.
 * Any returned sublist is unmodifiable.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 * @param <L> list type
 */
public abstract class AbstractHistoricalList<E, L extends List<E>> extends AbstractHistoricalCollection<E, L>
        implements HistoricalList<E>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param list L; initial list
     */
    protected AbstractHistoricalList(final HistoryManager historyManager, final L list)
    {
        super(historyManager, list);
    }

    // Altering List methods

    /** {@inheritDoc} */
    @Override
    public synchronized void add(final int index, final E value)
    {
        addEvent(new AddEvent<>(now().si, value, index));
        getCollection().add(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean add(final E value)
    {
        addEvent(new AddEvent<>(now().si, value, getCollection().size()));
        return getCollection().add(value);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized E remove(final int index)
    {
        addEvent(new RemoveEvent<>(now().si, getCollection().get(index), index));
        return getCollection().remove(index);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized boolean remove(final Object value)
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

    /** {@inheritDoc} */
    @Override
    public synchronized E set(final int index, final E value)
    {
        E previousValue = getCollection().get(index);
        if (!getCollection().get(index).equals(value))
        {
            remove(index);
            add(index, value);
        }
        return previousValue;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(final int index, final Collection<? extends E> c)
    {
        int ind = index;
        for (E e : c)
        {
            add(ind, e);
            ind++;
        }
        return !c.isEmpty();
    }

    // Non-altering List methods

    /** {@inheritDoc} */
    @Override
    public E get(final int index)
    {
        return getCollection().get(index);
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(final Object o)
    {
        return getCollection().indexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public int lastIndexOf(final Object o)
    {
        return getCollection().lastIndexOf(o);
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator<E> listIterator()
    {
        return listIterator(0);
    }

    /** {@inheritDoc} */
    @Override
    public ListIterator<E> listIterator(final int index)
    {
        return Collections.unmodifiableList(getCollection()).listIterator(index);
    }

    /** {@inheritDoc} */
    @Override
    public List<E> subList(final int fromIndex, final int toIndex)
    {
        return Collections.unmodifiableList(getCollection().subList(fromIndex, toIndex));
    }

    // Events

    /**
     * Abstract super class for events that add or remove a value from the list.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
    public abstract static class EventList<E, L extends List<E>> extends EventCollection<E, L>
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
    public static class AddEvent<E, L extends List<E>> extends EventList<E, L>
    {

        /**
         * Constructor.
         * @param time double; time of event
         * @param value E; value of event
         * @param index int; index
         */
        public AddEvent(final double time, final E value, final int index)
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
    public static class RemoveEvent<E, L extends List<E>> extends EventList<E, L>
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
            list.add(getIndex(), getValue());
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "RemoveEvent [time=" + getTime() + ", value=" + getValue() + ", index=" + getIndex() + "]";
        }

    }

}
