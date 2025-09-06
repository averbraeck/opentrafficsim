package org.opentrafficsim.animation.data;

import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * Object wrapper for animations, that forwards shape and id information to the wrapped object.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object
 */
public abstract class AnimationIdentifiableShape<T extends OtsShape & Identifiable> implements OtsShape, Identifiable
{

    /** The object. */
    private final T object;

    /**
     * Constructor.
     * @param object object
     */
    public AnimationIdentifiableShape(final T object)
    {
        this.object = object;
    }

    /**
     * Returns the object.
     * @return object
     */
    public T getObject()
    {
        return this.object;
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        return this.object.getLocation();
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.object.getRelativeContour();
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        return this.object.getAbsoluteContour();
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return this.object.getRelativeBounds();
    }

    @Override
    public String getId()
    {
        return this.object.getId();
    }

}
