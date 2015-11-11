package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;

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
    private final Angle.Abs direction;

    /** the slope as an angle. Horizontal is 0 degrees. */
    private final Angle.Abs slope;

    /** the links connected to the Node. */
    private final Set<Link> links = new HashSet<Link>();

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle. Horizontal is 0 degrees.
     */
    public OTSNode(final String id, final OTSPoint3D point, final Angle.Abs direction, final Angle.Abs slope)
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
        this(id, point, new Angle.Abs(0.0, AngleUnit.SI), new Angle.Abs(0.0, AngleUnit.SI));
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
    public final void addLink(final Link link)
    {
        this.links.add(link);
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link> getLinks()
    {
        // returns a safe copy
        return new HashSet<Link>(this.links);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isDirectionallyConnectedTo(final GTUType gtuType, final Node toNode)
    {
        for (Link link : getLinks())
        {
            if (toNode.equals(link.getEndNode()) && link.getDirectionality(gtuType).isForwardOrBoth())
            {
                return true;
            }
            if (toNode.equals(link.getStartNode()) && link.getDirectionality(gtuType).isBackwardOrBoth())
            {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final Angle.Abs getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final Angle.Abs getSlope()
    {
        return this.slope;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation()
    {
        return this.point.getDirectedPoint();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
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
