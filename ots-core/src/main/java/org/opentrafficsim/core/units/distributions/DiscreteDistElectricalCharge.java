package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ElectricalChargeUnit;
import org.djunits.value.vdouble.scalar.ElectricalCharge;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

/**
 * Discretely distributed electrical charge.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// This class was automatically generated
public class DiscreteDistElectricalCharge extends DiscreteDistDoubleScalar.Rel<ElectricalCharge, ElectricalChargeUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * Constructor.
     * @param distribution distribution
     * @param unit unit
     */
    public DiscreteDistElectricalCharge(final DistDiscrete distribution, final ElectricalChargeUnit unit)
    {
        super(distribution, unit);
    }

    @Override
    public ElectricalCharge get()
    {
        return new ElectricalCharge(getDistribution().draw(), (ElectricalChargeUnit) getUnit());
    }

    @Override
    public final String toString()
    {
        return "DiscreteDistElectricalCharge []";
    }

}
