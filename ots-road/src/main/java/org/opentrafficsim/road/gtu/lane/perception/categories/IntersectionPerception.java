package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * Perception category for traffic lights and conflicts.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface IntersectionPerception extends LaneBasedPerceptionCategory
{

    /**
     * Returns a set of traffic lights along the route. Traffic lights are sorted by headway value.
     * @param lane lane
     * @return set of traffic lights along the route
     */
    PerceptionCollectable<PerceivedTrafficLight, TrafficLight> getTrafficLights(RelativeLane lane);

    /**
     * Returns a set of conflicts along the route. Conflicts are sorted by headway value.
     * @param lane lane
     * @return set of conflicts along the route
     */
    PerceptionCollectable<PerceivedConflict, Conflict> getConflicts(RelativeLane lane);

    /**
     * Returns whether there is a conflict alongside to the left.
     * @return whether there is a conflict alongside to the left
     */
    boolean isAlongsideConflictLeft();

    /**
     * Returns whether there is a conflict alongside to the right.
     * @return whether there is a conflict alongside to the right
     */
    boolean isAlongsideConflictRight();

}
