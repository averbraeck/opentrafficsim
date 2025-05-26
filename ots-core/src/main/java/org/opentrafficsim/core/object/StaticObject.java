package org.opentrafficsim.core.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.core.network.NetworkException;

/**
 * A static object with a height that a GTU might have to avoid, or which can cause occlusion for perception. All objects are
 * potential event producers, which allows them to signal that their state has changed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class StaticObject extends LocalEventProducer implements LocatedObject
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** the id. */
    private final String id;

    /** Relative contour. */
    private final Polygon2d relativeContour;

    /** Absolute contour. */
    private final Polygon2d absoluteContour;

    /** Location. */
    private final DirectedPoint2d location;

    /** The height of the object. */
    private final Length height;

    /**
     * Constructor.
     * @param id the id
     * @param location location.
     * @param absoluteContour absolute contour
     * @param height the height of the object
     */
    protected StaticObject(final String id, final DirectedPoint2d location, final Polygon2d absoluteContour,
            final Length height)
    {
        Throw.whenNull(id, "id");
        Throw.whenNull(location, "location");
        Throw.whenNull(absoluteContour, "absoluteContour");
        Throw.whenNull(height, "height");

        this.id = id;
        this.location = location;
        this.relativeContour = new Polygon2d(OtsShape.toRelativeTransform(location).transform(absoluteContour.iterator()));
        this.absoluteContour = absoluteContour;
        this.height = height;
    }

    /**
     * Initialize the object after it has been fully created.
     * @throws NetworkException e.g. on error registering the object in the network
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void init() throws NetworkException
    {
        // notify the potential animation of the existence of a StaticObject
        // These next events are fired by the Network when the object is registered in the Network.
        // fireTimedEvent(Network.OBJECT_ADD_EVENT, this.id);
        // fireTimedEvent(Network.ANIMATION_OBJECT_ADD_EVENT, this);
    }

    /**
     * Make a static object and carry out the initialization after it has been fully created.
     * @param id the id
     * @param geometry the top-level 2D outline of the object
     * @param height the height of the object
     * @return the static object
     * @throws NetworkException e.g. on error registering the object in the network
     */
    public static StaticObject create(final String id, final Polygon2d geometry, final Length height) throws NetworkException
    {
        DirectedPoint2d point = new DirectedPoint2d(geometry.getBounds().midPoint(), 0.0);
        StaticObject staticObject = new StaticObject(id, point, geometry, height);
        staticObject.init();
        return staticObject;
    }

    /**
     * Make a static object with zero height and carry out the initialization after it has been fully created.
     * @param id the id
     * @param geometry the top-level 2D outline of the object
     * @return the static object
     * @throws NetworkException e.g. on error registering the object in the network
     */
    public static StaticObject create(final String id, final Polygon2d geometry) throws NetworkException
    {
        return create(id, geometry, Length.ZERO);
    }

    @Override
    public Polygon2d getAbsoluteContour()
    {
        return this.absoluteContour;
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.relativeContour;
    }

    @Override
    public final Length getHeight()
    {
        return this.height;
    }

    @Override
    public final String getId()
    {
        return this.id;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String getFullId()
    {
        return this.id;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint2d getLocation()
    {
        return this.location;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds2d getBounds()
    {
        return this.relativeContour.getBounds();
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "StaticObject [contour=" + getAbsoluteContour() + ", height=" + this.height + "]";
    }

}
