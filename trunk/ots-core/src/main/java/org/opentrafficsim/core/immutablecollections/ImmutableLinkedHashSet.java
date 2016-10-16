package org.opentrafficsim.core.immutablecollections;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import nl.tudelft.simulation.language.Throw;

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
public class ImmutableLinkedHashSet<E> extends ImmutableHashSet<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param collection the collection to use as the immutable set.
     */
    public ImmutableLinkedHashSet(final Collection<E> collection)
    {
        this(collection, Immutable.COPY);
    }

    /**
     * @param collection the collection to use as the immutable set.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableLinkedHashSet(final Collection<E> collection, final Immutable copyOrWrap)
    {
        super(copyOrWrap == Immutable.COPY ? new LinkedHashSet<E>(collection) : collection, copyOrWrap == Immutable.COPY);
        Throw.whenNull(copyOrWrap, "the copyOrWrap argument should be Immutable.COPY or Immutable.WRAP");
    }

    /**
     * @param collection the collection to use as the immutable set.
     */
    public ImmutableLinkedHashSet(final ImmutableCollection<E> collection)
    {
        this(collection, Immutable.COPY);
    }

    /**
     * @param collection the collection to use as the immutable set.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableLinkedHashSet(final ImmutableCollection<E> collection, final Immutable copyOrWrap)
    {
        this(collection.toCollection(), copyOrWrap);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        Set<E> set = getSet();
        if (null == set)
        {
            return "ImmutableLinkedHashSet []";
        }
        return "ImmutableLinkedHashSet [" + set.toString() + "]";
    }

}
