package org.opentrafficsim.core.network;

import java.io.Serializable;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.geometry.OTSLine3D;

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
 */
public class OTSLink implements Link, Serializable, LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** Link id. */
    private final String id;

    /** Start node (directional). */
    private final Node startNode;

    /** End node (directional). */
    private final Node endNode;

    /** Design line of the link. */
    private final OTSLine3D designLine;

    /** Link capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private Frequency.Abs capacity;

    /**
     * Construct a new link.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param designLine the OTSLine3D design line of the Link
     * @param capacity link capacity in GTUs per hour
     */
    public OTSLink(final String id, final Node startNode, final Node endNode, final OTSLine3D designLine,
        final Frequency.Abs capacity)
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
    public OTSLink(final String id, final Node startNode, final Node endNode, final OTSLine3D designLine)
    {
        this(id, startNode, endNode, designLine, new Frequency.Abs(Double.POSITIVE_INFINITY, PER_SECOND));
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Node getStartNode()
    {
        return this.startNode;
    }

    /** {@inheritDoc} */
    @Override
    public final Node getEndNode()
    {
        return this.endNode;
    }

    /** {@inheritDoc} */
    @Override
    public final Frequency.Abs getCapacity()
    {
        return this.capacity;
    }

    /** {@inheritDoc} */
    @Override
    public final void setCapacity(final Frequency.Abs capacity)
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
    public final Length.Rel getLength()
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
    public final DirectedPoint getLocation() 
    {
        // TODO maybe do without transformation to a LineString and cache the centroid?
        Point c = this.designLine.getLineString().getCentroid();
        return new DirectedPoint(new double[]{c.getX(), c.getY(), 0.0d});
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() 
    {
        // TODO maybe do without transformation to a LineString and cache the envelope / bounds?
        DirectedPoint c = getLocation();
        Envelope envelope = this.designLine.getLineString().getEnvelopeInternal();
        return new BoundingBox(new Point3d(envelope.getMinX() - c.x, envelope.getMinY() - c.y, 0.0d), new Point3d(envelope
            .getMaxX()
            - c.x, envelope.getMaxY() - c.y, 0.0d));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endNode == null) ? 0 : this.endNode.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.startNode == null) ? 0 : this.startNode.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OTSLink other = (OTSLink) obj;
        if (this.endNode == null)
        {
            if (other.endNode != null)
                return false;
        }
        else if (!this.endNode.equals(other.endNode))
            return false;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.startNode == null)
        {
            if (other.startNode != null)
                return false;
        }
        else if (!this.startNode.equals(other.startNode))
            return false;
        return true;
    }
}
