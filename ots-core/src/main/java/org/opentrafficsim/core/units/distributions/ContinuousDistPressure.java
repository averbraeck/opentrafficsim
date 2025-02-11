package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.PressureUnit;
import org.djunits.value.vdouble.scalar.Pressure;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed pressure.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistPressure extends ContinuousDistDoubleScalar.Rel<Pressure, PressureUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistPressure(final DistContinuous distribution, final PressureUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Pressure draw()
    {
        return new Pressure(getDistribution().draw(), (PressureUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistPressure []";
    }

}
