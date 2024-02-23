package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ElectricalPotentialUnit;
import org.djunits.value.vdouble.scalar.ElectricalPotential;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed electrical potential.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistElectricalPotential
        extends ContinuousDistDoubleScalar.Rel<ElectricalPotential, ElectricalPotentialUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit ElectricalPotentialUnit; units
     */
    public ContinuousDistElectricalPotential(final DistContinuous distribution, final ElectricalPotentialUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public ElectricalPotential draw()
    {
        return new ElectricalPotential(getDistribution().draw(), (ElectricalPotentialUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistElectricalPotential []";
    }

}
