package org.opentrafficsim.road.gtu.generator.headway;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.opentrafficsim.road.od.Interpolation;

/**
 * Demand pattern defined by a frequency vector, time vector and interpolation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param demandVector demand vector
 * @param timeVector time vector
 * @param interpolation interpolation
 */
public record DemandPattern(FrequencyVector demandVector, DurationVector timeVector, Interpolation interpolation)
        implements Arrivals
{

    @Override
    public Frequency getFrequency(final Duration time, final boolean sliceStart)
    {
        return this.interpolation.interpolateVector(time, this.demandVector, this.timeVector, sliceStart);
    }

    @Override
    public Optional<Duration> nextTimeSlice(final Duration time)
    {
        for (Duration d : this.timeVector)
        {
            if (d.gt(time))
            {
                return Optional.of(d);
            }
        }
        return Optional.empty();
    }

}
