package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed duration.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistDuration extends DiscreteDistDoubleScalar.Rel<Duration, DurationUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit DurationUnit; units
     */
    public DiscreteDistDuration(final DistDiscrete distribution, final DurationUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Duration draw()
    {
        return new Duration(getDistribution().draw(), (DurationUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistDuration []";
    }

}
