package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Simple class implementing a SortedSet. This is mainly for backwards compatibility. Methods that determine the elements 1-by-1
 * are much preferred for efficiency.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 */
@Deprecated
public class SortedSetPerceptionIterable<H extends Headway> extends TreeSet<H> implements PerceptionCollectable<H, LaneBasedGTU>
{

    /** */
    private static final long serialVersionUID = 20180219L;

    /** Network to obtain LaneBasedGTU. */
    private final OTSNetwork network;

    /**
     * Constructor.
     * @param otsNetwork network to obtain LaneBasedGTU
     */
    public SortedSetPerceptionIterable(final OTSNetwork otsNetwork)
    {
        this.network = otsNetwork;
    }

    /** {@inheritDoc} */
    @Override
    public <C, I> C collect(final Supplier<I> identity, final PerceptionAccumulator<? super LaneBasedGTU, I> accumulator,
            final PerceptionFinalizer<C, I> finalizer)
    {
        Intermediate<I> intermediate = new Intermediate<>(identity.get());
        Iterator<H> it = iterator();
        while (it.hasNext() && !intermediate.isStop())
        {
            H next = it.next();
            intermediate =
                    accumulator.accumulate(intermediate, (LaneBasedGTU) this.network.getGTU(next.getId()), next.getDistance());
            intermediate.step();
        }
        return finalizer.collect(intermediate.getObject());
    }

}
