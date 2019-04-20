package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical collections.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public interface HistoricalCollection<E> extends Collection<E>
{

    /**
     * Returns the current collection. This is not intended to be modified, and may be an unmodifiable.
     * @return Collection; current collection
     */
    Collection<E> get();

    /**
     * Returns a past collection.
     * @param time Time; time to obtain the collection at
     * @return Collection; past collection
     */
    Collection<E> get(Time time);

    /** {@inheritDoc} */
    @Override
    default boolean removeIf(Predicate<? super E> filter)
    {
        Objects.requireNonNull(filter);
        boolean removed = false;
        HashSet<E> removes = new HashSet<>();
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
