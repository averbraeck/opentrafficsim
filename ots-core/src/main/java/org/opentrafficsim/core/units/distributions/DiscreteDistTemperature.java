package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.TemperatureUnit;
import org.djunits.value.vdouble.scalar.Temperature;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed temperature.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistTemperature extends DiscreteDistDoubleScalar.Rel<Temperature, TemperatureUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution distribution
     * @param unit units
     */
    public DiscreteDistTemperature(final DistDiscrete distribution, final TemperatureUnit unit)
    {
        super(distribution, unit);

    }

    @Override
    public Temperature draw()
    {
        return new Temperature(getDistribution().draw(), (TemperatureUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistTemperature []";
    }

}
