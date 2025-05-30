package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed length.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistLength extends DiscreteDistDoubleScalar.Rel<Length, LengthUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistLength(final DistDiscrete distribution, final LengthUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Length get()
    {
        return new Length(getDistribution().draw(), (LengthUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistLength []";
    }

}
