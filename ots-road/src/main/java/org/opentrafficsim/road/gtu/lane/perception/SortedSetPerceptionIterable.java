package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.OTSRoadNetwork;

/**
 * Simple class implementing a SortedSet. This is mainly for backwards compatibility. Methods that determine the elements 1-by-1
 * are much preferred for efficiency.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 */
@Deprecated
public class SortedSetPerceptionIterable<H extends Headway> extends TreeSet<H> implements PerceptionCollectable<H, LaneBasedGtu>
{

    /** */
    private static final long serialVersionUID = 20180219L;

    /** Network to obtain LaneBasedGtu. */
    private final OTSRoadNetwork network;

    /**
     * Constructor.
     * @param otsNetwork OTSRoadNetwork; network to obtain LaneBasedGtu
     */
    public SortedSetPerceptionIterable(final OTSRoadNetwork otsNetwork)
    {
        this.network = otsNetwork;
    }

    /** {@inheritDoc} */
    @Override
    public <C, I> C collect(final Supplier<I> identity, final PerceptionAccumulator<? super LaneBasedGtu, I> accumulator,
            final PerceptionFinalizer<C, I> finalizer)
    {
        Intermediate<I> intermediate = new Intermediate<>(identity.get());
        Iterator<H> it = iterator();
        while (it.hasNext() && !intermediate.isStop())
        {
            H next = it.next();
            intermediate =
                    accumulator.accumulate(intermediate, (LaneBasedGtu) this.network.getGTU(next.getId()), next.getDistance());
            intermediate.step();
        }
        return finalizer.collect(intermediate.getObject());
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<LaneBasedGtu> underlying()
    {
        return new Iterator<LaneBasedGtu>()
        {
            /** {@inheritDoc} */
            @Override
            public boolean hasNext()
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public LaneBasedGtu next()
            {
                throw new NoSuchElementException();
            }

        };
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<UnderlyingDistance<LaneBasedGtu>> underlyingWithDistance()
    {
        return new Iterator<UnderlyingDistance<LaneBasedGtu>>()
        {
            /** {@inheritDoc} */
            @Override
            public boolean hasNext()
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public UnderlyingDistance<LaneBasedGtu> next()
            {
                throw new NoSuchElementException();
            }

        };
    }

}
