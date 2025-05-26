package org.opentrafficsim.animation.data;

import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation.TrafficLightDetectorData;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;

/**
 * Animation data of a TrafficLightDetector.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationTrafficLightDetectorData extends AnimationIdentifiableShape<TrafficLightDetector>
        implements TrafficLightDetectorData
{

    /**
     * Constructor.
     * @param trafficLigthDetector traffic light detector.
     */
    public AnimationTrafficLightDetectorData(final TrafficLightDetector trafficLigthDetector)
    {
        super(trafficLigthDetector);
    }

    @Override
    public boolean getOccupancy()
    {
        return getObject().getOccupancy();
    }

    @Override
    public String toString()
    {
        return "Traffic light detector " + getId();
    }

}
