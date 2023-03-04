package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.opentrafficsim.road.network.lane.Lane;

/**
 * Headway of a lane-based object.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface HeadwayLaneBasedObject extends Headway
{

    /**
     * Lane at which the object is located.
     * @return Lane; lane at which the object is located
     */
    Lane getLane();

}
