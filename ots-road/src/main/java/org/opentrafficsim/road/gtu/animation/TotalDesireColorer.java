package org.opentrafficsim.road.gtu.animation;

import java.awt.Color;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Colorer for total desire.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TotalDesireColorer extends DesireColorer
{

    /** */
    private static final long serialVersionUID = 20170414L;

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU gtu)
    {
        Parameters params = gtu.getParameters();
        if (params.contains(LmrsParameters.DLEFT) && params.contains(LmrsParameters.DRIGHT))
        {
            try
            {
                double dLeft = params.getParameter(LmrsParameters.DLEFT);
                double dRight = params.getParameter(LmrsParameters.DRIGHT);
                return getColor(dLeft, dRight);
            }
            catch (ParameterException exception)
            {
                exception.printStackTrace();
            }
        }
        return NA;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Total desire";
    }

}
