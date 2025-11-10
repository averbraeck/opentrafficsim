package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistSpeed extends DiscreteDistDoubleScalar.Rel<Speed, SpeedUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistSpeed(final DistDiscrete distribution, final SpeedUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Speed get()
    {
        return new Speed(getDistribution().draw(), (SpeedUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistSpeed []";
    }

}
