package org.opentrafficsim.road.od;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.opentrafficsim.base.OtsRuntimeException;

/**
 * Interpolation of demand.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum Interpolation
{

    /** Stepwise interpolation of demand. */
    STEPWISE
    {
        @Override
        Frequency interpolate(final Frequency frequency0, final Duration time0, final Frequency frequency1,
                final Duration time1, final Duration time)
        {
            return frequency0;
        }

        @Override
        int integrate(final Frequency frequency0, final Duration time0, final Frequency frequency1, final Duration time1)
        {
            return (int) (frequency0.getInUnit(FrequencyUnit.PER_HOUR)
                    * (time1.getInUnit(DurationUnit.HOUR) - time0.getInUnit(DurationUnit.HOUR)));
        }

        @Override
        public String toString()
        {
            return "STEPWISE";
        }
    },

    /** Linear interpolation of demand. */
    LINEAR
    {
        @Override
        Frequency interpolate(final Frequency frequency0, final Duration time0, final Frequency frequency1,
                final Duration time1, final Duration time)
        {
            return Frequency.interpolate(frequency0, frequency1, (time.si - time0.si) / (time1.si - time0.si));
        }

        @Override
        int integrate(final Frequency frequency0, final Duration time0, final Frequency frequency1, final Duration time1)
        {
            return (int) (0.5 * (frequency0.getInUnit(FrequencyUnit.PER_HOUR) + frequency1.getInUnit(FrequencyUnit.PER_HOUR))
                    * (time1.getInUnit(DurationUnit.HOUR) - time0.getInUnit(DurationUnit.HOUR)));
        }

        @Override
        public String toString()
        {
            return "LINEAR";
        }
    };

    /**
     * Interpolate between given frequencies.
     * @param frequency0 frequency at {@code time0}
     * @param time0 time of {@code frequency0} (&le; {@code time})
     * @param frequency1 frequency at {@code time1}
     * @param time1 time of {@code frequency1} (&gt; {@code time})
     * @param time {@code time0} &le; {@code time} &lt; {@code time1}
     * @return interpolated frequency
     */
    abstract Frequency interpolate(Frequency frequency0, Duration time0, Frequency frequency1, Duration time1, Duration time);

    /**
     * Integrates to the number of trips in given period.
     * @param frequency0 frequency at {@code time0}
     * @param time0 time of {@code frequency0} (&le; {@code time})
     * @param frequency1 frequency at {@code time1}
     * @param time1 time of {@code frequency1} (&gt; {@code time})
     * @return number of trips in given period
     */
    abstract int integrate(Frequency frequency0, Duration time0, Frequency frequency1, Duration time1);

    /**
     * Returns whether this is step-wise interpolation.
     * @return whether this is step-wise interpolation
     */
    public boolean isStepWise()
    {
        return this.equals(STEPWISE);
    }

    /**
     * Returns whether this is linear interpolation.
     * @return whether this is linear interpolation
     */
    public boolean isLinear()
    {
        return this.equals(LINEAR);
    }

    /**
     * Returns interpolated value from array at given time. If time is outside of the vector range, 0 is returned.
     * @param time time to determine the frequency at
     * @param demandVector demand vector
     * @param timeVector time vector
     * @param sliceStart whether the time is at the start of an arbitrary time slice
     * @return interpolated value from array at given time, or 0 when time is outside of range
     */
    public final Frequency interpolateVector(final Duration time, final FrequencyVector demandVector,
            final DurationVector timeVector, final boolean sliceStart)
    {
        try
        {
            // empty data or before start or after end, return 0
            // case 1: t < t(0)
            // case 2: sliceEnd & t == t(0), i.e. end of no-demand time before time array
            // case 3: sliceStart & t == t(end), i.e. start of no-demand time after time array
            // case 4: t > t(end)
            if (timeVector.size() == 0 || (sliceStart ? time.lt(timeVector.get(0)) : time.le(timeVector.get(0))) || (sliceStart
                    ? time.ge(timeVector.get(timeVector.size() - 1)) : time.gt(timeVector.get(timeVector.size() - 1))))
            {
                return new Frequency(0.0, FrequencyUnit.PER_HOUR); // Frequency.ZERO give "Hz" which is not nice for flow
            }
            // interpolate
            for (int i = 0; i < timeVector.size() - 1; i++)
            {
                // cases where we can take the slice from i to i+1
                // case 1: sliceStart & t(i+1) > t [edge case: t(i) = t]
                // case 2: sliceEnd & t(i+1) >= t [edge case: t(i+1) = t]
                if (sliceStart ? timeVector.get(i + 1).gt(time) : timeVector.get(i + 1).ge(time))
                {
                    return interpolate(demandVector.get(i), timeVector.get(i), demandVector.get(i + 1), timeVector.get(i + 1),
                            time);
                }
            }
        }
        catch (ValueRuntimeException ve)
        {
            // should not happen, vector lengths are checked when given is input
            throw new OtsRuntimeException("Index out of bounds.", ve);
        }
        // should not happen
        throw new OtsRuntimeException("Demand interpolation failed.");
    }

}
