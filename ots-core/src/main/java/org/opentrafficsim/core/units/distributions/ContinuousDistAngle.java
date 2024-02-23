package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed angle.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistAngle extends ContinuousDistDoubleScalar.Rel<Angle, AngleUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit AngleUnit; units
     */
    public ContinuousDistAngle(final DistContinuous distribution, final AngleUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Angle draw()
    {
        return new Angle(getDistribution().draw(), (AngleUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistAngle []";
    }

}
