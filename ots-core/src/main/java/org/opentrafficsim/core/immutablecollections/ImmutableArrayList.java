package org.opentrafficsim.core.immutablecollections;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.Throw;

/**
 * An immutable wrapper for an ArrayList.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this List
 */
public class ImmutableArrayList<E> extends ImmutableAbstractList<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param list the list to use as the immutable list.
     */
    public ImmutableArrayList(final List<E> list)
    {
        this(list, Immutable.COPY);
    }

    /**
     * @param list the list to use as the immutable list.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableArrayList(final List<E> list, final Immutable copyOrWrap)
    {
        super(copyOrWrap == Immutable.COPY ? new ArrayList<E>(list) : list, copyOrWrap == Immutable.COPY);
        Throw.whenNull(copyOrWrap, "the copyOrWrap argument should be Immutable.COPY or Immutable.WRAP");
    }

    /**
     * @param list the list to use as the immutable list.
     */
    public ImmutableArrayList(final ImmutableList<E> list)
    {
        this(list, Immutable.COPY);
    }

    /**
     * @param list the list to use as the immutable list.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableArrayList(final ImmutableList<E> list, final Immutable copyOrWrap)
    {
        this(((ImmutableAbstractList<E>) list).getList(), copyOrWrap);
    }

    /** {@inheritDoc} */
    @Override
    protected final ArrayList<E> getList()
    {
        return (ArrayList<E>) super.getList();
    }

    /** {@inheritDoc} */
    @Override
    public final List<E> toList()
    {
        return new ArrayList<E>(getList());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableList<E> subList(final int fromIndex, final int toIndex)
    {
        return new ImmutableArrayList<>(getList().subList(fromIndex, toIndex));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        List<E> list = getList();
        if (null == list)
        {
            return "ImmutableArrayList []";
        }
        return "ImmutableArrayList [" + list.toString() + "]";
    }

}
