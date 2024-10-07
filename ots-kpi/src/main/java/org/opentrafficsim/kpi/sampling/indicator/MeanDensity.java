package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Total travel time divided by the sum of areas (X * T).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class MeanDensity extends AbstractIndicator<LinearDensity>
{

    /** Travel time indicator. */
    private final TotalTravelTime travelTime;

    /**
     * @param travelTime travel time indicator
     */
    public MeanDensity(final TotalTravelTime travelTime)
    {
        this.travelTime = travelTime;
    }

    /** {@inheritDoc} */
    @Override
    // @docs/07-output/indicators.md
    protected <G extends GtuData> LinearDensity calculate(final Query<G, ?> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        double ttt = this.travelTime.getValue(query, startTime, endTime, trajectoryGroups).si;
        double area = 0;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            area += trajectoryGroup.getLength().si * (endTime.si - startTime.si);
        }
        return new LinearDensity(ttt / area, LinearDensityUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "MeanDensity [travelTime=" + this.travelTime + "]";
    }

}
