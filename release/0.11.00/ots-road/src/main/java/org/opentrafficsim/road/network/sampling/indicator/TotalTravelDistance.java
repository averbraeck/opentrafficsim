package org.opentrafficsim.road.network.sampling.indicator;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.sampling.Query;
import org.opentrafficsim.road.network.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.sampling.Trajectory;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TotalTravelDistance extends AbstractIndicator<LengthUnit, Length>
{

    /** {@inheritDoc} */
    @Override
    public final Length calculate(final Query query, final Duration startTime, final Duration endTime)
    {
        Length sum = Length.ZERO;
        for (TrajectoryGroup trajectoryGroup : query.getTrajectoryGroups(startTime, endTime))
        {
            for (Trajectory trajectory : trajectoryGroup.getTrajectories())
            {
                sum = sum.plus(trajectory.getTotalLength());
            }
        }
        return sum;
    }

    @SuppressWarnings("checkstyle:designforextension")
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TotalTravelDistance []";
    }

}
