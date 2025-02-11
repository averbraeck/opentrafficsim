package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AbsoluteTemperatureUnit;
import org.djunits.unit.TemperatureUnit;
import org.djunits.value.vdouble.scalar.AbsoluteTemperature;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed absolute temperature.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistAbsoluteTemperature
        extends ContinuousDistDoubleScalar.Abs<AbsoluteTemperature, AbsoluteTemperatureUnit, TemperatureUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistAbsoluteTemperature(final DistContinuous distribution, final AbsoluteTemperatureUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public AbsoluteTemperature draw()
    {
        return new AbsoluteTemperature(getDistribution().draw(), (AbsoluteTemperatureUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistAbsoluteTemperature []";
    }

}
