package org.opentrafficsim.road.gtu.generator.headway;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.road.od.Interpolation;

/**
 * Demand pattern defined by a frequency vector, time vector and interpolation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param demandVector demand vector
 * @param timeVector time vector
 * @param interpolation interpolation
 */
public record DemandPattern(FrequencyVector demandVector, TimeVector timeVector, Interpolation interpolation)
        implements Arrivals
{

    @Override
    public Frequency getFrequency(final Time time, final boolean sliceStart)
    {
        return this.interpolation.interpolateVector(time, this.demandVector, this.timeVector, sliceStart);
    }

    @Override
    public Time nextTimeSlice(final Time time)
    {
        for (Time d : this.timeVector)
        {
            if (d.gt(time))
            {
                return d;
            }
        }
        return null;
    }

}
