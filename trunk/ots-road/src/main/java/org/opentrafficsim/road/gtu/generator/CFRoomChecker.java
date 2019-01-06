package org.opentrafficsim.road.gtu.generator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This class places GTU's behind the leader at the desired headway (i.e. CF, car-following) and the speed of the leader, but no
 * further than the GTU could have traveled at the desired speed during the time since the desired arrival. With multiple
 * leaders, the leader that causes the most upstream following position is used.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CFRoomChecker implements RoomChecker
{

    /** {@inheritDoc} */
    @Override
    public Placement canPlace(final SortedSet<HeadwayGTU> leaders, final LaneBasedGTUCharacteristics characteristics,
            final Duration since, final Set<DirectedLanePosition> initialPosition) throws NetworkException, GTUException
    {
        Speed speedLimit = null;
        for (DirectedLanePosition lane : initialPosition)
        {
            try
            {
                Speed limit = lane.getLane().getSpeedLimit(characteristics.getGTUType());
                if (speedLimit == null || limit.gt(speedLimit))
                {
                    speedLimit = limit;
                }
            }
            catch (NetworkException exception)
            {
                // ignore
            }
        }
        Throw.when(speedLimit == null, IllegalStateException.class, "No speed limit could be determined for GTUType %s.",
                characteristics.getGTUType());
        Speed desiredSpeed = characteristics.getStrategicalPlannerFactory().peekDesiredSpeed(characteristics.getGTUType(),
                speedLimit, characteristics.getMaximumSpeed());
        desiredSpeed = desiredSpeed != null ? desiredSpeed : speedLimit; // speed limit def.
        if (leaders.isEmpty())
        {
            // no leaders: free
            return new Placement(desiredSpeed, initialPosition);
        }
        Length desiredHeadway =
                characteristics.getStrategicalPlannerFactory().peekDesiredHeadway(characteristics.getGTUType(), desiredSpeed);
        desiredHeadway = desiredHeadway != null ? desiredHeadway : desiredSpeed.multiplyBy(Duration.createSI(1.0)); // 1s def.
        // loop leaders and determine most downstream location that would be ok
        Length move = Length.POSITIVE_INFINITY;
        Speed generationSpeed = desiredSpeed;
        for (HeadwayGTU leader : leaders)
        {
            Speed speed = Speed.min(desiredSpeed, leader.getSpeed());
            Length headway =
                    characteristics.getStrategicalPlannerFactory().peekDesiredHeadway(characteristics.getGTUType(), speed);
            headway = headway != null ? headway : speed.multiplyBy(Duration.createSI(1.0)); // 1s def.
            double f = this.headwayFactor(desiredSpeed, desiredHeadway, speed, headway, leader.getLength());
            headway = headway.multiplyBy(f);
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
        move = Length.min(move, since.multiplyBy(generationSpeed)); // max distance the GTU would have moved until now
        // move this distance
        Set<DirectedLanePosition> generationPosition;
        if (move.eq0() || initialPosition.size() != 1)
        {
            generationPosition = initialPosition;
        }
        else
        {
            generationPosition = new HashSet<>();
            for (DirectedLanePosition dirPos : initialPosition)
            {
                Lane lane = dirPos.getLane();
                GTUDirectionality dir = dirPos.getGtuDirection();
                Length position = dirPos.getPosition();
                Length canMove = dir.isPlus() ? lane.getLength().minus(position) : position;
                while (canMove.lt(move))
                {
                    Map<Lane, GTUDirectionality> down = lane.downstreamLanes(dir, characteristics.getGTUType());
                    if (down.size() != 1)
                    {
                        // split or dead-end, fall back to original position
                        return new Placement(generationSpeed, initialPosition);
                    }
                    else
                    {
                        move = move.minus(canMove);
                        lane = down.keySet().iterator().next();
                        dir = down.get(lane);
                        position = dir.isPlus() ? Length.ZERO : lane.getLength();
                        canMove = lane.getLength();
                    }
                }
                position = dir.isPlus() ? position.plus(move) : position.minus(move);
                generationPosition.add(new DirectedLanePosition(lane, position, dir));
            }
        }
        return new Placement(generationSpeed, generationPosition);
    }

    /**
     * Returns a situation dependent headway factor to deal with spillback.
     * @param desiredSpeed Speed; desired speed
     * @param desiredHeadway Length; desired headway at desired speed
     * @param generationSpeed Speed; generation speed
     * @param generationHeadway Length; desired headway at generation speed
     * @param leaderLength Length; length of the leader
     * @return situation dependent headway factor to deal with spillback
     */
    protected double headwayFactor(final Speed desiredSpeed, final Length desiredHeadway, final Speed generationSpeed,
            final Length generationHeadway, final Length leaderLength)
    {
        return 1.0;
    }

}
