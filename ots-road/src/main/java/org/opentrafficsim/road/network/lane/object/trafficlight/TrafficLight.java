package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djutils.event.EventType;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface TrafficLight extends Locatable, LaneBasedObject
{
    /** @return the trafficLightColor */
    TrafficLightColor getTrafficLightColor();

    /**
     * Set the new traffic light color.
     * @param trafficLightColor TrafficLightColor; set the trafficLightColor
     */
    void setTrafficLightColor(TrafficLightColor trafficLightColor);

    /**
     * The <b>timed</b> event type for pub/sub indicating the change of color of a traffic light. <br>
     * Payload: Object[] {String trafficLightId, TrafficLight trafficLight, TrafficLightColor newColor}
     */
    EventType TRAFFICLIGHT_CHANGE_EVENT = new EventType("TRAFFICLIGHT.CHANGE");

}
