package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AngleSolidUnit;
import org.djunits.value.vdouble.scalar.AngleSolid;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed angle solid.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistAngleSolid extends ContinuousDistDoubleScalar.Rel<AngleSolid, AngleSolidUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit AngleSolidUnit; units
     */
    public ContinuousDistAngleSolid(final DistContinuous distribution, final AngleSolidUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public AngleSolid draw()
    {
        return new AngleSolid(getDistribution().draw(), (AngleSolidUnit) getUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistAngleSolid []";
    }

}
