package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ForceUnit;
import org.djunits.value.vdouble.scalar.Force;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed force.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistForce extends ContinuousDistDoubleScalar.Rel<Force, ForceUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit ForceUnit; units
     */
    public ContinuousDistForce(final DistContinuous distribution, final ForceUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Force draw()
    {
        return new Force(getDistribution().draw(), (ForceUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistForce []";
    }

}
