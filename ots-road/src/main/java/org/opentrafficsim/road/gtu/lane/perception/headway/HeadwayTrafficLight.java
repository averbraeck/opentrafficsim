package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Headway object for traffic lights.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface HeadwayTrafficLight extends HeadwayLaneBasedObject
{

    /**
     * Returns the traffic light color.
     * @return the traffic light color.
     */
    TrafficLightColor getTrafficLightColor();

    /**
     * Whether the perceiving GTU may turn on red.
     * @return boolean; whether the perceiving GTU may turn on red.
     */
    boolean canTurnOnRed();

}
