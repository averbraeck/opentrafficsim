package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.EnergyUnit;
import org.djunits.value.vdouble.scalar.Energy;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed energy.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistEnergy extends ContinuousDistDoubleScalar.Rel<Energy, EnergyUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistEnergy(final DistContinuous distribution, final EnergyUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Energy get()
    {
        return new Energy(getDistribution().draw(), (EnergyUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistEnergy []";
    }

}
