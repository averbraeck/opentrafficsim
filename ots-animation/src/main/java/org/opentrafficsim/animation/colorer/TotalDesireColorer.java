package org.opentrafficsim.animation.colorer;

import java.awt.Color;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Colorer for total desire.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TotalDesireColorer extends DesireColorer
{

    /** */
    private static final long serialVersionUID = 20170414L;

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final Gtu gtu)
    {
        Parameters params = gtu.getParameters();
        Double dLeft = params.getParameterOrNull(LmrsParameters.DLEFT);
        Double dRight = params.getParameterOrNull(LmrsParameters.DRIGHT);
        if (dLeft == null || dRight == null)
        {
            return NA;
        }
        return getColor(dLeft, dRight);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Total desire";
    }

}
