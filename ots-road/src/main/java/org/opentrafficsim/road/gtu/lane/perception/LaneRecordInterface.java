package org.opentrafficsim.road.gtu.lane.perception;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Interface representing a lane for search algorithms, in particular PerceptionIterable.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>\
 * @param <R> lane record type
 */
public interface LaneRecordInterface<R extends LaneRecordInterface<R>>
{

    /**
     * Returns a list of next lanes.
     * @return List; list of next lanes
     */
    List<? extends R> getNext();

    /**
     * Returns a list of previous lanes.
     * @return List; list of previous lanes
     */
    List<? extends R> getPrev();

    /**
     * Returns the distance from a reference to the start of this lane, negative for upstream distance.
     * @return Length; the distance from a reference to the start of this lane, negative for upstream distance
     */
    Length getStartDistance();

    /**
     * Returns the length of the lane.
     * @return Length; length of the lane.
     */
    Length getLength();

    /**
     * Returns the lane.
     * @return Lane lane;
     */
    Lane getLane();

    /**
     * Returns the distance from the reference to the given location.
     * @param position Length; position on the lane
     * @return Length; distance from the reference to the given location
     */
    default Length getDistanceToPosition(final Length position)
    {
        return Length.instantiateSI(getStartDistance().si + position.si);
    }

    /**
     * Returns whether the record is part of the downstream branch. This means the GTU can potentially get here and the lane is
     * not upstream or on the other branch upstream of a merge. Default implementation returns {@code true}.
     * @return Boolean; whether the record is part of the downstream branch
     */
    default boolean isDownstreamBranch()
    {
        return true;
    }

}
