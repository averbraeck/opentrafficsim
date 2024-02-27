package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Interface for lane structures.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LaneStructure
{

    /**
     * Updates the underlying structure shifting the root position to the input.
     * @param pos LanePosition; current position of the GTU
     * @param route Route; current route of the GTU
     * @param gtuType GtuType; GTU type
     * @throws GtuException on a problem while updating the structure
     */
    void update(LanePosition pos, Route route, GtuType gtuType) throws GtuException;

    /**
     * Returns the root record.
     * @return LaneRecord; root record
     */
    LaneStructureRecord getRootRecord();

    /**
     * Returns the extended cross-section, which includes all lanes for which a first record is present.
     * @return SortedSet; the cross-section
     */
    SortedSet<RelativeLane> getExtendedCrossSection();

    /**
     * Returns the first record on the given lane. This is often a record in the current cross section, but it may be one
     * downstream for a lane that starts further downstream.
     * @param lane RelativeLane; lane
     * @return first record on the given lane, or {@code null} if no such record
     */
    LaneStructureRecord getFirstRecord(RelativeLane lane);

    /**
     * Retrieve objects of a specific type. Returns objects over a maximum length of the look ahead distance downstream from the
     * relative position, or as far as the lane structure goes.
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGtu; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @return Map; sorted set of objects of requested type per lane
     * @throws GtuException if lane is not in current set
     */
    <T extends LaneBasedObject> Map<RelativeLane, SortedSet<Entry<T>>> getDownstreamObjects(Class<T> clazz, LaneBasedGtu gtu,
            RelativePosition.TYPE pos) throws GtuException;

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane structure goes.
     * @param lane RelativeLane; lane
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGtu; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @return SortedSet; sorted set of objects of requested type
     * @throws GtuException if lane is not in current set
     */
    <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjects(RelativeLane lane, Class<T> clazz, LaneBasedGtu gtu,
            RelativePosition.TYPE pos) throws GtuException;

    /**
     * Retrieve objects of a specific type. Returns objects over a maximum length of the look ahead distance downstream from the
     * relative position, or as far as the lane structure goes. Objects on links not on the route are ignored.
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGtu; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @param route Route; the route
     * @return SortedSet; sorted set of objects of requested type per lane
     * @throws GtuException if lane is not in current set
     */
    <T extends LaneBasedObject> Map<RelativeLane, SortedSet<Entry<T>>> getDownstreamObjectsOnRoute(Class<T> clazz,
            LaneBasedGtu gtu, RelativePosition.TYPE pos, Route route) throws GtuException;

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane structure goes. Objects on links not on the route are
     * ignored.
     * @param lane RelativeLane; lane
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGtu; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @param route Route; the route
     * @return SortedSet; sorted set of objects of requested type
     * @throws GtuException if lane is not in current set
     */
    <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjectsOnRoute(RelativeLane lane, Class<T> clazz,
            LaneBasedGtu gtu, RelativePosition.TYPE pos, Route route) throws GtuException;

    /**
     * Retrieve objects on a lane of a specific type. Returns upstream objects from the relative position for as far as the lane
     * structure goes. Distances to upstream objects are given as positive values.
     * @param lane RelativeLane; lane
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGtu; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @return SortedSet; sorted set of objects of requested type
     * @throws GtuException if lane is not in current set
     */
    <T extends LaneBasedObject> SortedSet<Entry<T>> getUpstreamObjects(RelativeLane lane, Class<T> clazz, LaneBasedGtu gtu,
            RelativePosition.TYPE pos) throws GtuException;

    /**
     * Wrapper to hold lane-based object and it's distance.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> class of lane based object contained
     */
    class Entry<T extends LaneBasedObject> implements Comparable<Entry<T>>
    {

        /** Distance to lane based object. */
        private final Length distance;

        /** Lane based object. */
        private final T laneBasedObject;

        /**
         * @param distance Length; distance to lane based object
         * @param laneBasedObject T; lane based object
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
