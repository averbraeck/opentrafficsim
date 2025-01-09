package org.opentrafficsim.road.gtu.generator.headway;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.distributions.Generator;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Headway generator using independent arrivals (exponential distribution) at a fixed average rate.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class HeadwayGenerator implements Generator<Duration>
{

    /** Demand level. */
    private final Frequency demand;

    /** the stream information. */
    private final StreamInterface stream;

    /**
     * Constructor.
     * @param demand demand.
     * @param stream the stream to use for generation.
     */
    public HeadwayGenerator(final Frequency demand, final StreamInterface stream)
    {
        this.demand = demand;
        this.stream = stream;
    }

    @Override
    public Duration draw()
    {
        return Duration.instantiateSI(-Math.log(this.stream.nextDouble()) / this.demand.si);
    }

}
