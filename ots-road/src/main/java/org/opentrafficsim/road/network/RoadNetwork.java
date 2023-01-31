package org.opentrafficsim.road.network;

import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.perception.PerceivableContext;

/**
 * RoadNetwork adds a number of methods to the Network class that are specific for roads, such as the LaneTypes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public interface RoadNetwork extends Network, PerceivableContext
{
    // interfaces that are unique to a Roadnetwork as opposed to a Network
}
