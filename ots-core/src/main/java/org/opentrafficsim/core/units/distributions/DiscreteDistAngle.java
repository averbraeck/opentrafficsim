package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed angle.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistAngle extends DiscreteDistDoubleScalar.Rel<Angle, AngleUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit AngleUnit; units
     */
    public DiscreteDistAngle(final DistDiscrete distribution, final AngleUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Angle draw()
    {
        return new Angle(getDistribution().draw(), (AngleUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistAngle []";
    }

}
