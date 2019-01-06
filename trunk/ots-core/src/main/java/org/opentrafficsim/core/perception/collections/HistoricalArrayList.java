package org.opentrafficsim.core.perception.collections;

import java.util.ArrayList;
import java.util.Collection;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

/**
 * ArrayList-valued historical state. The current array list is always maintained, and past states of the array list are
 * obtained by applying the events between now and the requested time in reverse.<br>
 * <br>
 * The {@code Iterator} returned by this class does not support the {@code remove()}, {@code add()} and {@code set()} methods.
 * Any returned sublist is unmodifiable.
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
public class HistoricalArrayList<E> extends AbstractHistoricalList<E, ArrayList<E>>
{

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     */
    public HistoricalArrayList(final HistoryManager historyManager)
    {
        super(historyManager, new ArrayList<>());
    }

    /**
     * Constructor.
     * @param historyManager HistoryManager; history manager
     * @param c Collection&lt;? extends E&gt;; initial collection
     */
    public HistoricalArrayList(final HistoryManager historyManager, final Collection<? extends E> c)
    {
        super(historyManager, new ArrayList<>(c));
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<E> get()
    {
        return getCollection();
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<E> get(final Time time)
    {
        if (isLastState(time))
        {
            return getCollection();
        }
        return fill(time, new ArrayList<>());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HistoricalArrayList [current=" + getCollection() + "]";
    }

}
