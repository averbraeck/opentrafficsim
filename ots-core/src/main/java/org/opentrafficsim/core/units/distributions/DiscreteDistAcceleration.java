package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed acceleration.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistAcceleration extends DiscreteDistDoubleScalar.Rel<Acceleration, AccelerationUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit AccelerationUnit; units
     */
    public DiscreteDistAcceleration(final DistDiscrete distribution, final AccelerationUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Acceleration draw()
    {
        return new Acceleration(getDistribution().draw(), (AccelerationUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistAcceleration []";
    }

}
