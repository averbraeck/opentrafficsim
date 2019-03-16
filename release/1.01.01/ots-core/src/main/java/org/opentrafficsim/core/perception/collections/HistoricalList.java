package org.opentrafficsim.core.perception.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface for historical lists.
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
public interface HistoricalList<E> extends HistoricalCollection<E>, List<E>
{

    /**
     * Returns the current list.
     * @return List; current list
     */
    List<E> get();

    /**
     * Returns a past list.
     * @param time Time; time to obtain the list at
     * @return List; past list
     */
    List<E> get(Time time);

    /** {@inheritDoc} */
    @Override
    default void replaceAll(final UnaryOperator<E> operator)
    {
        // super uses ListIterator.set(E), which is not supported
        Objects.requireNonNull(operator);
        for (int i = 0; i < size(); i++)
        {
            set(i, operator.apply(get(i)));
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    default void sort(final Comparator<? super E> c)
    {
        // super uses ListIterator.set(E), which is not supported
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        for (int i = 0; i < a.length; i++)
        {
            set(i, (E) a[i]);
        }
    }

}
