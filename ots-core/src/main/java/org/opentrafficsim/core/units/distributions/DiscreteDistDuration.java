package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed duration.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistDuration extends DiscreteDistDoubleScalar.Rel<Duration, DurationUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistDuration(final DistDiscrete distribution, final DurationUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Duration get()
    {
        return new Duration(getDistribution().draw(), (DurationUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistDuration []";
    }

}
