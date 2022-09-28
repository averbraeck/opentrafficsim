package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Set;

import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuType;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * The Node is a point with an id. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Aug 19, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public interface Node extends Locatable, Serializable, Identifiable, Drawable
{
    /**
     * Return the network in which this link is registered. Cannot be null.
     * @return Network; the network in which this link is registered
     */
    Network getNetwork();

    /** @return node id. */
    @Override
    String getId();

    /** @return point. */
    OTSPoint3D getPoint();

    /** @return heading. */
    double getHeading();

    /**
     * Add a link to this Node.
     * @param link Link; the link to add.
     */
    void addLink(Link link);

    /**
     * Remove a link from this Node.
     * @param link Link; the link to remove.
     */
    void removeLink(Link link);

    /** @return a safe copy of the links connected to this Node */
    ImmutableSet<Link> getLinks();

    /**
     * Determine the links connecting from the previous link via this Node for the given GTU type.
     * @param gtuType GtuType; the GTU type to determine the next links for
     * @param prevLink Link; the incoming link to the Node
     * @return a set of links connecting from the previous link via this Node for the given GTU type
     * @throws NetworkException if the incoming link is not connected to this node for the given GTU type
     */
    Set<Link> nextLinks(GtuType gtuType, Link prevLink) throws NetworkException;

    /**
     * Check if the current node is linked to the given Node in the specified direction for the given GtuType. This can mean
     * there is a Link from this node to toNode, and the LongitudinalDirectionality for the Link between this node and toNode is
     * FORWARD or BOTH; or there is a Link from toNode to this node, and the LongitudinalDirectionality for the Link between
     * toNode and this node is BACKWARD or BOTH for the provided GtuType.
     * @param gtuType GtuType; the GTU type to check the connection for.
     * @param toNode Node; the to node
     * @return whether two nodes are linked in the specified direction.
     */
    boolean isDirectionallyConnectedTo(GtuType gtuType, Node toNode);

    /**
     * Checks whether the node has only connector links going in and/or out, and no other links.
     * @return boolean; whether the node is a centroid, i.e. it <b>only</b> has connector links going in and out
     */
    boolean isCentroid();

    @Override
    Bounds getBounds();

    @Override
    DirectedPoint getLocation();
}
