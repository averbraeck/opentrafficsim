package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Total travel distance divided by the sum of areas (X * T).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MeanIntensity extends AbstractIndicator<Frequency>
{

    /** Travel distance indicator. */
    private final TotalTravelDistance travelDistance;

    /**
     * @param travelDistance TotalTravelDistance; travel distance indicator
     */
    public MeanIntensity(final TotalTravelDistance travelDistance)
    {
        this.travelDistance = travelDistance;
    }

    /** {@inheritDoc} */
    @Override
    protected <G extends GtuData> Frequency calculate(final Query<G, ?> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        double ttd = this.travelDistance.getValue(query, startTime, endTime, trajectoryGroups).si;
        double area = 0;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            area += trajectoryGroup.getLength().si * (endTime.si - startTime.si);
        }
        return new Frequency(ttd / area, FrequencyUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "MeanIntensity [travelDistance=" + this.travelDistance + "]";
    }

}
