package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of measurements with zero speed, preceded by a non-zero speed.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TotalNumberOfStops extends AbstractIndicator<Dimensionless>
{

    /** {@inheritDoc} */
    @Override
    protected <G extends GtuData> Dimensionless calculate(final Query<G, ?> query, final Time startTime,
            final Time endTime, final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        int sum = 0;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                float[] v = trajectory.getV();
                for (int i = 1; i < v.length; i++)
                {
                    if (v[i] == 0 && v[i - 1] > 0)
                    {
                        sum++;
                    }
                }
            }
        }
        return new Dimensionless(sum, DimensionlessUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TotalNumberOfStops []";
    }

}
