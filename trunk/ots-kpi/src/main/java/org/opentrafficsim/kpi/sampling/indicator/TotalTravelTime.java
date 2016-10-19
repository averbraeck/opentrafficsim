package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory durations.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TotalTravelTime extends AbstractIndicator<Duration>
{

    /** {@inheritDoc} */
    @Override
    public final Duration calculate(final Query query, final Time startTime, final Time endTime)
    {
        Duration sum = Duration.ZERO;
        for (TrajectoryGroup trajectoryGroup : query.getTrajectoryGroups(startTime, endTime))
        {
            for (Trajectory trajectory : trajectoryGroup.getTrajectories())
            {
                sum = sum.plus(trajectory.getTotalDuration());
            }
        }
        return sum;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TotalTravelTime []";
    }

}
