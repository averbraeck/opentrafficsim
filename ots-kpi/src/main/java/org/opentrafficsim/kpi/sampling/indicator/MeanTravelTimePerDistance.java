package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Inverse of mean speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MeanTravelTimePerDistance extends AbstractIndicator<Duration>
{

    /** Mean speed indicator. */
    private final MeanSpeed meanSpeed;

    /**
     * Constructor.
     * @param meanSpeed mean speed indicator
     */
    public MeanTravelTimePerDistance(final MeanSpeed meanSpeed)
    {
        this.meanSpeed = meanSpeed;
    }

    @Override
    protected <G extends GtuData> Duration calculate(final Query<G, ?> query, final Duration startTime, final Duration endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        return Duration.ofSI(1.0 / this.meanSpeed.getValue(query, startTime, endTime, trajectoryGroups).si);
    }

    @Override
    public String toString()
    {
        return "MeanTravelTime [meanTravelTime=" + this.meanSpeed + " (per km)]";
    }

}
