package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeLength;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructure.Entry;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayStopLine;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.Conflict.ConflictEnd;
import org.opentrafficsim.road.network.lane.conflict.ConflictPriority;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.language.Throw;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class DirectIntersectionPerception extends LaneBasedAbstractPerceptionCategory implements IntersectionPerception
{

    /** */
    private static final long serialVersionUID = 20160811L;
    
    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Set of traffic lights. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayTrafficLight>>> trafficLights = new HashMap<>();

    /** Set of conflicts. */
    private Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayConflict>>> conflicts = new HashMap<>();

    /** Conflicts alongside left. */
    private TimeStampedObject<Boolean> alongsideConflictLeft;

    /** Conflicts alongside right. */
    private TimeStampedObject<Boolean> alongsideConflictRight;

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * @param perception perception
     * @param headwayGtuType type of headway gtu to generate
     */
    public DirectIntersectionPerception(final LanePerception perception, final HeadwayGtuType headwayGtuType)
    {
        super(perception);
        this.headwayGtuType = headwayGtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final void updateTrafficLights() throws GTUException, ParameterException
    {
        this.trafficLights.clear();
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection())
        {
            SortedSet<HeadwayTrafficLight> set = new TreeSet<>();
            this.trafficLights.put(lane, new TimeStampedObject<>(set, getTimestamp()));
            SortedSet<Entry<TrafficLight>> trafficLightEntries = getPerception().getLaneStructure().getDownstreamObjectsOnRoute(
                    lane, TrafficLight.class, getGtu(), RelativePosition.FRONT, getGtu().getStrategicalPlanner().getRoute());
            for (Entry<TrafficLight> trafficLightEntry : trafficLightEntries)
            {
                set.add(new HeadwayTrafficLight(trafficLightEntry.getLaneBasedObject(), trafficLightEntry.getDistance()));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateConflicts() throws GTUException, ParameterException
    {
        this.conflicts.clear();
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection())
        {
            SortedSet<HeadwayConflict> set = new TreeSet<>();
            this.conflicts.put(lane, new TimeStampedObject<>(set, getTimestamp()));
            SortedSet<Entry<Conflict>> conflictEntries = getPerception().getLaneStructure().getDownstreamObjectsOnRoute(lane,
                    Conflict.class, getGtu(), RelativePosition.FRONT, getGtu().getStrategicalPlanner().getRoute());

            // Also find splits and merges we are on, i.e. past the conflict object, so look for ConflictEnd
            SortedSet<Entry<ConflictEnd>> conflictEndEntries = getPerception().getLaneStructure().getDownstreamObjectsOnRoute(
                    lane, ConflictEnd.class, getGtu(), RelativePosition.FRONT, getGtu().getStrategicalPlanner().getRoute());
            Set<Conflict> confs = new HashSet<>();
            for (Entry<Conflict> entry : conflictEntries)
            {
                confs.add(entry.getLaneBasedObject());
            }
            for (Entry<ConflictEnd> entry : conflictEndEntries)
            {
                Conflict conflict = entry.getLaneBasedObject().getConflict();
                if (!confs.contains(conflict))
                {
                    conflictEntries.add(new Entry<>(entry.getDistance().minus(conflict.getLength()), conflict));
                }
            }

            for (Entry<Conflict> entry : conflictEntries)
            {
                Conflict conflict = entry.getLaneBasedObject();
                if (!getGtu().getGTUType().isOfType(conflict.getGtuType()))
                {
                    // conflict not for us
                    continue;
                }
                Conflict otherConflict = conflict.getOtherConflict();
                ConflictType conflictType = conflict.getConflictType();
                ConflictPriority conflictPriority = conflict.conflictPriority();
                Class<? extends ConflictRule> conflictRuleType = conflict.getConflictRule().getClass();
                String id = conflict.getId();
                Length distance = entry.getDistance();
                Length length = conflict.getLength();
                Length conflictingLength = otherConflict.getLength();
                CrossSectionLink conflictingLink = otherConflict.getLane().getParentLink();

                // TODO get from link combination (needs to be a map property on the links)
                Length lookAhead = getGtu().getBehavioralCharacteristics().getParameter(LOOKAHEAD);
                Length conflictingVisibility = lookAhead;
                Speed conflictingSpeedLimit;
                try
                {
                    conflictingSpeedLimit = otherConflict.getLane().getHighestSpeedLimit();
                }
                catch (NetworkException exception)
                {
                    throw new RuntimeException("GTU type not available on conflicting lane.", exception);
                }
                // TODO stop lines (current models happen not to use this, but should be possible)
                HeadwayStopLine stopLine = new HeadwayStopLine("stopLineId", Length.ZERO);
                HeadwayStopLine conflictingStopLine = new HeadwayStopLine("conflictingStopLineId", Length.ZERO);

                // UPSTREAM GTU'S AND TRAFFIC LIGHT
                Lane conflictingLane = otherConflict.getLane();
                LongitudinalDirectionality longDir = conflictingLane.getDirectionality(otherConflict.getGtuType());
                Throw.when(longDir.isBoth(), UnsupportedOperationException.class,
                        "Conflicts on lanes with direction BOTH are not supported.");
                GTUDirectionality conflictingDirection =
                        longDir.isForward() ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
                Length position = otherConflict.getLongitudinalPosition();
                Length initDistance =
                        conflictingDirection.isPlus() ? conflictingLane.getLength().minus(position).neg() : position.neg();
                SortedSet<HeadwayGTU> upstreamConflictingGTUs = new TreeSet<>();
                Set<LaneInfo> currentLanes = new HashSet<>();
                currentLanes.add(new LaneInfo(conflictingLane, conflictingDirection, initDistance, position,
                        otherConflict.getGtuType()));
                Length trafficLightDistance = null;
                while (!currentLanes.isEmpty())
                {
                    Set<LaneInfo> upLanes = new HashSet<>();
                    for (LaneInfo laneInfo : currentLanes)
                    {
                        // traffic light
                        if (trafficLightDistance == null)
                        {
                            List<LaneBasedObject> objects =
                                    laneInfo.getLane().getObjectBehind(laneInfo.getPosition(), laneInfo.getDirection());
                            while (objects != null)
                            {
                                for (LaneBasedObject object : objects)
                                {
                                    if (object instanceof TrafficLight)
                                    {
                                        Length increment = laneInfo.getDirection().isPlus()
                                                ? laneInfo.getLane().getLength().minus(object.getLongitudinalPosition())
                                                : object.getLongitudinalPosition();
                                        trafficLightDistance = laneInfo.getDistance().plus(increment);
                                        break;
                                    }
                                }
                                if (trafficLightDistance == null)
                                {
                                    objects = laneInfo.getLane().getObjectBehind(objects.get(0).getLongitudinalPosition(),
                                            laneInfo.getDirection());
                                }
                                else
                                {
                                    objects = null;
                                }
                            }
                        }

                        // GTU's
                        LaneBasedGTU next = laneInfo.getLane().getGtuBehind(laneInfo.getPosition(), laneInfo.getDirection(),
                                RelativePosition.FRONT, getTimestamp());
                        while (next != null)
                        {
                            Length nextPosition = next.position(laneInfo.getLane(), next.getFront());
                            Length increment = laneInfo.getDirection().isPlus()
                                    ? laneInfo.getLane().getLength().minus(nextPosition) : nextPosition;
                            Length nextDistance = laneInfo.getDistance().plus(increment);
                            if (nextDistance.le(lookAhead) && (trafficLightDistance == null || (laneInfo.getDirection().isPlus()
                                    ? trafficLightDistance.lt(nextPosition) : trafficLightDistance.gt(nextPosition))))
                            {
                                // TODO also other HeadwayGTU type (i.e. not real)
                                // TODO GTU status (blinkers)
                                if (!next.getId().equals(getGtu().getId()))
                                {
                                    // do not add self
                                    upstreamConflictingGTUs.add(this.headwayGtuType.createHeadwayGtu(next, nextDistance));
                                }
                                next = laneInfo.getLane().getGtuBehind(next.position(laneInfo.getLane(), next.getRear()),
                                        laneInfo.getDirection(), RelativePosition.FRONT, getTimestamp());
                            }
                            else
                            {
                                next = null;
                            }
                        }
                        upLanes.addAll(laneInfo.getUpstreamLaneInfos(lookAhead));
                    }
                    currentLanes = upLanes;
                }

                // DOWNSTREAM GTU'S
                initDistance =
                        conflictingDirection.isPlus() ? position.neg() : conflictingLane.getLength().minus(position).neg();
                SortedSet<HeadwayGTU> downstreamConflictingGTUs = new TreeSet<>();
                currentLanes = new HashSet<>();
                currentLanes.add(new LaneInfo(conflictingLane, conflictingDirection, initDistance, position,
                        otherConflict.getGtuType()));
                while (!currentLanes.isEmpty())
                {
                    Set<LaneInfo> downLanes = new HashSet<>();
                    for (LaneInfo laneInfo : currentLanes)
                    {
                        LaneBasedGTU next = laneInfo.getLane().getGtuAhead(laneInfo.getPosition(), laneInfo.getDirection(),
                                RelativePosition.FRONT, getTimestamp()); // we use front to find, but rear to calculate distance
                        while (next != null)
                        {
                            Length nextPosition = next.position(laneInfo.getLane(), next.getRear()); // rear for distance
                            Length increment = laneInfo.getDirection().isPlus() ? nextPosition
                                    : laneInfo.getLane().getLength().minus(nextPosition);
                            Length nextDistance = laneInfo.getDistance().plus(increment);
                            if (nextDistance.le(lookAhead))
                            {
                                // TODO also other HeadwayGTU type (i.e. not real)
                                // TODO GTU status (blinkers)
                                HeadwayGTU gtu;
                                if (nextDistance.ge(otherConflict.getLength()))
                                {
                                    gtu = this.headwayGtuType.createHeadwayGtu(next,
                                            nextDistance.minus(otherConflict.getLength()));
                                }
                                else
                                {
                                    // adjacent with (i.e. on) conflict
                                    Length overlapFront = nextDistance.plus(next.getLength()).minus(otherConflict.getLength());
                                    Length overlapRear = nextDistance;
                                    Length overlap = otherConflict.getLength(); // start with conflict length
                                    if (overlapFront.lt0())
                                    {
                                        overlap = overlap.plus(overlapFront); // subtract front being before the conflict end
                                    }
                                    if (overlapRear.gt0())
                                    {
                                        overlap = overlap.minus(overlapRear); // subtract rear being past the conflict start
                                    }
                                    gtu = this.headwayGtuType.createHeadwayGtu(next, overlapFront, overlap, overlapRear);
                                }
                                if (!next.getId().equals(getGtu().getId()))
                                {
                                    // do not add self
                                    downstreamConflictingGTUs.add(gtu);
                                }
                                next = laneInfo.getLane().getGtuAhead(next.position(laneInfo.getLane(), next.getFront()),
                                        laneInfo.getDirection(), RelativePosition.FRONT, getTimestamp());
                            }
                            else
                            {
                                next = null;
                            }
                        }
                        downLanes.addAll(laneInfo.getDownstreamLaneInfos(lookAhead));
                    }
                    currentLanes = downLanes;
                }

                // add conflict to set
                HeadwayConflict headwayConflict = new HeadwayConflict(conflictType, conflictPriority, conflictRuleType, id,
                        distance, length, conflictingLength, upstreamConflictingGTUs, downstreamConflictingGTUs,
                        conflictingVisibility, conflictingSpeedLimit, conflictingLink, stopLine, conflictingStopLine);
                set.add(headwayConflict);
                if (trafficLightDistance != null && trafficLightDistance.le(lookAhead))
                {
                    headwayConflict.setConflictingTrafficLight(trafficLightDistance, conflict.isPermitted());
                }
            }
        }

        // alongside
        for (RelativeLane lane : new RelativeLane[] { RelativeLane.LEFT, RelativeLane.RIGHT })
        {
            if (getPerception().getLaneStructure().getCrossSection().contains(lane))
            {
                SortedSet<Entry<Conflict>> conflictEntries = getPerception().getLaneStructure().getUpstreamObjects(lane,
                        Conflict.class, getGtu(), RelativePosition.FRONT);
                boolean alongside = false;
                if (!conflictEntries.isEmpty())
                {
                    Entry<Conflict> entry = conflictEntries.first();
                    alongside = entry.getDistance().si < entry.getLaneBasedObject().getLength().si + getGtu().getLength().si;

                }
                if (lane.isLeft())
                {
                    this.alongsideConflictLeft = new TimeStampedObject<>(alongside, getTimestamp());
                }
                else
                {
                    this.alongsideConflictRight = new TimeStampedObject<>(alongside, getTimestamp());
                }
            }
        }
    }

    /**
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static final class LaneInfo
    {

        /** Lane. */
        private final Lane lane;

        /** GTU direction. */
        private final GTUDirectionality direction;

        /** Distance so far to start or end of lane. */
        private final Length distance;

        /** Position to search from (i.e. conflict location, start or end of lane). */
        private final Length position;

        /** GTU type. */
        private final GTUType gtuType;

        /**
         * @param lane lane
         * @param direction GTU direction
         * @param distance distance so far to start or end of lane
         * @param position position to search from (i.e. conflict location, start or end of lane)
         * @param gtuType GTU type
         */
        LaneInfo(final Lane lane, final GTUDirectionality direction, final Length distance, final Length position,
                final GTUType gtuType)
        {
            this.lane = lane;
            this.direction = direction;
            this.distance = distance;
            this.position = position;
            this.gtuType = gtuType;
        }

        /**
         * @return lane.
         */
        public Lane getLane()
        {
            return this.lane;
        }

        /**
         * @return direction.
         */
        public GTUDirectionality getDirection()
        {
            return this.direction;
        }

        /**
         * @return distance.
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /**
         * @return position.
         */
        public Length getPosition()
        {
            return this.position;
        }

        /**
         * @param maxDistance maximum search distance
         * @return set of upstream lane info's
         */
        public Set<LaneInfo> getUpstreamLaneInfos(final Length maxDistance)
        {
            // TODO use set of gtu types that may be conflicting
            Length nextDistance = this.distance.plus(this.lane.getLength());
            Set<LaneInfo> set = new HashSet<>();
            if (nextDistance.le(maxDistance))
            {
                Map<Lane, GTUDirectionality> map = this.lane.upstreamLanes(this.direction, this.gtuType);
                if (!map.isEmpty())
                {
                    for (Lane l : map.keySet())
                    {
                        GTUDirectionality nextDirection = map.get(l);
                        Length nextLocation = nextDirection.isPlus() ? l.getLength() : Length.ZERO;
                        set.add(new LaneInfo(l, nextDirection, nextDistance, nextLocation, this.gtuType));
                    }
                }
            }
            return set;
        }

        /**
         * @param maxDistance maximum search distance
         * @return set of downstream lane info's
         */
        public Set<LaneInfo> getDownstreamLaneInfos(final Length maxDistance)
        {
            // TODO use set of gtu types that may be conflicting
            Length nextDistance = this.distance.plus(this.lane.getLength());
            Set<LaneInfo> set = new HashSet<>();
            if (nextDistance.le(maxDistance))
            {
                Map<Lane, GTUDirectionality> map = this.lane.downstreamLanes(this.direction, this.gtuType);
                if (!map.isEmpty())
                {
                    for (Lane l : map.keySet())
                    {
                        GTUDirectionality nextDirection = map.get(l);
                        Length nextLocation = nextDirection.isPlus() ? Length.ZERO : l.getLength();
                        set.add(new LaneInfo(l, nextDirection, nextDistance, nextLocation, this.gtuType));
                    }
                }
            }
            return set;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.direction == null) ? 0 : this.direction.hashCode());
            result = prime * result + ((this.distance == null) ? 0 : this.distance.hashCode());
            result = prime * result + ((this.gtuType == null) ? 0 : this.gtuType.hashCode());
            result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
            result = prime * result + ((this.position == null) ? 0 : this.position.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            LaneInfo other = (LaneInfo) obj;
            if (this.direction != other.direction)
            {
                return false;
            }
            if (this.distance == null)
            {
                if (other.distance != null)
                {
                    return false;
                }
            }
            else if (!this.distance.equals(other.distance))
            {
                return false;
            }
            if (this.gtuType == null)
            {
                if (other.gtuType != null)
                {
                    return false;
                }
            }
            else if (!this.gtuType.equals(other.gtuType))
            {
                return false;
            }
            if (this.lane == null)
            {
                if (other.lane != null)
                {
                    return false;
                }
            }
            else if (!this.lane.equals(other.lane))
            {
                return false;
            }
            if (this.position == null)
            {
                if (other.position != null)
                {
                    return false;
                }
            }
            else if (!this.position.equals(other.position))
            {
                return false;
            }
            return true;
        }

    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayTrafficLight> getTrafficLights(final RelativeLane lane)
    {
        return getObjectOrNull(this.trafficLights.get(lane));
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayConflict> getConflicts(final RelativeLane lane)
    {
        return getObjectOrNull(this.conflicts.get(lane));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAlongsideConflictLeft()
    {
        return this.alongsideConflictLeft == null ? false : this.alongsideConflictLeft.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isAlongsideConflictRight()
    {
        return this.alongsideConflictRight == null ? false : this.alongsideConflictRight.getObject();
    }

    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<SortedSet<HeadwayTrafficLight>> getTimeStampedTrafficLights(final RelativeLane lane)
    {
        return this.trafficLights.get(lane);
    }

    /**
     * Returns a time stamped set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    public final TimeStampedObject<SortedSet<HeadwayConflict>> getTimeStampedConflicts(final RelativeLane lane)
    {
        return this.conflicts.get(lane);
    }

    /**
     * Returns whether there is a conflict alongside to the left, time stamped.
     * @return whether there is a conflict alongside to the left
     */
    public final TimeStampedObject<Boolean> isAlongsideConflictLeftTimeStamped()
    {
        return this.alongsideConflictLeft;
    }

    /**
     * Returns whether there is a conflict alongside to the right, time stamped.
     * @return whether there is a conflict alongside to the right
     */
    public final TimeStampedObject<Boolean> isAlongsideConflictRightTimeStamped()
    {
        return this.alongsideConflictRight;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "DirectIntersectionPerception";
    }

}
