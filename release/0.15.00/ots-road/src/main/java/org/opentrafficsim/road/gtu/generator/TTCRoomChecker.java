package org.opentrafficsim.road.gtu.generator;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Room checker based on time-to-collision. The room is considered ok if:
 * <ol>
 * <li>The headway is larger than speed*1.0s + 3m</li>
 * <li>time-to-collision &lt; value
 * </ol>
 * where 'value' is a given value in the constructor.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TTCRoomChecker implements RoomChecker
{

    /** Time to collision. */
    private final Duration ttc;

    /**
     * Constructor.
     * @param ttc time to collision
     */
    public TTCRoomChecker(final Duration ttc)
    {
        this.ttc = ttc;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed canPlace(final Speed leaderSpeed, final Length headway,
            final LaneBasedGTUCharacteristics laneBasedGTUCharacteristics) throws NetworkException
    {
        Speed speed = Speed.min(leaderSpeed, laneBasedGTUCharacteristics.getMaximumSpeed());
        for (DirectedLanePosition dlp : laneBasedGTUCharacteristics.getInitialLongitudinalPositions())
        {
            if (dlp.getLane().getLaneType().isCompatible(laneBasedGTUCharacteristics.getGTUType()))
            {
                speed = Speed.min(speed, dlp.getLane().getSpeedLimit(laneBasedGTUCharacteristics.getGTUType()));
            }
        }
        if ((speed.le(leaderSpeed) || headway.divideBy(speed.minus(leaderSpeed)).gt(this.ttc))
                && headway.gt(speed.multiplyBy(new Duration(1.0, DurationUnit.SI)).plus(new Length(3.0, LengthUnit.SI))))
        {
            return speed;
        }
        return null;
    }

}
