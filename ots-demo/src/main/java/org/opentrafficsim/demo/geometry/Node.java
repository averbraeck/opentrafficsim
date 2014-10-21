package org.opentrafficsim.demo.geometry;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractNode;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Node extends AbstractNode<String, Coordinate>
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

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(getPoint().x, getPoint().y, 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(0.1, 0.1, 0.0);
    }
}
