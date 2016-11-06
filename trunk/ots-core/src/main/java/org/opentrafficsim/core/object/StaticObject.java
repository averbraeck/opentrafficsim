package org.opentrafficsim.core.object;

import java.io.Serializable;

import javax.media.j3d.Bounds;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;

import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.Throw;
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

    /** the id. */
    private final String id;

    /** The top-level 2D outline of the object. */
    private final OTSLine3D geometry;

    /** The height of the object. */
    private final Length height;

    /**
     * @param id the id
     * @param geometry the top-level 2D outline of the object
     * @param height the height of the object
     */
    public StaticObject(final String id, final OTSLine3D geometry, final Length height)
    {
        super();

        Throw.whenNull(id, "object id cannot be null");
        Throw.whenNull(geometry, "geometry cannot be null");
        Throw.whenNull(height, "geometry cannot be null");

        this.id = id;
        this.geometry = geometry;
        this.height = height;
    }

    /**
     * @param id the id
     * @param geometry the top-level 2D outline of the object
     */
    public StaticObject(final String id, final OTSLine3D geometry)
    {
        this(id, geometry, Length.ZERO);
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
    public final DirectedPoint getLocation()
    {
        return this.geometry.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
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
     * @param newNetwork the new network to which the clone belongs
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public StaticObject clone(final Network newNetwork, final OTSSimulatorInterface newSimulator, final boolean animation)
            throws NetworkException
    {
        return new StaticObject(this.id, this.geometry, this.height);
    }

}
