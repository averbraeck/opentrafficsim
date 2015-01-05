package org.opentrafficsim.core.network;

import java.util.Set;

/**
 * The Node is a point with an id. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the Node.
 * @param <P> the point type, e.g., com.vividsolutions.jts.geom.Point, DirectedPoint.
 */
public interface Node<ID, P>
{
    /** @return node id. */
    ID getId();

    /** @return point. */
    P getPoint();

    /**
     * Add an incoming link to this Node.
     * @param linkIn the link to add.
     */
    void addLinkIn(Link<?, ? extends Node<ID, P>> linkIn);

    /**
     * Add an outgoing link to this Node.
     * @param linkOut the link to add.
     */
    void addLinkOut(final Link<?, ? extends Node<ID, P>> linkOut);

    /** @return linksIn. */
    Set<Link<?, ? extends Node<ID, P>>> getLinksIn();

    /** @return linksOut. */
    Set<Link<?, ? extends Node<ID, P>>> getLinksOut();
}
