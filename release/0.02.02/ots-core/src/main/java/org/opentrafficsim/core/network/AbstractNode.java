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

import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * The Node is a point with an id. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type.
 * @param <P> the point type, e.g., com.vividsolutions.jts.geom.Point, DirectedPoint.
 */
public abstract class AbstractNode<ID, P> implements Node<ID, P>, LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** the node id. */
    private final ID id;

    /** the point. */
    private final P point;

    /** the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians). */
    private final DoubleScalar.Abs<AnglePlaneUnit> direction;

    /** the slope as an angle. */
    private final DoubleScalar.Abs<AngleSlopeUnit> slope;

    /** the incoming links. */
    private final Set<Link<?, ? extends Node<ID, P>>> linksIn = new HashSet<Link<?, ? extends Node<ID, P>>>();

    /** the outgoing links. */
    private final Set<Link<?, ? extends Node<ID, P>>> linksOut = new HashSet<Link<?, ? extends Node<ID, P>>>();

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle.
     */
    public AbstractNode(final ID id, final P point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope)
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
    public AbstractNode(final ID id, final P point)
    {
        this(id, point, new DoubleScalar.Abs<AnglePlaneUnit>(0.0, AnglePlaneUnit.SI),
                new DoubleScalar.Abs<AngleSlopeUnit>(0.0, AngleSlopeUnit.SI));
    }

    /**
     * @return node id.
     */
    public final ID getId()
    {
        return this.id;
    }

    /**
     * @return point.
     */
    public final P getPoint()
    {
        return this.point;
    }

    /**
     * @return the x-value of the point.
     */
    public abstract double getX();

    /**
     * @return the y-value of the point.
     */
    public abstract double getY();

    /**
     * @return the z-value of the point.
     */
    public abstract double getZ();

    /** {@inheritDoc} */
    @Override
    public final void addLinkIn(final Link<?, ? extends Node<ID, P>> linkIn)
    {
        this.linksIn.add(linkIn);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLinkOut(final Link<?, ? extends Node<ID, P>> linkOut)
    {
        this.linksOut.add(linkOut);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link<?, ? extends Node<ID, P>>> getLinksIn()
    {
        // XXX: should return a copy?
        return this.linksIn;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link<?, ? extends Node<ID, P>>> getLinksOut()
    {
        // XXX: should return a copy?
        return this.linksOut;
    }

    /**
     * @return direction.
     */
    public final DoubleScalar.Abs<AnglePlaneUnit> getDirection()
    {
        return this.direction;
    }

    /**
     * @return slope.
     */
    public final DoubleScalar.Abs<AngleSlopeUnit> getSlope()
    {
        return this.slope;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{getX(), getY(), getZ()});
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 10.0d);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "Node " + this.id;
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
        AbstractNode<ID, P> other = (AbstractNode<ID, P>) obj;
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
