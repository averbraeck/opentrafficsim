package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed speed.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistSpeed extends DiscreteDistDoubleScalar.Rel<Speed, SpeedUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit SpeedUnit; units
     */
    public DiscreteDistSpeed(final DistDiscrete distribution, final SpeedUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Speed draw()
    {
        return new Speed(getDistribution().draw(), (SpeedUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistSpeed []";
    }

}
