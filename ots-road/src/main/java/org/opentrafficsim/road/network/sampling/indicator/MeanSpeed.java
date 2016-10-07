package org.opentrafficsim.road.network.sampling.indicator;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.road.network.sampling.Query;

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
public class MeanSpeed extends AbstractIndicator<SpeedUnit, Speed>
{

    /** Travel distance indicator. */
    private final TotalTravelDistance travelDistance;

    /** Travel time indicator. */
    private final TotalTravelTime travelTime;

    /**
     * @param travelDistance travel distance indicator
     * @param travelTime travel time indicator
     */
    public MeanSpeed(final TotalTravelDistance travelDistance, final TotalTravelTime travelTime)
    {
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed calculate(final Query query, final Duration startTime, final Duration endTime)
    {
        return this.travelDistance.getValue(query, startTime, endTime).divideBy(
            this.travelTime.getValue(query, startTime, endTime));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "MeanSpeed [travelDistance=" + this.travelDistance + ", travelTime=" + this.travelTime + "]";
    }

}
