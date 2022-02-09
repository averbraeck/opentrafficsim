package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The Node is a point with an id. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** the Network. */
    private final Network network;

    /** The node id. */
    private final String id;

    /** The point. */
    private final OTSPoint3D point;

    /** Heading. */
    private final double heading;

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
    private Map<GTUType, Map<Link, Set<Link>>> connections = null;

    /**
     * Construction of a Node.
     * @param network Network; the network.
     * @param id String; the id of the Node.
     * @param point OTSPoint3D; the point with usually an x and y setting.
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public OTSNode(final Network network, final String id, final OTSPoint3D point) throws NetworkException
    {
        this(network, id, point, Double.NaN);
    }

    /**
     * Construction of a Node.
     * @param network Network; the network.
     * @param id String; the id of the Node.
     * @param point OTSPoint3D; the point with usually an x and y setting.
     * @param heading double; heading
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public OTSNode(final Network network, final String id, final OTSPoint3D point, final double heading) throws NetworkException
    {
        Throw.whenNull(network, "network cannot be null");
        Throw.whenNull(id, "id cannot be null");
        Throw.whenNull(point, "point cannot be null");

        this.network = network;
        this.id = id;
        this.point = new OTSPoint3D(point.x, point.y, point.z);
        this.heading = heading;

        this.network.addNode(this);
    }

    /** {@inheritDoc} */
    @Override
    public final Network getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSPoint3D getPoint()
    {
        return this.point;
    }

    /** {@inheritDoc} */
    @Override
    public double getHeading()
    {
        return this.heading;
    }

    /** {@inheritDoc} */
    @Override
    public final void addLink(final Link link)
    {
        this.links.add(link);
        this.cachedLinks = null; // invalidate the cache
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLink(final Link link)
    {
        this.links.remove(link);
        this.cachedLinks = null; // invalidate the cache
    }

    /**
     * Add a single connection for a GTU type to the connections map. The data structures will be created if it does not exist
     * yet.
     * @param gtuType GTUType; the GTU type for which this connection is made
     * @param incomingLink Link; the link that connects to this Node
     * @param outgoingLink Link; the link that the GTU can use to depart from this Node when coming from the incoming link
     * @throws NetworkException in case one of the links is not (correctly) connected to this Node
     */
    public final void addConnection(final GTUType gtuType, final Link incomingLink, final Link outgoingLink)
            throws NetworkException
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

        if (!(incomingLink.getEndNode().equals(this) && incomingLink.getDirectionality(gtuType).isForwardOrBoth()
                || incomingLink.getStartNode().equals(this) && incomingLink.getDirectionality(gtuType).isBackwardOrBoth()))
        {
            throw new NetworkException("addConnection: incoming link " + incomingLink + " not connected to node " + this
                    + " for GTU type " + gtuType);
        }

        if (!(outgoingLink.getStartNode().equals(this) && outgoingLink.getDirectionality(gtuType).isForwardOrBoth()
                || outgoingLink.getEndNode().equals(this) && outgoingLink.getDirectionality(gtuType).isBackwardOrBoth()))
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
     * @param gtuType GTUType; the GTU type for which this connection is made
     * @param incomingLink Link; the link that connects to this Node
     * @param outgoingLinks Set&lt;Link&gt;; a set of links that the GTU can use to depart from this Node when coming from the
     *            incoming link
     * @throws NetworkException in case one of the links is not (correctly) connected to this Node
     */
    public final void addConnections(final GTUType gtuType, final Link incomingLink, final Set<Link> outgoingLinks)
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

        if (!((incomingLink.getEndNode().equals(this) && incomingLink.getDirectionality(gtuType).isForwardOrBoth())
                || (incomingLink.getStartNode().equals(this) && incomingLink.getDirectionality(gtuType).isBackwardOrBoth())))
        {
            throw new NetworkException("addConnections: incoming link " + incomingLink + " not connected to node " + this
                    + " for GTU type " + gtuType);
        }

        for (Link outgoingLink : outgoingLinks)
        {
            if (!((outgoingLink.getStartNode().equals(this) && outgoingLink.getDirectionality(gtuType).isForwardOrBoth())
                    || (outgoingLink.getEndNode().equals(this) && outgoingLink.getDirectionality(gtuType).isBackwardOrBoth())))
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

    /** {@inheritDoc} */
    @Override
    public final ImmutableSet<Link> getLinks()
    {
        if (this.cachedLinks == null)
        {
            this.cachedLinks = new ImmutableHashSet<>(this.links);
        }
        return this.cachedLinks;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Link> nextLinks(final GTUType gtuType, final Link prevLink) throws NetworkException
    {
        // ------------------------------------------- check consistency
        if (!this.links.contains(prevLink))
        {
            throw new NetworkException("nextLinks: incoming link " + prevLink + " for node " + this + " not in links set");
        }

        if (!(prevLink.getEndNode().equals(this) && prevLink.getDirectionality(gtuType).isForwardOrBoth()
                || prevLink.getStartNode().equals(this) && prevLink.getDirectionality(gtuType).isBackwardOrBoth()))
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
    public boolean isCentroid()
    {
        boolean result = false;
        for (Link link : getLinks())
        {
            if (!link.getLinkType().isConnector())
            {
                return false;
            }
            else
            {
                result = true;
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        return this.point.getDirectedPoint();
    }

    /** Margin around node in m when computing bounding sphere. */
    public static final double BOUNDINGRADIUS = 10.0;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        return new Bounds(-BOUNDINGRADIUS, BOUNDINGRADIUS, -BOUNDINGRADIUS, BOUNDINGRADIUS, -BOUNDINGRADIUS, BOUNDINGRADIUS);
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

    /**
     * Clone the OTSode for e.g., copying a network.
     * @param newNetwork Network; the new network to which the clone belongs
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public OTSNode clone1(final Network newNetwork) throws NetworkException
    {
        return new OTSNode(newNetwork, this.id, this.point);
    }

    /**
     * Complete the cloning of the OTSode for e.g., copying a network. Call this method after all the links have been
     * constructed in the new network.
     * @param newNetwork Network; the new network to which the clone belongs
     * @return the completed clone
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public OTSNode clone2(final Network newNetwork) throws NetworkException
    {
        OTSNode clone = (OTSNode) newNetwork.getNode(this.id);
        if (this.connections != null)
        {
            Map<GTUType, Map<Link, Set<Link>>> newConnections = new LinkedHashMap<>();
            for (GTUType gtuType : this.connections.keySet())
            {
                Map<Link, Set<Link>> newConnMap = new LinkedHashMap<>();
                for (Link link : this.connections.get(gtuType).keySet())
                {
                    Set<Link> newLinkSet = new LinkedHashSet<>();
                    for (Link setLink : this.connections.get(gtuType).get(link))
                    {
                        newLinkSet.add(newNetwork.getLink(setLink.getId()));
                    }
                    newConnMap.put(newNetwork.getLink(link.getId()), newLinkSet);
                }
                newConnections.put(gtuType, newConnMap);
            }
            clone.connections = newConnections;
        }
        return clone;
    }

}
