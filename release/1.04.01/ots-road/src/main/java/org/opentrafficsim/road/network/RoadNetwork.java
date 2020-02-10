package org.opentrafficsim.road.network;

import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.perception.PerceivableContext;
import org.opentrafficsim.road.definitions.RoadDefinitions;

/**
 * RoadNetwork adds a number of methods to the Network class that are specific for roads, such as the LaneTypes. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public interface RoadNetwork extends Network, PerceivableContext, RoadDefinitions
{
    // interfaces that are unique to a Roadnetwork as opposed to a Network
}
