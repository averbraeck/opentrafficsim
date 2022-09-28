package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.ElectricalChargeUnit;
import org.djunits.value.vdouble.scalar.ElectricalCharge;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed electrical charge.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistElectricalCharge extends ContinuousDistDoubleScalar.Rel<ElectricalCharge, ElectricalChargeUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit ElectricalChargeUnit; units
     */
    public ContinuousDistElectricalCharge(final DistContinuous distribution, final ElectricalChargeUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public ElectricalCharge draw()
    {
        return new ElectricalCharge(getDistribution().draw(), (ElectricalChargeUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistElectricalCharge []";
    }

}
