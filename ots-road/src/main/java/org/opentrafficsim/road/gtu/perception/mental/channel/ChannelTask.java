package org.opentrafficsim.road.gtu.perception.mental.channel;

import org.opentrafficsim.road.gtu.perception.mental.Task;

/**
 * Task in the context of a channel-based perception.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ChannelTask extends Task
{

    /** Standard front channel. */
    Object FRONT = "Front";

    /** Standard rear channel. */
    Object REAR = "Rear";

    /** Standard left channel. */
    Object LEFT = "Left";

    /** Standard right channel. */
    Object RIGHT = "Right";

    /** Standard in-vehicle channel. */
    Object IN_VEHICLE = "InVehicle";

    /**
     * Return the channel this task pertains to.
     * @return channel this task pertains to.
     */
    Object getChannel();

}
