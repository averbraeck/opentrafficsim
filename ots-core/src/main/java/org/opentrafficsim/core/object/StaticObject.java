package org.opentrafficsim.core.object;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventProducer;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;

/**
 * A static object with a height that a GTU might have to avoid, or which can cause occlusion for perception. All objects are
 * potential event producers, which allows them to signal that their state has changed.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StaticObject extends EventProducer implements ObjectInterface, Serializable, Identifiable, Drawable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** the id. */
    private final String id;

    /** The top-level 2D outline of the object. */
    private final OTSLine3D geometry;

    /** The height of the object. */
    private final Length height;

    /**
     * @param id String; the id
     * @param geometry OTSLine3D; the top-level 2D outline of the object
     * @param height Length; the height of the object
     */
    protected StaticObject(final String id, final OTSLine3D geometry, final Length height)
    {
        Throw.whenNull(id, "object id cannot be null");
        Throw.whenNull(geometry, "geometry cannot be null");
        Throw.whenNull(height, "height cannot be null");

        this.id = id;
        this.geometry = geometry;
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
        // These next events are fired by the OTSNetwork when the object is registered in the Network.
        // fireTimedEvent(Network.OBJECT_ADD_EVENT, this.id);
        // fireTimedEvent(Network.ANIMATION_OBJECT_ADD_EVENT, this);
    }

    /**
     * Make a static object and carry out the initialization after it has been fully created.
     * @param id String; the id
     * @param geometry OTSLine3D; the top-level 2D outline of the object
     * @param height Length; the height of the object
     * @return the static object
     * @throws NetworkException e.g. on error registering the object in the network
     */
    public static StaticObject create(final String id, final OTSLine3D geometry, final Length height) throws NetworkException
    {
        StaticObject staticObject = new StaticObject(id, geometry, height);
        staticObject.init();
        return staticObject;
    }

    /**
     * Make a static object with zero height and carry out the initialization after it has been fully created.
     * @param id String; the id
     * @param geometry OTSLine3D; the top-level 2D outline of the object
     * @return the static object
     * @throws NetworkException e.g. on error registering the object in the network
     */
    public static StaticObject create(final String id, final OTSLine3D geometry) throws NetworkException
    {
        return create(id, geometry, Length.ZERO);
    }

    /** {@inheritDoc} */
    @Override
    public final OTSLine3D getGeometry()
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
    public DirectedPoint getLocation()
    {
        return this.geometry.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        return this.geometry.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "StaticObject3D [geometry=" + getGeometry() + ", height=" + this.height + "]";
    }

    /**
     * Clone the StaticObject for e.g., copying a network.
     * @param newNetwork Network; the new network to which the clone belongs
     * @param animation boolean; whether to (re)create animation or not
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public StaticObject clone(final Network newNetwork, final boolean animation) throws NetworkException
    {
        // FIXME: why does this method have any arguments?
        return new StaticObject(this.id, this.geometry, this.height);
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return this.id;
    }

}
