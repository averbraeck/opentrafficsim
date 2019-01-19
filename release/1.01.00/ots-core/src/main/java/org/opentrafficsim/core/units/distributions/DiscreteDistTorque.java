package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.TorqueUnit;
import org.djunits.value.vdouble.scalar.Torque;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed torque.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistTorque extends DiscreteDistDoubleScalar.Rel<Torque, TorqueUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit TorqueUnit; units
     */
    public DiscreteDistTorque(final DistDiscrete distribution, final TorqueUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Torque draw()
    {
        return new Torque(getDistribution().draw(), (TorqueUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistTorque []";
    }

}
