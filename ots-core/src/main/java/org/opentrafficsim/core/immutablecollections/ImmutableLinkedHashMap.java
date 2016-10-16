package org.opentrafficsim.core.immutablecollections;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.language.Throw;

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
public class ImmutableLinkedHashMap<K, V> extends ImmutableHashMap<K, V>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /**
     * @param map the map to use as the immutable map.
     */
    public ImmutableLinkedHashMap(final Map<K, V> map)
    {
        this(map, Immutable.COPY);
    }

    /**
     * @param map the map to use as the immutable map.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableLinkedHashMap(final Map<K, V> map, final Immutable copyOrWrap)
    {
        super(copyOrWrap == Immutable.COPY ? new HashMap<K, V>(map) : map, copyOrWrap == Immutable.COPY);
        Throw.whenNull(copyOrWrap, "the copyOrWrap argument should be Immutable.COPY or Immutable.WRAP");
    }

    /**
     * @param immutableMap the map to use as the immutable map.
     */
    public ImmutableLinkedHashMap(final ImmutableHashMap<K, V> immutableMap)
    {
        this(immutableMap, Immutable.COPY);
    }

    /**
     * @param immutableMap the map to use as the immutable map.
     * @param copyOrWrap COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection
     */
    public ImmutableLinkedHashMap(final ImmutableHashMap<K, V> immutableMap, final Immutable copyOrWrap)
    {
        this(((ImmutableAbstractMap<K, V>) immutableMap).getMap(), copyOrWrap);
    }

    /**
     * @param immutableMap the set to use as the immutable set.
     */
    public ImmutableLinkedHashMap(final ImmutableLinkedHashMap<K, V> immutableMap)
    {
        this(immutableMap.toMap());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        Map<K, V> map = getMap();
        if (null == map)
        {
            return "ImmutableLinkedHashMap []";
        }
        return "ImmutableLinkedHashMap [" + map.toString() + "]";
    }

}
