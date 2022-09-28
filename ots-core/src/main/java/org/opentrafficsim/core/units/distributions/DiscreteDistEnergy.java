package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.EnergyUnit;
import org.djunits.value.vdouble.scalar.Energy;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed energy.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistEnergy extends DiscreteDistDoubleScalar.Rel<Energy, EnergyUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit EnergyUnit; units
     */
    public DiscreteDistEnergy(final DistDiscrete distribution, final EnergyUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Energy draw()
    {
        return new Energy(getDistribution().draw(), (EnergyUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistEnergy []";
    }

}
