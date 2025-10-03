package org.opentrafficsim.core.perception.collections;

import java.util.ArrayList;
import java.util.Collection;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * ArrayList-valued historical state. The current array list is always maintained, and past states of the array list are
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
public class HistoricalArrayList<E> extends AbstractHistoricalList<E, ArrayList<E>>
{

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     */
    public HistoricalArrayList(final HistoryManager historyManager, final Object owner)
    {
        super(historyManager, owner, new ArrayList<>());
    }

    /**
     * Constructor.
     * @param historyManager history manager
     * @param owner object that owns the historical value
     * @param c initial collection
     */
    public HistoricalArrayList(final HistoryManager historyManager, final Object owner, final Collection<? extends E> c)
    {
        super(historyManager, owner, new ArrayList<>(c));
    }

    @Override
    public ArrayList<E> get()
    {
        return getCollection();
    }

    @Override
    public ArrayList<E> get(final Duration time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new ArrayList<>());
    }

    @Override
    public String toString()
    {
        return "HistoricalArrayList [current=" + getCollection() + "]";
    }

}
