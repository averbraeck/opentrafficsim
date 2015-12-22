package org.opentrafficsim.core.object;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;

/**
 * A static object that a GTU might have to avoid, or which can cause occlusion for perception.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class StaticObject implements ObjectInterface
{
    /** the top-level 2D outline of the object. */
    private final OTSLine3D geometry;

    /** the height of the object. */
    private final Length.Rel height;

    /**
     * @param geometry the top-level 2D outline of the object
     * @param height the height of the object
     */
    public StaticObject(final OTSLine3D geometry, final Length.Rel height)
    {
        super();
        this.geometry = geometry;
        this.height = height;
    }

    /**
     * @return geometry
     */
    public final OTSLine3D getGeometry()
    {
        return this.geometry;
    }

    /**
     * @return height
     */
    public final Length.Rel getHeight()
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

}
