package org.opentrafficsim.animation.data;

import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Animation data of a Lane.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationLaneData extends AnimationCrossSectionElementData<Lane> implements LaneData
{

    /**
     * Constructor.
     * @param lane lane.
     */
    public AnimationLaneData(final Lane lane)
    {
        super(lane);
    }

    @Override
    public String toString()
    {
        return "Lane " + getObject().getFullId();
    }

}
