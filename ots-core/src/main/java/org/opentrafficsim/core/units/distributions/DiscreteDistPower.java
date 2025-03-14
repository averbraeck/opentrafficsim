package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.PowerUnit;
import org.djunits.value.vdouble.scalar.Power;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed power.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistPower extends DiscreteDistDoubleScalar.Rel<Power, PowerUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistPower(final DistDiscrete distribution, final PowerUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Power get()
    {
        return new Power(getDistribution().draw(), (PowerUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistPower []";
    }

}
