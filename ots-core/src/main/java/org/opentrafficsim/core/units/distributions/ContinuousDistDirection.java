package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed direction.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistDirection extends ContinuousDistDoubleScalar.Abs<Direction, DirectionUnit, AngleUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution distribution
     * @param unit units
     */
    public ContinuousDistDirection(final DistContinuous distribution, final DirectionUnit unit)
    {
        super(distribution, unit);

    }

    @Override
    public Direction draw()
    {
        return new Direction(getDistribution().draw(), (DirectionUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistDirection []";
    }

}
