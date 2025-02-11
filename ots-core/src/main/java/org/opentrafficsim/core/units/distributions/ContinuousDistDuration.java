package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed duration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistDuration extends ContinuousDistDoubleScalar.Rel<Duration, DurationUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistDuration(final DistContinuous distribution, final DurationUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Duration draw()
    {
        return new Duration(getDistribution().draw(), (DurationUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistDuration []";
    }

}
