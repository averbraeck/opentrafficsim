package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed frequency.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistFrequency extends DiscreteDistDoubleScalar.Rel<Frequency, FrequencyUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit FrequencyUnit; units
     */
    public DiscreteDistFrequency(final DistDiscrete distribution, final FrequencyUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Frequency draw()
    {
        return new Frequency(getDistribution().draw(), (FrequencyUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistFrequency []";
    }

}
