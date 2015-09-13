package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AnglePlaneUnit;
import org.djunits.unit.AngleSlopeUnit;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * The Node is a point with an id. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSNode implements Node, LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the node id. */
    private final String id;

    /** the point. */
    private final OTSPoint3D point;

    /** the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians). */
    private final AnglePlane.Abs direction;

    /** the slope as an angle. */
    private final AngleSlope.Abs slope;

    /** the incoming links. */
    private final Set<Link> linksIn = new HashSet<Link>();

    /** the outgoing links. */
    private final Set<Link> linksOut = new HashSet<Link>();

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle.
     */
    public OTSNode(final String id, final OTSPoint3D point, final AnglePlane.Abs direction, final AngleSlope.Abs slope)
    {
        this.id = id;
        this.point = point;
        this.direction = direction;
        this.slope = slope;
    }

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     */
    public OTSNode(final String id, final OTSPoint3D point)
    {
        this(id, point, new AnglePlane.Abs(0.0, AnglePlaneUnit.SI), new AngleSlope.Abs(0.0, AngleSlopeUnit.SI));
    }

    /**
     * @return node id.
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return point.
     */
    public final OTSPoint3D getPoint()
    {
        return this.point;
    }

    /** {@inheritDoc} */
    @Override
    public final void addLinkIn(final Link linkIn)
    {
        this.linksIn.add(linkIn);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLinkOut(final Link linkOut)
    {
        this.linksOut.add(linkOut);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link> getLinksIn()
    {
        // XXX: should return a copy?
        return this.linksIn;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link> getLinksOut()
    {
        // XXX: should return a copy?
        return this.linksOut;
    }

    /** {@inheritDoc} */
    @Override
    public final AnglePlane.Abs getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final AngleSlope.Abs getSlope()
    {
        return this.slope;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return this.point.getDirectedPoint();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 10.0d);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "OTSNode [id=" + this.id + ", point=" + this.point + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        OTSNode other = (OTSNode) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }
}
