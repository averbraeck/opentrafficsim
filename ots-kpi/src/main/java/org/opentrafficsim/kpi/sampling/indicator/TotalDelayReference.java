package org.opentrafficsim.kpi.sampling.indicator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.kpi.sampling.data.ReferenceSpeed;

/**
 * Delay based on reference speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TotalDelayReference extends AbstractIndicator<Duration>
{

    /** Reference speed extended data type. */
    private static final ReferenceSpeed REF_SPEED_TYPE = ReferenceSpeed.INSTANCE;

    /**
     * Constructor.
     */
    public TotalDelayReference()
    {
        //
    }

    @Override
    protected final <G extends GtuData> Duration calculate(final Query<G, ?> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        Map<String, Duration> gtuTimes = new LinkedHashMap<>();
        Map<String, Duration> gtuRefTimes = new LinkedHashMap<>();
        for (TrajectoryGroup<? extends GtuData> trajectoryGroup : trajectoryGroups)
        {
            try
            {
                for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                {
                    Duration sumTime;
                    Duration sumRefTime;
                    if (gtuTimes.containsKey(trajectory.getGtuId()))
                    {
                        sumTime = gtuTimes.get(trajectory.getGtuId());
                        sumRefTime = gtuRefTimes.get(trajectory.getGtuId());
                    }
                    else
                    {
                        sumTime = Duration.ZERO;
                        sumRefTime = Duration.ZERO;
                    }
                    Throw.when(!trajectory.contains(REF_SPEED_TYPE), UnsupportedOperationException.class,
                            "TotalDelayReference can only work with trajectories that have %s extended data.",
                            REF_SPEED_TYPE.getId());
                    FloatSpeedVector refSpeed = trajectory.getExtendedData(REF_SPEED_TYPE);
                    float[] x = trajectory.getX();
                    for (int i = 1; i < refSpeed.size(); i++)
                    {
                        double refV = refSpeed.get(i - 1).si;
                        double dx = x[i] - x[i - 1];
                        sumRefTime = sumRefTime.plus(new Duration(dx / refV, DurationUnit.SI));
                    }
                    gtuTimes.put(trajectory.getGtuId(), sumTime.plus(trajectory.getTotalDuration()));
                    gtuRefTimes.put(trajectory.getGtuId(), sumRefTime);
                }
            }
            catch (SamplingException exception)
            {
                throw new RuntimeException("Exception while trying to determine delay in trajectory.", exception);
            }
        }
        Duration delaySum = Duration.ZERO;
        for (String id : gtuTimes.keySet())
        {
            Duration gtuTime = gtuTimes.get(id);
            Duration gtuRefTime = gtuRefTimes.get(id);
            if (gtuTime.gt(gtuRefTime))
            {
                delaySum = delaySum.plus(gtuTime.minus(gtuRefTime));
            }
        }
        return delaySum;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TotalDelayReference";
    }

}
