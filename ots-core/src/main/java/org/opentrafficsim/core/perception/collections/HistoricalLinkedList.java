package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * LinkedList-valued historical state. The current linked list is always maintained, and past states of the linked list are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()}, {@code add()} and {@code set()} methods.
 * Any returned sublist is unmodifiable.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalLinkedList<E> extends AbstractHistoricalList<E, LinkedList<E>> implements HistoricalDeque<E>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalLinkedList(final HistoryManager historyManager)
    {
        super(historyManager, new LinkedList<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param c Collection&lt;? extends E&gt;; initial collection
     */
    public HistoricalLinkedList(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new LinkedList<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public LinkedList<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public LinkedList<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new LinkedList<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalLinkedList [current=" + getCollection() + "]";
    }

    // Altering LinkedList methods

    // add tail

    /** {@inheritDoc} */
    @Override
    public boolean offer(final E e)
    {
        return offerLast(e);
    }

    /** {@inheritDoc} */
    @Override
    public boolean offerLast(final E e)
    {
        boolean added = getCollection().offer(e);
        if (added)
        {
            addEvent(new AddEvent<>(now().si, e, size() - 1));
        }
        return added;
    }

    /** {@inheritDoc} */
    @Override
    public void addLast(final E e)
    {
        add(e);
    }

    // remove head

    /** {@inheritDoc} */
    @Override
    public E remove()
    {
        return removeFirst();
    }

    /** {@inheritDoc} */
    @Override
    public E removeFirst()
    {
        if (isEmpty())
        {
            return getCollection().remove(); // throw Exception as remove does
        }
        E element = peek();
        remove(element);
        return element;
    }

    /** {@inheritDoc} */
    @Override
    public E pop()
    {
        return removeFirst();
    }

    /** {@inheritDoc} */
    @Override
    public E pollFirst()
    {
        if (isEmpty())
        {
            return null;
        }
        E element = peek();
        remove(element);
        return element;
    }

    /** {@inheritDoc} */
    @Override
    public E poll()
    {
        return pollFirst();
    }

    // add head

    /** {@inheritDoc} */
    @Override
    public void addFirst(final E e)
    {
        add(0, e);
    }

    /** {@inheritDoc} */
    @Override
    public void push(final E e)
    {
        addFirst(e);
    }

    /** {@inheritDoc} */
    @Override
    public boolean offerFirst(final E e)
    {
        boolean added = getCollection().offerFirst(e);
        if (added)
        {
            addEvent(new AddEvent<>(now().si, e, 0));
        }
        return added;
    }

    // remove tail

    /** {@inheritDoc} */
    @Override
    public E removeLast()
    {
        if (isEmpty())
        {
            getCollection().removeLast(); // throw Exception as removeLast does
        }
        return remove(size() - 1);
    }

    /** {@inheritDoc} */
    @Override
    public E pollLast()
    {
        if (isEmpty())
        {
            return null;
        }
        return remove(size() - 1);
    }

    // occurrence

    /** {@inheritDoc} */
    @Override
    public boolean removeFirstOccurrence(final Object o)
    {
        for (int i = 0; i < size(); i++)
        {
            if (Objects.equals(get(i), o))
            {
                remove(i);
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeLastOccurrence(final Object o)
    {
        for (int i = size() - 1; i >= 0; i--)
        {
            if (Objects.equals(get(i), o))
            {
                remove(i);
                return true;
            }
        }
        return false;
    }

    // Non-altering LinkedList methods

    /** {@inheritDoc} */
    @Override
    public E element()
    {
        return getCollection().element();
    }

    /** {@inheritDoc} */
    @Override
    public E peek()
    {
        return getCollection().peek();
    }

    /** {@inheritDoc} */
    @Override
    public E getFirst()
    {
        return getCollection().getFirst();
    }

    /** {@inheritDoc} */
    @Override
    public E getLast()
    {
        return getCollection().getLast();
    }

    /** {@inheritDoc} */
    @Override
    public E peekFirst()
    {
        return getCollection().peekFirst();
    }

    /** {@inheritDoc} */
    @Override
    public E peekLast()
    {
        return getCollection().peekLast();
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * <i>This implementation copies a list and reverses the order before returning the iterator. This is not efficient and it
     * should be avoided when possible.</i>
     */
    @Override
    public Iterator<E> descendingIterator()
    {
        List<E> list = new LinkedList<>(getCollection());
        Collections.reverse(list);
        return Collections.unmodifiableList(list).iterator();
    }

}
