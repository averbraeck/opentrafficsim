package org.opentrafficsim.road.network.sampling;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class TravelDistance implements Indicator<LengthUnit, Length>
{

    /** {@inheritDoc} */
    @Override
    public final Length calculate(final Query query)
    {
        double sum = 0;
        for (Trajectories trajectories : query.getTrajectories())
        {
            for (Trajectory trajectory : trajectories.getTrajectories())
            {
                float[] x = trajectory.getX();
                sum += (x[x.length - 1] - x[0]);
            }
        }
        return new Length(sum, LengthUnit.SI);
    }

}
