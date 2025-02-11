package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AreaUnit;
import org.djunits.value.vdouble.scalar.Area;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed area.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistArea extends DiscreteDistDoubleScalar.Rel<Area, AreaUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistArea(final DistDiscrete distribution, final AreaUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Area get()
    {
        return new Area(getDistribution().draw(), (AreaUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistArea []";
    }

}
