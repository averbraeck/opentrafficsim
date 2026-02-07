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
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 * @param <L> list type
 */
public abstract class AbstractHistoricalList<E, L extends List<E>> extends AbstractHistoricalCollection<E, L>
        implements HistoricalList<E>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param list initial list
     */
    protected AbstractHistoricalList(final HistoryManager historyManager, final Object owner, final L list)
    {
        super(historyManager, owner, list);
    }

    // Altering List methods

    @Override
    public synchronized void add(final int index, final E value)
    {
        addEvent(new AddEvent<>(now().si, value, index));
        getCollection().add(index, value);
    }

    @Override
    public synchronized boolean add(final E value)
    {
        addEvent(new AddEvent<>(now().si, value, getCollection().size()));
        return getCollection().add(value);
    }

    @Override
    public synchronized E remove(final int index)
    {
        addEvent(new RemoveEvent<>(now().si, getCollection().get(index), index));
        return getCollection().remove(index);
    }

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

    @Override
    public E get(final int index)
    {
        return getCollection().get(index);
    }

    @Override
    public int indexOf(final Object o)
    {
        return getCollection().indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o)
    {
        return getCollection().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator()
    {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(final int index)
    {
        return Collections.unmodifiableList(getCollection()).listIterator(index);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex)
    {
        return Collections.unmodifiableList(getCollection().subList(fromIndex, toIndex));
    }

    // Events

    /**
     * Abstract super class for events that add or remove a value from the list.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <E> element type
     * @param <L> list type
     */
    public abstract static class EventList<E, L extends List<E>> extends EventCollection<E, L>
    {

        /** Index of the value. */
        private final int index;

        /**
         * Constructor.
         * @param time time of event
         * @param value value of event
         * @param index index
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
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <E> element type
     * @param <L> list type
     */
    public static class AddEvent<E, L extends List<E>> extends EventList<E, L>
    {

        /**
         * Constructor.
         * @param time time of event
         * @param value value of event
         * @param index index
         */
        public AddEvent(final double time, final E value, final int index)
        {
            super(time, value, index);
        }

        @Override
        public void restore(final L list)
        {
            list.remove(getIndex());
        }

        @Override
        public String toString()
        {
            return "AddEvent [time=" + getTime() + ", value=" + getValue() + ", index=" + getIndex() + "]";
        }

    }

    /**
     * Class for events that remove a value from the list.
     * <p>
     * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <E> element type
     * @param <L> list type
     */
    public static class RemoveEvent<E, L extends List<E>> extends EventList<E, L>
    {

        /**
         * Constructor.
         * @param time time of event
         * @param value value of event
         * @param index index the value is at
         */
        public RemoveEvent(final double time, final E value, final int index)
        {
            super(time, value, index);
        }

        @Override
        public void restore(final L list)
        {
            list.add(getIndex(), getValue());
        }

        @Override
        public String toString()
        {
            return "RemoveEvent [time=" + getTime() + ", value=" + getValue() + ", index=" + getIndex() + "]";
        }

    }

}
