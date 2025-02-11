package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Position;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed position.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistPosition extends ContinuousDistDoubleScalar.Abs<Position, PositionUnit, LengthUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistPosition(final DistContinuous distribution, final PositionUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Position draw()
    {
        return new Position(getDistribution().draw(), (PositionUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistPosition []";
    }

}
