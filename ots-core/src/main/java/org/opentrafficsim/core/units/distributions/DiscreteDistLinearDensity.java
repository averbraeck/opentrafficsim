package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed linear density.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistLinearDensity extends DiscreteDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit LinearDensityUnit; units
     */
    public DiscreteDistLinearDensity(final DistDiscrete distribution, final LinearDensityUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public LinearDensity draw()
    {
        return new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistLinearDensity []";
    }

}
