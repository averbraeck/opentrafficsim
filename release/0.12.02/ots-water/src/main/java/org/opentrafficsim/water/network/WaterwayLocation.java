package org.opentrafficsim.water.network;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSGeometryException;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class WaterwayLocation implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20161106L;

    /** the waterway. */
    private final Waterway waterway;

    /** position along the waterway, in the direction of the design line. */
    private final Length position;

    /** cached location. */
    private final DirectedPoint location;

    /**
     * @param waterway the waterway
     * @param position position along the waterway, in the direction of the design line
     * @throws OTSGeometryException in case the position is less than zero, or more than the length of the waterway.
     */
    public WaterwayLocation(final Waterway waterway, final Length position) throws OTSGeometryException
    {
        Throw.whenNull(waterway, "waterway cannot be null");
        Throw.whenNull(position, "position cannot be null");
        this.waterway = waterway;
        this.position = position;
        this.location = waterway.getDesignLine().getLocation(position);
    }

    /**
     * @return waterway the waterway
     */
    public final Waterway getWaterway()
    {
        return this.waterway;
    }

    /**
     * @return position position along the waterway, in the direction of the design line
     */
    public final Length getPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingSphere();
    }

}
