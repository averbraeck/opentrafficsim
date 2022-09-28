package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DensityUnit;
import org.djunits.value.vdouble.scalar.Density;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed density.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistDensity extends ContinuousDistDoubleScalar.Rel<Density, DensityUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit DensityUnit; units
     */
    public ContinuousDistDensity(final DistContinuous distribution, final DensityUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Density draw()
    {
        return new Density(getDistribution().draw(), (DensityUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistDensity []";
    }

}
