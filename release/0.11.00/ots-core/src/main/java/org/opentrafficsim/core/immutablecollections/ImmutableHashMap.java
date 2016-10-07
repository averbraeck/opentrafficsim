package org.opentrafficsim.core.immutablecollections;

import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.core.Throw;

/**
 * An immutable wrapper for a HashMap.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <K> the key type of content of this Map
 * @param <V> the value type of content of this Map
 */
public class ImmutableHashMap<K, V> extends ImmutableAbstractMap<K, V>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param map the map to use as the immutable map.
     */
    public ImmutableHashMap(final Map<K, V> map)
    {
        this(map, Immutable.COPY);
    }

    /**
     * @param map the map to use as the immutable map.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableHashMap(final Map<K, V> map, final Immutable copyOrWrap)
    {
        super(copyOrWrap == Immutable.COPY ? new HashMap<K, V>(map) : map, copyOrWrap == Immutable.COPY);
        Throw.whenNull(copyOrWrap, "the copyOrWrap argument should be Immutable.COPY or Immutable.WRAP");
    }

    /**
     * @param map the map to use as the immutable map.
     * @param copy boolean; indicates whether the immutable is a copy or a wrap
     */
    protected ImmutableHashMap(final Map<K, V> map, final boolean copy)
    {
        super(map, copy);
    }

    /**
     * @param immutableMap the map to use as the immutable map.
     */
    public ImmutableHashMap(final ImmutableHashMap<K, V> immutableMap)
    {
        this(immutableMap, Immutable.COPY);
    }

    /**
     * @param immutableMap the map to use as the immutable map.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableHashMap(final ImmutableHashMap<K, V> immutableMap, final Immutable copyOrWrap)
    {
        this(((ImmutableAbstractMap<K, V>) immutableMap).getMap(), copyOrWrap);
    }

    /** {@inheritDoc} */
    @Override
    protected final HashMap<K, V> getMap()
    {
        return (HashMap<K, V>) super.getMap();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<K, V> toMap()
    {
        return new HashMap<K, V>(getMap());
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSet<K> keySet()
    {
        return new ImmutableHashSet<K>(getMap().keySet());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        Map<K, V> map = getMap();
        if (null == map)
        {
            return "ImmutableHashMap []";
        }
        return "ImmutableHashMap [" + map.toString() + "]";
    }

}
