package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Set;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

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
public interface Node extends LocatableInterface, Serializable
{
    /** @return node id. */
    String getId();

    /** @return point. */
    OTSPoint3D getPoint();

    /** @return the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians). */
    Angle.Abs getDirection();

    /** @return the slope as an angle. Horizontal is 0 degrees. */
    Angle.Abs getSlope();

    /**
     * Add a link to this Node.
     * @param link the link to add.
     */
    void addLink(Link link);

    /** @return the links connected to this Node */
    Set<Link> getLinks();

    /**
     * Determine the links connecting from the previous link via this Node for the given GTU type.
     * @param gtuType the GTU type to determine the next links for
     * @param prevLink the incoming link to the Node
     * @return a set of links connecting from the previous link via this Node for the given GTU type
     * @throws NetworkException if thhe incoming link is not connected to this node for the given GTU type
     */
    Set<Link> nextLinks(GTUType gtuType, Link prevLink) throws NetworkException;

    /**
     * Check if the current node is linked to the given Node in the specified direction for the given GTUType. This can mean
     * there is a Link from this node to toNode, and the LongitudinalDirectionality for the Link between this node and toNode is
     * FORWARD or BOTH; or there is a Link from toNode to this node, and the LongitudinalDirectionality for the Link between
     * toNode and this node is BACKWARD or BOTH for the provided GTUType.
     * @param gtuType the GTU type to check the connection for.
     * @param toNode the to node
     * @return whether two nodes are linked in the specified direction.
     */
    boolean isDirectionallyConnectedTo(GTUType gtuType, Node toNode);

    /** {@inheritDoc} */
    @Override
    DirectedPoint getLocation();

    /** {@inheritDoc} */
    @Override
    Bounds getBounds();
}
