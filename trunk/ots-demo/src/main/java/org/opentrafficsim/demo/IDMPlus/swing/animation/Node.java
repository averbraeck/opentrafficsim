package org.opentrafficsim.demo.IDMPlus.swing.animation;

import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractNode;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class Node extends AbstractNode<String, Point2d>
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /**
     * @param id the id of the node
     * @param point the node coordinates (x, y).
     */
    public Node(final String id, final Point2d point)
    {
        super(id, point);
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{getPoint().getX(), getPoint().getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 10.0d);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "Node [point=" + getPoint() + "]";
    }

}
