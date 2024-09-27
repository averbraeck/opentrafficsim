package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for Node data.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NodeTransceiver extends AbstractTransceiver
{
    /** The OTS network. */
    private final Network network;

    /** Transceiver for the Node ids. */
    private final TransceiverInterface nodeIdSource;

    /**
     * Construct a new NodeTransceiver.
     * @param network the network
     * @param nodeIdSource the transceiver that can produce all Node ids in the Network
     */
    public NodeTransceiver(final Network network, final NodeIdTransceiver nodeIdSource)
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
        OrientedPoint2d nodeLocation = node.getLocation();
        return new Object[] {node.getId(),
                new PositionVector(new double[] {nodeLocation.x, nodeLocation.y}, PositionUnit.METER),
                new Direction(nodeLocation.getDirZ(), DirectionUnit.EAST_DEGREE), node.getLinks().size()};
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
