package org.opentrafficsim.road.gtu.lane.perception.object;

import org.opentrafficsim.road.network.lane.Lane;

/**
 * Lane based perceived object.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PerceivedLaneBasedObject extends PerceivedObject
{

    /**
     * Lane at which the object is located.
     * @return lane at which the object is located
     */
    Lane getLane();

}
