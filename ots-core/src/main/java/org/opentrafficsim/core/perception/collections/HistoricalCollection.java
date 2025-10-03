package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Predicate;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface for historical collections.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalCollection<E> extends Collection<E>
{

    /**
     * Returns the current collection. This is not intended to be modified, and may be an unmodifiable.
     * @return current collection
     */
    Collection<E> get();

    /**
     * Returns a past collection.
     * @param time simulation time to obtain the collection at
     * @return past collection
     */
    Collection<E> get(Duration time);

    @Override
    default boolean removeIf(final Predicate<? super E> filter)
    {
        Objects.requireNonNull(filter);
        boolean removed = false;
        LinkedHashSet<E> removes = new LinkedHashSet<>();
        final Iterator<E> each = iterator();
        while (each.hasNext())
        {
            E next = each.next();
            if (filter.test(next))
            {
                // super uses Iterator.remove() which is not supported, can't use remove() due to concurrency
                removes.add(next);
                removed = true;
            }
        }
        for (E e : removes)
        {
            remove(e);
        }
        return removed;
    }

}
