package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed speed.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistSpeed extends ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>
{

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistSpeed(final DistContinuous distribution, final SpeedUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Speed get()
    {
        return new Speed(getDistribution().draw(), (SpeedUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistSpeed []";
    }

}
