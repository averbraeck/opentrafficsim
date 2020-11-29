package org.opentrafficsim.road.network.lane;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * The Node is a point with an id and a direction. It is used in the network to connect Links.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2019-04-25 17:00:14 +0200 (Thu, 25 Apr 2019) $, @version $Revision: 5424 $, by $Author: pknoppers $,
 * initial version May 5, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class OTSRoadNode extends OTSNode
{
    /** ... */
    private static final long serialVersionUID = 20190528L;

    /** Direction of traffic at this node. */
    private final Direction direction;

    /**
     * Construct a new OTSRoadNode.
     * @param network Network; the network
     * @param id String; name of the node
     * @param point OTSPoint3D location of the node
     * @param direction Direction; driving direction at the node
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public OTSRoadNode(final Network network, final String id, final OTSPoint3D point, final Direction direction)
            throws NetworkException
    {
        super(network, id, point);
        Throw.whenNull(direction, "direction cannot be null");
        this.direction = direction;
    }

    @Override
    public final DirectedPoint getLocation()
    {
        OTSPoint3D p = getPoint();
        return new DirectedPoint(p.x, p.y, p.z, 0, 0, this.direction.si);
    }
    
}
