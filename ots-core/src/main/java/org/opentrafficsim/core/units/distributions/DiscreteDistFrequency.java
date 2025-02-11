package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed frequency.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistFrequency extends DiscreteDistDoubleScalar.Rel<Frequency, FrequencyUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistFrequency(final DistDiscrete distribution, final FrequencyUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Frequency draw()
    {
        return new Frequency(getDistribution().draw(), (FrequencyUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistFrequency []";
    }

}
