package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.FlowMassUnit;
import org.djunits.value.vdouble.scalar.FlowMass;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed flow mass.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistFlowMass extends ContinuousDistDoubleScalar.Rel<FlowMass, FlowMassUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public ContinuousDistFlowMass(final DistContinuous distribution, final FlowMassUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public FlowMass get()
    {
        return new FlowMass(getDistribution().draw(), (FlowMassUnit) getDisplayUnit());
    }

    @Override
    public final String toString()
    {
        return "ContinuousDistFlowMass []";
    }

}
