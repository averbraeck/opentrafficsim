package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.FlowMassUnit;
import org.djunits.value.vdouble.scalar.FlowMass;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed flow mass.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistFlowMass extends DiscreteDistDoubleScalar.Rel<FlowMass, FlowMassUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution distribution
     * @param unit units
     */
    public DiscreteDistFlowMass(final DistDiscrete distribution, final FlowMassUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public FlowMass draw()
    {
        return new FlowMass(getDistribution().draw(), (FlowMassUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistFlowMass []";
    }

}
