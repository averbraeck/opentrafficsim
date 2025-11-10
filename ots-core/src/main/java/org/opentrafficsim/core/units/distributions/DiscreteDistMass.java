package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.MassUnit;
import org.djunits.value.vdouble.scalar.Mass;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed mass.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistMass extends DiscreteDistDoubleScalar.Rel<Mass, MassUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistMass(final DistDiscrete distribution, final MassUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Mass get()
    {
        return new Mass(getDistribution().draw(), (MassUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistMass []";
    }

}
