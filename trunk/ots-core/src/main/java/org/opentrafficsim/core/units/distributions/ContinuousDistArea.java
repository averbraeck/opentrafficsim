package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.AreaUnit;
import org.djunits.value.vdouble.scalar.Area;

import nl.tudelft.simulation.jstats.distributions.DistContinuous;

/**
 * Continuously distributed area.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 29 aug. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// This class was automatically generated
public class ContinuousDistArea extends ContinuousDistDoubleScalar.Rel<Area, AreaUnit>
{

    /** */
    private static final long serialVersionUID = 20180829L;

    /**
     * @param distribution DistContinuous; distribution
     * @param unit AreaUnit; units
     */
    public ContinuousDistArea(final DistContinuous distribution, final AreaUnit unit)
    {
        super(distribution, unit);

    }

    /** {@inheritDoc} */
    @Override
    public Area draw()
    {
        return new Area(getDistribution().draw(), (AreaUnit) getDisplayUnit());
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ContinuousDistArea []";
    }

}
