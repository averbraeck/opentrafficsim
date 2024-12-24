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
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalLinkedList<E> extends AbstractHistoricalList<E, LinkedList<E>> implements HistoricalDeque<E>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalLinkedList(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new LinkedList<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param c initial collection
     */
    public HistoricalLinkedList(final HistoryManager historyManager, final Object owner, final Collection<? extends E> c)
    {
        super(historyManager, owner, new LinkedList<>(c));
    }

    @Override
    public LinkedList<E> get()
    {
        return getCollection();
    }

    @Override
    public LinkedList<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new LinkedList<>());
    }

    @Override
    public String toString()
    {
        return "HistoricalLinkedList [current=" + getCollection() + "]";
    }

    // Altering LinkedList methods

    // add tail

    @Override
    public boolean offer(final E e)
    {
        return offerLast(e);
    }

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

    @Override
    public void addLast(final E e)
    {
        add(e);
    }

    // remove head

    @Override
    public E remove()
    {
        return removeFirst();
    }

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

    @Override
    public E pop()
    {
        return removeFirst();
    }

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

    @Override
    public E poll()
    {
        return pollFirst();
    }

    // add head

    @Override
    public void addFirst(final E e)
    {
        add(0, e);
    }

    @Override
    public void push(final E e)
    {
        addFirst(e);
    }

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

    @Override
    public E removeLast()
    {
        if (isEmpty())
        {
            getCollection().removeLast(); // throw Exception as removeLast does
        }
        return remove(size() - 1);
    }

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

    @Override
    public E element()
    {
        return getCollection().element();
    }

    @Override
    public E peek()
    {
        return getCollection().peek();
    }

    @Override
    public E getFirst()
    {
        return getCollection().getFirst();
    }

    @Override
    public E getLast()
    {
        return getCollection().getLast();
    }

    @Override
    public E peekFirst()
    {
        return getCollection().peekFirst();
    }

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

    /**
     * Returns a reversed list.<br>
     * <br>
     * <i>This implementation copies a list and reverses the order before returning. This is not efficient and it should be
     * avoided when possible.</i>
     * @return reversed list
     */
    public LinkedList<E> reversed()
    {
        LinkedList<E> list = new LinkedList<>(getCollection());
        Collections.reverse(list);
        return list;
    }

}
