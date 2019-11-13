package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ElectricalChargeUnit;
import org.djunits.value.vdouble.scalar.ElectricalCharge;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed electrical charge.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistElectricalCharge extends DiscreteDistDoubleScalar.Rel<ElectricalCharge, ElectricalChargeUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistDiscrete; distribution
     * @param unit ElectricalChargeUnit; units
     */
    public DiscreteDistElectricalCharge(final DistDiscrete distribution, final ElectricalChargeUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public ElectricalCharge draw()
    {
        return new ElectricalCharge(getDistribution().draw(), (ElectricalChargeUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DiscreteDistElectricalCharge []";
    }

}
