package org.opentrafficsim.road.gtu.lane.perception;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Simple implementation of {@code PerceptionIterable} which wraps a set. Constructors are available for an empty set, a
 * single-valued set, or a sorted set.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 26 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        this.set = new HashSet<>();
    }

    /**
     * Creates a single-value iterable.
     * @param headway H; headway
     */
    public PerceptionIterableSet(final H headway)
    {
        this.set = new HashSet<>();
        this.set.add(headway);
    }

    /**
     * Creates an iterable from a sorted set.
     * @param headways SortedSet&lt;H&gt;; set of headway
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
