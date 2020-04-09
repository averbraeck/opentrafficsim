package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.OTSNetwork;

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
        super("GTU transceiver",
                new MetaData("GTU id", "GTU id",
                        new ObjectDescriptor[] { new ObjectDescriptor("GTU id", "GTU id", "".getClass()) }),
                GTU.MOVE_EVENT);
//                new MetaData("GTU data", "GTU id, position, speed, acceleration",
//                        new ObjectDescriptor[] { new ObjectDescriptor("GTU id", "", "".getClass()),
//                                new ObjectDescriptor("GTUType id", "", "".getClass()),
//                                new ObjectDescriptor("x", "", double.class), new ObjectDescriptor("y", "", double.class),
//                                new ObjectDescriptor("z", "", double.class), new ObjectDescriptor("heading", "", double.class),
//                                new ObjectDescriptor("Speed", "", Speed.class),
//                                new ObjectDescriptor("Acceleration", "", Acceleration.class) }));
        this.network = network;
        this.gtuIdSource = gtuIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public final TransceiverInterface getIdSource(final int addressLevel)
    {
        Throw.when(addressLevel != 0, IndexOutOfBoundsException.class, "Only addressLevel 0 is valid");
        return this.gtuIdSource;
    }

    /** {@inheritDoc} */
    @Override
    public final Object[] get(final Object[] address) throws RemoteException
    {
        getAddressFields().verifyComposition(address);
        GTU gtu = this.network.getGTU((String) address[0]);
        if (null == gtu)
        {
            return null;
        }
        DirectedPoint gtuPosition = gtu.getLocation();
        return new Object[] { gtu.getId(), gtu.getGTUType().getId(), gtuPosition.x, gtuPosition.y, gtuPosition.z,
                gtuPosition.getRotZ(), gtu.getSpeed(), gtu.getAcceleration() };
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTUTransceiver [network=" + network + ", super=" + super.toString() + "]";
    }

}
