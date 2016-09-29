package org.opentrafficsim.road.network.sampling;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;

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
public class MeanSpeed implements Indicator<SpeedUnit, Speed>
{

    /** Travel distance indicator. */
    private final TravelDistance travelDistance;

    /** Travel time indicator. */
    private final TravelTime travelTime;

    /**
     * @param travelDistance travel distance indicator
     * @param travelTime travel time indicator
     */
    public MeanSpeed(final TravelDistance travelDistance, final TravelTime travelTime)
    {
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed calculate(final Query query, final Duration startTime, final Duration endTime)
    {
        // TODO let indicator not recalculate if requested at the same time
        return this.travelDistance.calculate(query, startTime, endTime).divideBy(
            this.travelTime.calculate(query, startTime, endTime));
    }

}
