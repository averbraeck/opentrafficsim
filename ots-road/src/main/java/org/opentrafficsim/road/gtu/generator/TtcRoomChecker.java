package org.opentrafficsim.road.gtu.generator;

import java.util.SortedSet;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.network.LanePosition;

/**
 * Room checker based on time-to-collision. The room is considered ok if:
 * <ol>
 * <li>The headway is larger than speed*1.0s + 3m</li>
 * <li>time-to-collision &lt; value
 * </ol>
 * where 'value' is a given value in the constructor.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TtcRoomChecker implements RoomChecker
{

    /** Time to collision. */
    private final Duration ttc;

    /**
     * Constructor.
     * @param ttc time to collision
     */
    public TtcRoomChecker(final Duration ttc)
    {
        this.ttc = ttc;
    }

    @Override
    public final Placement canPlace(final SortedSet<PerceivedGtu> leaders, final LaneBasedGtuCharacteristics characteristics,
            final Duration since, final LanePosition initialPosition) throws NetworkException, GtuException
    {
        Speed speedLimit = initialPosition.lane().getSpeedLimit(characteristics.getGtuType());
        Speed desiredSpeedProxy = Speed.min(characteristics.getMaximumSpeed(), speedLimit);
        if (leaders.isEmpty())
        {
            return new Placement(desiredSpeedProxy, initialPosition);
        }
        PerceivedGtu leader = leaders.first();
        Speed speed = Speed.min(leader.getSpeed(), desiredSpeedProxy);
        if ((speed.le(leader.getSpeed()) || leader.getDistance().divide(speed.minus(leader.getSpeed())).gt(this.ttc)) && leader
                .getDistance().gt(speed.times(new Duration(1.0, DurationUnit.SI)).plus(new Length(3.0, LengthUnit.SI))))
        {
            return new Placement(speed, initialPosition);
        }
        return Placement.NO;
    }

    /**
     * Returns the TTC value.
     * @return TTC value
     */
    public final Duration getTtc()
    {
        return this.ttc;
    }

    @Override
    public final String toString()
    {
        return "TTCRoomChecker [ttc=" + this.ttc + "]";
    }

}
