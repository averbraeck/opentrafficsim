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

import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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
 * @param <NODEID> the NODEID type.
 */
public class OTSNode<NODEID> implements Node<NODEID>, LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** the node id. */
    private final NODEID id;

    /** the point. */
    private final OTSPoint3D point;

    /** the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians). */
    private final DoubleScalar.Abs<AnglePlaneUnit> direction;

    /** the slope as an angle. */
    private final DoubleScalar.Abs<AngleSlopeUnit> slope;

    /** the incoming links. */
    private final Set<Link<?, NODEID>> linksIn = new HashSet<Link<?, NODEID>>();

    /** the outgoing links. */
    private final Set<Link<?, NODEID>> linksOut = new HashSet<Link<?, NODEID>>();

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle.
     */
    public OTSNode(final NODEID id, final OTSPoint3D point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
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
    public OTSNode(final NODEID id, final OTSPoint3D point)
    {
        this(id, point, new DoubleScalar.Abs<AnglePlaneUnit>(0.0, AnglePlaneUnit.SI), new DoubleScalar.Abs<AngleSlopeUnit>(
            0.0, AngleSlopeUnit.SI));
    }

    /**
     * @return node id.
     */
    public final NODEID getId()
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
    public final void addLinkIn(final Link<?, NODEID> linkIn)
    {
        this.linksIn.add(linkIn);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLinkOut(final Link<?, NODEID> linkOut)
    {
        this.linksOut.add(linkOut);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link<?, NODEID>> getLinksIn()
    {
        // XXX: should return a copy?
        return this.linksIn;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link<?, NODEID>> getLinksOut()
    {
        // XXX: should return a copy?
        return this.linksOut;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AnglePlaneUnit> getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AngleSlopeUnit> getSlope()
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
        OTSNode<NODEID> other = (OTSNode<NODEID>) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        return true;
    }

    /**
     * String ID implementation of the OTSNode.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version an 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends OTSNode<String>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * @param id String id.
         * @param point the location of the Node.
         */
        public STR(final String id, final OTSPoint3D point)
        {
            super(id, point);
        }

        /**
         * Construct a new Node.
         * @param id ID; the String Id of the new Node
         * @param point OTSPoint3D; the location of the new Node
         * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
         * @param slope the slope as an angle.
         */
        public STR(final String id, final OTSPoint3D point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope)
        {
            super(id, point, direction, slope);
        }
    }

    /**
     * Integer ID implementation of the OTSNode.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version an 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends OTSNode<Integer>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * @param id Integer id.
         * @param point the location of the Node.
         */
        public INT(final int id, final OTSPoint3D point)
        {
            super(id, point);
        }

        /**
         * Construct a new Node.
         * @param id ID; the Integer Id of the new Node
         * @param point OTSPoint3D; the location of the new Node
         * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
         * @param slope the slope as an angle.
         */
        public INT(final int id, final OTSPoint3D point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope)
        {
            super(id, point, direction, slope);
        }
    }

}
