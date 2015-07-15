package org.opentrafficsim.core.network.geotools;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.network.AbstractLink;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * Link using Geotools Coordinates.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJan 4, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <IDL> the ID type of the Link, e.g., String or Integer.
 * @param <IDN> the ID type of the Node, e.g., String or Integer.
 */
public class LinkGeotools<IDL, IDN> extends AbstractLink<IDL, IDN, Coordinate, NodeGeotools<IDN>>
{
    /** */
    private static final long serialVersionUID = 20150104L;

    /** The geometry of this link. May be null. */
    private LinearGeometry geometry = null;

    /**
     * Construct a new link.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param length link length in a length unit
     * @param capacity link capacity in GTUs per hour
     */
    public LinkGeotools(final IDL id, final NodeGeotools<IDN> startNode, final NodeGeotools<IDN> endNode,
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
    public LinkGeotools(final IDL id, final NodeGeotools<IDN> startNode, final NodeGeotools<IDN> endNode,
            final DoubleScalar.Rel<LengthUnit> length)
    {
        super(id, startNode, endNode, length);
    }

    /**
     * @return the geometry of this link. <br>
     *         <b>Do not modify the returned object or chaos will ensue.</b>
     */
    public final LinearGeometry getGeometry()
    {
        return this.geometry;
    }

    /**
     * @param geometry set the geometry of this link.
     */
    public final void setGeometry(final LinearGeometry geometry)
    {
        this.geometry = geometry;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation() throws RemoteException
    {
        Point c = getGeometry().getLineString().getCentroid();
        return new DirectedPoint(new double[]{c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds() throws RemoteException
    {
        DirectedPoint c = getLocation();
        if (getGeometry() != null)
        {
            Envelope envelope = getGeometry().getLineString().getEnvelopeInternal();
            return new BoundingBox(new Point3d(envelope.getMinX() - c.x, envelope.getMinY() - c.y, 0.0d), new Point3d(
                    envelope.getMaxX() - c.x, envelope.getMaxY() - c.y, 0.0d));
        }
        double minX = Math.min(getEndNode().getX(), getStartNode().getX());
        double minY = Math.min(getEndNode().getY(), getStartNode().getY());
        double maxX = Math.max(getEndNode().getX(), getStartNode().getX());
        double maxY = Math.max(getEndNode().getY(), getStartNode().getY());
        return new BoundingBox(new Point3d(minX - c.x, minY - c.y, 0.0d), new Point3d(maxX - c.x, maxY - c.y, 0.0d));
    }

    /**
     * String ID implementation of the Point link.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author:
     * pknoppers $, initial versionJan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends LinkGeotools<String, String>
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
        public STR(final String id, final NodeGeotools.STR startNode, final NodeGeotools.STR endNode,
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
        public STR(final String id, final NodeGeotools.STR startNode, final NodeGeotools.STR endNode,
                final DoubleScalar.Rel<LengthUnit> length)
        {
            super(id, startNode, endNode, length);
        }
    }

    /**
     * Integer ID implementation of the Point link.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author:
     * pknoppers $, initial versionJan 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends LinkGeotools<Integer, Integer>
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
        public INT(final int id, final NodeGeotools.INT startNode, final NodeGeotools.INT endNode,
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
        public INT(final int id, final NodeGeotools.INT startNode, final NodeGeotools.INT endNode,
                final DoubleScalar.Rel<LengthUnit> length)
        {
            super(id, startNode, endNode, length);
        }
    }

}
