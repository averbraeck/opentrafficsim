package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of measurements with zero speed, preceded by a non-zero speed.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
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
    protected <G extends GtuDataInterface> Dimensionless calculate(final Query<G> query, final Time startTime,
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
