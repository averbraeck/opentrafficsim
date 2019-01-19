package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.media.j3d.Bounds;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * A standard implementation of a link between two OTSNodes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSLink extends EventProducer implements Link, Serializable, Locatable
{
    /** */
    private static final long serialVersionUID = 20150101L;

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
    private final OTSLine3D designLine;

    /** The simulator on which events can be scheduled. */
    private final OTSSimulatorInterface simulator;

    /** The GTUs on this Link. */
    private final Set<GTU> gtus = new LinkedHashSet<>();

    /**
     * Construct a new link.
     * @param id String; the link id
     * @param network Network; the network to which the link belongs
     * @param startNode Node; start node (directional)
     * @param endNode Node; end node (directional)
     * @param linkType LinkType; Link type to indicate compatibility with GTU types
     * @param designLine OTSLine3D; the OTSLine3D design line of the Link
     * @param simulator OTSSimulatorInterface; the simulator on which events can be scheduled
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public OTSLink(final Network network, final String id, final Node startNode, final Node endNode, final LinkType linkType,
            final OTSLine3D designLine, final OTSSimulatorInterface simulator) throws NetworkException
    {
        Throw.whenNull(network, "network cannot be null");
        Throw.whenNull(id, "id cannot be null");
        Throw.whenNull(startNode, "startNode cannot be null");
        Throw.whenNull(endNode, "endNode cannot be null");
        Throw.whenNull(linkType, "linkType cannot be null");
        Throw.whenNull(designLine, "designLine cannot be null");
        Throw.whenNull(simulator, "simulator cannot be null");

        this.network = network;
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.linkType = linkType;
        this.startNode.addLink(this);
        this.endNode.addLink(this);
        this.designLine = designLine;
        this.simulator = simulator;
        this.network.addLink(this);
    }

    /**
     * Clone a link for a new network.
     * @param newNetwork Network; the new network to which the clone belongs
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @param link OTSLink; the link to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected OTSLink(final Network newNetwork, final OTSSimulatorInterface newSimulator, final OTSLink link)
            throws NetworkException
    {
        this(newNetwork, link.id, newNetwork.getNode(link.startNode.getId()), newNetwork.getNode(link.endNode.getId()),
                link.linkType, link.designLine, newSimulator);
    }

    /** {@inheritDoc} */
    @Override
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType)
    {
        return this.getLinkType().getDirectionality(gtuType, true);
    }

    /** {@inheritDoc} */
    @Override
    public final void addGTU(final GTU gtu)
    {
        if (!this.gtus.contains(gtu))
        {
            this.gtus.add(gtu);
            fireTimedEvent(Link.GTU_ADD_EVENT, new Object[] { gtu.getId(), gtu, this.gtus.size() },
                    gtu.getSimulator().getSimulatorTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void removeGTU(final GTU gtu)
    {
        if (this.gtus.contains(gtu))
        {
            this.gtus.remove(gtu);
            fireTimedEvent(Link.GTU_REMOVE_EVENT, new Object[] { gtu.getId(), gtu, this.gtus.size() },
                    gtu.getSimulator().getSimulatorTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Set<GTU> getGTUs()
    {
        return new LinkedHashSet<>(this.gtus);
    }

    /** {@inheritDoc} */
    @Override
    public final int getGTUCount()
    {
        return this.gtus.size();
    }

    /** {@inheritDoc} */
    @Override
    public final Network getNetwork()
    {
        return this.network;
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
    public final LinkType getLinkType()
    {
        return this.linkType;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSLine3D getDesignLine()
    {
        return this.designLine;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
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
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
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

    /**
     * Clone the OTSLink for e.g., copying a network.
     * @param newNetwork Network; the new network to which the clone belongs
     * @param newSimulator OTSSimulatorInterface; the new simulator for this network
     * @return a clone of this object
     * @throws NetworkException in case the cloning fails
     */
    @SuppressWarnings("checkstyle:designforextension")
    public OTSLink clone(final Network newNetwork, final OTSSimulatorInterface newSimulator) throws NetworkException
    {
        return new OTSLink(newNetwork, newSimulator, this);
    }
}
