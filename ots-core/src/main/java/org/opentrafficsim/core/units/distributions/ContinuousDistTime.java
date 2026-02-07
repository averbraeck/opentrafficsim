package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed time.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistTime extends ContinuousDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistTime(final DistContinuous distribution, final TimeUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Time get()
    {
        return new Time(getDistribution().draw(), (TimeUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistTime []";
    }

}
