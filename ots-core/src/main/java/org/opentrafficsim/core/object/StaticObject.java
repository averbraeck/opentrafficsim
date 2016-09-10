package org.opentrafficsim.core.object;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;

import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * A static object with a height that a GTU might have to avoid, or which can cause occlusion for perception. All objects are
 * potential event producers, which allows them to signal that their state has changed.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StaticObject extends EventProducer implements ObjectInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The top-level 2D outline of the object. */
    private final OTSLine3D geometry;

    /** The height of the object. */
    private final Length height;

    /**
     * @param geometry the top-level 2D outline of the object
     * @param height the height of the object
     */
    public StaticObject(final OTSLine3D geometry, final Length height)
    {
        super();
        this.geometry = geometry;
        this.height = height;
    }

    /**
     * @param geometry the top-level 2D outline of the object
     */
    public StaticObject(final OTSLine3D geometry)
    {
        this(geometry, Length.ZERO);
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
    public final DirectedPoint getLocation() throws RemoteException
    {
        return this.geometry.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
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

}
