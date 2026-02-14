package org.opentrafficsim.animation.data;

import java.awt.Color;

import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLight;

/**
 * Animation data of a TrafficLight.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationTrafficLightData extends AnimationLaneBasedObjectData<TrafficLight> implements TrafficLightData
{

    /**
     * Constructor.
     * @param trafficLight traffic light.
     */
    public AnimationTrafficLightData(final TrafficLight trafficLight)
    {
        super(trafficLight);
    }

    @Override
    public Color getColor()
    {
        switch (getObject().getTrafficLightColor())
        {
            case RED:
            {
                return Color.RED;
            }
            case GREEN:
            case PREGREEN:
            {
                return Color.GREEN;
            }
            case YELLOW:
            {
                return Color.YELLOW;
            }
            default:
            {
                return Color.BLACK;
            }
        }
    }

    @Override
    public String toString()
    {
        return "Traffic light " + getObject().getFullId();
    }

}
