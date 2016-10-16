package org.opentrafficsim.kpi.sampling.indicator;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MeanTripLength extends AbstractIndicator<Length>
{

    /** {@inheritDoc} */
    @Override
    public final Length calculate(final Query query, final Time startTime, final Time endTime)
    {
        Length sum = Length.ZERO;
        Set<String> gtuIds = new HashSet<>();
        for (TrajectoryGroup trajectoryGroup : query.getTrajectoryGroups(startTime, endTime))
        {
            for (Trajectory trajectory : trajectoryGroup.getTrajectories())
            {
                sum = sum.plus(trajectory.getTotalLength());
                gtuIds.add(trajectory.getGtuId());
            }
        }
        return sum.divideBy(gtuIds.size());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "MeanTripLength []";
    }

}
