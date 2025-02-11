package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.SolidAngleUnit;
import org.djunits.value.vdouble.scalar.SolidAngle;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed angle solid.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistSolidAngle extends ContinuousDistDoubleScalar.Rel<SolidAngle, SolidAngleUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistSolidAngle(final DistContinuous distribution, final SolidAngleUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public SolidAngle get()
    {
        return new SolidAngle(getDistribution().draw(), (SolidAngleUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistSolidAngle []";
    }

}
