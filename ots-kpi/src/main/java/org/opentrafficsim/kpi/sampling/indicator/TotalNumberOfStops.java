package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of measurements with zero speed, preceded by a non-zero speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TotalNumberOfStops extends AbstractIndicator<Dimensionless>
{

    /**
     * Constructor.
     */
    public TotalNumberOfStops()
    {
        //
    }

    @Override
    protected <G extends GtuData> Dimensionless calculate(final Query<G, ?> query, final Duration startTime,
            final Duration endTime, final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        int sum = 0;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                if (trajectory.size() > 1)
                {
                    float vPrev = trajectory.getV(0);
                    for (int i = 1; i < trajectory.size(); i++)
                    {
                        float v = trajectory.getV(i);
                        if (v == 0 && vPrev > 0)
                        {
                            sum++;
                        }
                        vPrev = v;
                    }
                }
            }
        }
        return new Dimensionless(sum, DimensionlessUnit.SI);
    }

    @Override
    public String toString()
    {
        return "TotalNumberOfStops";
    }

}
