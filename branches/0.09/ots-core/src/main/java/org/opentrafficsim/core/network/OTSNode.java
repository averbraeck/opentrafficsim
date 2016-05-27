package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Direction;
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
public class OTSNode implements Node, Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The node id. */
    private final String id;

    /** The point. */
    private final OTSPoint3D point;

    /** The 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians). */
    private final Direction direction;

    /** The slope as an angle. Horizontal is 0 degrees. */
    private final Direction slope;

    /** The links connected to the Node. */
    private final Set<Link> links = new HashSet<Link>();

    /**
     * Map with connections per GTU type. When this map is null, the all connections that are possible for the GTU type will be
     * included, with the exception of the U-turn. When exceptions are taken into account, the map has to be completely filled
     * as it replaces the default. The map gives per GTU type a map of incoming links that are connected to outgoing links,
     * which are stored in a Set.
     */
    private Map<GTUType, Map<Link, Set<Link>>> connections = null;

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle. Horizontal is 0 degrees.
     */
    public OTSNode(final String id, final OTSPoint3D point, final Direction direction, final Direction slope)
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
        this(id, point, new Direction(0.0, AngleUnit.SI), new Direction(0.0, AngleUnit.SI));
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
    public final void removeLink(final Link link)
    {
        this.links.remove(link);
    }

    /**
     * Add a single connection for a GTU type to the connections map. The data structures will be created is it does not exist
     * yet.
     * @param gtuType the GTU type for which this connection is made
     * @param incomingLink the link that connects to this Node
     * @param outgoingLink the link that the GTU can use to depart from this Node when coming from the incoming link
     * @throws NetworkException in case one of the links is not (correctly) connected to this Node
     */
    public final void addConnection(final GTUType gtuType, final Link incomingLink, final Link outgoingLink)
        throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(incomingLink))
        {
            throw new NetworkException("addConnection: incoming link " + incomingLink + " for node " + this
                + " not in links set");
        }

        if (!this.links.contains(outgoingLink))
        {
            throw new NetworkException("addConnection: outgoing link " + outgoingLink + " for node " + this
                + " not in links set");
        }

        if (!(incomingLink.getEndNode().equals(this) && incomingLink.getDirectionality(gtuType).isForwardOrBoth() || incomingLink
            .getStartNode().equals(this) && incomingLink.getDirectionality(gtuType).isBackwardOrBoth()))
        {
            throw new NetworkException("addConnection: incoming link " + incomingLink + " not connected to node "
                + this + " for GTU type " + gtuType);
        }

        if (!(outgoingLink.getStartNode().equals(this) && outgoingLink.getDirectionality(gtuType).isForwardOrBoth() || outgoingLink
            .getEndNode().equals(this) && outgoingLink.getDirectionality(gtuType).isBackwardOrBoth()))
        {
            throw new NetworkException("addConnection: outgoing link " + outgoingLink + " not connected to node "
                + this + " for GTU type " + gtuType);
        }

        // ------------------------------------------- make datasets if needed
        if (this.connections == null)
        {
            this.connections = new HashMap<>();
        }

        if (!this.connections.containsKey(gtuType))
        {
            this.connections.put(gtuType, new HashMap<>());
        }

        Map<Link, Set<Link>> gtuMap = this.connections.get(gtuType);
        if (!gtuMap.containsKey(incomingLink))
        {
            gtuMap.put(incomingLink, new HashSet<>());
        }

        // ------------------------------------------- add the connection
        gtuMap.get(incomingLink).add(outgoingLink);
    }

    /**
     * Add a set of connections for a GTU type to the connections map. The data structures will be created is it does not exist
     * yet.
     * @param gtuType the GTU type for which this connection is made
     * @param incomingLink the link that connects to this Node
     * @param outgoingLinks a set of links that the GTU can use to depart from this Node when coming from the incoming link
     * @throws NetworkException in case one of the links is not (correctly) connected to this Node
     */
    public final void addConnections(final GTUType gtuType, final Link incomingLink, final Set<Link> outgoingLinks)
        throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(incomingLink))
        {
            throw new NetworkException("addConnections: incoming link " + incomingLink + " for node " + this
                + " not in links set");
        }

        if (!this.links.containsAll(outgoingLinks))
        {
            throw new NetworkException("addConnections: outgoing links " + outgoingLinks + " for node " + this
                + " not all in links set");
        }

        if (!((incomingLink.getEndNode().equals(this) && incomingLink.getDirectionality(gtuType).isForwardOrBoth()) || (incomingLink
            .getStartNode().equals(this) && incomingLink.getDirectionality(gtuType).isBackwardOrBoth())))
        {
            throw new NetworkException("addConnections: incoming link " + incomingLink + " not connected to node "
                + this + " for GTU type " + gtuType);
        }

        for (Link outgoingLink : outgoingLinks)
        {
            if (!((outgoingLink.getStartNode().equals(this) && outgoingLink.getDirectionality(gtuType)
                .isForwardOrBoth()) || (outgoingLink.getEndNode().equals(this) && outgoingLink.getDirectionality(
                gtuType).isBackwardOrBoth())))
            {
                throw new NetworkException("addConnections: outgoing link " + outgoingLink + " not connected to node "
                    + this + " for GTU type " + gtuType);
            }
        }

        // ------------------------------------------- make datasets if needed
        if (this.connections == null)
        {
            this.connections = new HashMap<>();
        }

        if (!this.connections.containsKey(gtuType))
        {
            this.connections.put(gtuType, new HashMap<>());
        }

        Map<Link, Set<Link>> gtuMap = this.connections.get(gtuType);
        if (!gtuMap.containsKey(incomingLink))
        {
            gtuMap.put(incomingLink, new HashSet<>());
        }

        // ------------------------------------------- add the connections
        gtuMap.get(incomingLink).addAll(outgoingLinks);
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
    public final Set<Link> nextLinks(final GTUType gtuType, final Link prevLink) throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(prevLink))
        {
            throw new NetworkException("nextLinks: incoming link " + prevLink + " for node " + this
                + " not in links set");
        }

        if (!(prevLink.getEndNode().equals(this) && prevLink.getDirectionality(gtuType).isForwardOrBoth() || prevLink
            .getStartNode().equals(this) && prevLink.getDirectionality(gtuType).isBackwardOrBoth()))
        {
            throw new NetworkException("nextLinks: incoming link " + prevLink + " not connected to node " + this
                + " for GTU type " + gtuType);
        }

        Set<Link> result = new HashSet<>();

        // -------------------------------- check if explicit connections are present
        if (this.connections != null)
        {
            if (!this.connections.containsKey(gtuType))
            {
                return result;
            }
            if (!this.connections.get(gtuType).containsKey(prevLink))
            {
                return result;
            }
            result.addAll(this.connections.get(gtuType).get(prevLink)); // defensive copy
            return result;
        }

        // ----------------------------- defensive copy of the connections for the gtuType
        for (Link link : getLinks())
        {
            if ((link.getStartNode().equals(this) && link.getDirectionality(gtuType).isForwardOrBoth())
                || (link.getEndNode().equals(this) && link.getDirectionality(gtuType).isBackwardOrBoth()))
            {
                if (!link.equals(prevLink)) // no U-turn
                {
                    result.add(link);
                }
            }
        }
        return result;
    }

    /**
     * Note: this method does not take into account explicitly defined connections, as the previous link is not given. <br>
     * {@inheritDoc}
     */
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
    public final Direction getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final Direction getSlope()
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
