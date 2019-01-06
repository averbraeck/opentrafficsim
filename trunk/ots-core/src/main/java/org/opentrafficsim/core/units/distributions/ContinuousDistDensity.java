package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.DensityUnit;
import org.djunits.value.vdouble.scalar.Density;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed density.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistDensity extends ContinuousDistDoubleScalar.Rel<Density, DensityUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit DensityUnit; units
     */
    public ContinuousDistDensity(final DistContinuous distribution, final DensityUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Density draw()
    {
        return new Density(getDistribution().draw(), (DensityUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistDensity []";
    }

}
