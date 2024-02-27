package org.opentrafficsim.animation.data;

import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Animation data of a Lane.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationLaneData extends AnimationCrossSectionElementData<Lane> implements LaneData
{

    /**
     * Constructor.
     * @param lane Lane; lane.
     */
    public AnimationLaneData(final Lane lane)
    {
        super(lane);
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return getElement().getId();
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getCenterLine()
    {
        return getElement().getCenterLine().getLine2d();
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkId()
    {
        return getElement().getLink().getId();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Lane " + getElement().getFullId();
    }

}
