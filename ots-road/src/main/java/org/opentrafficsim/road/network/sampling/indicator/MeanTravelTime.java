package org.opentrafficsim.road.network.sampling.indicator;

import java.util.HashSet;
import java.util.Set;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.network.sampling.Query;
import org.opentrafficsim.road.network.sampling.Trajectories;

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
public class MeanTravelTime extends AbstractIndicator<TimeUnit, Duration>
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
    public final Duration calculate(final Query query, final Duration startTime, final Duration endTime)
    {
        Length cumulLength = Length.ZERO;
        Set<Link> links = new HashSet<>();
        for (Trajectories trajectories : query.getTrajectories(startTime, endTime))
        {
            if (!links.contains(trajectories.getLaneDirection().getLane().getParentLink()))
            {
                cumulLength = cumulLength.plus(trajectories.getLength()); // TODO should be average lane length of link
                links.add(trajectories.getLaneDirection().getLane().getParentLink());
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
