package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Position;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed position.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistPosition extends DiscreteDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistPosition(final DistDiscrete distribution, final PositionUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Position get()
    {
        return new Position(getDistribution().draw(), (PositionUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistPosition []";
    }

}
