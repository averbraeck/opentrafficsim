package org.opentrafficsim.road.gtu.lane;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CollisionDetector implements PerceptionCollector<Void, LaneBasedGTU, Void>
{

    /** GTU id. */
    private final String id;

    /**
     * Constructor.
     * @param id String; GTU id
     */
    public CollisionDetector(final String id)
    {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public Supplier<Void> getIdentity()
    {
        return new Supplier<Void>()
        {
            /** {@inheritDoc} */
            @Override
            public Void get()
            {
                return null;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionAccumulator<LaneBasedGTU, Void> getAccumulator()
    {
        return new PerceptionAccumulator<LaneBasedGTU, Void>()
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public Intermediate<Void> accumulate(final Intermediate<Void> intermediate, final LaneBasedGTU object,
                    final Length distance)
            {
                Throw.when(distance.lt0(), CollisionException.class, "GTU %s collided with GTU %s", CollisionDetector.this.id,
                        object.getId());
                intermediate.stop();
                return intermediate;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionFinalizer<Void, Void> getFinalizer()
    {
        return new PerceptionFinalizer<Void, Void>()
        {
            @Override
            public Void collect(final Void intermediate)
            {
                return null;
            }
        };
    }

}
