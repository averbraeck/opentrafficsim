package org.opentrafficsim.road.gtu.strategical.od;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;

/**
 * Interpolation of demand.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        Frequency interpolate(final Frequency frequency0, final Duration time0, final Frequency frequency1,
            final Duration time1, final Duration time)
        {
            return frequency0;
        }

        /** {@inheritDoc} */
        @Override
        int integrate(final Frequency frequency0, final Duration time0, final Frequency frequency1, final Duration time1)
        {
            return (int) (frequency0.getInUnit(FrequencyUnit.PER_HOUR) * (time1.getInUnit(TimeUnit.HOUR) - time0
                .getInUnit(TimeUnit.HOUR)));
        }
    },

    /** Linear interpolation of demand. */
    LINEAR
    {
        /** {@inheritDoc} */
        @Override
        Frequency interpolate(final Frequency frequency0, final Duration time0, final Frequency frequency1,
            final Duration time1, final Duration time)
        {
            return Frequency.interpolate(frequency0, frequency1, (time.si - time0.si) / (time1.si - time0.si));
        }

        /** {@inheritDoc} */
        @Override
        int integrate(final Frequency frequency0, final Duration time0, final Frequency frequency1, final Duration time1)
        {
            return (int) (0.5 * (frequency0.getInUnit(FrequencyUnit.PER_HOUR) + frequency1.getInUnit(FrequencyUnit.PER_HOUR)) * (time1
                .getInUnit(TimeUnit.HOUR) - time0.getInUnit(TimeUnit.HOUR)));
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
    abstract Frequency
        interpolate(Frequency frequency0, Duration time0, Frequency frequency1, Duration time1, Duration time);

    /**
     * Integrates to the number of trips in given period.
     * @param frequency0 frequency at {@code time0}
     * @param time0 time of {@code frequency0} (&le; {@code time})
     * @param frequency1 frequency at {@code time1}
     * @param time1 time of {@code frequency1} (&gt; {@code time})
     * @return number of trips in given period
     */
    abstract int integrate(Frequency frequency0, Duration time0, Frequency frequency1, Duration time1);

}
