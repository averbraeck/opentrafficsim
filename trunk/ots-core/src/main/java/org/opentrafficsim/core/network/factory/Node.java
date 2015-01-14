package org.opentrafficsim.core.network.factory;

import org.opentrafficsim.core.network.geotools.NodeGeotools;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Node extends NodeGeotools.STR
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param id id
     * @param coordinate coordinate
     */
    public Node(final String id, final Coordinate coordinate)
    {
        super(id, coordinate);
    }
}
