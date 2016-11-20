package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

import nl.tudelft.simulation.language.Throw;

/**
 * This data structure can clearly indicate the lane structure ahead of us, e.g. in the following situation:
 * 
 * <pre>
 *     (---- a ----)(---- b ----)(---- c ----)(---- d ----)(---- e ----)(---- f ----)(---- g ----)  
 *                                             __________                             __________
 *                                            / _________ 1                          / _________ 2
 *                                           / /                                    / /
 *                                __________/ /             _______________________/ /
 *  1  ____________ ____________ /_ _ _ _ _ _/____________ /_ _ _ _ _ _ _ _ _ _ _ _ /      
 *  0 |_ _X_ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ \____________
 * -1 |____________|_ _ _ _ _ _ |____________|____________|  __________|____________|____________| 3
 * -2              / __________/                           \ \  
 *        ________/ /                                       \ \___________  
 *      5 _________/                                         \____________  4
 * </pre>
 * 
 * When the GTU is looking ahead, it needs to know that when it continues to destination 3, it needs to shift one lane to the
 * right at some point, but <b>not</b> two lanes to the right in link b, and not later than at the end of link f. When it needs
 * to go to destination 1, it needs to shift to the left in link c. When it has to go to destination 2, it has to shift to the
 * left, but not earlier than at link e. At node [de], it is possible to leave the rightmost lane of link e, and go to
 * destination 4. The rightmost lane just splits into two lanes at the end of link d, and the GTU can either continue driving to
 * destination 3, turn right to destination 4. This means that the right lane of link d has <b>two</b> successor lanes.
 * <p>
 * In the data structures, lanes are numbered laterally. Suppose that the lane where vehicle X resides would be number 0.
 * Consistent with "left is positive" for angles, the lane right of X would have number -1, and entry 5 would have number -2.
 * <p>
 * In the data structure, this can be indicated as follows (N = next, P = previous, L = left, R = right, D = lane drop, . =
 * continued but not in this structure). The merge lane in b is considered "off limits" for the GTUs on the "main" lane -1; the
 * "main" lane 0 is considered off limits from the exit lanes on c, e, and f. Still, we need to maintain pointers to these
 * lanes, as we are interested in the GTUs potentially driving next to us, feeding into our lane, etc.
 * 
 * <pre>
 *       1                0               -1               -2
 *       
 *                       ROOT 
 *                   _____|_____      ___________      ___________            
 *                  |_-_|_._|_R_|----|_L_|_._|_-_|    |_-_|_._|_-_|  a           
 *                        |                |                |
 *                   _____V_____      _____V_____      _____V_____            
 *                  |_-_|_N_|_R_|----|_L_|_N_|_R_|&lt;---|_L_|_D_|_-_|  b           
 *                        |                |                 
 *  ___________      _____V_____      _____V_____                 
 * |_-_|_N_|_R_|&lt;---|_L_|_N_|_R_|----|_L_|_N_|_-_|                   c
 *       |                |                |                 
 *  _____V_____      _____V_____      _____V_____                 
 * |_-_|_._|_-_|    |_-_|_N_|_R_|----|_L_|_NN|_-_|                   d          
 *                        |                ||_______________ 
 *  ___________      _____V_____      _____V_____      _____V_____            
 * |_-_|_N_|_R_|&lt;---|_L_|_N_|_R_|----|_L_|_N_|_-_|    |_-_|_N_|_-_|  e          
 *       |                |                |                |
 *  _____V_____      _____V_____      _____V_____      _____V_____            
 * |_-_|_N_|_R_|&lt;---|_L_|_D_|_R_|----|_L_|_N_|_-_|    |_-_|_._|_-_|  f          
 *       |                                 |                 
 *  _____V_____                       _____V_____                             
 * |_-_|_._|_-_|                     |_-_|_._|_-_|                   g
 * </pre>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneStructure implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The lanes from which we observe the situation. */
    private LaneStructureRecord rootLSR;

    /** Look ahead distance. */
    private Length lookAhead;

    /** Lane structure records of the cross section. */
    private TreeMap<RelativeLane, LaneStructureRecord> crossSectionRecords = new TreeMap<>();

    /** Lane structure records grouped per relative lane. */
    private final Map<RelativeLane, Set<LaneStructureRecord>> relativeLaneMap = new HashMap<>();

    /**
     * @param rootLSR the root record.
     * @param lookAhead look ahead distance
     */
    public LaneStructure(final LaneStructureRecord rootLSR, final Length lookAhead)
    {
        this.rootLSR = rootLSR;
        this.lookAhead = lookAhead;
    }

    /**
     * @return rootLSR
     */
    public final LaneStructureRecord getRootLSR()
    {
        return this.rootLSR;
    }

    /**
     * Returns the cross section.
     * @return cross section
     */
    public final SortedSet<RelativeLane> getCrossSection()
    {
        return this.crossSectionRecords.navigableKeySet();
    }

    /**
     * @param lane lane to check
     * @return record at given lane
     * @throws GTUException if the lane is not in the cross section
     */
    public final LaneStructureRecord getLaneLSR(final RelativeLane lane) throws GTUException
    {
        Throw.when(!this.crossSectionRecords.containsKey(lane), GTUException.class,
                "The requested lane %s is not in the most recent cross section.", lane);
        return this.crossSectionRecords.get(lane);
    }

    /**
     * Removes all mappings to relative lanes that are not in the most recent cross section.
     * @param map map to clear mappings from
     */
    public final void removeInvalidMappings(final Map<RelativeLane, ?> map)
    {
        Iterator<RelativeLane> iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            RelativeLane lane = iterator.next();
            if (!this.crossSectionRecords.containsKey(lane))
            {
                iterator.remove();
            }
        }
    }

    /**
     * Adds a lane structure record in a mapping from relative lanes.
     * @param lsr lane structure record
     * @param relativeLane relative lane
     */
    public final void addLaneStructureRecord(final LaneStructureRecord lsr, final RelativeLane relativeLane)
    {
        if (!this.relativeLaneMap.containsKey(relativeLane))
        {
            this.relativeLaneMap.put(relativeLane, new HashSet<>());
        }
        this.relativeLaneMap.get(relativeLane).add(lsr);
        if (lsr.getStartDistance().le(Length.ZERO) && lsr.getStartDistance().plus(lsr.getLane().getLength()).ge(Length.ZERO))
        {
            this.crossSectionRecords.put(relativeLane, lsr);
        }
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane map goes.
     * @param lane lane
     * @param clazz class of objects to find
     * @param gtu gtu
     * @param pos relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @SuppressWarnings("unchecked")
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjects(final RelativeLane lane,
            final Class<T> clazz, final GTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        LaneStructureRecord record = this.getLaneLSR(lane);
        Length ds = gtu.getRelativePositions().get(pos).getDx().minus(gtu.getReference().getDx());
        // the list is ordered, but only for DIR_PLUS, need to do our own ordering
        Length minimumPosition;
        Length maximumPosition;
        if (record.getDirection().isPlus())
        {
            minimumPosition = ds.minus(record.getStartDistance());
            maximumPosition = record.getLane().getLength();
        }
        else
        {
            minimumPosition = Length.ZERO;
            maximumPosition = record.getLane().getLength().plus(record.getStartDistance()).minus(ds);
        }
        SortedSet<Entry<T>> set = new TreeSet<>();
        Length distance;
        for (LaneBasedObject object : record.getLane().getLaneBasedObjects(minimumPosition, maximumPosition))
        {
            distance = record.getDistanceToPosition(object.getLongitudinalPosition()).minus(ds);
            if (clazz.isAssignableFrom(object.getClass()) && distance.le(this.lookAhead))
            {
                // unchecked, but the above isAssignableFrom assures correctness
                set.add(new Entry<>(distance, (T) object));
            }
        }
        getDownstreamObjectsRecursive(set, record, clazz, ds);
        return set;
    }

    /**
     * Recursive search for lane based objects downstream.
     * @param set set to store entries into
     * @param record current record
     * @param clazz class of objects to find
     * @param ds distance from reference to chosen relative position
     * @param <T> type of objects to find
     */
    @SuppressWarnings("unchecked")
    private <T extends LaneBasedObject> void getDownstreamObjectsRecursive(final SortedSet<Entry<T>> set,
            final LaneStructureRecord record, final Class<T> clazz, final Length ds)
    {
        if (record.getNext().isEmpty() || record.getNext().get(0).getStartDistance().gt(this.lookAhead))
        {
            return;
        }
        for (LaneStructureRecord next : record.getNext())
        {
            Length distance;
            for (LaneBasedObject object : next.getLane().getLaneBasedObjects())
            {
                distance = next.getDistanceToPosition(object.getLongitudinalPosition()).minus(ds);
                if (clazz.isAssignableFrom(object.getClass()) && distance.le(this.lookAhead))
                {
                    // unchecked, but the above isAssignableFrom assures correctness
                    set.add(new Entry<>(distance, (T) object));
                }
            }
            getDownstreamObjectsRecursive(set, next, clazz, ds);
        }
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns upstream objects from the relative position for as far as the lane
     * map goes. Distances to upstream objects are given as positive values.
     * @param lane lane
     * @param clazz class of objects to find
     * @param gtu gtu
     * @param pos relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @SuppressWarnings("unchecked")
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getUpstreamObjects(final RelativeLane lane,
            final Class<T> clazz, final GTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        LaneStructureRecord record = this.getLaneLSR(lane);
        Length ds = gtu.getReference().getDx().minus(gtu.getRelativePositions().get(pos).getDx());
        // the list is ordered, but only for DIR_PLUS, need to do our own ordering
        Length minimumPosition;
        Length maximumPosition;
        if (record.getDirection().isPlus())
        {
            minimumPosition = Length.ZERO;
            maximumPosition = record.getStartDistance().neg().minus(ds);
        }
        else
        {
            minimumPosition = record.getLane().getLength().plus(record.getStartDistance()).plus(ds);
            maximumPosition = record.getLane().getLength();
        }
        SortedSet<Entry<T>> set = new TreeSet<>();
        Length distance;
        for (LaneBasedObject object : record.getLane().getLaneBasedObjects(minimumPosition, maximumPosition))
        {
            if (clazz.isAssignableFrom(object.getClass()))
            {
                distance = record.getDistanceToPosition(object.getLongitudinalPosition()).neg().minus(ds);
                // unchecked, but the above isAssignableFrom assures correctness
                set.add(new Entry<>(distance, (T) object));
            }
        }
        getUpstreamObjectsRecursive(set, record, clazz, ds);
        return set;
    }

    /**
     * Recursive search for lane based objects upstream.
     * @param set set to store entries into
     * @param record current record
     * @param clazz class of objects to find
     * @param ds distance from reference to chosen relative position
     * @param <T> type of objects to find
     */
    @SuppressWarnings("unchecked")
    private <T extends LaneBasedObject> void getUpstreamObjectsRecursive(final SortedSet<Entry<T>> set,
            final LaneStructureRecord record, final Class<T> clazz, final Length ds)
    {
        for (LaneStructureRecord prev : record.getPrev())
        {
            Length distance;
            for (LaneBasedObject object : prev.getLane().getLaneBasedObjects())
            {
                if (clazz.isAssignableFrom(object.getClass()))
                {
                    distance = prev.getDistanceToPosition(object.getLongitudinalPosition()).neg().minus(ds);
                    // unchecked, but the above isAssignableFrom assures correctness
                    set.add(new Entry<>(distance, (T) object));
                }
            }
            getUpstreamObjectsRecursive(set, prev, clazz, ds);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneStructure [rootLSR=" + this.rootLSR + "]";
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> class of lane based object contained
     */
    public class Entry<T extends LaneBasedObject> implements Comparable<Entry<T>>
    {

        /** Distance to lane based object. */
        private final Length distance;

        /** Lane based object. */
        private final T laneBasedObject;

        /**
         * @param distance distance to lane based object
         * @param laneBasedObject lane based object
         */
        public Entry(final Length distance, final T laneBasedObject)
        {
            this.distance = distance;
            this.laneBasedObject = laneBasedObject;
        }

        /**
         * @return distance.
         */
        public final Length getDistance()
        {
            return this.distance;
        }

        /**
         * @return laneBasedObject.
         */
        public final T getLaneBasedObject()
        {
            return this.laneBasedObject;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.distance == null) ? 0 : this.distance.hashCode());
            result = prime * result + ((this.laneBasedObject == null) ? 0 : this.laneBasedObject.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public final boolean equals(final Object obj)
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
            Entry<?> other = (Entry<?>) obj;
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
            if (this.laneBasedObject == null)
            {
                if (other.laneBasedObject != null)
                {
                    return false;
                }
            }
            // laneBasedObject does not implement equals...
            else if (!this.laneBasedObject.equals(other.laneBasedObject))
            {
                return false;
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Entry<T> arg)
        {
            int d = this.distance.compareTo(arg.distance);
            if (d != 0 || this.laneBasedObject.equals(arg.laneBasedObject))
            {
                return d; // different distance (-1 or 1), or same distance but also equal lane based object (0)
            }
            return 1; // same distance, unequal lane based object (1)
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LaneStructure.Entry [distance=" + this.distance + ", laneBasedObject=" + this.laneBasedObject + "]";
        }

    }

}
