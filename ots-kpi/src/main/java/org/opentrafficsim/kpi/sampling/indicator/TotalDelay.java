package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory durations minus the sum of trajectory lengths divided by a reference speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// TODO something better than a global reference speed defined at the indicator
public class TotalDelay extends AbstractIndicator<Duration>
{

    /** Reference speed for delay. */
    private final Speed referenceSpeed;

    /**
     * @param referenceSpeed reference speed for delay
     */
    public TotalDelay(final Speed referenceSpeed)
    {
        this.referenceSpeed = referenceSpeed;
    }

    /** {@inheritDoc} */
    @Override
    protected <G extends GtuData> Duration calculate(final Query<G, ?> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        Duration sumTime = Duration.ZERO;
        Length sumDist = Length.ZERO;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            // TODO: use data points and limit speed per interval
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                sumTime = sumTime.plus(trajectory.getTotalDuration());
                sumDist = sumDist.plus(trajectory.getTotalLength());
            }
        }
        return sumTime.minus(sumDist.divide(this.referenceSpeed));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TotalDelay [referenceSpeed=" + this.referenceSpeed + "]";
    }

}
