package org.opentrafficsim.core.network.point2d;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractLink;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 4, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <IDL> the ID type of the Link, e.g., String or Integer.
 * @param <IDN> the ID type of the Node, e.g., String or Integer.
 */
public class LinkPoint2D<IDL, IDN> extends AbstractLink<IDL, IDN, Point2D, NodePoint2D<IDN>>
{
    /** */
    private static final long serialVersionUID = 20150104L;

    /**
     * Construct a new link.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param length link length in a length unit
     * @param capacity link capacity in GTUs per hour
     */
    public LinkPoint2D(final IDL id, final NodePoint2D<IDN> startNode, final NodePoint2D<IDN> endNode,
        final DoubleScalar.Rel<LengthUnit> length, final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        super(id, startNode, endNode, length, capacity);
    }

    /**
     * Construct a new link with infinite capacity.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param length link length in a length unit
     */
    public LinkPoint2D(final IDL id, final NodePoint2D<IDN> startNode, final NodePoint2D<IDN> endNode,
        final DoubleScalar.Rel<LengthUnit> length)
    {
        super(id, startNode, endNode, length);
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        double x = (getEndNode().getX() - getStartNode().getX()) / 2.0;
        double y = (getEndNode().getY() - getStartNode().getY()) / 2.0;
        return new DirectedPoint(new double[] {x, y, 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        DirectedPoint c = getLocation();
        double minX = Math.min(getEndNode().getX(), getStartNode().getX());
        double minY = Math.min(getEndNode().getY(), getStartNode().getY());
        double maxX = Math.max(getEndNode().getX(), getStartNode().getX());
        double maxY = Math.max(getEndNode().getY(), getStartNode().getY());
        return new BoundingBox(new Point3d(minX - c.x, minY - c.y, 0.0d), new Point3d(maxX - c.x, maxY - c.y, 0.0d));
    }

    /**
     * String ID implementation of the Point2D link.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends LinkPoint2D<String, String>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * Construct a new link.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param length link length in a length unit
         * @param capacity link capacity in GTUs per hour
         */
        public STR(final String id, final NodePoint2D.STR startNode, final NodePoint2D.STR endNode,
            final DoubleScalar.Rel<LengthUnit> length, final DoubleScalar.Abs<FrequencyUnit> capacity)
        {
            super(id, startNode, endNode, length, capacity);
        }

        /**
         * Construct a new link with infinite capacity.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param length link length in a length unit
         */
        public STR(final String id, final NodePoint2D.STR startNode, final NodePoint2D.STR endNode,
            final DoubleScalar.Rel<LengthUnit> length)
        {
            super(id, startNode, endNode, length);
        }
    }

    /**
     * Integer ID implementation of the Point2D link.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends LinkPoint2D<Integer, Integer>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * Construct a new link.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param length link length in a length unit
         * @param capacity link capacity in GTUs per hour
         */
        public INT(final int id, final NodePoint2D.INT startNode, final NodePoint2D.INT endNode,
            final DoubleScalar.Rel<LengthUnit> length, final DoubleScalar.Abs<FrequencyUnit> capacity)
        {
            super(id, startNode, endNode, length, capacity);
        }

        /**
         * Construct a new link with infinite capacity.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param length link length in a length unit
         */
        public INT(final int id, final NodePoint2D.INT startNode, final NodePoint2D.INT endNode,
            final DoubleScalar.Rel<LengthUnit> length)
        {
            super(id, startNode, endNode, length);
        }
    }

}
