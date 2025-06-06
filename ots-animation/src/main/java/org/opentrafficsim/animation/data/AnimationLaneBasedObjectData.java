package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.ClickableLineLocatable;
import org.opentrafficsim.draw.ClickableLocatable;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Animation data of a LaneBasedObject.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> lane based object type
 */
public abstract class AnimationLaneBasedObjectData<T extends LaneBasedObject> extends AnimationIdentifiableShape<T>
        implements LaneBasedObjectData, ClickableLineLocatable
{

    /**
     * Constructor.
     * @param laneBasedObject laneBasedObject.
     */
    public AnimationLaneBasedObjectData(final T laneBasedObject)
    {
        super(laneBasedObject);
    }

    @Override
    public Length getLaneWidth()
    {
        return getObject().getLane().getWidth(getObject().getLongitudinalPosition());
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsShape.transformLine(getObject().getLine(), getLocation());
    }

    @Override
    public String getId()
    {
        return getObject().getFullId();
    }

    @Override
    public Bounds2d getBounds()
    {
        return ClickableLocatable.getBounds(this);
    }

}
