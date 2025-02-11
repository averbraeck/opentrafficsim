package org.opentrafficsim.road.gtu.generator;

import java.util.Set;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * This class places GTU's behind the leader at the desired headway (i.e. CF, car-following) and the speed of the leader, but no
 * further than the GTU could have traveled at the desired speed during the time since the desired arrival. With multiple
 * leaders, the leader that causes the most upstream following position is used.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CfRoomChecker implements RoomChecker
{

    /**
     * Constructor.
     */
    public CfRoomChecker()
    {
        //
    }

    @Override
    public Placement canPlace(final SortedSet<HeadwayGtu> leaders, final LaneBasedGtuCharacteristics characteristics,
            final Duration since, final LanePosition initialPosition) throws NetworkException, GtuException
    {
        Speed speedLimit = initialPosition.lane().getSpeedLimit(characteristics.getGtuType());
        Throw.when(speedLimit == null, IllegalStateException.class, "No speed limit could be determined for GtuType %s.",
                characteristics.getGtuType());
        Speed desiredSpeed = characteristics.getStrategicalPlannerFactory().peekDesiredSpeed(characteristics.getGtuType(),
                speedLimit, characteristics.getMaximumSpeed());
        desiredSpeed = desiredSpeed != null ? desiredSpeed : speedLimit; // speed limit def.
        if (leaders.isEmpty())
        {
            // no leaders: free
            return new Placement(desiredSpeed, initialPosition);
        }
        Length desiredHeadway =
                characteristics.getStrategicalPlannerFactory().peekDesiredHeadway(characteristics.getGtuType(), desiredSpeed);
        desiredHeadway = desiredHeadway != null ? desiredHeadway : desiredSpeed.times(Duration.instantiateSI(1.0)); // 1s def.
        // loop leaders and determine most downstream location that would be ok
        Length move = Length.POSITIVE_INFINITY;
        Speed generationSpeed = desiredSpeed;
        for (HeadwayGtu leader : leaders)
        {
            Speed speed = Speed.min(desiredSpeed, leader.getSpeed());
            Length headway =
                    characteristics.getStrategicalPlannerFactory().peekDesiredHeadway(characteristics.getGtuType(), speed);
            headway = headway != null ? headway : speed.times(Duration.instantiateSI(1.0)); // 1s def.
            double f = this.headwayFactor(desiredSpeed, desiredHeadway, speed, headway, leader.getLength());
            headway = headway.times(f);
            if (leader.getDistance().lt(headway))
            {
                // not enough space to this leader
                return Placement.NO;
            }
            Length moveToLeader = leader.getDistance().minus(headway);
            if (moveToLeader.lt(move))
            {
                move = moveToLeader;
                generationSpeed = speed;
            }
        }
        move = Length.min(move, since.times(generationSpeed)); // max distance the GTU would have moved until now
        // move this distance
        LanePosition generationPosition;
        if (move.eq0())
        {
            generationPosition = initialPosition;
        }
        else
        {
            Lane lane = initialPosition.lane();
            Length position = initialPosition.position();
            Length canMove = lane.getLength().minus(position);
            while (canMove.lt(move))
            {
                Set<Lane> down = lane.nextLanes(characteristics.getGtuType());
                if (down.size() != 1)
                {
                    // split or dead-end, fall back to original position
                    return new Placement(generationSpeed, initialPosition);
                }
                else
                {
                    move = move.minus(canMove);
                    lane = down.iterator().next();
                    position = Length.ZERO;
                    canMove = lane.getLength();
                }
            }
            position = position.plus(move);
            generationPosition = new LanePosition(lane, position);
        }
        return new Placement(generationSpeed, generationPosition);
    }

    /**
     * Returns a situation dependent headway factor to deal with spillback.
     * @param desiredSpeed desired speed
     * @param desiredHeadway desired headway at desired speed
     * @param generationSpeed generation speed
     * @param generationHeadway desired headway at generation speed
     * @param leaderLength length of the leader
     * @return situation dependent headway factor to deal with spillback
     */
    protected double headwayFactor(final Speed desiredSpeed, final Length desiredHeadway, final Speed generationSpeed,
            final Length generationHeadway, final Length leaderLength)
    {
        return 1.0;
    }

}
