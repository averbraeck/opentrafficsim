package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed direction.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistDirection extends DiscreteDistDoubleScalar.Abs<Direction, DirectionUnit, AngleUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistDirection(final DistDiscrete distribution, final DirectionUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Direction get()
    {
        return new Direction(getDistribution().draw(), (DirectionUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistDirection []";
    }

}
