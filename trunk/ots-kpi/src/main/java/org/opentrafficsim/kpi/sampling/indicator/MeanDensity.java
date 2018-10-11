package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Total travel time divided by the sum of areas (X * T).
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MeanDensity extends AbstractIndicator<LinearDensity>
{

    /** Travel time indicator. */
    private final TotalTravelTime travelTime;

    /**
     * @param travelTime TotalTravelTime; travel time indicator
     */
    public MeanDensity(final TotalTravelTime travelTime)
    {
        this.travelTime = travelTime;
    }

    /** {@inheritDoc} */
    @Override
    protected LinearDensity calculate(final Query query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup> trajectoryGroups)
    {
        double ttt = this.travelTime.getValue(query, startTime, endTime, trajectoryGroups).si;
        double area = 0;
        for (TrajectoryGroup trajectoryGroup : trajectoryGroups)
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
