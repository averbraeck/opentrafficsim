package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractLink;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Link extends AbstractLink<String, AreaNode> implements LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20140921L;

    /** name. */
    private final String name;

    /** speed. */
    private double speed;

    /**
     * @param id the id of the link (Long).
     * @param nodeA the start node of the link.
     * @param nodeB the end node of the link.
     * @param length the length of the link with a unit.
     * @param name the human readable name of the link, e.g. a street name.
     */
    public Link(final String nr, final AreaNode nodeA, final AreaNode nodeB, final DoubleScalar<LengthUnit> length,
            final String name)
    {
        super(nr, nodeA, nodeB, length);
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(new double[]{getStartNode().getPoint().getX(), getStartNode().getPoint().getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(new Point3d(0.0d, 0.0d, 0.0d), new Point3d(getEndNode().getPoint().getX()
                - getStartNode().getPoint().getX(), getEndNode().getPoint().getY() - getStartNode().getPoint().getY(),
                0.0d));
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return speed
     */
    public final double getSpeed()
    {
        return this.speed;
    }

    /**
     * @param speed set speed
     */
    public final void setSpeed(final double speed)
    {
        this.speed = speed;
    }

}
