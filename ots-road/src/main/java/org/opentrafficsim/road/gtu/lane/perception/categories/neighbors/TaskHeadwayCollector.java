package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;

/**
 * Simple collector implementation to obtain time headway.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskHeadwayCollector implements PerceptionCollector<Duration, LaneBasedGTU, Duration>
{

    /** Own speed. */
    private final Speed speed;

    /**
     * Constructor.
     * @param speed Speed; speed
     */
    public TaskHeadwayCollector(final Speed speed)
    {
        this.speed = speed;
    }

    /** {@inheritDoc} */
    @Override
    public Supplier<Duration> getIdentity()
    {
        return new Supplier<Duration>()
        {
            @Override
            public Duration get()
            {
                return null; // if no leader
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionAccumulator<LaneBasedGTU, Duration> getAccumulator()
    {
        return new PerceptionAccumulator<LaneBasedGTU, Duration>()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public Intermediate<Duration> accumulate(final Intermediate<Duration> intermediate, final LaneBasedGTU object,
                    final Length distance)
            {
                intermediate.setObject(distance.divide(TaskHeadwayCollector.this.speed));
                intermediate.stop(); // need only 1 leader
                return intermediate;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionFinalizer<Duration, Duration> getFinalizer()
    {
        return new PerceptionFinalizer<Duration, Duration>()
        {
            @Override
            public Duration collect(final Duration intermediate)
            {
                return intermediate == null ? intermediate : (intermediate.gt0() ? intermediate : Duration.ZERO);
            }
        };
    }

}
