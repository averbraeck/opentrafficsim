package org.opentrafficsim.animation.data;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
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
    private final CrossSectionLink link;
    
    /** Bounds. */
    private final Bounds2d bounds = new Bounds2d(2.0, 2.0);
    
    /** Contour. */
    private final Polygon2d contour;

    /**
     * Constructor.
     * @param link link.
     */
    public AnimationPriorityData(final CrossSectionLink link)
    {
        this.link = link;
        this.contour = OtsLocatable.boundsAsContour(this);
    }

    @Override
    public Point2d getLocation()
    {
        return this.link.getDesignLine().getLocationFractionExtended(0.5);
    }
    
    @Override
    public Polygon2d getContour()
    {
        return this.contour;
    }
    
    @Override
    public Bounds2d getBounds()
    {
        return this.bounds;
    }

    @Override
    public boolean isAllStop()
    {
        return this.link.getPriority().isAllStop();
    }

    @Override
    public boolean isBusStop()
    {
        return this.link.getPriority().isBusStop();
    }

    @Override
    public boolean isNone()
    {
        return this.link.getPriority().isNone();
    }

    @Override
    public boolean isPriority()
    {
        return this.link.getPriority().isPriority();
    }

    @Override
    public boolean isStop()
    {
        return this.link.getPriority().isStop();
    }

    @Override
    public boolean isYield()
    {
        return this.link.getPriority().isYield();
    }

    @Override
    public String toString()
    {
        return "Priority " + this.link.getId();
    }

}
