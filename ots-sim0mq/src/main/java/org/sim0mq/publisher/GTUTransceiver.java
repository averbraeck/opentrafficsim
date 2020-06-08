package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.OTSNetwork;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Transceiver for GTU data.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTransceiver extends AbstractEventTransceiver
{
    /** The network. */
    private final OTSNetwork network;

    /** Transceiver for the GTU ids. */
    private final TransceiverInterface gtuIdSource;

    /**
     * Construct a GTUTransceiver.
     * @param network Network; the Network
     * @param gtuIdSource GTUIdTransceiver; the transceiver that can produce all active GTU ids in the Network
     */
    public GTUTransceiver(final OTSNetwork network, final GTUIdTransceiver gtuIdSource)
    {
        super("GTU transceiver", new MetaData("GTU id", "GTU id",
                new ObjectDescriptor[] { new ObjectDescriptor("GTU id", "GTU id", String.class) }), GTU.MOVE_EVENT);
        this.network = network;
        this.gtuIdSource = gtuIdSource;
    }

    /**
     * {@inheritDoc}
     * @throws Sim0MQException
     */
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
            returnWrapper.encodeReplyAndTransmit(bad);
            return null;
        }

        GTU gtu = this.network.getGTU((String) address[0]);
        if (null == gtu)
        {
            returnWrapper.encodeReplyAndTransmit("No GTU found with id \"" + address[0] + "\"");
            return null;
        }
        DirectedPoint gtuPosition = gtu.getLocation();
        return new Object[] { gtu.getId(), gtu.getGTUType().getId(),
                new OTSPoint3D(gtuPosition).doubleVector(PositionUnit.METER),
                new Direction(gtuPosition.getZ(), DirectionUnit.EAST_DEGREE), gtu.getSpeed(), gtu.getAcceleration() };
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTUTransceiver [network=" + network.getId() + ", super=" + super.toString() + "]";
    }

}
