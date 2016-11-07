package org.opentrafficsim.base.immutablecollections;

import java.util.Map;

import nl.tudelft.simulation.language.Throw;

/**
 * An abstract base class for an immutable wrapper for a Map.
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
public abstract class ImmutableAbstractMap<K, V> implements ImmutableMap<K, V>
{
    /** */
    private static final long serialVersionUID = 20160507L;

    /** the map that is wrapped, without giving access to methods that can change it. */
    private final Map<K, V> map;

    /** COPY stores a safe, internal copy of the collection; WRAP stores a pointer to the original collection. */
    private final Immutable copyOrWrap;

    /**
     * Construct an abstract immutable map. Make sure that the argument is a safe copy of the map of the right type!
     * @param map a safe copy of the map to use as the immutable map
     * @param copy indicate whether the immutable is a copy or a wrap
     */
    protected ImmutableAbstractMap(final Map<K, V> map, final boolean copy)
    {
        Throw.whenNull(map, "the map argument cannot be null");
        this.map = map;
        this.copyOrWrap = copy ? Immutable.COPY : Immutable.WRAP;
    }

    /**
     * Prepare the map of the right type for use a subclass. Implement e.g. as follows:
     * 
     * <pre>
     * {@literal @}Override
     * protected HashMap&lt;E&gt; getMap()
     * {
     *     return (HashMap&lt;E&gt;) super.getMap();
     * }
     * </pre>
     * @return the map of the right type for use a subclass
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Map<K, V> getMap()
    {
        return this.map;
    }

    /** {@inheritDoc} */
    @Override
    public final int size()
    {
        return this.map.size();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsKey(final Object key)
    {
        return this.map.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean containsValue(final Object value)
    {
        return this.map.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public final V get(final Object key)
    {
        return this.map.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableCollection<V> values()
    {
        return new ImmutableHashSet<>(this.map.values());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isWrap()
    {
        return this.copyOrWrap == Immutable.WRAP;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.map == null) ? 0 : this.map.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImmutableAbstractMap<?, ?> other = (ImmutableAbstractMap<?, ?>) obj;
        if (this.map == null)
        {
            if (other.map != null)
                return false;
        }
        else if (!this.map.equals(other.map))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Immutable[" + this.map.toString() + "]";
    }
}
