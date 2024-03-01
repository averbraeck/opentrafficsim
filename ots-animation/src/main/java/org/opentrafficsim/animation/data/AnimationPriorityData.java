package org.opentrafficsim.animation.data;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.road.PriorityAnimation.PriorityData;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

/**
 * Priority data for animation.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationPriorityData implements PriorityData
{

    /** Link. */
    final private CrossSectionLink link;

    /**
     * Constructor.
     * @param link CrossSectionLink; link.
     */
    public AnimationPriorityData(final CrossSectionLink link)
    {
        this.link = link;
    }

    /** {@inheritDoc} */
    @Override
    public Point2d getLocation()
    {
        return this.link.getDesignLine().getLocationFractionExtended(0.5);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAllStop()
    {
        return this.link.getPriority().isAllStop();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBusStop()
    {
        return this.link.getPriority().isBusStop();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNone()
    {
        return this.link.getPriority().isNone();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPriority()
    {
        return this.link.getPriority().isPriority();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStop()
    {
        return this.link.getPriority().isStop();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isYield()
    {
        return this.link.getPriority().isYield();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Priority " + this.link.getId();
    }

}
