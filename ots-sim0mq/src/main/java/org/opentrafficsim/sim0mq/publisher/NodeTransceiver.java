package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OtsNetwork;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for Node data.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NodeTransceiver extends AbstractTransceiver
{
    /** The OTS network. */
    private final OtsNetwork network;

    /** Transceiver for the Node ids. */
    private final TransceiverInterface nodeIdSource;

    /**
     * Construct a new NodeTransceiver.
     * @param network OtsNetwork; the network
     * @param nodeIdSource NodeIdTransceiver; the transceiver that can produce all Node ids in the Network
     */
    public NodeTransceiver(final OtsNetwork network, final NodeIdTransceiver nodeIdSource)
    {
        super("Node transceiver",
                new MetaData("Node id", "Node id",
                        new ObjectDescriptor[] {new ObjectDescriptor("Node id", "Node id", String.class)}),
                new MetaData("Node data", "Node id, position, direction, number of Links",
                        new ObjectDescriptor[] {new ObjectDescriptor("Node id", "Node id", String.class),
                                new ObjectDescriptor("Position", "Position", PositionVector.class),
                                new ObjectDescriptor("Direction", "Direction", Direction.class),
                                new ObjectDescriptor("Number of links", "Number of links", Integer.class)}));
        this.network = network;
        this.nodeIdSource = nodeIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        String bad = verifyMetaData(getAddressFields(), address);
        if (bad != null)
        {
            returnWrapper.nack(bad);
            return null;
        }
        Node node = this.network.getNode((String) address[0]);
        if (null == node)
        {
            returnWrapper.nack("Network does not contain a node with id " + address[0]);
            return null;
        }
        return new Object[] {node.getId(), node.getPoint().doubleVector(PositionUnit.METER),
                OtsPoint3d.direction(node.getLocation(), DirectionUnit.EAST_RADIAN), node.getLinks().size()};
    }

    /** {@inheritDoc} */
    @Override
    public TransceiverInterface getIdSource(final int addressLevel, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        if (addressLevel != 0)
        {
            returnWrapper.nack("Only empty address is valid");
            return null;
        }
        return this.nodeIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasIdSource()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NodeTransceiver [network=" + this.network + ", super=" + super.toString() + "]";
    }

}
