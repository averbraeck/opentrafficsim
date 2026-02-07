package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.TemperatureUnit;
import org.djunits.value.vdouble.scalar.Temperature;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed temperature.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistTemperature extends DiscreteDistDoubleScalar.Rel<Temperature, TemperatureUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistTemperature(final DistDiscrete distribution, final TemperatureUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Temperature get()
    {
        return new Temperature(getDistribution().draw(), (TemperatureUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistTemperature []";
    }

}
