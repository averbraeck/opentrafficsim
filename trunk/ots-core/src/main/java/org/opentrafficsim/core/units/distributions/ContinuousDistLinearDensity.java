package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed linear density.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistLinearDensity extends ContinuousDistDoubleScalar.Rel<LinearDensity, LinearDensityUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit LinearDensityUnit; units
     */
    public ContinuousDistLinearDensity(final DistContinuous distribution, final LinearDensityUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public LinearDensity draw()
    {
        return new LinearDensity(getDistribution().draw(), (LinearDensityUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistLinearDensity []";
    }

}
