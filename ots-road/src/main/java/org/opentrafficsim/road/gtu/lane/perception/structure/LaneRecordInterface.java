package org.opentrafficsim.road.gtu.lane.perception.structure;

import java.util.Collections;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Interface representing a lane for search algorithms, in particular PerceptionIterable.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <R> lane record type
 */
public interface LaneRecordInterface<R extends LaneRecordInterface<R>>
{

    /**
     * Returns a list of next lanes.
     * @return list of next lanes
     */
    Set<? extends R> getNext();

    /**
     * Returns a list of previous lanes.
     * @return list of previous lanes
     */
    Set<? extends R> getPrev();

    /**
     * Get lateral lanes.
     * @return lateral lanes.
     */
    default Set<? extends R> lateral()
    {
        return Collections.emptySet();
    }

    /**
     * Returns the distance from a reference to the start of this lane, negative for upstream distance.
     * @return the distance from a reference to the start of this lane, negative for upstream distance
     */
    Length getStartDistance();

    /**
     * Returns the length of the lane.
     * @return length of the lane.
     */
    default Length getLength()
    {
        return getLane().getLength();
    }

    /**
     * Returns the lane.
     * @return lane
     */
    Lane getLane();

    /**
     * Returns the distance from the reference to the given location.
     * @param position position on the lane
     * @return distance from the reference to the given location
     */
    default Length getDistanceToPosition(final Length position)
    {
        return Length.ofSI(getStartDistance().si + position.si);
    }

    /**
     * Returns whether the record is part of the downstream branch. This means the GTU can potentially get here and the lane is
     * not upstream or on the other branch upstream of a merge. Default implementation returns {@code true}.
     * @return whether the record is part of the downstream branch
     */
    default boolean isDownstreamBranch()
    {
        return true;
    }

    /**
     * Returns the merge distance, i.e. the ego-distance after which this road merges with the road of this record. Returns zero
     * if this is not the other road upstream of a merge.
     * @return merge distance.
     */
    default Length getMergeDistance()
    {
        return Length.ZERO;
    }

}
