package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed time.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistTime extends DiscreteDistDoubleScalar.Abs<Time, TimeUnit, DurationUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution distribution
     * @param unit units
     */
    public DiscreteDistTime(final DistDiscrete distribution, final TimeUnit unit)
    {
        super(distribution, unit);

    }

    @Override
    public Time draw()
    {
        return new Time(getDistribution().draw(), (TimeUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistTime []";
    }

}
