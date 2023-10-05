package org.opentrafficsim.animation.data;

import java.rmi.RemoteException;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation.TrafficLightDetectorData;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;

/**
 * Animation data of a TrafficLightDetector.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationTrafficLightDetectorData implements TrafficLightDetectorData
{

    /** Traffic light detector. */
    private final TrafficLightDetector trafficLigthDetector;

    /**
     * Constructor.
     * @param trafficLigthDetector TrafficLightDetector; traffic light detector.
     */
    public AnimationTrafficLightDetectorData(final TrafficLightDetector trafficLigthDetector)
    {
        this.trafficLigthDetector = trafficLigthDetector;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.trafficLigthDetector.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds<?, ?, ?> getBounds() throws RemoteException
    {
        return this.trafficLigthDetector.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getGeometry()
    {
        return this.trafficLigthDetector.getGeometry();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getOccupancy()
    {
        return this.trafficLigthDetector.getOccupancy();
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.trafficLigthDetector.getId();
    }

    /**
     * Returns the traffic light detector.
     * @return TrafficLightDetector; traffic light detector.
     */
    public TrafficLightDetector getTrafficLightDetector()
    {
        return this.trafficLigthDetector;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrafficLightDetector " + this.trafficLigthDetector.getId();
    }

}
