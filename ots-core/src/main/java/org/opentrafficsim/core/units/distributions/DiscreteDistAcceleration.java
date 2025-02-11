package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed acceleration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistAcceleration extends DiscreteDistDoubleScalar.Rel<Acceleration, AccelerationUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistAcceleration(final DistDiscrete distribution, final AccelerationUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Acceleration get()
    {
        return new Acceleration(getDistribution().draw(), (AccelerationUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistAcceleration []";
    }

}
