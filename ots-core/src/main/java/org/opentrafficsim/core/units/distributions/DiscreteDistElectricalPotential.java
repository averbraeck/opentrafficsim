package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ElectricalPotentialUnit;
import org.djunits.value.vdouble.scalar.ElectricalPotential;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed electrical potential.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistElectricalPotential extends DiscreteDistDoubleScalar.Rel<ElectricalPotential, ElectricalPotentialUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution distribution
     * @param unit units
     */
    public DiscreteDistElectricalPotential(final DistDiscrete distribution, final ElectricalPotentialUnit unit)
    {
        super(distribution, unit);

    }

    @Override
    public ElectricalPotential draw()
    {
        return new ElectricalPotential(getDistribution().draw(), (ElectricalPotentialUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistElectricalPotential []";
    }

}
