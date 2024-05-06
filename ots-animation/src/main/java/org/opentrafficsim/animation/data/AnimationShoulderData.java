package org.opentrafficsim.animation.data;

import org.opentrafficsim.draw.road.CrossSectionElementAnimation.ShoulderData;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Animation data of a shoulder.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationShoulderData extends AnimationCrossSectionElementData<Lane> implements ShoulderData
{

    /**
     * Constructor.
     * @param shoulder Lane; shoulder.
     */
    public AnimationShoulderData(final Lane shoulder)
    {
        super(shoulder);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Shoulder " + getElement().getLink().getId() + " " + getElement().getOffsetAtBegin();
    }

}
