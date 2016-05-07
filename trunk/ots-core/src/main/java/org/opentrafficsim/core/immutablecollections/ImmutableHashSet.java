package org.opentrafficsim.core.immutablecollections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An immutable wrapper for a HashSet.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this Set
 */
public class ImmutableHashSet<E> extends ImmutableAbstractSet<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param collection the set to use as the immutable set.
     */
    public ImmutableHashSet(final Collection<E> collection)
    {
        super(new HashSet<E>(collection));
    }

    /**
     * @param collection the set to use as the immutable set.
     */
    public ImmutableHashSet(final ImmutableCollection<E> collection)
    {
        this(collection.toCollection());
    }

    /** {@inheritDoc} */
    @Override
    protected final HashSet<E> getSet()
    {
        return (HashSet<E>) super.getSet();
    }

    /** {@inheritDoc} */
    @Override
    public final Set<E> toSet()
    {
        return new HashSet<E>(getSet());
    }
}
