package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory durations.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TotalTravelTime extends AbstractIndicator<Duration>
{

    /**
     * Constructor.
     */
    public TotalTravelTime()
    {
        //
    }

    @Override
    protected <G extends GtuData> Duration calculate(final Query<G, ?> query, final Duration startTime, final Duration endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        Duration sum = Duration.ZERO;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                sum = sum.plus(trajectory.getTotalDuration());
            }
        }
        return sum;
    }

    @Override
    public String toString()
    {
        return "TotalTravelTime";
    }

}
