package org.opentrafficsim.animation.data;

import org.opentrafficsim.draw.road.LaneDetectorAnimation.LaneDetectorData;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;

/**
 * Animation data of a LaneDetector.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationLaneDetectorData extends AnimationLaneBasedObjectData<LaneDetector> implements LaneDetectorData
{

    /**
     * Constructor.
     * @param laneDetector lane detector.
     */
    public AnimationLaneDetectorData(final LaneDetector laneDetector)
    {
        super(laneDetector);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Lane detector " + getObject().getFullId();
    }

}
