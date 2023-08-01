package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.network.Network;
import org.sim0mq.Sim0MQException;

/**
 * Transceiver for GTU data.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuTransceiver extends AbstractEventTransceiver
{
    /** The network. */
    private final Network network;

    /** Transceiver for the GTU ids. */
    private final TransceiverInterface gtuIdSource;

    /**
     * Construct a GtuTransceiver.
     * @param network Network; the Network
     * @param gtuIdSource GtuIdTransceiver; the transceiver that can produce all active GTU ids in the Network
     */
    public GtuTransceiver(final Network network, final GtuIdTransceiver gtuIdSource)
    {
        super("GTU transceiver", new MetaData("GTU id", "GTU id",
                new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class)}), Gtu.MOVE_EVENT);
        this.network = network;
        this.gtuIdSource = gtuIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public final TransceiverInterface getIdSource(final int addressLevel, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        if (addressLevel != 0)
        {
            returnWrapper.encodeReplyAndTransmit("Only empty address is valid");
            throw new IndexOutOfBoundsException("Only empty address is valid");
        }
        return this.gtuIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasIdSource()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] get(final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        String bad = verifyMetaData(getAddressFields(), address);
        if (bad != null)
        {
            returnWrapper.nack(bad);
            return null;
        }

        Gtu gtu = this.network.getGTU((String) address[0]);
        if (null == gtu)
        {
            returnWrapper.nack("No GTU found with id \"" + address[0] + "\"");
            return null;
        }
        OrientedPoint2d gtuPosition = gtu.getLocation();
        return new Object[] {gtu.getId(), gtu.getType().getId(),
                DoubleVector.instantiate(new double[] {gtuPosition.x, gtuPosition.y}, PositionUnit.METER, StorageType.DENSE),
                new Direction(gtuPosition.getDirZ(), DirectionUnit.EAST_DEGREE), gtu.getSpeed(), gtu.getAcceleration()};
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GtuTransceiver [network=" + this.network.getId() + ", super=" + super.toString() + "]";
    }

}
