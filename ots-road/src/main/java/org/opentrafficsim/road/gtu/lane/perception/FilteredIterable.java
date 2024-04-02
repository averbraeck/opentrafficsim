package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Returns only those elements that comply with the predicate.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type
 */
public class FilteredIterable<T extends Headway> implements Iterable<T>
{

    /** Iterable. */
    private final Iterable<T> iterable;

    /** Predicate. */
    private final Predicate<T> predicate;

    /**
     * @param iterable Iterable&lt;T&gt;; iterable
     * @param predicate Predicate&lt;T&gt;; predicate
     */
    public FilteredIterable(final Iterable<T> iterable, final Predicate<T> predicate)
    {
        this.iterable = iterable;
        this.predicate = predicate;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            @SuppressWarnings("synthetic-access")
            /** iterator */
            private Iterator<T> it = FilteredIterable.this.iterable.iterator();

            /** net */
            private T next;

            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public boolean hasNext()
            {
                if (this.next != null)
                {
                    return true;
                }
                while (this.next == null && this.it.hasNext())
                {
                    T n = this.it.next();
                    if (FilteredIterable.this.predicate.test(n))
                    {
                        this.next = n;
                    }
                }
                return this.next != null;
            }

            /** {@inheritDoc} */
            @Override
            public T next()
            {
                if (hasNext())
                {
                    T n = this.next;
                    this.next = null;
                    return n;
                }
                throw new NoSuchElementException();
            }
        };
    }

}
