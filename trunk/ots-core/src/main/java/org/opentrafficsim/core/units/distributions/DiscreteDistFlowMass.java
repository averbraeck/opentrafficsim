package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.FlowMassUnit;
import org.djunits.value.vdouble.scalar.FlowMass;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed flow mass.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistFlowMass extends DiscreteDistDoubleScalar.Rel<FlowMass, FlowMassUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit FlowMassUnit; units
     */
    public DiscreteDistFlowMass(final DistDiscrete distribution, final FlowMassUnit unit)
    {
        super(distribution, unit);
        
    }

    /** {@inheritDoc} */
    @Override
    public FlowMass draw()
    {
        return new FlowMass(getDistribution().draw(), (FlowMassUnit) getUnit());
    }
    
    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistFlowMass []";
    }

}
