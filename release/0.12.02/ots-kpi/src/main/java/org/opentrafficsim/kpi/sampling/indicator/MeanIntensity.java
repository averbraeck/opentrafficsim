package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Total travel distance divided by the sum of areas (X * T).
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MeanIntensity extends AbstractIndicator<Frequency>
{

    /** Travel distance indicator. */
    private final TotalTravelDistance travelDistance;

    /**
     * @param travelDistance travel distance indicator
     */
    public MeanIntensity(final TotalTravelDistance travelDistance)
    {
        this.travelDistance = travelDistance;
    }
    
    /** {@inheritDoc} */
    @Override
    protected Frequency calculate(Query query, Time startTime, Time endTime)
    {
        double ttd = this.travelDistance.getValue(query, startTime, endTime).si;
        double area = 0;
        for (TrajectoryGroup trajectoryGroup : query.getTrajectoryGroups(startTime, endTime))
        {
            area += trajectoryGroup.getLength().si * (endTime.si - startTime.si);
        }
        return new Frequency(ttd / area, FrequencyUnit.SI);
    }

}
