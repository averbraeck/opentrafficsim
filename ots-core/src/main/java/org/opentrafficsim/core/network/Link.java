package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.SpatialObject;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.core.gtu.Gtu;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * A standard implementation of a link between two Nodes.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Aug 19, 2014 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Link extends LocalEventProducer
        implements HierarchicallyTyped<LinkType, Link>, SpatialObject, Locatable, Serializable, Identifiable, Drawable
{

    /** */
    private static final long serialVersionUID = 20150101L;

    /**
     * The <b>timed</b> event type for pub/sub indicating the removal of a GTU from the link. <br>
     * Payload: Object[] {String gtuId, int count_after_removal}
     */
    public static final EventType GTU_REMOVE_EVENT = new EventType("LINK.GTU.REMOVE",
            new MetaData("GTU exited link", "GTU removed from link", new ObjectDescriptor[] {
                    new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Number of GTUs in link", "Resulting number of GTUs in link", Integer.class)}));

    /**
     * The <b>timed</b> event type for pub/sub indicating the addition of a GTU to the link. <br>
     * Payload: Object[] {String gtuId, int count_after_addition}
     */
    public static final EventType GTU_ADD_EVENT = new EventType("LINK.GTU.ADD",
            new MetaData("GTU entered link", "GTU added to link", new ObjectDescriptor[] {
                    new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Number of GTUs in link", "Resulting number of GTUs in link", Integer.class)}));

    /** the Network. */
    private final Network network;

    /** Link id. */
    private final String id;

    /** Start node (directional). */
    private final Node startNode;

    /** End node (directional). */
    private final Node endNode;

    /** Link type to indicate compatibility with GTU types. */
    private final LinkType linkType;

    /** Design line of the link. */
    private final OtsLine3d designLine;

    /** the shape. */
    private final OtsShape shape;

    /** The GTUs on this Link. */
    private final Set<Gtu> gtus = new LinkedHashSet<>();

    /**
     * Construct a new link.
     * @param network Network; the network to which the link belongs
     * @param id String; the link id
     * @param startNode Node; start node (directional)
     * @param endNode Node; end node (directional)
     * @param linkType LinkType; Link type to indicate compatibility with GTU types
     * @param designLine OtsLine3d; the OtsLine3d design line of the Link
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Link(final Network network, final String id, final Node startNode, final Node endNode, final LinkType linkType,
            final OtsLine3d designLine) throws NetworkException
    {
        Throw.whenNull(network, "network cannot be null");
        Throw.whenNull(id, "id cannot be null");
        Throw.whenNull(startNode, "startNode cannot be null (link %s)", id);
        Throw.whenNull(endNode, "endNode cannot be null (link %s)", id);
        Throw.whenNull(linkType, "linkType cannot be null (link %s)", id);
        Throw.whenNull(designLine, "designLine cannot be null (link %s)", id);

        this.network = network;
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.linkType = linkType;
        this.startNode.addLink(this);
        this.endNode.addLink(this);
        this.designLine = designLine;
        try
        {
            this.shape = new OtsShape(this.designLine.offsetLine(0.5).getPoints());
        }
        catch (OtsGeometryException exception)
        {
            throw new NetworkException(exception);
        }
        this.network.addLink(this);
    }

    /**
     * Add a GTU to this link (e.g., for statistical purposes, or for a model on macro level). It is safe to add a GTU again. No
     * warning or error will be given. The GTU_ADD_EVENT will only be fired when the GTU was not already on the link.
     * @param gtu Gtu; the GTU to add.
     */
    public final void addGTU(final Gtu gtu)
    {
        // TODO verify that gtu.getSimulator() equals getSimulator() ?
        if (!this.gtus.contains(gtu))
        {
            this.gtus.add(gtu);
            fireTimedEvent(Link.GTU_ADD_EVENT, new Object[] {gtu.getId(), this.gtus.size()},
                    gtu.getSimulator().getSimulatorTime());
        }
    }

    /**
     * Remove a GTU from this link. It is safe to try to remove a GTU again. No warning or error will be given. The
     * GTU_REMOVE_EVENT will only be fired when the GTU was on the link.
     * @param gtu Gtu; the GTU to remove.
     */
    public final void removeGTU(final Gtu gtu)
    {
        // TODO verify that gtu.getSimulator() equals getSimulator() ?
        if (this.gtus.contains(gtu))
        {
            this.gtus.remove(gtu);
            fireTimedEvent(Link.GTU_REMOVE_EVENT, new Object[] {gtu.getId(), this.gtus.size()},
                    gtu.getSimulator().getSimulatorTime());
        }
    }

    /**
     * Provide a safe copy of the set of GTUs.
     * @return Set&lt;GTU&gt;; a safe copy of the set of GTUs
     */
    public final Set<Gtu> getGTUs()
    {
        return new LinkedHashSet<>(this.gtus);
    }

    /**
     * Provide the number of GTUs on this link.
     * @return int; the number of GTUs on this link
     */
    public final int getGTUCount()
    {
        return this.gtus.size();
    }

    /**
     * Returns whether the link is a connector. By default this returns {@code false}.
     * @return boolean; whether the link is a connector, by default this returns {@code false}.
     */
    public boolean isConnector()
    {
        return false;
    }

    /**
     * Return the network in which this link is registered. Cannot be null.
     * @return Network; the network in which this link is registered
     */
    public Network getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the start node.
     * @return Node; start node.
     */
    public final Node getStartNode()
    {
        return this.startNode;
    }

    /**
     * Returns the end node.
     * @return Node; end node.
     */
    public final Node getEndNode()
    {
        return this.endNode;
    }

    /** {@inheritDoc} */
    @Override
    public final LinkType getType()
    {
        return this.linkType;
    }

    /**
     * Returns the design line.
     * @return OtsLine3d; design line.
     */
    public final OtsLine3d getDesignLine()
    {
        return this.designLine;
    }

    /** {@inheritDoc} */
    @Override
    public OtsShape getShape()
    {
        return this.shape;
    }

    /**
     * Returns the simulator.
     * @return OtsSimulatorInterface; simulator.
     */
    public final OtsSimulatorInterface getSimulator()
    {
        return getNetwork().getSimulator();
    }

    /**
     * Returns the length of the link.
     * @return Length; length of the link.
     */
    public final Length getLength()
    {
        return this.designLine.getLength();
    }

    /** the location with 0.01 m extra height. */
    private DirectedPoint zLocation = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        if (this.zLocation == null)
        {
            DirectedPoint p = this.designLine.getLocation();
            this.zLocation = new DirectedPoint(p.x, p.y, p.z + 0.01, p.getRotX(), p.getRotY(), p.getRotZ());
        }
        return this.zLocation;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        return this.designLine.getBounds();
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
    @SuppressWarnings("checkstyle:designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endNode == null) ? 0 : this.endNode.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.linkType == null) ? 0 : this.linkType.hashCode());
        result = prime * result + ((this.startNode == null) ? 0 : this.startNode.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Link other = (Link) obj;
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
        if (this.linkType == null)
        {
            if (other.linkType != null)
                return false;
        }
        else if (!this.linkType.equals(other.linkType))
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
