package org.opentrafficsim.road.network.sampling.indicator;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.road.network.sampling.Query;
import org.opentrafficsim.road.network.sampling.Trajectory;
import org.opentrafficsim.road.network.sampling.TrajectoryGroup;

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
public class TotalNumberOfStops extends AbstractIndicator<Dimensionless>
{

    /** {@inheritDoc} */
    @Override
    public final Dimensionless calculate(final Query query, final Time startTime, final Time endTime)
    {
        int sum = 0;
        for (TrajectoryGroup trajectoryGroup : query.getTrajectoryGroups(startTime, endTime))
        {
            for (Trajectory trajectory : trajectoryGroup.getTrajectories())
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
