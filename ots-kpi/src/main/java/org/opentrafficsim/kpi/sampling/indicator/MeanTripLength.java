package org.opentrafficsim.kpi.sampling.indicator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory lengths divided by number of GTU's.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MeanTripLength extends AbstractIndicator<Length>
{

    /**
     * Constructor.
     */
    public MeanTripLength()
    {
        //
    }

    @Override
    // @docs/07-output/indicators.md
    protected <G extends GtuData> Length calculate(final Query<G, ?> query, final Duration startTime, final Duration endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        Length sum = Length.ZERO;
        Set<String> gtuIds = new LinkedHashSet<>();
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                sum = sum.plus(trajectory.getTotalLength());
                gtuIds.add(trajectory.getGtuId());
            }
        }
        return sum.divide(gtuIds.size());
    }

    @Override
    public String toString()
    {
        return "MeanTripLength";
    }

}
