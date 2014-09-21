package org.opentrafficsim.demo.ntm;

import java.rmi.RemoteException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractNode;

import com.vividsolutions.jts.geom.Point;

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
public class AreaNode extends AbstractNode<Long, Point>
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** the area to which the node belongs. */
    private final Area area;

    /**
     * @param centroid the center of the area for the simplified graph.
     * @param area the area to which the node belongs.
     */
    public AreaNode(final Point centroid, final Area area)
    {
        super(area.getNr(), centroid);
        this.area = area;
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

    /**
     * @return area
     */
    public final Area getArea()
    {
        return this.area;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "Node [centroid=" + getPoint() + "]";
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.area == null) ? 0 : this.area.hashCode());
        result = prime * result + ((getPoint() == null) ? 0 : getPoint().hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:designforextension"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AreaNode other = (AreaNode) obj;
        if (this.area == null)
        {
            if (other.area != null)
                return false;
        }
        else if (!this.area.equals(other.area))
            return false;
        if (getPoint() == null)
        {
            if (other.getPoint() != null)
                return false;
        }
        else if (!getPoint().equals(other.getPoint()))
            return false;
        return true;
    }

}
