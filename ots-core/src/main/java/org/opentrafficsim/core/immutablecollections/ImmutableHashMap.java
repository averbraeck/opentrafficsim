package org.opentrafficsim.core.immutablecollections;

import java.util.HashMap;
import java.util.Map;

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
     * @param map the set to use as the immutable set.
     */
    public ImmutableHashMap(final Map<? extends K, ? extends V> map)
    {
        super(new HashMap<K, V>(map));
    }

    /**
     * @param immutableMap the set to use as the immutable set.
     */
    public ImmutableHashMap(final ImmutableHashMap<? extends K, ? extends V> immutableMap)
    {
        this(immutableMap.toMap());
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

}
