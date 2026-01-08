package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;

/**
 * Simple implementation of {@code PerceptionIterable} which wraps a set. Constructors are available for an empty set, a
 * single-valued set, or a sorted set.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <P> perceived object type
 */
public class PerceptionIterableSet<P extends PerceivedObject> implements PerceptionIterable<P>
{

    /** Internal set. */
    private Set<P> set;

    /**
     * Creates an empty iterable.
     */
    public PerceptionIterableSet()
    {
        this.set = new LinkedHashSet<>();
    }

    /**
     * Creates a single-value iterable.
     * @param headway headway
     */
    public PerceptionIterableSet(final P headway)
    {
        this.set = new LinkedHashSet<>();
        this.set.add(headway);
    }

    /**
     * Creates an iterable from a sorted set.
     * @param headways set of headway
     */
    public PerceptionIterableSet(final SortedSet<P> headways)
    {
        this.set = headways;
    }

    @Override
    public Iterator<P> iterator()
    {
        return this.set.iterator();
    }

    @Override
    public P first()
    {
        return this.set.iterator().next();
    }

    @Override
    public boolean isEmpty()
    {
        return this.set.isEmpty();
    }

}
