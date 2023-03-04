package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed linear density.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistLinearDensity extends ContinuousDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit LinearDensityUnit; units
     */
    public ContinuousDistLinearDensity(final DistContinuous distribution, final LinearDensityUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public LinearDensity draw()
    {
        return new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistLinearDensity []";
    }

}
