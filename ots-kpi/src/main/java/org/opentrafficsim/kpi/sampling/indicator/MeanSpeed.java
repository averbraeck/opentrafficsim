package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory lengths divided by sum of trajectory durations.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MeanSpeed extends AbstractIndicator<Speed>
{

    /** Travel distance indicator. */
    private final TotalTravelDistance travelDistance;

    /** Travel time indicator. */
    private final TotalTravelTime travelTime;

    /**
     * @param travelDistance TotalTravelDistance; travel distance indicator
     * @param travelTime TotalTravelTime; travel time indicator
     */
    public MeanSpeed(final TotalTravelDistance travelDistance, final TotalTravelTime travelTime)
    {
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
    }

    /** {@inheritDoc} */
    @Override
    protected <G extends GtuData> Speed calculate(final Query<G, ?> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        return this.travelDistance.getValue(query, startTime, endTime, trajectoryGroups)
                .divide(this.travelTime.getValue(query, startTime, endTime, trajectoryGroups));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "MeanSpeed [travelDistance=" + this.travelDistance + ", travelTime=" + this.travelTime + "]";
    }

}
