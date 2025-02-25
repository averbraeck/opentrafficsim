package org.opentrafficsim.animation.data;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
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
public class AnimationTrafficLightDetectorData implements TrafficLightDetectorData
{

    /** Traffic light detector. */
    private final TrafficLightDetector trafficLigthDetector;

    /**
     * Constructor.
     * @param trafficLigthDetector traffic light detector.
     */
    public AnimationTrafficLightDetectorData(final TrafficLightDetector trafficLigthDetector)
    {
        this.trafficLigthDetector = trafficLigthDetector;
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        return this.trafficLigthDetector.getLocation();
    }

    @Override
    public Bounds2d getBounds()
    {
        return this.trafficLigthDetector.getBounds();
    }

    @Override
    public Polygon2d getContour()
    {
        return this.trafficLigthDetector.getContour();
    }

    @Override
    public boolean getOccupancy()
    {
        return this.trafficLigthDetector.getOccupancy();
    }

    @Override
    public String getId()
    {
        return this.trafficLigthDetector.getId();
    }

    /**
     * Returns the traffic light detector.
     * @return traffic light detector.
     */
    public TrafficLightDetector getTrafficLightDetector()
    {
        return this.trafficLigthDetector;
    }

    @Override
    public String toString()
    {
        return "Traffic light detector " + this.trafficLigthDetector.getId();
    }

}
