package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed linear density.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistLinearDensity extends DiscreteDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistLinearDensity(final DistDiscrete distribution, final LinearDensityUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public LinearDensity get()
    {
        return new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistLinearDensity []";
    }

}
