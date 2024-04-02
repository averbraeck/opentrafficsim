package org.opentrafficsim.core.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.BoundingPolygon;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.network.NetworkException;

/**
 * A static object with a height that a GTU might have to avoid, or which can cause occlusion for perception. All objects are
 * potential event producers, which allows them to signal that their state has changed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class StaticObject extends LocalEventProducer implements LocatedObject, Drawable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** the id. */
    private final String id;

    /** The top-level 2D outline of the object. */
    private final PolyLine2d geometry;

    /** Location. */
    private final OrientedPoint2d location;

    /** Bounds. */
    private final OtsBounds2d bounds;

    /** The height of the object. */
    private final Length height;

    /**
     * @param id String; the id
     * @param location OrientedPoint2d; location.
     * @param geometry PolyLine2d; the top-level 2D outline of the object
     * @param height Length; the height of the object
     */
    protected StaticObject(final String id, final OrientedPoint2d location, final PolyLine2d geometry, final Length height)
    {
        Throw.whenNull(id, "object id cannot be null");
        Throw.whenNull(geometry, "geometry cannot be null");
        Throw.whenNull(height, "height cannot be null");

        this.id = id;
        this.geometry = geometry;
        this.location = location;

        this.bounds = BoundingPolygon.geometryToBounds(location, geometry);
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
     * @param id String; the id
     * @param geometry Polygon2d; the top-level 2D outline of the object
     * @param height Length; the height of the object
     * @return the static object
     * @throws NetworkException e.g. on error registering the object in the network
     */
    public static StaticObject create(final String id, final Polygon2d geometry, final Length height) throws NetworkException
    {
        OrientedPoint2d point = new OrientedPoint2d(geometry.getBounds().midPoint(), 0.0);
        StaticObject staticObject = new StaticObject(id, point, geometry, height);
        staticObject.init();
        return staticObject;
    }

    /**
     * Make a static object with zero height and carry out the initialization after it has been fully created.
     * @param id String; the id
     * @param geometry Polygon2d; the top-level 2D outline of the object
     * @return the static object
     * @throws NetworkException e.g. on error registering the object in the network
     */
    public static StaticObject create(final String id, final Polygon2d geometry) throws NetworkException
    {
        return create(id, geometry, Length.ZERO);
    }

    /** {@inheritDoc} */
    @Override
    public PolyLine2d getGeometry()
    {
        return this.geometry;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getHeight()
    {
        return this.height;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String getFullId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public OrientedPoint2d getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public OtsBounds2d getBounds()
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "StaticObject [geometry=" + getGeometry() + ", height=" + this.height + "]";
    }

}
