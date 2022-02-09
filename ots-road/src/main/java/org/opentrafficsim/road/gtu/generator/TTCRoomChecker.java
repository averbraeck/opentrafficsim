package org.opentrafficsim.road.gtu.generator;

import java.util.Set;
import java.util.SortedSet;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Room checker based on time-to-collision. The room is considered ok if:
 * <ol>
 * <li>The headway is larger than speed*1.0s + 3m</li>
 * <li>time-to-collision &lt; value
 * </ol>
 * where 'value' is a given value in the constructor.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param ttc Duration; time to collision
     */
    public TTCRoomChecker(final Duration ttc)
    {
        this.ttc = ttc;
    }

    /** {@inheritDoc} */
    @Override
    public final Placement canPlace(final SortedSet<HeadwayGTU> leaders, final LaneBasedGTUCharacteristics characteristics,
            final Duration since, final Set<DirectedLanePosition> initialPosition) throws NetworkException, GTUException
    {
        Speed speedLimit = initialPosition.iterator().next().getLane().getSpeedLimit(characteristics.getGTUType());
        Speed desiredSpeedProxy = Speed.min(characteristics.getMaximumSpeed(), speedLimit);
        if (leaders.isEmpty())
        {
            return new Placement(desiredSpeedProxy, initialPosition);
        }
        HeadwayGTU leader = leaders.first();
        Speed speed = Speed.min(leader.getSpeed(), desiredSpeedProxy);
        for (DirectedLanePosition dlp : initialPosition)
        {
            if (dlp.getLane().getLaneType().isCompatible(characteristics.getGTUType(), dlp.getGtuDirection()))
            {
                speed = Speed.min(speed, dlp.getLane().getSpeedLimit(characteristics.getGTUType()));
            }
        }
        if ((speed.le(leader.getSpeed()) || leader.getDistance().divide(speed.minus(leader.getSpeed())).gt(this.ttc))
                && leader.getDistance()
                        .gt(speed.times(new Duration(1.0, DurationUnit.SI)).plus(new Length(3.0, LengthUnit.SI))))
        {
            return new Placement(speed, initialPosition);
        }
        return Placement.NO;
    }
    
    /**
     * Returns the TTC value.
     * @return Duration; TTC value
     */
    public final Duration getTtc()
    {
        return this.ttc;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TTCRoomChecker [ttc=" + this.ttc + "]";
    }

}
