package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class OTSLink<LINKID, NODEID> implements Link<LINKID, NODEID>, Serializable, LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** Link id. */
    private final LINKID id;

    /** Start node (directional). */
    private final Node<NODEID> startNode;

    /** End node (directional). */
    private final Node<NODEID> endNode;

    /** Design line of the link. */
    private final OTSLine3D designLine;

    /** Link capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar.Abs<FrequencyUnit> capacity;

    /**
     * Construct a new link.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param designLine the OTSLine3D design line of the Link
     * @param capacity link capacity in GTUs per hour
     */
    public OTSLink(final LINKID id, final Node<NODEID> startNode, final Node<NODEID> endNode, final OTSLine3D designLine,
        final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        // TODO Add directionality to a link?
        this.startNode.addLinkOut(this);
        this.endNode.addLinkIn(this);
        this.designLine = designLine;
        setCapacity(capacity);
    }

    /**
     * Construct a new link with infinite capacity.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param designLine the OTSLine3D design line of the Link
     */
    public OTSLink(final LINKID id, final Node<NODEID> startNode, final Node<NODEID> endNode, final OTSLine3D designLine)
    {
        this(id, startNode, endNode, designLine, new DoubleScalar.Abs<FrequencyUnit>(Double.POSITIVE_INFINITY,
            FrequencyUnit.PER_SECOND));
    }

    /** {@inheritDoc} */
    @Override
    public final LINKID getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Node<NODEID> getStartNode()
    {
        return this.startNode;
    }

    /** {@inheritDoc} */
    @Override
    public final Node<NODEID> getEndNode()
    {
        return this.endNode;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<FrequencyUnit> getCapacity()
    {
        return this.capacity;
    }

    /** {@inheritDoc} */
    @Override
    public final void setCapacity(final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        this.capacity = capacity;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSLine3D getDesignLine()
    {
        return this.designLine;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.designLine.getLength();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return this.id.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        // TODO maybe do without transformation to a LineString and cache the centroid?
        Point c = this.designLine.getLineString().getCentroid();
        return new DirectedPoint(new double[] {c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        // TODO maybe do without transformation to a LineString and cache the envelope / bounds?
        DirectedPoint c = getLocation();
        Envelope envelope = this.designLine.getLineString().getEnvelopeInternal();
        return new BoundingBox(new Point3d(envelope.getMinX() - c.x, envelope.getMinY() - c.y, 0.0d), new Point3d(envelope
            .getMaxX()
            - c.x, envelope.getMaxY() - c.y, 0.0d));
    }
}
