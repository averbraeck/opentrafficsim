package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Simple implementation of {@code PerceptionIterable} which wraps a set. Constructors are available for an empty set, a
 * single-valued set, or a sorted set.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <H> headway type
 */
public class PerceptionIterableSet<H extends Headway> implements PerceptionIterable<H>
{

    /** Internal set. */
    private Set<H> set;

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
    public PerceptionIterableSet(final H headway)
    {
        this.set = new LinkedHashSet<>();
        this.set.add(headway);
    }

    /**
     * Creates an iterable from a sorted set.
     * @param headways set of headway
     */
    public PerceptionIterableSet(final SortedSet<H> headways)
    {
        this.set = headways;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<H> iterator()
    {
        return this.set.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public H first()
    {
        return this.set.iterator().next();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        return this.set.isEmpty();
    }

}
