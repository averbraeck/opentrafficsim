package org.opentrafficsim.core.immutablecollections;

import java.util.EnumSet;

/**
 * An immutable wrapper for a EnumSet.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <E> the type of content of this EnumSet
 */
public class ImmutableEnumSet<E extends Enum<E>> extends ImmutableAbstractSet<E>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param enumSet the set to use as the immutable set.
     */
    public ImmutableEnumSet(final EnumSet<E> enumSet)
    {
        super(EnumSet.copyOf(enumSet));
    }

    /**
     * @param enumSet the set to use as the immutable set.
     */
    public ImmutableEnumSet(final ImmutableEnumSet<E> enumSet)
    {
        super(enumSet.toSet());
    }

    /** {@inheritDoc} */
    @Override
    protected final EnumSet<E> getSet()
    {
        return (EnumSet<E>) super.getSet();
    }

    /** {@inheritDoc} */
    @Override
    public final EnumSet<E> toSet()
    {
        return EnumSet.copyOf(getSet());
    }

    /**
     * Creates an immutable enum set with the same element type as the specified immutable enum set, containing the same
     * elements (if any).
     * @param <E> The class of the elements in the set
     * @param s the immutable enum set from which to initialize this immutable enum set
     * @return An immutable copy of the specified immutable enum set.
     * @throws NullPointerException if <tt>s</tt> is null
     */
    public static <E extends Enum<E>> ImmutableEnumSet<E> copyOf(final ImmutableEnumSet<E> s)
    {
        return new ImmutableEnumSet<E>(s.getSet());
    }
}
