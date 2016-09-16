package org.opentrafficsim.road.gtu.strategical.od;

import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Interpolation of demand.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
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
        @Override
        Frequency interpolate(final Frequency frequency0, final Time time0, final Frequency frequency1, final Time time1,
            final Time time)
        {
            return frequency0;
        }
    },

    /** Linear interpolation of demand. */
    LINEAR
    {
        @Override
        Frequency interpolate(final Frequency frequency0, final Time time0, final Frequency frequency1, final Time time1,
            final Time time)
        {
            return Frequency.interpolate(frequency0, frequency1, (time.si - time0.si) / (time1.si - time0.si));
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
    abstract Frequency interpolate(Frequency frequency0, Time time0, Frequency frequency1, Time time1, Time time);

}
