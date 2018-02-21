package org.opentrafficsim.road.gtu.lane.perception;

import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Interface representing a lane for search algorithms, in particular PerceptionIterable.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <R> the type
 */
public interface LaneRecord<R extends LaneRecord<R>>
{

    /**
     * Returns a list of next lanes. Callers of this method do not have to mind GTUDirectionality, this is taken care of.
     * @return List; list of next lanes
     */
    List<R> getNext();

    /**
     * Returns a list of previous lanes. Callers of this method do not have to mind GTUDirectionality, this is taken care of.
     * @return List; list of previous lanes
     */
    List<R> getPrev();

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
     * Returns the direction of travel.
     * @return GTUDirectionality; direction of travel
     */
    GTUDirectionality getDirection();

    /**
     * Returns the lane.
     * @return Lane lane;
     */
    Lane getLane();

    /**
     * Returns the distance from the reference to the given location. Callers of this method do not have to mind
     * GTUDirectionality, this is taken care of.
     * @param position Length; position on the lane
     * @return Length; distance from the reference to the given location
     */
    default Length getDistanceToPosition(final Length position)
    {
        return getStartDistance().plus(getDirection().isPlus() ? position : getLength().minus(position));
    }

}
