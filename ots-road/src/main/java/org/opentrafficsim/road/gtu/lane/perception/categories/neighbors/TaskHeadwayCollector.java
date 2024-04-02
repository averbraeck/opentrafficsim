package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;

/**
 * Simple collector implementation to obtain time headway.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TaskHeadwayCollector implements PerceptionCollector<Duration, LaneBasedGtu, Duration>
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
    public PerceptionAccumulator<LaneBasedGtu, Duration> getAccumulator()
    {
        return new PerceptionAccumulator<LaneBasedGtu, Duration>()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public Intermediate<Duration> accumulate(final Intermediate<Duration> intermediate, final LaneBasedGtu object,
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
