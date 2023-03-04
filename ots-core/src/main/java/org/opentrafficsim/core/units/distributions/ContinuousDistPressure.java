package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.PressureUnit;
import org.djunits.value.vdouble.scalar.Pressure;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed pressure.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistPressure extends ContinuousDistDoubleScalar.Rel<Pressure, PressureUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit PressureUnit; units
     */
    public ContinuousDistPressure(final DistContinuous distribution, final PressureUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Pressure draw()
    {
        return new Pressure(getDistribution().draw(), (PressureUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistPressure []";
    }

}
