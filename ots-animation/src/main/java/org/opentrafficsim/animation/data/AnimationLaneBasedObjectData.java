package org.opentrafficsim.animation.data;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.ClickableLineLocatable;
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
public abstract class AnimationLaneBasedObjectData<T extends LaneBasedObject>
        implements LaneBasedObjectData, ClickableLineLocatable
{

    /** Lane based object. */
    private final T laneBasedObject;

    /** Shape (cached). */
    private OtsShape shape;

    /**
     * Constructor.
     * @param laneBasedObject laneBasedObject.
     */
    public AnimationLaneBasedObjectData(final T laneBasedObject)
    {
        this.laneBasedObject = laneBasedObject;
    }

    @Override
    public Length getLaneWidth()
    {
        return this.laneBasedObject.getLane().getWidth(this.laneBasedObject.getLongitudinalPosition());
    }

    @Override
    public OrientedPoint2d getLocation()
    {
        return this.laneBasedObject.getLocation();
    }

    @Override
    public Polygon2d getContour()
    {
        return this.laneBasedObject.getContour();
    }
    
    @Override
    public OtsShape getShape()
    {
        if (this.shape == null)
        {
            this.shape = LaneBasedObjectData.super.getShape();
        }
        return this.shape;
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(this.laneBasedObject.getLine(), getLocation());
    }

    @Override
    public String getId()
    {
        return this.laneBasedObject.getId();
    }

    /**
     * Returns the wrapped object.
     * @return wrapped object.
     */
    public T getObject()
    {
        return this.laneBasedObject;
    }

}
