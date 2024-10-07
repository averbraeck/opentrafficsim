package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.base.geometry.PolygonShape;
import org.opentrafficsim.base.geometry.SpatialObject;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * The Node is a point with an id. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Aug 19, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Node
        implements HierarchicallyTyped<NodeType, Node>, SpatialObject, OtsLocatable, Serializable, Identifiable, Drawable
{
    /** */
    private static final long serialVersionUID = 20150722L;

    /** The Network. */
    private final Network network;

    /** The node id. */
    private final String id;

    /** The point. */
    private final OrientedPoint2d point;

    /** The contour. */
    private final Polygon2d contour;

    /** Shape. */
    private final OtsShape shape;

    /** The links connected to the Node. */
    private final Set<Link> links = new LinkedHashSet<>();

    /** The cached immutable set of links to return. */
    private ImmutableSet<Link> cachedLinks = null;

    /**
     * Map with connections per GTU type. When this map is null, the all connections that are possible for the GTU type will be
     * included, with the exception of the U-turn. When exceptions are taken into account, the map has to be completely filled
     * as it replaces the default. The map gives per GTU type a map of incoming links that are connected to outgoing links,
     * which are stored in a Set.
     */
    private Map<GtuType, Map<Link, Set<Link>>> connections = null;

    /**
     * Construction of a Node. Direction will be 0.0.
     * @param network the network.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public Node(final Network network, final String id, final Point2d point) throws NetworkException
    {
        this(network, id, new OrientedPoint2d(point.x, point.y, 0.0));
    }

    /**
     * Construction of a Node.
     * @param network the network.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param heading heading
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public Node(final Network network, final String id, final Point2d point, final Direction heading) throws NetworkException
    {
        this(network, id, new OrientedPoint2d(point.x, point.y, heading.si));
    }

    /**
     * Construction of a Node.
     * @param network the network.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public Node(final Network network, final String id, final OrientedPoint2d point) throws NetworkException
    {
        Throw.whenNull(network, "network cannot be null");
        Throw.whenNull(id, "id cannot be null");
        Throw.whenNull(point, "point cannot be null");

        this.network = network;
        this.id = id;
        this.point = point;

        double x = this.point.x;
        double y = this.point.y;
        this.contour = new Polygon2d(new Point2d(x - 0.5, y - 0.5), new Point2d(x - 0.5, y + 0.5),
                new Point2d(x + 0.5, y + 0.5), new Point2d(x + 0.5, y - 0.5));
        this.shape = new PolygonShape(OtsLocatable.relativeContour(this));

        this.network.addNode(this);
    }

    /**
     * Return the network in which this link is registered. Cannot be null.
     * @return the network in which this link is registered
     */
    public Network getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns the point without direction.
     * @return point.
     */
    public OrientedPoint2d getPoint()
    {
        return this.point;
    }

    /** {@inheritDoc} */
    @Override
    public Polygon2d getContour()
    {
        return this.contour;
    }

    /** {@inheritDoc} */
    @Override
    public OtsShape getShape()
    {
        return this.shape;
    }

    /**
     * Returns the heading.
     * @return heading.
     */
    public Direction getHeading()
    {
        return Direction.instantiateSI(this.point.dirZ);
    }

    /**
     * Add a link to this Node.
     * @param link the link to add.
     */
    public void addLink(final Link link)
    {
        this.links.add(link);
        this.cachedLinks = null; // invalidate the cache
    }

    /**
     * Remove a link from this Node.
     * @param link the link to remove.
     */
    public void removeLink(final Link link)
    {
        this.links.remove(link);
        this.cachedLinks = null; // invalidate the cache
    }

    /**
     * Add a single connection for a GTU type to the connections map. The data structures will be created if it does not exist
     * yet.
     * @param gtuType the GTU type for which this connection is made
     * @param incomingLink the link that connects to this Node
     * @param outgoingLink the link that the GTU can use to depart from this Node when coming from the incoming link
     * @throws NetworkException in case one of the links is not (correctly) connected to this Node
     */
    public void addConnection(final GtuType gtuType, final Link incomingLink, final Link outgoingLink) throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(incomingLink))
        {
            throw new NetworkException(
                    "addConnection: incoming link " + incomingLink + " for node " + this + " not in links set");
        }

        if (!this.links.contains(outgoingLink))
        {
            throw new NetworkException(
                    "addConnection: outgoing link " + outgoingLink + " for node " + this + " not in links set");
        }

        if (!(incomingLink.getEndNode().equals(this) && incomingLink.getType().isCompatible(gtuType)))
        {
            throw new NetworkException("addConnection: incoming link " + incomingLink + " not connected to node " + this
                    + " for GTU type " + gtuType);
        }

        if (!(outgoingLink.getStartNode().equals(this) && outgoingLink.getType().isCompatible(gtuType)))
        {
            throw new NetworkException("addConnection: outgoing link " + outgoingLink + " not connected to node " + this
                    + " for GTU type " + gtuType);
        }

        // ------------------------------------------- make datasets if needed
        if (this.connections == null)
        {
            this.connections = new LinkedHashMap<>();
        }

        if (!this.connections.containsKey(gtuType))
        {
            this.connections.put(gtuType, new LinkedHashMap<>());
        }

        Map<Link, Set<Link>> gtuMap = this.connections.get(gtuType);
        if (!gtuMap.containsKey(incomingLink))
        {
            gtuMap.put(incomingLink, new LinkedHashSet<>());
        }

        // ------------------------------------------- add the connection
        gtuMap.get(incomingLink).add(outgoingLink);
    }

    /**
     * Add a set of connections for a GTU type to the connections map. The data structures will be created if it does not exist
     * yet.
     * @param gtuType the GTU type for which this connection is made
     * @param incomingLink the link that connects to this Node
     * @param outgoingLinks a set of links that the GTU can use to depart from this Node when coming from the incoming link
     * @throws NetworkException in case one of the links is not (correctly) connected to this Node
     */
    public void addConnections(final GtuType gtuType, final Link incomingLink, final Set<Link> outgoingLinks)
            throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(incomingLink))
        {
            throw new NetworkException(
                    "addConnections: incoming link " + incomingLink + " for node " + this + " not in links set");
        }

        if (!this.links.containsAll(outgoingLinks))
        {
            throw new NetworkException(
                    "addConnections: outgoing links " + outgoingLinks + " for node " + this + " not all in links set");
        }

        if (!(incomingLink.getEndNode().equals(this) && incomingLink.getType().isCompatible(gtuType)))
        {
            throw new NetworkException("addConnections: incoming link " + incomingLink + " not connected to node " + this
                    + " for GTU type " + gtuType);
        }

        for (Link outgoingLink : outgoingLinks)
        {
            if (!(outgoingLink.getStartNode().equals(this) && outgoingLink.getType().isCompatible(gtuType)))
            {
                throw new NetworkException("addConnections: outgoing link " + outgoingLink + " not connected to node " + this
                        + " for GTU type " + gtuType);
            }
        }

        // ------------------------------------------- make datasets if needed
        if (this.connections == null)
        {
            this.connections = new LinkedHashMap<>();
        }

        if (!this.connections.containsKey(gtuType))
        {
            this.connections.put(gtuType, new LinkedHashMap<>());
        }

        Map<Link, Set<Link>> gtuMap = this.connections.get(gtuType);
        if (!gtuMap.containsKey(incomingLink))
        {
            gtuMap.put(incomingLink, new LinkedHashSet<>());
        }

        // ------------------------------------------- add the connections
        gtuMap.get(incomingLink).addAll(outgoingLinks);
    }

    /**
     * Returns the links connected to this node.
     * @return links.
     */
    public ImmutableSet<Link> getLinks()
    {
        if (this.cachedLinks == null)
        {
            this.cachedLinks = new ImmutableHashSet<>(this.links);
        }
        return this.cachedLinks;
    }

    /**
     * Determine the links connecting from the previous link via this Node for the given GTU type.
     * @param gtuType the GTU type to determine the next links for
     * @param prevLink the incoming link to the Node
     * @return a set of links connecting from the previous link via this Node for the given GTU type
     * @throws NetworkException if the incoming link is not connected to this node for the given GTU type
     */
    public Set<Link> nextLinks(final GtuType gtuType, final Link prevLink) throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(prevLink))
        {
            throw new NetworkException("nextLinks: incoming link " + prevLink + " for node " + this + " not in links set");
        }

        if (!(prevLink.getEndNode().equals(this) && prevLink.getType().isCompatible(gtuType)))
        {
            throw new NetworkException(
                    "nextLinks: incoming link " + prevLink + " not connected to node " + this + " for GTU type " + gtuType);
        }

        Set<Link> result = new LinkedHashSet<>();

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
            if ((link.getStartNode().equals(this) && link.getType().isCompatible(gtuType)))
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
     * Check if the current node is linked to the given Node for the given GtuType. This means there is a direct Link from this
     * node to toNode for the provided GtuType.
     * @param gtuType the GTU type to check the connection for.
     * @param toNode the to node
     * @return whether two nodes are linked for the GTU type.
     */
    public boolean isConnectedTo(final GtuType gtuType, final Node toNode)
    {
        for (Link link : getLinks())
        {
            if (toNode.equals(link.getEndNode()) && link.getType().isCompatible(gtuType))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the node has only connector links going in and/or out, and no other links.
     * @return whether the node is a centroid, i.e. it <b>only</b> has connector links going in and out
     */
    public boolean isCentroid()
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public NodeType getType()
    {
        return NodeType.NODE;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public OrientedPoint2d getLocation()
    {
        return this.point;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds2d getBounds()
    {
        return new Bounds2d(-1.0, 1.0, -1.0, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Node [id=" + this.id + ", point=" + this.point + "]";
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
        Node other = (Node) obj;
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
