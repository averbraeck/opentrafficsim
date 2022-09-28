package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.Vector;

import org.djunits.value.vdouble.scalar.Time;
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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalVector<E> extends AbstractHistoricalList<E, Vector<E>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalVector(final HistoryManager historyManager)
    {
        super(historyManager, new Vector<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param c Collection&lt;? extends E&gt;; initial collection
     */
    public HistoricalVector(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new Vector<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public Vector<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public Vector<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new Vector<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalVector [current=" + getCollection() + "]";
    }

}
