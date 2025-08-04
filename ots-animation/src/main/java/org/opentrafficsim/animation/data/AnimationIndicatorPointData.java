package org.opentrafficsim.animation.data;

import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.draw.road.IndicatorPointAnimation.IndicatorPointData;
import org.opentrafficsim.road.network.lane.object.IndicatorPoint;

/**
 * Animation data of a IndicatorPoint.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationIndicatorPointData implements IndicatorPointData
{

    /** Speed sign. */
    private final IndicatorPoint indicatorPoint;

    /**
     * Constructor.
     * @param IndicatorPoint IndicatorPoint; speed sign.
     */
    public AnimationIndicatorPointData(final IndicatorPoint IndicatorPoint)
    {
        this.indicatorPoint = IndicatorPoint;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.indicatorPoint.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getBounds()
    {
        return this.indicatorPoint.getBounds();
    }

    /**
     * Returns the speed sign.
     * @return IndicatorPoint; speed sign.
     */
    public IndicatorPoint getIndicatorPoint()
    {
        return this.indicatorPoint;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Speed sign " + this.indicatorPoint.getId();
    }

}
