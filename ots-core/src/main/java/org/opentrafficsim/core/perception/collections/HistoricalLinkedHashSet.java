package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * LinkedHashSet-valued historical state. The current linked hash set is always maintained, and past states of the linked hash
 * set are obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method. Any returned subset is
 * unmodifiable.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalLinkedHashSet<E> extends AbstractHistoricalCollection<E, LinkedHashSet<E>> implements HistoricalSet<E>
{

    /**
     * Constructor.
     * @param historyManager history manager
     */
    public HistoricalLinkedHashSet(final HistoryManager historyManager)
    {
        super(historyManager, new LinkedHashSet<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param c initial collection
     */
    public HistoricalLinkedHashSet(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new LinkedHashSet<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public LinkedHashSet<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public LinkedHashSet<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new LinkedHashSet<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalLinkedHashSet [current=" + getCollection() + "]";
    }

}
