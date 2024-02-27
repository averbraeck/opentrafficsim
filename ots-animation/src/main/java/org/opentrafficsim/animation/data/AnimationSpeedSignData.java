package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.draw.road.SpeedSignAnimation.SpeedSignData;
import org.opentrafficsim.road.network.lane.object.SpeedSign;

/**
 * Animation data of a SpeedSign.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationSpeedSignData implements SpeedSignData
{

    /** Speed sign. */
    private final SpeedSign speedSign;

    /**
     * Constructor.
     * @param speedSign SpeedSign; speed sign.
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
    public OtsBounds2d getBounds()
    {
        return this.speedSign.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public Speed getSpeed()
    {
        return this.speedSign.getSpeed();
    }

    /**
     * Returns the speed sign.
     * @return SpeedSign; speed sign.
     */
    public SpeedSign getSpeedSign()
    {
        return this.speedSign;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SpeedSign " + this.speedSign.getId();
    }

}
