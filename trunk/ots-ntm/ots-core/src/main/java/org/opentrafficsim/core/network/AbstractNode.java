package org.opentrafficsim.core.network;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

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
 * @param <ID> the ID type.
 * @param <P> the point type, e.g., com.vividsolutions.jts.geom.Point, DirectedPoint. 
 */
public abstract class AbstractNode<ID, P> implements LocatableInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** the node id. */
    private ID id;

    /** the point. */
    private P point;

    /**
     * Construction of a Node.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     */

    public AbstractNode(final ID id, final P point)
    {
        this.id = id;
        this.point = point;
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

}
