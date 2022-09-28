package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * LinkedHashSet-valued historical state. The current hash set is always maintained, and past states of the hash set are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method. Any returned subset is
 * unmodifiable.
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
public class HistoricalHashSet<E> extends AbstractHistoricalCollection<E, LinkedHashSet<E>> implements HistoricalSet<E>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalHashSet(final HistoryManager historyManager)
    {
        super(historyManager, new LinkedHashSet<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param c Collection&lt;? extends E&gt;; initial collection
     */
    public HistoricalHashSet(final HistoryManager historyManager, final Collection<? extends E> c)
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
        return "HistoricalHashSet [current=" + getCollection() + "]";
    }

}
