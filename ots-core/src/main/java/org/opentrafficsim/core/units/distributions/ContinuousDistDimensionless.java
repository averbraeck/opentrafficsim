package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed dimensionless.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistDimensionless extends ContinuousDistDoubleScalar.Rel<Dimensionless, DimensionlessUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistDimensionless(final DistContinuous distribution, final DimensionlessUnit unit)
    {
        super(distribution, unit);

    }

    @Override
    public Dimensionless draw()
    {
        return new Dimensionless(getDistribution().draw(), (DimensionlessUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistDimensionless []";
    }

}
