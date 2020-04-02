package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.Throw;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
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
public class GTUTransceiver extends AbstractTransceiver
{
    /** The network. */
    private final OTSNetwork network;

    /** Transceiver for the GTU ids. */
    private final TransceiverInterface gtuIdSource;

    /**
     * Construct a GTUTransceiver.
     * @param network Network; the Network
     * @param gtuIdSource TransceiverInterface; the transceiver that can produce all active GTU ids in the Network
     */
    public GTUTransceiver(final OTSNetwork network, final TransceiverInterface gtuIdSource)
    {
        super("GTU transceiver", new FieldDescriptor[] { new FieldDescriptor("GTU id", "".getClass()) },
                new FieldDescriptor[] { new FieldDescriptor("GTU id", "".getClass()),
                        new FieldDescriptor("GTUType id", "".getClass()), new FieldDescriptor("x", double.class),
                        new FieldDescriptor("y", double.class), new FieldDescriptor("z", double.class),
                        new FieldDescriptor("heading", double.class), new FieldDescriptor("Speed", Speed.class),
                        new FieldDescriptor("Acceleration", Acceleration.class) });
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
        verifyAddressComponents(address);
        GTU gtu = this.network.getGTU((String) address[0]);
        if (null == gtu)
        {
            return null;
        }
        DirectedPoint gtuPosition = gtu.getLocation();
        return new Object[] { gtu.getId(), gtu.getGTUType().getId(), gtuPosition.x, gtuPosition.y, gtuPosition.z,
                gtuPosition.getRotZ(), gtu.getSpeed(), gtu.getAcceleration() };
    }

}
