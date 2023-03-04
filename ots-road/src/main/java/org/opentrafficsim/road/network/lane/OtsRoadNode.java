package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OtsNode;

/**
 * The Node is a point with an id and a direction. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OtsRoadNode extends OtsNode
{
    /** ... */
    private static final long serialVersionUID = 20190528L;

    /** Direction of traffic at this node. */
    private final Direction direction;

    /**
     * Construct a new OtsRoaNode.
     * @param network Network; the network
     * @param id String; name of the node
     * @param point OtsPoint3d location of the node
     * @param direction Direction; driving direction at the node
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public OtsRoadNode(final Network network, final String id, final OtsPoint3d point, final Direction direction)
            throws NetworkException
    {
        super(network, id, point);
        Throw.whenNull(direction, "direction cannot be null");
        this.direction = direction;
    }

    @Override
    public final DirectedPoint getLocation()
    {
        OtsPoint3d p = getPoint();
        return new DirectedPoint(p.x, p.y, p.z, 0, 0, this.direction.si);
    }

}
