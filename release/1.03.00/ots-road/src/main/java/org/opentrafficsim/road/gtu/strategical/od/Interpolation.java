package org.opentrafficsim.road.gtu.strategical.od;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;

/**
 * Interpolation of demand.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum Interpolation
{

    /** Stepwise interpolation of demand. */
    STEPWISE
    {
        /** {@inheritDoc} */
        @Override
        Frequency interpolate(final Frequency frequency0, final Time time0, final Frequency frequency1, final Time time1,
                final Time time)
        {
            return frequency0;
        }

        /** {@inheritDoc} */
        @Override
        int integrate(final Frequency frequency0, final Time time0, final Frequency frequency1, final Time time1)
        {
            return (int) (frequency0.getInUnit(FrequencyUnit.PER_HOUR)
                    * (time1.getInUnit(TimeUnit.BASE_HOUR) - time0.getInUnit(TimeUnit.BASE_HOUR)));
        }
    },

    /** Linear interpolation of demand. */
    LINEAR
    {
        /** {@inheritDoc} */
        @Override
        Frequency interpolate(final Frequency frequency0, final Time time0, final Frequency frequency1, final Time time1,
                final Time time)
        {
            return Frequency.interpolate(frequency0, frequency1, (time.si - time0.si) / (time1.si - time0.si));
        }

        /** {@inheritDoc} */
        @Override
        int integrate(final Frequency frequency0, final Time time0, final Frequency frequency1, final Time time1)
        {
            return (int) (0.5 * (frequency0.getInUnit(FrequencyUnit.PER_HOUR) + frequency1.getInUnit(FrequencyUnit.PER_HOUR))
                    * (time1.getInUnit(TimeUnit.BASE_HOUR) - time0.getInUnit(TimeUnit.BASE_HOUR)));
        }
    };

    /**
     * Interpolate between given frequencies.
     * @param frequency0 Frequency; frequency at {@code time0}
     * @param time0 Time; time of {@code frequency0} (&le; {@code time})
     * @param frequency1 Frequency; frequency at {@code time1}
     * @param time1 Time; time of {@code frequency1} (&gt; {@code time})
     * @param time Time; {@code time0} &le; {@code time} &lt; {@code time1}
     * @return interpolated frequency
     */
    abstract Frequency interpolate(Frequency frequency0, Time time0, Frequency frequency1, Time time1, Time time);

    /**
     * Integrates to the number of trips in given period.
     * @param frequency0 Frequency; frequency at {@code time0}
     * @param time0 Time; time of {@code frequency0} (&le; {@code time})
     * @param frequency1 Frequency; frequency at {@code time1}
     * @param time1 Time; time of {@code frequency1} (&gt; {@code time})
     * @return number of trips in given period
     */
    abstract int integrate(Frequency frequency0, Time time0, Frequency frequency1, Time time1);

    /**
     * @return whether this is step-wise interpolation
     */
    public boolean isStepWise()
    {
        return this.equals(STEPWISE);
    }

    /**
     * @return whether this is linear interpolation
     */
    public boolean isLinear()
    {
        return this.equals(LINEAR);
    }

    /**
     * Returns interpolated value from array at given time. If time is outside of the vector range, 0 is returned.
     * @param time Time; time to determine the frequency at
     * @param demandVector FrequencyVector; demand vector
     * @param timeVector TimeVector; time vector
     * @param sliceStart boolean; whether the time is at the start of an arbitrary time slice
     * @return interpolated value from array at given time, or 0 when time is outside of range
     */
    public final Frequency interpolateVector(final Time time, final FrequencyVector demandVector, final TimeVector timeVector,
            final boolean sliceStart)
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
            throw new RuntimeException("Index out of bounds.", ve);
        }
        // should not happen
        throw new RuntimeException("Demand interpolation failed.");
    }

}
