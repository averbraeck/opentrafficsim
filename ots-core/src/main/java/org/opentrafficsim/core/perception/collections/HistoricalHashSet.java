package org.opentrafficsim.core.perception.collections;

import java.util.Collection;
import java.util.HashSet;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * HashSet-valued historical state. The current hash set is always maintained, and past states of the hash set are obtained by
 * applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()} method. Any returned subset is
 * unmodifiable.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> element type
 */
public class HistoricalHashSet<E> extends AbstractHistoricalCollection<E, HashSet<E>> implements HistoricalSet<E>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalHashSet(final HistoryManager historyManager)
    {
        super(historyManager, new HashSet<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param c Collection&lt;? extends E&gt;; initial collection
     */
    public HistoricalHashSet(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new HashSet<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public HashSet<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public HashSet<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new HashSet<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalHashSet [current=" + getCollection() + "]";
    }

}
