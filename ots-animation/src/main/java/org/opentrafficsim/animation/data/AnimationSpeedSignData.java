package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Speed;
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
public class AnimationSpeedSignData extends AnimationIdentifiableShape<SpeedSign> implements SpeedSignData
{

    /**
     * Constructor.
     * @param speedSign speed sign.
     */
    public AnimationSpeedSignData(final SpeedSign speedSign)
    {
        super(speedSign);
    }

    @Override
    public Speed getSpeed()
    {
        return getObject().getSpeed();
    }

    @Override
    public String toString()
    {
        return "Speed sign " + getId();
    }

}
