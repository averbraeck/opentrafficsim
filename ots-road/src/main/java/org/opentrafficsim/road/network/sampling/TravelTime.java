package org.opentrafficsim.road.network.sampling;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;

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
public class TravelTime implements Indicator<TimeUnit, Duration>
{

    /** {@inheritDoc} */
    @Override
    public final Duration calculate(final Query query, final Duration startTime, final Duration endTime)
    {
        double sum = 0;
        for (Trajectories trajectories : query.getTrajectories(startTime, endTime))
        {
            for (Trajectory trajectory : trajectories.getTrajectorySet())
            {
                float[] t = trajectory.getX();
                sum += (t[t.length - 1] - t[0]);
            }
        }
        return new Duration(sum, TimeUnit.SI);
    }

}
