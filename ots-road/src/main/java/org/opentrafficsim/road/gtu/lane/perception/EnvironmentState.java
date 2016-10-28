package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Set;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.LaneBasedObject;

/**
 * Provides 'friendly' access to the LaneStructure from a GTU point of view.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface EnvironmentState
{

    /**
     * Retrieve objects on a lane of a specific type.
     * @param viewingDirection direction to look at
     * @param relativeLane lane to look at
     * @param clazz class of objects to obtain
     * @param <T> type of the objects
     * @return Sorted set of objects of requested class
     */
    <T extends LaneBasedObject> TreeMap<Length, Set<T>> getSortedObjects(final ViewingDirection viewingDirection,
            final RelativeLane relativeLane, final Class<T> clazz);

    /**
     * Direction to look.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 okt. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    enum ViewingDirection
    {
        /** Forward direction. */
        FORWARD,

        /** Backward direction. */
        BACKWARD;
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
    class Entry<T extends LaneBasedObject> implements Comparable<Entry<T>>
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
            super();
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
        public String toString()
        {
            return "EnvironmentState.Entry [distance=" + this.distance + ", laneBasedObject=" + this.laneBasedObject + "]";
        }

    }
}
