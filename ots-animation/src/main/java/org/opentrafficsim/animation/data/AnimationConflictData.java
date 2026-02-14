package org.opentrafficsim.animation.data;

import java.awt.Color;

import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.road.network.conflict.Conflict;

/**
 * Animation data of a Conflict.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationConflictData extends AnimationLaneBasedObjectData<Conflict> implements ConflictData
{

    /**
     * Constructor.
     * @param conflict conflict.
     */
    public AnimationConflictData(final Conflict conflict)
    {
        super(conflict);
    }

    @Override
    public Color getColor()
    {
        switch (getObject().conflictPriority())
        {
            case SPLIT:
                return Color.BLUE;
            case PRIORITY:
                return Color.GREEN;
            case YIELD:
                return Color.ORANGE;
            default:
                return Color.RED;
        }
    }

    @Override
    public boolean isCrossing()
    {
        return getObject().getConflictType().isCrossing();
    }

    @Override
    public boolean isPermitted()
    {
        return getObject().isPermitted();
    }

    @Override
    public String toString()
    {
        return "Conflict " + getObject().getLane().getFullId() + " " + getObject().getLongitudinalPosition();
    }

}
