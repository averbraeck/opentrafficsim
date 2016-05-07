package org.opentrafficsim.core.immutablecollections;

import java.util.Iterator;

/**
 * An immutable iterator over elements, wrapping a "mutable" iterator. The default remove method from the interface will throw
 * an exception.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the element type
 */
public class ImmutableIterator<E> implements Iterator<E>
{
    /** the wrapped iterator. */
    private final Iterator<E> iterator;

    /**
     * @param iterator the iterator to wrap as an immutable iterator.
     */
    public ImmutableIterator(final Iterator<E> iterator)
    {
        super();
        this.iterator = iterator;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean hasNext()
    {
        return this.iterator.hasNext();
    }

    /** {@inheritDoc} */
    @Override
    public final E next()
    {
        return this.iterator.next();
    }

}
