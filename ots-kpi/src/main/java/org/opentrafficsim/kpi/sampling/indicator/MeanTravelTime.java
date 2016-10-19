package org.opentrafficsim.kpi.sampling.indicator;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Sum of (approximate) link lengths divided by mean speed. 
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MeanTravelTime extends AbstractIndicator<Duration>
{

    /** Mean speed indicator. */
    private final MeanSpeed meanSpeed;

    /**
     * @param meanSpeed mean speed indicator
     */
    public MeanTravelTime(final MeanSpeed meanSpeed)
    {
        this.meanSpeed = meanSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final Duration calculate(final Query query, final Time startTime, final Time endTime)
    {
        Length cumulLength = Length.ZERO;
        Set<LinkDataInterface> links = new HashSet<>();
        for (TrajectoryGroup trajectoryGroup : query.getTrajectoryGroups(startTime, endTime))
        {
            if (!links.contains(trajectoryGroup.getLaneDirection().getLaneData().getLinkData()))
            {
                cumulLength = cumulLength.plus(trajectoryGroup.getLength()); // TODO should be average lane length of link
                links.add(trajectoryGroup.getLaneDirection().getLaneData().getLinkData());
            }
        }
        return cumulLength.divideBy(this.meanSpeed.getValue(query, startTime, endTime));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "MeanTravelTime [meanSpeed=" + this.meanSpeed + "]";
    }

}
