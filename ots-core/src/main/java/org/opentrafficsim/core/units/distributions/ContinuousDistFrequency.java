package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed frequency.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistFrequency extends ContinuousDistDoubleScalar.Rel<Frequency, FrequencyUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistFrequency(final DistContinuous distribution, final FrequencyUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Frequency get()
    {
        return new Frequency(getDistribution().draw(), (FrequencyUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistFrequency []";
    }

}
