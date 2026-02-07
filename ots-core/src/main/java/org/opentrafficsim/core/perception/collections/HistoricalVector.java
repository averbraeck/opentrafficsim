package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Vector;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * Vector-valued historical state. The current vector is always maintained, and past states of the vector are obtained by
 * applying the events between now and the requested time in reverse.<br>
 * <br>
 * This class does not implement all {@code Vector} methods, but only those shared with {@code List}. The returned argument is
 * however a {@code List}.<br>
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
 */
public class HistoricalVector<E> extends AbstractHistoricalList<E, Vector<E>>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalVector(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new Vector<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param c initial collection
     */
    public HistoricalVector(final HistoryManager historyManager, final Object owner, final Collection<? extends E> c)
    {
        super(historyManager, owner, new Vector<>(c));
    }

    @Override
    public Vector<E> get()
    {
        return getCollection();
    }

    @Override
    public Vector<E> get(final Duration time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new Vector<>());
    }

    @Override
    public String toString()
    {
        return "HistoricalVector [current=" + getCollection() + "]";
    }

}
