package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.draw.road.SpeedSignAnimation.SpeedSignData;
import org.opentrafficsim.road.network.lane.object.SpeedSign;

/**
 * Animation data of a SpeedSign.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationSpeedSignData implements SpeedSignData
{

    /** Speed sign. */
    private final SpeedSign speedSign;

    /**
     * Constructor.
     * @param speedSign speed sign.
     */
    public AnimationSpeedSignData(final SpeedSign speedSign)
    {
        this.speedSign = speedSign;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.speedSign.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d getContour()
    {
        return this.speedSign.getContour();
    }

    /** {@inheritDoc} */
    @Override
    public Speed getSpeed()
    {
        return this.speedSign.getSpeed();
    }

    /**
     * Returns the speed sign.
     * @return speed sign.
     */
    public SpeedSign getSpeedSign()
    {
        return this.speedSign;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Speed sign " + this.speedSign.getId();
    }

}
