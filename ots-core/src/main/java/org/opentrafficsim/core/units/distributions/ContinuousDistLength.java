package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed length.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistLength extends ContinuousDistDoubleScalar.Rel<Length, LengthUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistLength(final DistContinuous distribution, final LengthUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Length get()
    {
        return new Length(getDistribution().draw(), (LengthUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistLength []";
    }

}
