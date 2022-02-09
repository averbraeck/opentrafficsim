package org.opentrafficsim.road.gtu.lane.tactical.routesystem;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableMap.ImmutableEntry;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Default route system. This system stores a set for each combination of a {@code Lane}, {@code GTUDirectionality},
 * {@code Route} and {@code GTUType}. The set provides route information over a given distance beyond the end of the lane. If
 * more distance is required, the set is recalculated. If less is required, a subset is returned. Distances in the returned
 * route information are adjusted for the specific position of the GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 7 nov. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultRouteSystem implements RouteSystem
{

    /** Cache. */
    private MultiKeyMap<LaneChangeInfoSet> cache = new MultiKeyMap<>(Lane.class, GTUDirectionality.class, Route.class,
        GTUType.class);

    /** {@inheritDoc} */
    @Override
    public SortedSet<LaneChangeInfo> getLaneChangeInfo(final DirectedLanePosition position, final Length front,
            final Route route, final GTUType gtuType, final Length distance)
    {
        /*
        // obtain set
        LaneChangeInfoSet set = this.cache.get(() -> determineSet(position, route, gtuType, distance), position.getLane(),
            position.getGtuDirection(), route, gtuType);
        // check if previous search was far enough
        if (set.suppliesDistance(distance))
        {
            // far enough, return info with distances adjusted to GTU position
            Length pos = position.getGtuDirection().isPlus() ? position.getPosition() : position.getLane().getLength().minus(
                position.getPosition());
            return set.getAdjustedSet(pos, distance);
        }
        */
        // previously calculated set does not have sufficient length, clear and recalculate
        this.cache.clear(position.getLane(), position.getGtuDirection(), route, gtuType);
        return getLaneChangeInfo(position, front, route, gtuType, distance);
    }

    /**
     * Determines a set of lane change info.
     * @param position DirectedLanePosition; position
     * @param front Length; distance required for the front (relative to reference position)
     * @param route Route; route, may be {@code null}
     * @param gtuType GTUType; GTU type
     * @param distance Length; distance over which required lane changes are desired to be known
     * @return SortedSet&lt;LaneChangeInfo&gt;; lane change information
     * @throws GTUException in case of multiple adjacent lanes
     */
    private LaneChangeInfoSet determineSet(final DirectedLanePosition position, final Length front, final Route route,
            final GTUType gtuType, final Length distance) throws GTUException
    {
        // the search can stop as soon as all lanes are reachable on a link that starts beyond 'distance' of the end of the lane

        Map<LaneRecord, LaneChangeInfo> currentSet = new LinkedHashMap<>();
        Map<LaneRecord, LaneChangeInfo> nextSet = new LinkedHashMap<>();
        Length startDistance = (position.getGtuDirection().isPlus() ? position.getPosition() : position.getLane().getLength()
            .minus(position.getPosition())).plus(front).neg();
        LaneRecord record = new LaneRecord(startDistance, position.getLane(), position.getGtuDirection());
        Length remainingDistance = position.getLane().getLength().minus(startDistance);
        currentSet.put(record, new LaneChangeInfo(0, remainingDistance, record.isDeadEnd(gtuType),
            LateralDirectionality.NONE));

        while (!currentSet.isEmpty())
        {
            // move lateral
            nextSet.putAll(currentSet);
            for (LateralDirectionality lat : new LateralDirectionality[] {LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT})
            {
                for (LaneRecord laneRecord : currentSet.keySet())
                {
                    laneRecord = lat.isLeft() ? laneRecord.left(gtuType) : laneRecord.right(gtuType);
                    if (!currentSet.containsKey(laneRecord))
                    {
                        LaneChangeInfo info = currentSet.get(laneRecord);
                        LaneChangeInfo adjInfo = new LaneChangeInfo(info.getNumberOfLaneChanges() + 1, remainingDistance,
                            laneRecord.isDeadEnd(gtuType), lat);
                        nextSet.put(laneRecord, adjInfo);
                    }
                }
            }
        }

        return new LaneChangeInfoSet(null, null);
    }

    /**
     * Set of lane change info, that can be adjusted for a specific GTU position on the origin lane.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Feb 11, 2020 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */
    private class LaneChangeInfoSet
    {

        /** Distance of info that this set can supply. */
        private final Length dist;

        /** Set with info. */
        private final SortedSet<LaneChangeInfo> set;

        /**
         * @param distance Length; distance within which the info is gathered
         * @param set SortedSet&lt;LaneChangeInfo&gt;; set with info
         */
        LaneChangeInfoSet(final Length distance, final SortedSet<LaneChangeInfo> set)
        {
            this.dist = distance;
            this.set = set;
        }

        /**
         * Returns whether this set can supply up to the requested distance.
         * @param distance Length; requested distance of info
         * @return boolean; whether this set can supply up to the requested distance
         */
        public boolean suppliesDistance(final Length distance)
        {
            return distance.le(this.dist);
        }

        /**
         * Returns the set of information with distances adjusted to the GTU position.
         * @param position Length; position of the GTU on the origin lane
         * @param distance Length; maximum distance of included info
         * @return SortedSet&lt;LaneChangeInfo&gt;; set of information with distances adjusted to the GTU position
         */
        public SortedSet<LaneChangeInfo> getAdjustedSet(final Length position, final Length distance)
        {
            SortedSet<LaneChangeInfo> adjustedSet = new TreeSet<>();
            for (LaneChangeInfo info : this.set)
            {
                Length adjustedDistance = info.getRemainingDistance().minus(position);
                if (adjustedDistance.le(distance))
                {
                    adjustedSet.add(new LaneChangeInfo(info.getNumberOfLaneChanges(), adjustedDistance, info.deadEnd(), info
                        .getLateralDirectionality()));
                }
                else
                {
                    // we can stop, further info is beyond the requested scope
                    break;
                }
            }
            return adjustedSet;
        }

    }

    /**
     * Helper class to aid route algorithms.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 feb. 2020 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class LaneRecord
    {

        /** Start distance. */
        private final Length startDistance;

        /** Lane. */
        private final Lane lane;

        /** Direction of travel. */
        private final GTUDirectionality direction;

        /** Previous record. */
        private LaneRecord prev;

        /** Left record. */
        private LaneRecord left;

        /** Whether the left records was determined. */
        private boolean determinedLeft = false;

        /** Right record. */
        private LaneRecord right;

        /** Whether the right records was determined. */
        private boolean determinedRight = false;

        /**
         * @param startDistance Length; start distance
         * @param lane Lane; lane
         * @param direction GTUDirectionality; direction of travel
         */
        LaneRecord(final Length startDistance, final Lane lane, final GTUDirectionality direction)
        {
            this.startDistance = startDistance;
            this.lane = lane;
            this.direction = direction;
        }

        /**
         * Returns a set of next lanes along the route. This set may be empty. Note that this does not mean the lane can be
         * considered a dead-end, as the route is considered.
         * @param route Route route;
         * @param gtuType GTUType; gtuType
         * @return set of next lanes, which may be empty.
         */
        public Set<LaneRecord> next(final Route route, final GTUType gtuType)
        {
            Set<LaneRecord> set = new LinkedHashSet<>();
            ImmutableMap<Lane, GTUDirectionality> lanes = this.lane.downstreamLanes(this.direction, gtuType);
            for (ImmutableEntry<Lane, GTUDirectionality> entry : lanes.entrySet())
            {
                // check route
                Node from;
                Node to;
                if (entry.getValue().isPlus())
                {
                    from = entry.getKey().getParentLink().getStartNode();
                    to = entry.getKey().getParentLink().getEndNode();
                }
                else
                {
                    from = entry.getKey().getParentLink().getEndNode();
                    to = entry.getKey().getParentLink().getStartNode();
                }
                if (route.indexOf(to) == route.indexOf(from) + 1)
                {
                    LaneRecord record = new LaneRecord(this.getStartDistance().plus(this.lane.getLength()), entry.getKey(),
                        entry.getValue());
                    record.prev = this;
                    set.add(record);
                }
            }
            return set;
        }

        /**
         * Returns the previous (upstream) record.
         * @return LaneRecord; previous (upstream) record
         */
        public LaneRecord prev()
        {
            return this.prev;
        }

        /**
         * Returns the left adjacent lane, or {@code null} if no such lane.
         * @param gtuType GTUType; GTU type
         * @return left adjacent lane, or {@code null} if no such lane
         * @throws GTUException in case of multiple adjacent lanes
         */
        public LaneRecord left(final GTUType gtuType) throws GTUException
        {
            if (!this.determinedLeft)
            {
                this.left = adjacent(gtuType, LateralDirectionality.LEFT);
                this.determinedLeft = true;
                if (this.left.lane.accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtuType, this.left.direction)
                    .contains(this.lane))
                {
                    this.left.right = this;
                }
                this.left.determinedRight = true;
            }
            return this.left;
        }

        /**
         * Returns the right adjacent lane, or {@code null} if no such lane.
         * @param gtuType GTUType; GTU type
         * @return right adjacent lane, or {@code null} if no such lane
         * @throws GTUException in case of multiple adjacent lanes
         */
        public LaneRecord right(final GTUType gtuType) throws GTUException
        {
            if (!this.determinedRight)
            {
                this.right = adjacent(gtuType, LateralDirectionality.RIGHT);
                this.determinedRight = true;
                if (this.right.lane.accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtuType, this.right.direction)
                    .contains(this.lane))
                {
                    this.right.left = this;
                }
                this.right.determinedLeft = true;
            }
            return this.right;
        }

        /**
         * Returns the left or right adjacent lane, or {@code null} if no such lane.
         * @param gtuType GTUType; GTU type
         * @param lat LateralDirectionality; left or right
         * @return left or right adjacent lane, or {@code null} if no such lane
         * @throws GTUException in case of multiple adjacent lanes
         */
        private LaneRecord adjacent(final GTUType gtuType, final LateralDirectionality lat) throws GTUException
        {
            Set<Lane> set = this.lane.accessibleAdjacentLanesLegal(lat, gtuType, this.direction);
            Throw.when(set.size() > 1, GTUException.class,
                "Default route system found multiple adjacent lanes, which is not supported.");
            if (set.size() == 1)
            {
                return new LaneRecord(this.startDistance, set.iterator().next(), this.direction);
            }
            return null;
        }

        /**
         * Returns the start distance, relative to the end of the origin lane.
         * @return Length; start distance, relative to the end of the origin lane
         */
        public Length getStartDistance()
        {
            return this.startDistance;
        }

        /**
         * Returns whether there is no available next lane (ignoring the route).
         * @param gtuType GTUType; GTU type
         * @return whether there is no available next lane (ignoring the route)
         */
        public boolean isDeadEnd(final GTUType gtuType)
        {
            return this.lane.downstreamLanes(this.direction, gtuType).isEmpty();
        }

    }

}
