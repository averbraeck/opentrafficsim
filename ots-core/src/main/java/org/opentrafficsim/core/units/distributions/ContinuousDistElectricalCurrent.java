package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ElectricalCurrentUnit;
import org.djunits.value.vdouble.scalar.ElectricalCurrent;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed electrical current.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistElectricalCurrent extends ContinuousDistDoubleScalar.Rel<ElectricalCurrent, ElectricalCurrentUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistElectricalCurrent(final DistContinuous distribution, final ElectricalCurrentUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public ElectricalCurrent get()
    {
        return new ElectricalCurrent(getDistribution().draw(), (ElectricalCurrentUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistElectricalCurrent []";
    }

}
