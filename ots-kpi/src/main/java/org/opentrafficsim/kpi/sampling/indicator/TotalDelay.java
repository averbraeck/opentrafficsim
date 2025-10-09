package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;
import java.util.function.BiFunction;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory delays relative to a reference speed that can be specified per lane.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TotalDelay extends AbstractIndicator<Duration>
{

    /** Reference speed for delay. */
    private final BiFunction<LaneData<?>, String, Speed> referenceSpeedProvider;

    /**
     * Constructor using fixed reference speed.
     * @param referenceSpeed reference speed for delay
     */
    public TotalDelay(final Speed referenceSpeed)
    {
        this((lane, gtuTypeId) -> referenceSpeed);
    }

    /**
     * Constructor using reference speed provider.
     * @param referenceSpeedProvider reference speed provider
     */
    public TotalDelay(final BiFunction<LaneData<?>, String, Speed> referenceSpeedProvider)
    {
        this.referenceSpeedProvider = referenceSpeedProvider;
    }

    @Override
    protected <G extends GtuData> Duration calculate(final Query<G, ?> query, final Duration startTime, final Duration endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        double delay = 0.0;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                Speed referenceSpeedOnLane =
                        this.referenceSpeedProvider.apply(trajectoryGroup.getLane(), trajectory.getGtuTypeId());
                double d = trajectory.getTotalDuration().si - trajectory.getTotalLength().si / referenceSpeedOnLane.si;
                if (d > 0.0)
                {
                    delay += d;
                }
            }
        }
        return Duration.ofSI(delay);
    }

    @Override
    public String toString()
    {
        return "TotalDelay [referenceSpeed=" + this.referenceSpeedProvider + "]";
    }

}
