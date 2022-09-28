package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed length.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistLength extends ContinuousDistDoubleScalar.Rel<Length, LengthUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit LengthUnit; units
     */
    public ContinuousDistLength(final DistContinuous distribution, final LengthUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Length draw()
    {
        return new Length(getDistribution().draw(), (LengthUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistLength []";
    }

}
