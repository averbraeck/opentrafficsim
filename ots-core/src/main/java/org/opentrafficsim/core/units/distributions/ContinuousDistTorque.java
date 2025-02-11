package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.TorqueUnit;
import org.djunits.value.vdouble.scalar.Torque;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed torque.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistTorque extends ContinuousDistDoubleScalar.Rel<Torque, TorqueUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistTorque(final DistContinuous distribution, final TorqueUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public Torque get()
    {
        return new Torque(getDistribution().draw(), (TorqueUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistTorque []";
    }

}
