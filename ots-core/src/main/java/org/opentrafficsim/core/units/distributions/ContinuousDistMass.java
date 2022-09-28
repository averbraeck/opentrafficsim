package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.MassUnit;
import org.djunits.value.vdouble.scalar.Mass;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed mass.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistMass extends ContinuousDistDoubleScalar.Rel<Mass, MassUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit MassUnit; units
     */
    public ContinuousDistMass(final DistContinuous distribution, final MassUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Mass draw()
    {
        return new Mass(getDistribution().draw(), (MassUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistMass []";
    }

}
