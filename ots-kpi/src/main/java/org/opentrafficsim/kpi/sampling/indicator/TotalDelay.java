package org.opentrafficsim.kpi.sampling.indicator;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of trajectory durations minus the sum of trajectory lengths divided by a reference speed.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO something better than a global reference speed defined at the indicator
public class TotalDelay extends AbstractIndicator<Duration>
{

    /** Reference speed for delay. */
    private final Speed referenceSpeed;

    /**
     * @param referenceSpeed Speed; reference speed for delay
     */
    public TotalDelay(final Speed referenceSpeed)
    {
        this.referenceSpeed = referenceSpeed;
    }

    /** {@inheritDoc} */
    @Override
    protected <G extends GtuDataInterface> Duration calculate(final Query<G> query, final Time startTime, final Time endTime,
            final List<TrajectoryGroup<G>> trajectoryGroups)
    {
        Duration sumTime = Duration.ZERO;
        Length sumDist = Length.ZERO;
        for (TrajectoryGroup<?> trajectoryGroup : trajectoryGroups)
        {
            // TODO: use data points and limit speed per interval
            for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
            {
                sumTime = sumTime.plus(trajectory.getTotalDuration());
                sumDist = sumDist.plus(trajectory.getTotalLength());
            }
        }
        return sumTime.minus(sumDist.divide(this.referenceSpeed));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TotalDelay [referenceSpeed=" + this.referenceSpeed + "]";
    }

}
