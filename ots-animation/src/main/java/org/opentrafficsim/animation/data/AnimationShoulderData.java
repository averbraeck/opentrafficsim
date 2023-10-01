package org.opentrafficsim.animation.data;

import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.road.network.lane.Shoulder;

/**
 * Animation data of a Shoulder.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationShoulderData extends AnimationCrossSectionElementData<Shoulder>
{

    /**
     * Constructor.
     * @param shoulder Shoulder; shoulder.
     */
    public AnimationShoulderData(final Shoulder shoulder)
    {
        super(shoulder);
    }

    /** {@inheritDoc} */
    @Override
    public double getZ()
    {
        return DrawLevel.SHOULDER.getZ();
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Shoulder " + getElement().getLink().getId() + " " + getElement().getDesignLineOffsetAtBegin();
    }

}
