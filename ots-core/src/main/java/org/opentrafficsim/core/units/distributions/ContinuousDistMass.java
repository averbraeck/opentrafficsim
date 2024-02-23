package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.MassUnit;
import org.djunits.value.vdouble.scalar.Mass;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed mass.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistMass extends ContinuousDistDoubleScalar.Rel<Mass, MassUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit MassUnit; units
     */
    public ContinuousDistMass(final DistContinuous distribution, final MassUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Mass draw()
    {
        return new Mass(getDistribution().draw(), (MassUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistMass []";
    }

}
