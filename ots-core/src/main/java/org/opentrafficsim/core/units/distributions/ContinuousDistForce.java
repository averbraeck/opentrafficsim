package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ForceUnit;
import org.djunits.value.vdouble.scalar.Force;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed force.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistForce extends ContinuousDistDoubleScalar.Rel<Force, ForceUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistForce(final DistContinuous distribution, final ForceUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Force get()
    {
        return new Force(getDistribution().draw(), (ForceUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistForce []";
    }

}
