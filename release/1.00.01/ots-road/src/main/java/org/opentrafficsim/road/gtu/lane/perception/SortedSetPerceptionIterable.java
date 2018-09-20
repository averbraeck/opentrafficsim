package org.opentrafficsim.road.gtu.lane.perception;

import java.util.TreeSet;
import java.util.function.Supplier;

import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Simple class implementing a SortedSet. This is mainly for backwards compatibility. Methods that determine the elements 1-by-1
 * are much preferred for efficiency.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying object type
 */
@Deprecated
public class SortedSetPerceptionIterable<H extends Headway, U> extends TreeSet<H> implements PerceptionCollectable<H, U>
{

    /** */
    private static final long serialVersionUID = 20180219L;

    /** {@inheritDoc} */
    @Override
    public <C, I> C collect(final Supplier<I> identity, final PerceptionAccumulator<? super U, I> accumulator,
            final PerceptionFinalizer<C, I> finalizer)
    {
        throw new UnsupportedOperationException();
    }

}
