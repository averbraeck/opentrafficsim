package org.opentrafficsim.road.gtu.perception;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;

import org.opentrafficsim.road.gtu.perception.object.PerceivedObject;

/**
 * Simple implementation of {@code PerceptionIterable} which wraps a set or a single value. Constructors are available for no
 * element, a single element, or a sorted set.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <P> perceived object type
 */
public class PerceptionIterableSet<P extends PerceivedObject> implements PerceptionIterable<P>
{

    /** Single element. */
    private final P single;

    /** Internal set. */
    private final SortedSet<P> set;

    /**
     * Creates an empty iterable.
     */
    public PerceptionIterableSet()
    {
        this.single = null;
        this.set = null;
    }

    /**
     * Creates a single-value iterable.
     * @param headway headway
     */
    public PerceptionIterableSet(final P headway)
    {
        this.single = headway;
        this.set = null;
    }

    /**
     * Creates an iterable from a sorted set.
     * @param headways set of headway
     */
    public PerceptionIterableSet(final SortedSet<P> headways)
    {
        this.single = null;
        this.set = headways;
    }

    @Override
    public Iterator<P> iterator()
    {
        return this.single == null ? this.set.iterator() : Collections.singleton(this.single).iterator();
    }

    @Override
    public P first()
    {
        return this.single == null ? this.set.first() : this.single;
    }

    @Override
    public boolean isEmpty()
    {
        return this.single == null && (this.set == null || this.set.isEmpty());
    }

}
