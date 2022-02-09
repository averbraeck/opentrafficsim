package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djutils.event.TimedEventType;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
    TimedEventType TRAFFICLIGHT_CHANGE_EVENT = new TimedEventType("TRAFFICLIGHT.CHANGE");

}
