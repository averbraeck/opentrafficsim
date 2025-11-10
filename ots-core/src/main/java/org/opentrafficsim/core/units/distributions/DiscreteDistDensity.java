package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DensityUnit;
import org.djunits.value.vdouble.scalar.Density;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed density.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistDensity extends DiscreteDistDoubleScalar.Rel<Density, DensityUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistDensity(final DistDiscrete distribution, final DensityUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Density get()
    {
        return new Density(getDistribution().draw(), (DensityUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistDensity []";
    }

}
